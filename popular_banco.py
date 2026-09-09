# Script de dados mockados: limpa as tabelas e insere usuarios, itens, uma sessao e leituras de exemplo para testes.

from sqlalchemy import text

from app.database import FabricaDeSessao
from app.enums import Papel, StatusItem, StatusLeitura, StatusSessao
from app.models.item import Item
from app.models.leitura import Leitura
from app.models.sessao_inventario import SessaoInventario
from app.models.usuario import Usuario
from app.seguranca import gerar_hash_senha


def limpar_tabelas(sessao):
    sessao.execute(
        text("TRUNCATE leituras, sessoes_inventario, itens, usuarios RESTART IDENTITY CASCADE")
    )
    sessao.commit()


def popular():
    sessao = FabricaDeSessao()
    try:
        limpar_tabelas(sessao)

        admin = Usuario(
            nome_usuario="admin",
            senha_hash=gerar_hash_senha("admin123"),
            papel=Papel.admin.value,
            ativo=True,
        )
        funcionario = Usuario(
            nome_usuario="funcionario",
            senha_hash=gerar_hash_senha("func123"),
            papel=Papel.funcionario.value,
            ativo=True,
        )
        sessao.add_all([admin, funcionario])
        sessao.commit()
        sessao.refresh(admin)
        sessao.refresh(funcionario)

        itens = [
            Item(epc="EPC-TEC-001", nome="Rolo de malha dry fit", categoria="Tecido", tipo="Rolo de 20m", cor="Preto", fornecedor="Malharia alfa", status=StatusItem.ativo.value, criado_por=admin.id),
            Item(epc="EPC-TEC-002", nome="Rolo de malha dry fit", categoria="Tecido", tipo="Rolo de 20m", cor="Azul", fornecedor="Malharia alfa", status=StatusItem.ativo.value, criado_por=admin.id),
            Item(epc="EPC-TEC-003", nome="Rolo de tecido plano", categoria="Tecido", tipo="Rolo de 10m", cor="Branco", fornecedor="Textil beta", status=StatusItem.ativo.value, criado_por=admin.id),
            Item(epc="EPC-TEC-004", nome="Rolo de ribana", categoria="Tecido", tipo="Rolo de 10m", cor="Preto", fornecedor="Textil beta", status=StatusItem.ativo.value, criado_por=admin.id),
            Item(epc="EPC-TIN-001", nome="Lata de tinta serigrafia", categoria="Tinta", tipo="Lata de 1kg", cor="Branco", fornecedor="Tintas gama", status=StatusItem.ativo.value, criado_por=admin.id),
            Item(epc="EPC-TIN-002", nome="Lata de tinta serigrafia", categoria="Tinta", tipo="Lata de 1kg", cor="Preto", fornecedor="Tintas gama", status=StatusItem.ativo.value, criado_por=admin.id),
            Item(epc="EPC-TIN-003", nome="Lata de tinta serigrafia", categoria="Tinta", tipo="Lata de 3,6kg", cor="Vermelho", fornecedor="Tintas gama", status=StatusItem.ativo.value, criado_por=admin.id),
        ]
        sessao.add_all(itens)
        sessao.commit()
        for item in itens:
            sessao.refresh(item)

        inventario = SessaoInventario(status=StatusSessao.aberta.value, criado_por=funcionario.id)
        sessao.add(inventario)
        sessao.commit()
        sessao.refresh(inventario)

        itens_encontrados = [itens[0], itens[1], itens[2], itens[4], itens[5]]
        leituras = []
        for item in itens_encontrados:
            leituras.append(
                Leitura(
                    sessao_id=inventario.id,
                    epc=item.epc,
                    item_id=item.id,
                    status=StatusLeitura.encontrado.value,
                    dispositivo="DT50P-01",
                    operador_id=funcionario.id,
                )
            )
        leituras.append(
            Leitura(
                sessao_id=inventario.id,
                epc="EPC-DESCONHECIDO-001",
                item_id=None,
                status=StatusLeitura.nao_identificado.value,
                dispositivo="DT50P-01",
                operador_id=funcionario.id,
            )
        )
        sessao.add_all(leituras)
        sessao.commit()

        print("Banco populado com sucesso.")
        print("Usuarios de teste:")
        print("  admin / admin123 (papel admin)")
        print("  funcionario / func123 (papel funcionario)")
        print(f"Itens cadastrados: {len(itens)} (4 de tecido, 3 de tinta)")
        print(f"Sessao de inventario id={inventario.id} (aberta) com {len(leituras)} leituras")
        print("  5 encontradas, 1 nao identificada (EPC-DESCONHECIDO-001)")
        print(f"  itens esperados mas ausentes: {itens[3].nome} ({itens[3].epc}), {itens[6].nome} ({itens[6].epc})")
    finally:
        sessao.close()


if __name__ == "__main__":
    popular()
