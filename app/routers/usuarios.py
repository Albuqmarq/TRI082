# Rotas de usuarios. Nesta etapa expoe apenas os dados do proprio usuario autenticado.

from fastapi import APIRouter, Depends

from app.dependencias import obter_usuario_atual
from app.models.usuario import Usuario
from app.schemas.usuario import UsuarioResposta

roteador = APIRouter(prefix="/usuarios", tags=["Usuarios"])


@roteador.get("/me", response_model=UsuarioResposta)
def obter_meus_dados(usuario_atual: Usuario = Depends(obter_usuario_atual)):
    return usuario_atual
