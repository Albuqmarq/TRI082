# Dependencias reutilizaveis das rotas: identifica o usuario autenticado a partir do token JWT.

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from app.database import obter_sessao
from app.models.usuario import Usuario
from app.seguranca import decodificar_token

esquema_token = HTTPBearer()


def obter_usuario_atual(
    credenciais: HTTPAuthorizationCredentials = Depends(esquema_token),
    sessao: Session = Depends(obter_sessao),
) -> Usuario:
    erro_credenciais = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Token invalido ou expirado",
    )
    try:
        dados = decodificar_token(credenciais.credentials)
        usuario_id = int(dados.get("sub"))
    except (jwt.PyJWTError, TypeError, ValueError):
        raise erro_credenciais

    usuario = sessao.get(Usuario, usuario_id)
    if usuario is None or not usuario.ativo:
        raise erro_credenciais
    return usuario
