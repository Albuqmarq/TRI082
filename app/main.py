# Ponto de entrada da API FastAPI. Cria a aplicacao e expoe uma verificacao de saude que testa o banco

from fastapi import Depends, FastAPI
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.database import obter_sessao

app = FastAPI(title="API de Inventario RFID TRI082", version="0.1.0")


@app.get("/saude", tags=["Saude"])
def verificar_saude(sessao: Session = Depends(obter_sessao)):
    sessao.execute(text("SELECT 1"))
    return {"status": "ok", "banco": "conectado"}
