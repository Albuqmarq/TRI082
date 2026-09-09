# Regras de negocio dos usuarios: normalizacao do nome, verificacao de duplicidade e hash de senha.

from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from app.models.usuario import Usuario
from app.normalizacao import normalizar_nome_usuario
from app.repositorios import usuarios as repositorio
from app.schemas.usuario import UsuarioAtualizar, UsuarioCriar
from app.seguranca import gerar_hash_senha


def cadastrar_usuario(sessao: Session, dados: UsuarioCriar) -> Usuario:
    nome = normalizar_nome_usuario(dados.nome_usuario)
    if repositorio.obter_por_nome(sessao, nome) is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Ja existe um usuario com esse nome",
        )
    usuario = Usuario(
        nome_usuario=nome,
        senha_hash=gerar_hash_senha(dados.senha),
        papel=dados.papel.value,
        ativo=True,
    )
    return repositorio.criar(sessao, usuario)


def listar_usuarios(sessao: Session) -> list[Usuario]:
    return repositorio.listar(sessao)


def obter_usuario(sessao: Session, usuario_id: int) -> Usuario:
    usuario = repositorio.obter_por_id(sessao, usuario_id)
    if usuario is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario nao encontrado",
        )
    return usuario


def atualizar_usuario(sessao: Session, usuario_id: int, dados: UsuarioAtualizar) -> Usuario:
    usuario = obter_usuario(sessao, usuario_id)
    if dados.papel is not None:
        usuario.papel = dados.papel.value
    if dados.senha is not None:
        usuario.senha_hash = gerar_hash_senha(dados.senha)
    if dados.ativo is not None:
        usuario.ativo = dados.ativo
    return repositorio.salvar(sessao, usuario)
