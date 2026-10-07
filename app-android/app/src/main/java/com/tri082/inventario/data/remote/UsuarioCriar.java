// Corpo enviado ao criar uma conta de usuario.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class UsuarioCriar {

    @SerializedName("nome_usuario")
    private final String nomeUsuario;

    @SerializedName("senha")
    private final String senha;

    @SerializedName("papel")
    private final String papel;

    public UsuarioCriar(String nomeUsuario, String senha, String papel) {
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.papel = papel;
    }
}
