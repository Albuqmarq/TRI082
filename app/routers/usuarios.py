# Rotas de usuarios: dados do proprio usuario para qualquer logado, e gestao de contas restrita ao admin.

from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.database import obter_sessao
from app.dependencias import exigir_admin, obter_usuario_atual
from app.models.usuario import Usuario
from app.schemas.usuario import UsuarioAtualizar, UsuarioCriar, UsuarioResposta
from app.servicos import usuarios as servico

roteador = APIRouter(prefix="/usuarios", tags=["Usuarios"])


@roteador.get("/me", response_model=UsuarioResposta)
def obter_meus_dados(usuario_atual: Usuario = Depends(obter_usuario_atual)):
    return usuario_atual


@roteador.get("", response_model=list[UsuarioResposta])
def listar(
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    return servico.listar_usuarios(sessao)


@roteador.post("", response_model=UsuarioResposta, status_code=status.HTTP_201_CREATED)
def cadastrar(
    dados: UsuarioCriar,
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    return servico.cadastrar_usuario(sessao, dados)


@roteador.patch("/{usuario_id}", response_model=UsuarioResposta)
def editar(
    usuario_id: int,
    dados: UsuarioAtualizar,
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    return servico.atualizar_usuario(sessao, usuario_id, dados)
