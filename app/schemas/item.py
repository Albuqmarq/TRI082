# Schemas de item: dados recebidos no cadastro e na edicao, e formato devolvido pela API.

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.enums import StatusItem


class ItemCriar(BaseModel):
    epc: str = Field(min_length=1)
    nome: str = Field(min_length=1)
    categoria: str = Field(min_length=1)
    tipo: str = Field(min_length=1)
    cor: str | None = None
    fornecedor: str | None = None


class ItemAtualizar(BaseModel):
    nome: str | None = Field(default=None, min_length=1)
    categoria: str | None = Field(default=None, min_length=1)
    tipo: str | None = Field(default=None, min_length=1)
    cor: str | None = None
    fornecedor: str | None = None


class ItemResposta(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    epc: str
    nome: str
    categoria: str
    tipo: str
    cor: str | None
    fornecedor: str | None
    status: StatusItem
    criado_por: int
    criado_em: datetime
    atualizado_em: datetime
