# Regras de negocio do inventario: abertura de sessao, registro de leitura, finalizacao, calculo de resultado e pendencias.

from datetime import datetime, timezone

from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.enums import StatusItem, StatusLeitura, StatusSessao
from app.models.leitura import Leitura
from app.models.sessao_inventario import SessaoInventario
from app.models.tag_ignorada import TagIgnorada
from app.repositorios import itens as repositorio_itens
from app.repositorios import leituras as repositorio_leituras
from app.repositorios import sessoes as repositorio_sessoes
from app.repositorios import tags_ignoradas as repositorio_ignoradas
from app.schemas.inventario import LeituraCriar


def criar_sessao(sessao: Session, criado_por: int) -> SessaoInventario:
    inventario = SessaoInventario(status=StatusSessao.aberta.value, criado_por=criado_por)
    return repositorio_sessoes.criar(sessao, inventario)


def listar_sessoes(sessao: Session) -> list[SessaoInventario]:
    contagens = repositorio_leituras.contar_por_sessao(sessao)
    inventarios = repositorio_sessoes.listar(sessao)
    for inventario in inventarios:
        inventario.total_leituras = contagens.get(inventario.id, 0)
    return inventarios


def obter_sessao_inventario(sessao: Session, inventario_id: int) -> SessaoInventario:
    inventario = repositorio_sessoes.obter_por_id(sessao, inventario_id)
    if inventario is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Sessao de inventario nao encontrada",
        )
    return inventario


def registrar_leitura(
    sessao: Session, inventario_id: int, dados: LeituraCriar, operador_id: int
) -> Leitura:
    inventario = obter_sessao_inventario(sessao, inventario_id)
    if inventario.status != StatusSessao.aberta.value:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A sessao de inventario ja foi finalizada",
        )

    epc = dados.epc.strip()
    leitura_existente = repositorio_leituras.obter_por_sessao_e_epc(sessao, inventario_id, epc)
    if leitura_existente is not None:
        return leitura_existente

    item = repositorio_itens.obter_por_epc(sessao, epc)
    if item is not None and item.status == StatusItem.ativo.value:
        item_id = item.id
        status_leitura = StatusLeitura.encontrado.value
    else:
        item_id = None
        status_leitura = StatusLeitura.nao_identificado.value

    leitura = Leitura(
        sessao_id=inventario_id,
        epc=epc,
        item_id=item_id,
        status=status_leitura,
        dispositivo=dados.dispositivo,
        operador_id=operador_id,
    )
    if dados.timestamp is not None:
        leitura.timestamp = dados.timestamp
    return repositorio_leituras.criar(sessao, leitura)


def listar_leituras(sessao: Session, inventario_id: int) -> list[Leitura]:
    obter_sessao_inventario(sessao, inventario_id)
    return repositorio_leituras.listar_por_sessao(sessao, inventario_id)


def calcular_resultado(sessao: Session, inventario_id: int) -> dict:
    inventario = obter_sessao_inventario(sessao, inventario_id)
    itens_ativos = repositorio_itens.listar_ativos(sessao)
    if inventario.data_fim is not None:
        # Contagem ja fechada: so conta como esperado o item que existia quando ela terminou.
        itens_ativos = [item for item in itens_ativos if item.criado_em <= inventario.data_fim]
    leituras = repositorio_leituras.listar_por_sessao(sessao, inventario_id)

    ids_encontrados = set()
    nao_identificados = []
    for leitura in leituras:
        if leitura.status == StatusLeitura.encontrado.value and leitura.item_id is not None:
            ids_encontrados.add(leitura.item_id)
        elif leitura.status == StatusLeitura.nao_identificado.value:
            item = repositorio_itens.obter_por_epc(sessao, leitura.epc)
            if item is not None and item.status == StatusItem.ativo.value:
                ids_encontrados.add(item.id)
            else:
                nao_identificados.append(leitura)

    esperados_encontrados = []
    esperados_ausentes = []
    for item in itens_ativos:
        if item.id in ids_encontrados:
            esperados_encontrados.append(item)
        else:
            esperados_ausentes.append(item)

    resumo = {
        "total_esperado": len(itens_ativos),
        "total_encontrados": len(esperados_encontrados),
        "total_ausentes": len(esperados_ausentes),
        "total_nao_identificados": len(nao_identificados),
    }
    return {
        "sessao_id": inventario.id,
        "status": inventario.status,
        "data_inicio": inventario.data_inicio,
        "data_fim": inventario.data_fim,
        "resumo": resumo,
        "esperados_encontrados": esperados_encontrados,
        "esperados_ausentes": esperados_ausentes,
        "nao_identificados": nao_identificados,
    }


def finalizar_sessao(sessao: Session, inventario_id: int, finalizado_por: int) -> dict:
    inventario = obter_sessao_inventario(sessao, inventario_id)
    if inventario.status == StatusSessao.finalizada.value:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A sessao de inventario ja foi finalizada",
        )
    inventario.status = StatusSessao.finalizada.value
    inventario.data_fim = datetime.now(timezone.utc)
    inventario.finalizado_por = finalizado_por
    repositorio_sessoes.salvar(sessao, inventario)
    return calcular_resultado(sessao, inventario_id)


def listar_pendencias(sessao: Session) -> list[dict]:
    leituras = repositorio_leituras.listar_nao_identificadas(sessao)
    ignoradas = repositorio_ignoradas.listar_por_epc(sessao)
    agrupado: dict[str, dict] = {}
    for leitura in leituras:
        ignorada_em = ignoradas.get(leitura.epc)
        if ignorada_em is not None and leitura.timestamp <= ignorada_em:
            continue
        item = repositorio_itens.obter_por_epc(sessao, leitura.epc)
        if item is not None and item.status == StatusItem.ativo.value:
            continue
        if leitura.epc not in agrupado:
            agrupado[leitura.epc] = {
                "epc": leitura.epc,
                "ocorrencias": 0,
                "ultima_leitura": leitura.timestamp,
            }
        agrupado[leitura.epc]["ocorrencias"] += 1
        if leitura.timestamp > agrupado[leitura.epc]["ultima_leitura"]:
            agrupado[leitura.epc]["ultima_leitura"] = leitura.timestamp
    return list(agrupado.values())


def ignorar_tag(sessao: Session, epc: str, usuario_id: int) -> None:
    epc = epc.strip()
    item = repositorio_itens.obter_por_epc(sessao, epc)
    if item is not None and item.status == StatusItem.ativo.value:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A tag ja esta cadastrada como item",
        )
    tag = repositorio_ignoradas.obter_por_epc(sessao, epc)
    if tag is None:
        tag = TagIgnorada(epc=epc, ignorado_por=usuario_id)
    else:
        tag.ignorado_por = usuario_id
    tag.ignorado_em = datetime.now(timezone.utc)
    repositorio_ignoradas.salvar(sessao, tag)
