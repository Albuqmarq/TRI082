# Rota de autenticacao: recebe usuario e senha, valida e devolve um token JWT. Unica rota publica.

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import obter_sessao
from app.models.usuario import Usuario
from app.schemas.auth import LoginRequisicao, TokenResposta
from app.seguranca import criar_token_acesso, verificar_senha

roteador = APIRouter(prefix="/auth", tags=["Autenticacao"])


@roteador.post("/login", response_model=TokenResposta)
def login(dados: LoginRequisicao, sessao: Session = Depends(obter_sessao)):
    usuario = (
        sessao.query(Usuario)
        .filter(Usuario.nome_usuario == dados.nome_usuario)
        .first()
    )
    if usuario is None or not verificar_senha(dados.senha, usuario.senha_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Usuario ou senha invalidos",
        )
    if not usuario.ativo:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Usuario desativado",
        )
    token = criar_token_acesso(usuario.id, usuario.papel)
    return TokenResposta(token_acesso=token)
