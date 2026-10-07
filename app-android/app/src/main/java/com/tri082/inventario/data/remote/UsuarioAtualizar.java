// Corpo enviado ao editar uma conta. Campos nulos nao sao enviados, entao so muda o que foi preenchido.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class UsuarioAtualizar {

    @SerializedName("papel")
    private final String papel;

    @SerializedName("senha")
    private final String senha;

    @SerializedName("ativo")
    private final Boolean ativo;

    public UsuarioAtualizar(String papel, String senha, Boolean ativo) {
        this.papel = papel;
        this.senha = senha;
        this.ativo = ativo;
    }
}
