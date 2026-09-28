// Guarda as leituras da tela de leitura em memoria, para manter a lista ao trocar de aba e limpar so quando o operador pedir.
// A contagem por tag e um apoio de teste; no produto final a deduplicacao evita a duplicacao.

package com.tri082.inventario.data.local;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class EstadoLeitura {

    public static class Registro {
        public final String epc;
        public Integer itemId;
        public String nome;
        public String tipo;
        public String cor;
        public int contagem;
        public boolean resolvida;
        public boolean cadastrada;

        Registro(String epc) {
            this.epc = epc;
        }
    }

    private static final LinkedHashMap<String, Registro> mapa = new LinkedHashMap<>();

    private EstadoLeitura() {
    }

    public static boolean contem(String epc) {
        return mapa.containsKey(epc);
    }

    public static void registrar(String epc) {
        Registro registro = mapa.get(epc);
        if (registro == null) {
            registro = new Registro(epc);
            mapa.put(epc, registro);
        }
        registro.contagem++;
    }

    public static void definirDados(String epc, int itemId, String nome, String tipo, String cor) {
        Registro registro = mapa.get(epc);
        if (registro != null) {
            registro.itemId = itemId;
            registro.nome = nome;
            registro.tipo = tipo;
            registro.cor = cor;
            registro.resolvida = true;
            registro.cadastrada = true;
        }
    }

    public static void definirNaoCadastrada(String epc) {
        Registro registro = mapa.get(epc);
        if (registro != null) {
            registro.resolvida = true;
            registro.cadastrada = false;
        }
    }

    public static List<Registro> lista() {
        return new ArrayList<>(mapa.values());
    }

    public static int totalTags() {
        return mapa.size();
    }

    public static void remover(String epc) {
        mapa.remove(epc);
    }

    public static void limpar() {
        mapa.clear();
    }
}
