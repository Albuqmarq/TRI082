# Acesso a dados das tags ignoradas: apenas consultas e gravacoes no banco.

from sqlalchemy.orm import Session

from app.models.tag_ignorada import TagIgnorada


def listar_por_epc(sessao: Session) -> dict:
    return {tag.epc: tag.ignorado_em for tag in sessao.query(TagIgnorada).all()}


def obter_por_epc(sessao: Session, epc: str) -> TagIgnorada | None:
    return sessao.query(TagIgnorada).filter(TagIgnorada.epc == epc).first()


def salvar(sessao: Session, tag: TagIgnorada) -> TagIgnorada:
    sessao.add(tag)
    sessao.commit()
    sessao.refresh(tag)
    return tag
