// Guarda a sessao de inventario ativa e a ultima finalizada, para a leitura registrar na sessao certa e a tela mostrar o resultado.

package com.tri082.inventario.data.local;

public final class SessaoInventario {

    private static Integer idAtivo;
    private static Integer idFinalizado;

    private SessaoInventario() {
    }

    public static boolean ativa() {
        return idAtivo != null;
    }

    public static Integer getAtivo() {
        return idAtivo;
    }

    public static Integer getFinalizado() {
        return idFinalizado;
    }

    public static void definirAtivo(int id) {
        idAtivo = id;
        idFinalizado = null;
    }

    public static void finalizar() {
        idFinalizado = idAtivo;
        idAtivo = null;
    }

    public static void limpar() {
        idAtivo = null;
        idFinalizado = null;
    }
}
