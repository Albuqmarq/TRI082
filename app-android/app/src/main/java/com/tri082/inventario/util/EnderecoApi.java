// Regras puras do endereco da API: validacao de IP e porta e montagem da URL base.

package com.tri082.inventario.util;

public final class EnderecoApi {

    private EnderecoApi() {
    }

    public static boolean ipValido(String ip) {
        if (ip == null) {
            return false;
        }
        String valor = ip.trim();
        return !valor.isEmpty() && valor.matches("[A-Za-z0-9.\\-]+");
    }

    public static boolean portaValida(String porta) {
        if (porta == null) {
            return false;
        }
        try {
            int numero = Integer.parseInt(porta.trim());
            return numero >= 1 && numero <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String montarUrlBase(String ip, String porta) {
        return "http://" + ip.trim() + ":" + porta.trim() + "/";
    }
}
