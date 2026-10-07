# Schemas do painel: numeros gerais da operacao em um periodo, mostrados no dashboard do app.

from datetime import datetime

from pydantic import BaseModel


class UltimaContagem(BaseModel):
    sessao_id: int
    data_fim: datetime
    total_esperado: int
    total_encontrados: int
    total_ausentes: int
    total_nao_identificados: int


class PainelResumo(BaseModel):
    periodo_dias: int
    itens_total: int
    itens_novos: int
    inventarios_abertos: int
    inventarios_finalizados: int
    inventarios_finalizados_total: int
    leituras: int
    ultima_contagem: UltimaContagem | None
