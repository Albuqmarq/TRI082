# Schemas de inventario: formato das sessoes e das leituras recebidas e devolvidas pela API.

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.enums import StatusLeitura, StatusSessao


class SessaoResposta(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    status: StatusSessao
    data_inicio: datetime
    data_fim: datetime | None
    criado_por: int
    finalizado_por: int | None
    total_leituras: int = 0


class LeituraCriar(BaseModel):
    epc: str = Field(min_length=1)
    timestamp: datetime | None = None
    dispositivo: str | None = None


class LeituraResposta(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    sessao_id: int
    epc: str
    item_id: int | None
    status: StatusLeitura
    timestamp: datetime
    dispositivo: str | None
    operador_id: int


class ItemResumo(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    epc: str
    nome: str
    categoria: str
    tipo: str
    cor: str | None
    fornecedor: str | None


class LeituraResumo(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    epc: str
    timestamp: datetime
    dispositivo: str | None


class ResumoContagem(BaseModel):
    total_esperado: int
    total_encontrados: int
    total_ausentes: int
    total_nao_identificados: int


class ResultadoInventario(BaseModel):
    sessao_id: int
    status: StatusSessao
    data_inicio: datetime
    data_fim: datetime | None
    resumo: ResumoContagem
    esperados_encontrados: list[ItemResumo]
    esperados_ausentes: list[ItemResumo]
    nao_identificados: list[LeituraResumo]


class PendenciaResposta(BaseModel):
    epc: str
    ocorrencias: int
    ultima_leitura: datetime
