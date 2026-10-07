# Rota do dashboard do painel, liberada a qualquer usuario logado.

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import obter_sessao
from app.dependencias import obter_usuario_atual
from app.models.usuario import Usuario
from app.schemas.painel import PainelResumo
from app.servicos import painel as servico

roteador = APIRouter(prefix="/painel", tags=["Painel"])


@roteador.get("/resumo", response_model=PainelResumo)
def resumo(
    dias: int = Query(default=30, ge=1, le=365),
    sessao: Session = Depends(obter_sessao),
    usuario_atual: Usuario = Depends(obter_usuario_atual),
):
    return servico.resumir(sessao, dias)
