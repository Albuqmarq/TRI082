# MVP TRI082

MVP da API de controle de inventario RFID da TRI082.

## Requisitos

- Python 3.13
- Docker Desktop instalado e aberto

## Como rodar

1. Criar o ambiente virtual e instalar as dependencias:

```bash
python -m venv .venv
.venv\Scripts\python.exe -m pip install -r requirements.txt
```

2. Criar o arquivo .env a partir do exemplo e definir uma SECRET_KEY propria:

```bash
copy .env.example .env
```

3. Subir o banco PostgreSQL no Docker:

```bash
docker compose up -d
```

4. Criar as tabelas no banco:

```bash
.venv\Scripts\alembic.exe upgrade head
```

5. Popular o banco com dados de teste:

```bash
.venv\Scripts\python.exe popular_banco.py
```

6. Rodar a API:

```bash
.venv\Scripts\python.exe -m uvicorn app.main:app --reload --port 8000
```

A API fica disponivel em http://localhost:8000. Para desligar o banco depois, use `docker compose down`.

## Como testar

Abra http://localhost:8000/docs no navegador para ver e executar todos os endpoints.

Usuarios de teste criados pelo script:

- admin / admin123 (administrador)
- funcionario / func123 (funcionario)
