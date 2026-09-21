// Guarda a sessao do usuario (token e papel) de forma criptografada no aparelho, com uma copia em memoria para acesso rapido.

package com.tri082.inventario.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public final class Sessao {

    private static final String ARQUIVO = "sessao_tri082";
    private static final String CHAVE_TOKEN = "token";
    private static final String CHAVE_PAPEL = "papel";

    private static SharedPreferences prefs;
    private static String token;
    private static String papel;

    private Sessao() {
    }

    public static void inicializar(Context context) {
        if (prefs != null) {
            return;
        }
        prefs = criarArmazenamentoSeguro(context.getApplicationContext());
        token = prefs.getString(CHAVE_TOKEN, null);
        papel = prefs.getString(CHAVE_PAPEL, null);
    }

    public static void definirToken(String novoToken) {
        token = novoToken;
        if (prefs != null) {
            prefs.edit().putString(CHAVE_TOKEN, novoToken).apply();
        }
    }

    public static void definirPapel(String novoPapel) {
        papel = novoPapel;
        if (prefs != null) {
            prefs.edit().putString(CHAVE_PAPEL, novoPapel).apply();
        }
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
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
    }

    private static SharedPreferences criarArmazenamentoSeguro(Context context) {
        try {
            MasterKey chave = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    ARQUIVO,
                    chave,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Falha ao abrir o armazenamento seguro da sessao", e);
        }
    }
}
