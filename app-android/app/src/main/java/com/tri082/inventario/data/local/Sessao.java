// Guarda a sessao do usuario logado (token e papel) durante o uso do aplicativo.
// Nesta fase fica em memoria; a issue #11 passa a guardar de forma criptografada no aparelho.

package com.tri082.inventario.data.local;

public final class Sessao {

    private static String token;
    private static String papel;

    private Sessao() {
    }

    public static void definirToken(String novoToken) {
        token = novoToken;
    }

    public static void definirPapel(String novoPapel) {
        papel = novoPapel;
    }

    public static String getToken() {
        return token;
    }

    public static String getPapel() {
        return papel;
    }

    public static boolean ativa() {
        return token != null && !token.isEmpty();
    }

    public static void limpar() {
        token = null;
        papel = null;
    }
}
