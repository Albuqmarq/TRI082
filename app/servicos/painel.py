# Regras do dashboard: reune os numeros da operacao (itens, inventarios e leituras) a partir de uma data limite.

from datetime import datetime, timedelta, timezone

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.enums import StatusItem, StatusSessao
from app.models.item import Item
from app.models.leitura import Leitura
from app.models.sessao_inventario import SessaoInventario
from app.servicos.inventario import calcular_resultado


def resumir(sessao: Session, dias: int) -> dict:
    limite = datetime.now(timezone.utc) - timedelta(days=dias)
    itens_ativos = sessao.query(Item).filter(Item.status == StatusItem.ativo.value)
    finalizadas = sessao.query(SessaoInventario).filter(
        SessaoInventario.status == StatusSessao.finalizada.value
    )

    return {
        "periodo_dias": dias,
        "itens_total": itens_ativos.count(),
        "itens_novos": itens_ativos.filter(Item.criado_em >= limite).count(),
        "inventarios_abertos": sessao.query(SessaoInventario)
        .filter(SessaoInventario.status == StatusSessao.aberta.value)
        .count(),
        "inventarios_finalizados": finalizadas.filter(SessaoInventario.data_fim >= limite).count(),
        "inventarios_finalizados_total": finalizadas.count(),
        "leituras": sessao.query(Leitura).filter(Leitura.timestamp >= limite).count(),
        "ultima_contagem": _ultima_contagem(sessao),
    }


def _ultima_contagem(sessao: Session) -> dict | None:
    com_leituras = (
        sessao.query(SessaoInventario.id)
        .join(Leitura, Leitura.sessao_id == SessaoInventario.id)
        .filter(SessaoInventario.status == StatusSessao.finalizada.value)
        .group_by(SessaoInventario.id)
        .having(func.count(Leitura.id) > 0)
        .order_by(SessaoInventario.data_fim.desc())
        .first()
    )
    if com_leituras is None:
        return None
    resultado = calcular_resultado(sessao, com_leituras.id)
    return {"sessao_id": resultado["sessao_id"], "data_fim": resultado["data_fim"], **resultado["resumo"]}
