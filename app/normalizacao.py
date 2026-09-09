# Normalizacao de campos de texto antes de gravar, evitando duplicidade de valores equivalentes como Preto, preto e PRETO.

def normalizar_texto(valor: str | None) -> str | None:
    if valor is None:
        return None
    texto = " ".join(valor.split())
    if not texto:
        return None
    return texto[0].upper() + texto[1:].lower()


def normalizar_nome_usuario(valor: str) -> str:
    return " ".join(valor.split()).lower()
