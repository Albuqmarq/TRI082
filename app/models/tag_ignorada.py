# Modelo da tabela de tags ignoradas: o Administrador dispensa as leituras de uma tag ate aquele momento.
# Se a tag for lida de novo depois de ignorado_em, ela volta a aparecer nas pendencias.

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String
from sqlalchemy.sql import func

from app.database import Base


class TagIgnorada(Base):
    __tablename__ = "tags_ignoradas"

    id = Column(Integer, primary_key=True)
    epc = Column(String, unique=True, nullable=False, index=True)
    ignorado_por = Column(Integer, ForeignKey("usuarios.id"), nullable=False)
    ignorado_em = Column(DateTime(timezone=True), nullable=False, server_default=func.now())
