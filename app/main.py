# Ponto de entrada da API FastAPI. Cria a aplicacao, registra as rotas e expoe uma verificacao de saude

from fastapi import Depends, FastAPI
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.database import obter_sessao
from app.routers import auth, inventarios, itens, pendencias, usuarios

app = FastAPI(title="API de Inventario RFID TRI082", version="0.1.0")

app.include_router(auth.roteador)
app.include_router(usuarios.roteador)
app.include_router(itens.roteador)
app.include_router(inventarios.roteador)
app.include_router(pendencias.roteador)


@app.get("/saude", tags=["Saude"])
def verificar_saude(sessao: Session = Depends(obter_sessao)):
    sessao.execute(text("SELECT 1"))
    return {"status": "ok", "banco": "conectado"}
