# Modelo da tabela de leituras de tags RFID. Leitura sem item associado e com status nao_identificado representa uma pendencia de cadastro.

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from app.database import Base


class Leitura(Base):
    __tablename__ = "leituras"

    id = Column(Integer, primary_key=True)
    sessao_id = Column(Integer, ForeignKey("sessoes_inventario.id"), nullable=False, index=True)
    epc = Column(String, nullable=False, index=True)
    item_id = Column(Integer, ForeignKey("itens.id"), nullable=True)
    status = Column(String, nullable=False)
    timestamp = Column(DateTime(timezone=True), nullable=False, server_default=func.now())
    dispositivo = Column(String, nullable=True)
    operador_id = Column(Integer, ForeignKey("usuarios.id"), nullable=False)

    sessao = relationship("SessaoInventario", back_populates="leituras")
    item = relationship("Item")
