# Modelo da tabela de sessoes de inventario, que agrupam as leituras feitas durante uma contagem.

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from app.database import Base


class SessaoInventario(Base):
    __tablename__ = "sessoes_inventario"

    id = Column(Integer, primary_key=True)
    status = Column(String, nullable=False, default="aberta")
    data_inicio = Column(DateTime(timezone=True), nullable=False, server_default=func.now())
    data_fim = Column(DateTime(timezone=True), nullable=True)
    criado_por = Column(Integer, ForeignKey("usuarios.id"), nullable=False)
    finalizado_por = Column(Integer, ForeignKey("usuarios.id"), nullable=True)

    leituras = relationship("Leitura", back_populates="sessao")
