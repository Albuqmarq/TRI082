# Schemas de usuario expostos pela API. As respostas nunca incluem a senha nem o hash.

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.enums import Papel


class UsuarioCriar(BaseModel):
    nome_usuario: str = Field(min_length=1)
    senha: str = Field(min_length=6)
    papel: Papel


class UsuarioAtualizar(BaseModel):
    papel: Papel | None = None
    senha: str | None = Field(default=None, min_length=6)
    ativo: bool | None = None


class UsuarioResposta(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    nome_usuario: str
    papel: Papel
    ativo: bool
    criado_em: datetime
