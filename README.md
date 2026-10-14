# TRI082 - Inventario com RFID

Sistema de controle de inventario por RFID feito para a TRI082. Cada peca recebe uma etiqueta RFID e o funcionario faz a contagem com um coletor Android, sem precisar ler item por item. No fim da contagem o sistema mostra o que era esperado, o que foi encontrado, o que esta faltando e as tags que nao tem cadastro.

O projeto tem duas partes:

- **API** (pasta raiz): FastAPI com banco PostgreSQL. Guarda o catalogo de itens, os usuarios, as leituras e as sessoes de inventario.
- **Aplicativo** (pasta `app-android`): app Android nativo em Java, feito para o coletor Urovo DT50P. Faz login, leitura das tags, inventario, localizacao de item pelo sinal, catalogo, pendencias de cadastro, relatorios e painel.

Desenvolvido por Marco Albuquerque e Ulysses Bomfim.

## O que precisa ter instalado

Para a API:

- Python 3.13
- Docker Desktop (usado so para rodar o PostgreSQL)
- Git

Para o aplicativo:

- Android Studio (versao recente, com JDK 17 e Android SDK 35)
- Um coletor Urovo DT50P para ler as tags. O app abre em outros celulares e no emulador, mas a leitura RFID so funciona no coletor.

A biblioteca do leitor RFID (`URFIDLibrary`) ja vem no repositorio, em `app-android/app/libs`.

## Como rodar a API

Os comandos abaixo sao para o terminal do Windows, dentro da pasta do projeto.

1. Baixe o projeto:

```bash
git clone https://github.com/Albuqmarq/TRI082.git
cd TRI082
```

2. Crie o ambiente virtual e instale as dependencias:

```bash
python -m venv .venv
.venv\Scripts\python.exe -m pip install -r requirements.txt
```

3. Crie o arquivo `.env` a partir do exemplo. Troque o valor de `SECRET_KEY` por qualquer texto longo e aleatorio:

```bash
copy .env.example .env
```

4. Com o Docker Desktop aberto, suba o banco:

```bash
docker compose up -d
```

5. Crie as tabelas:

```bash
.venv\Scripts\alembic.exe upgrade head
```

6. Cadastre os usuarios e alguns itens de teste:

```bash
.venv\Scripts\python.exe popular_banco.py
```

7. Rode a API liberada para a rede, para o coletor conseguir acessar:

```bash
.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Para conferir se esta funcionando, abra http://localhost:8000/docs no navegador. Essa pagina lista todas as rotas e permite testar cada uma.

Para desligar o banco depois, use `docker compose down`. Os dados continuam salvos para a proxima vez.

## Como rodar o aplicativo

1. Abra a pasta `app-android` no Android Studio e espere o Gradle terminar de sincronizar.
2. No coletor, ative o modo desenvolvedor e a depuracao USB, e ligue ele no computador pelo cabo.
3. Escolha o coletor na lista de aparelhos e clique em Run.

Se preferir gerar o APK pelo terminal, dentro da pasta `app-android`:

```bash
gradlew.bat assembleDebug
```

O arquivo fica em `app-android/app/build/outputs/apk/debug/app-debug.apk`.

## Ligando o app na API

O coletor e o computador que roda a API precisam estar na mesma rede Wi-Fi.

1. No computador, descubra o IP com o comando `ipconfig` (linha "Endereco IPv4" do adaptador Wi-Fi).
2. Na tela de login do app, toque em **Configurar servidor**, informe esse IP e a porta `8000` e salve.
3. Entre com um dos usuarios de teste.

No emulador do Android Studio, use o IP `10.0.2.2`, que aponta para o proprio computador.

Se o app nao conectar, abra `http://IP-DO-COMPUTADOR:8000/docs` no navegador do coletor. Se a pagina nao abrir, o problema e de rede ou do firewall do Windows, que precisa liberar a porta 8000.

## Usuarios de teste

Criados pelo script `popular_banco.py`:

- admin / admin123 (administrador)
- funcionario / func123 (funcionario)

O administrador ve todas as telas, inclusive cadastro de itens, pendencias e usuarios. O funcionario faz leitura, inventario e localizacao.
