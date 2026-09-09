# Acesso a dados das sessoes de inventario: apenas consultas e gravacoes no banco.

from sqlalchemy.orm import Session

from app.models.sessao_inventario import SessaoInventario


def criar(sessao: Session, inventario: SessaoInventario) -> SessaoInventario:
    sessao.add(inventario)
    sessao.commit()
    sessao.refresh(inventario)
    return inventario


def listar(sessao: Session) -> list[SessaoInventario]:
    return sessao.query(SessaoInventario).order_by(SessaoInventario.id.desc()).all()


def obter_por_id(sessao: Session, inventario_id: int) -> SessaoInventario | None:
    return sessao.get(SessaoInventario, inventario_id)


def salvar(sessao: Session, inventario: SessaoInventario) -> SessaoInventario:
    sessao.commit()
    sessao.refresh(inventario)
    return inventario
