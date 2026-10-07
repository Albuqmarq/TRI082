# Rotas de pendencias de cadastro: leituras nao identificadas que ainda nao tem item correspondente. Somente admin.

from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.database import obter_sessao
from app.dependencias import exigir_admin
from app.models.usuario import Usuario
from app.schemas.inventario import PendenciaResposta
from app.servicos import inventario as servico

roteador = APIRouter(prefix="/pendencias", tags=["Pendencias"])


@roteador.get("", response_model=list[PendenciaResposta])
def listar_pendencias(
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    return servico.listar_pendencias(sessao)


@roteador.post("/{epc}/ignorar", status_code=status.HTTP_204_NO_CONTENT)
def ignorar(
    epc: str,
    sessao: Session = Depends(obter_sessao),
    admin: Usuario = Depends(exigir_admin),
):
    servico.ignorar_tag(sessao, epc, admin.id)
