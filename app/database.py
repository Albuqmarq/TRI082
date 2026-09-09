# Acesso ao banco via SQLAlchemy: cria a engine de conexao, a fabrica de sessoes e a base dos modelos

from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

from app.config import configuracoes

engine = create_engine(configuracoes.database_url, pool_pre_ping=True)

FabricaDeSessao = sessionmaker(bind=engine, autoflush=False, autocommit=False)

Base = declarative_base()


def obter_sessao():
    sessao = FabricaDeSessao()
    try:
        yield sessao
    finally:
        sessao.close()
