# Modelo da tabela de usuarios, com papel de acesso e senha sempre armazenada como hash.

from sqlalchemy import Boolean, Column, DateTime, Integer, String
from sqlalchemy.sql import func

from app.database import Base


class Usuario(Base):
    __tablename__ = "usuarios"

    id = Column(Integer, primary_key=True)
    nome_usuario = Column(String, unique=True, nullable=False, index=True)
    senha_hash = Column(String, nullable=False)
    papel = Column(String, nullable=False)
    ativo = Column(Boolean, nullable=False, default=True)
    criado_em = Column(DateTime(timezone=True), nullable=False, server_default=func.now())
