# Rotas de sessoes de inventario e leituras. Liberadas a qualquer usuario logado (admin ou funcionario).

from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.database import obter_sessao
from app.dependencias import obter_usuario_atual
from app.models.usuario import Usuario
from app.schemas.inventario import (
    LeituraCriar,
    LeituraResposta,
    ResultadoInventario,
    SessaoResposta,
)
from app.servicos import inventario as servico

roteador = APIRouter(prefix="/inventarios", tags=["Inventarios"])


@roteador.post("", response_model=SessaoResposta, status_code=status.HTTP_201_CREATED)
def criar_sessao(
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.criar_sessao(sessao, usuario_atual.id)


@roteador.get("", response_model=list[SessaoResposta])
def listar_sessoes(
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.listar_sessoes(sessao)


@roteador.get("/{inventario_id}", response_model=SessaoResposta)
def detalhar_sessao(
    inventario_id: int,
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.obter_sessao_inventario(sessao, inventario_id)


@roteador.post(
    "/{inventario_id}/leituras",
    response_model=LeituraResposta,
    status_code=status.HTTP_201_CREATED,
)
def registrar_leitura(
    inventario_id: int,
    dados: LeituraCriar,
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.registrar_leitura(sessao, inventario_id, dados, usuario_atual.id)


@roteador.get("/{inventario_id}/leituras", response_model=list[LeituraResposta])
def listar_leituras(
    inventario_id: int,
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.listar_leituras(sessao, inventario_id)


@roteador.post("/{inventario_id}/finalizar", response_model=ResultadoInventario)
def finalizar(
    inventario_id: int,
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.finalizar_sessao(sessao, inventario_id, usuario_atual.id)


@roteador.get("/{inventario_id}/resultado", response_model=ResultadoInventario)
def resultado(
    inventario_id: int,
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.calcular_resultado(sessao, inventario_id)
