// Dados de uma conta de usuario retornados pela API (o proprio usuario ou a lista do Administrador).

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class UsuarioResposta {

    @SerializedName("id")
    private int id;

    @SerializedName("nome_usuario")
    private String nomeUsuario;

    @SerializedName("papel")
    private String papel;

    @SerializedName("ativo")
    private boolean ativo;

    public int getId() {
        return id;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
