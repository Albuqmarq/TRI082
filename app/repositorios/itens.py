# Acesso a dados dos itens: apenas consultas e gravacoes no banco, sem regra de negocio.

from sqlalchemy.orm import Session

from app.enums import StatusItem
from app.models.item import Item


def listar_ativos(sessao: Session) -> list[Item]:
    return (
        sessao.query(Item)
        .filter(Item.status == StatusItem.ativo.value)
        .order_by(Item.id)
        .all()
    )


def obter_por_id(sessao: Session, item_id: int) -> Item | None:
    return sessao.get(Item, item_id)


def obter_por_epc(sessao: Session, epc: str) -> Item | None:
    return sessao.query(Item).filter(Item.epc == epc).first()


def listar_recentes_por_criador(sessao: Session, criador_id: int, limite: int) -> list[Item]:
    return (
        sessao.query(Item)
        .filter(Item.criado_por == criador_id, Item.status == StatusItem.ativo.value)
        .order_by(Item.criado_em.desc())
        .limit(limite)
        .all()
    )


def criar(sessao: Session, item: Item) -> Item:
    sessao.add(item)
    sessao.commit()
    sessao.refresh(item)
    return item


def salvar(sessao: Session, item: Item) -> Item:
    sessao.commit()
    sessao.refresh(item)
    return item
