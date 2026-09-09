# Rotas de itens do catalogo. Leitura liberada a qualquer usuario logado, escrita restrita ao admin.

from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.database import obter_sessao
from app.dependencias import exigir_admin, obter_usuario_atual
from app.models.usuario import Usuario
from app.schemas.item import ItemAtualizar, ItemCriar, ItemResposta
from app.servicos import itens as servico

roteador = APIRouter(prefix="/itens", tags=["Itens"])


@roteador.get("", response_model=list[ItemResposta])
def listar(
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.listar_itens(sessao)


@roteador.get("/recomendacao", response_model=list[ItemResposta])
def recomendacao(
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    return servico.recomendar_itens(sessao, admin.id)


@roteador.get("/epc/{epc}", response_model=ItemResposta)
def buscar_por_epc(
    epc: str,
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.obter_item_por_epc(sessao, epc)


@roteador.get("/{item_id}", response_model=ItemResposta)
def detalhar(
    item_id: int,
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.obter_item(sessao, item_id)


@roteador.post("", response_model=ItemResposta, status_code=status.HTTP_201_CREATED)
def cadastrar(
    dados: ItemCriar,
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    return servico.cadastrar_item(sessao, dados, admin.id)


@roteador.patch("/{item_id}", response_model=ItemResposta)
def editar(
    item_id: int,
    dados: ItemAtualizar,
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    return servico.atualizar_item(sessao, item_id, dados)


@roteador.delete("/{item_id}", status_code=status.HTTP_204_NO_CONTENT)
def excluir(
    item_id: int,
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    servico.excluir_item(sessao, item_id)
