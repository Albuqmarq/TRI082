# Schemas de usuario expostos pela API. As respostas nunca incluem a senha nem o hash.

from datetime import datetime

from pydantic import BaseModel, ConfigDict

from app.enums import Papel


class UsuarioResposta(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    nome_usuario: str
    papel: Papel
    ativo: bool
    criado_em: datetime
