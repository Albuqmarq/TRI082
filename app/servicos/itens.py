# Regras de negocio dos itens: normalizacao de texto, verificacao de EPC duplicado e exclusao logica.

from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.enums import StatusItem
from app.models.item import Item
from app.normalizacao import normalizar_texto
from app.repositorios import itens as repositorio
from app.schemas.item import ItemAtualizar, ItemCriar


def cadastrar_item(sessao: Session, dados: ItemCriar, criado_por: int) -> Item:
    epc = dados.epc.strip()
    if repositorio.obter_por_epc(sessao, epc) is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Ja existe um item cadastrado com esse EPC",
        )
    item = Item(
        epc=epc,
        nome=normalizar_texto(dados.nome),
        categoria=normalizar_texto(dados.categoria),
        tipo=normalizar_texto(dados.tipo),
        cor=normalizar_texto(dados.cor),
        fornecedor=normalizar_texto(dados.fornecedor),
        status=StatusItem.ativo.value,
        criado_por=criado_por,
    )
    return repositorio.criar(sessao, item)


def listar_itens(sessao: Session) -> list[Item]:
    return repositorio.listar_ativos(sessao)


def obter_item(sessao: Session, item_id: int) -> Item:
    item = repositorio.obter_por_id(sessao, item_id)
    if item is None or item.status != StatusItem.ativo.value:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Item nao encontrado",
        )
    return item


def obter_item_por_epc(sessao: Session, epc: str) -> Item:
    item = repositorio.obter_por_epc(sessao, epc.strip())
    if item is None or item.status != StatusItem.ativo.value:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Nenhum item ativo encontrado para esse EPC",
        )
    return item


def atualizar_item(sessao: Session, item_id: int, dados: ItemAtualizar) -> Item:
    item = obter_item(sessao, item_id)
    if dados.nome is not None:
        item.nome = normalizar_texto(dados.nome)
    if dados.categoria is not None:
        item.categoria = normalizar_texto(dados.categoria)
    if dados.tipo is not None:
        item.tipo = normalizar_texto(dados.tipo)
    if dados.cor is not None:
        item.cor = normalizar_texto(dados.cor)
    if dados.fornecedor is not None:
        item.fornecedor = normalizar_texto(dados.fornecedor)
    return repositorio.salvar(sessao, item)


def excluir_item(sessao: Session, item_id: int) -> None:
    item = obter_item(sessao, item_id)
    item.status = StatusItem.excluido.value
    repositorio.salvar(sessao, item)


def recomendar_itens(sessao: Session, criador_id: int, limite: int = 5) -> list[Item]:
    return repositorio.listar_recentes_por_criador(sessao, criador_id, limite)
