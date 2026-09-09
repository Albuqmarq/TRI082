# Configuracoes da aplicacao, carregadas a partir de variaveis de ambiente e do arquivo .env

from pydantic_settings import BaseSettings, SettingsConfigDict


class Configuracoes(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    database_url: str
    secret_key: str
    algoritmo_jwt: str = "HS256"
    minutos_expiracao_token: int = 480


configuracoes = Configuracoes()
