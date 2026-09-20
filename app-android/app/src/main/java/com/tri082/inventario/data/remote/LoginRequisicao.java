// Corpo enviado no login: usuario e senha.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class LoginRequisicao {

    @SerializedName("nome_usuario")
    private final String nomeUsuario;

    private final String senha;

    public LoginRequisicao(String nomeUsuario, String senha) {
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
    }
}
