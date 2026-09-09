# Acesso a dados dos usuarios: apenas consultas e gravacoes no banco.

from sqlalchemy.orm import Session

from app.models.usuario import Usuario


def listar(sessao: Session) -> list[Usuario]:
    return sessao.query(Usuario).order_by(Usuario.id).all()


def obter_por_id(sessao: Session, usuario_id: int) -> Usuario | None:
    return sessao.get(Usuario, usuario_id)


def obter_por_nome(sessao: Session, nome_usuario: str) -> Usuario | None:
    return sessao.query(Usuario).filter(Usuario.nome_usuario == nome_usuario).first()


def criar(sessao: Session, usuario: Usuario) -> Usuario:
    sessao.add(usuario)
    sessao.commit()
    sessao.refresh(usuario)
    return usuario


def salvar(sessao: Session, usuario: Usuario) -> Usuario:
    sessao.commit()
    sessao.refresh(usuario)
    return usuario
