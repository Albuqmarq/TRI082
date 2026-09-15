// Guarda e recupera o endereco da API no aparelho e monta a URL base usada pelo cliente HTTP.

package com.tri082.inventario.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import com.tri082.inventario.util.EnderecoApi;

public class ConfiguracaoApi {

    private static final String PREFS = "config_tri082";
    private static final String CHAVE_IP = "api_ip";
    private static final String CHAVE_PORTA = "api_porta";
    private static final String PORTA_PADRAO = "8000";

    private final SharedPreferences prefs;

    public ConfiguracaoApi(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void salvar(String ip, String porta) {
        prefs.edit()
                .putString(CHAVE_IP, ip.trim())
                .putString(CHAVE_PORTA, porta.trim())
                .apply();
    }

    public String obterIp() {
        return prefs.getString(CHAVE_IP, "");
    }

    public String obterPorta() {
        return prefs.getString(CHAVE_PORTA, PORTA_PADRAO);
    }

    public boolean estaConfigurado() {
        return !obterIp().isEmpty();
    }

    public String obterUrlBase() {
        return EnderecoApi.montarUrlBase(obterIp(), obterPorta());
    }
}
