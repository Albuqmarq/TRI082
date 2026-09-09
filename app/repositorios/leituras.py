# Acesso a dados das leituras: apenas consultas e gravacoes no banco.

from sqlalchemy.orm import Session

from app.enums import StatusLeitura
from app.models.leitura import Leitura


def listar_nao_identificadas(sessao: Session) -> list[Leitura]:
    return (
        sessao.query(Leitura)
        .filter(Leitura.status == StatusLeitura.nao_identificado.value)
        .order_by(Leitura.timestamp.desc())
        .all()
    )


def obter_por_sessao_e_epc(sessao: Session, inventario_id: int, epc: str) -> Leitura | None:
    return (
        sessao.query(Leitura)
        .filter(Leitura.sessao_id == inventario_id, Leitura.epc == epc)
        .first()
    )


def listar_por_sessao(sessao: Session, inventario_id: int) -> list[Leitura]:
    return (
        sessao.query(Leitura)
        .filter(Leitura.sessao_id == inventario_id)
        .order_by(Leitura.id)
        .all()
    )


def criar(sessao: Session, leitura: Leitura) -> Leitura:
    sessao.add(leitura)
    sessao.commit()
    sessao.refresh(leitura)
    return leitura
