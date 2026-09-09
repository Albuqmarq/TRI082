# Schemas de autenticacao: dados de login recebidos e formato do token retornado.

from pydantic import BaseModel


class LoginRequisicao(BaseModel):
    nome_usuario: str
    senha: str


class TokenResposta(BaseModel):
    token_acesso: str
    tipo_token: str = "bearer"
