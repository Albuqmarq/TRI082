# Valores permitidos para os campos de dominio controlado, usados na validacao dos schemas e no codigo.

from enum import Enum


class Papel(str, Enum):
    admin = "admin"
    funcionario = "funcionario"


class StatusItem(str, Enum):
    ativo = "ativo"
    excluido = "excluido"


class StatusSessao(str, Enum):
    aberta = "aberta"
    finalizada = "finalizada"


class StatusLeitura(str, Enum):
    encontrado = "encontrado"
    nao_identificado = "nao_identificado"
