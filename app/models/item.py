# Modelo da tabela de itens do catalogo, identificados internamente por id e pela tag RFID no campo epc.

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String
from sqlalchemy.sql import func

from app.database import Base


class Item(Base):
    __tablename__ = "itens"

    id = Column(Integer, primary_key=True)
    epc = Column(String, unique=True, nullable=False, index=True)
    nome = Column(String, nullable=False)
    categoria = Column(String, nullable=False)
    tipo = Column(String, nullable=False)
    cor = Column(String, nullable=True)
    fornecedor = Column(String, nullable=True)
    status = Column(String, nullable=False, default="ativo")
    criado_por = Column(Integer, ForeignKey("usuarios.id"), nullable=False)
    criado_em = Column(DateTime(timezone=True), nullable=False, server_default=func.now())
    atualizado_em = Column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now(),
        onupdate=func.now(),
    )
