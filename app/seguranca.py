# Funcoes de seguranca: hash e verificacao de senha com bcrypt, e geracao e leitura do token JWT.

from datetime import datetime, timedelta, timezone

import jwt
from passlib.context import CryptContext

from app.config import configuracoes

contexto_senha = CryptContext(schemes=["bcrypt"], deprecated="auto")


def gerar_hash_senha(senha: str) -> str:
    return contexto_senha.hash(senha)


def verificar_senha(senha: str, senha_hash: str) -> bool:
    return contexto_senha.verify(senha, senha_hash)


def criar_token_acesso(usuario_id: int, papel: str) -> str:
    expira_em = datetime.now(timezone.utc) + timedelta(
        minutes=configuracoes.minutos_expiracao_token
    )
    dados = {"sub": str(usuario_id), "papel": papel, "exp": expira_em}
    return jwt.encode(dados, configuracoes.secret_key, algorithm=configuracoes.algoritmo_jwt)


def decodificar_token(token: str) -> dict:
    return jwt.decode(token, configuracoes.secret_key, algorithms=[configuracoes.algoritmo_jwt])
