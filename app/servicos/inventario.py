# Regras de negocio do inventario: abertura de sessao, registro de leitura com resolucao automatica de item e deduplicacao.

from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.enums import StatusItem, StatusLeitura, StatusSessao
from app.models.leitura import Leitura
from app.models.sessao_inventario import SessaoInventario
from app.repositorios import itens as repositorio_itens
from app.repositorios import leituras as repositorio_leituras
from app.repositorios import sessoes as repositorio_sessoes
from app.schemas.inventario import LeituraCriar


def criar_sessao(sessao: Session, criado_por: int) -> SessaoInventario:
    inventario = SessaoInventario(status=StatusSessao.aberta.value, criado_por=criado_por)
    return repositorio_sessoes.criar(sessao, inventario)


def listar_sessoes(sessao: Session) -> list[SessaoInventario]:
    return repositorio_sessoes.listar(sessao)


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
