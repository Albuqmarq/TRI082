// Corpo enviado ao cadastrar um item novo.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class ItemCriar {

    @SerializedName("epc")
    private final String epc;

    @SerializedName("nome")
    private final String nome;

    @SerializedName("categoria")
    private final String categoria;

    @SerializedName("tipo")
    private final String tipo;

    @SerializedName("cor")
    private final String cor;

    @SerializedName("fornecedor")
    private final String fornecedor;

    public ItemCriar(String epc, String nome, String categoria, String tipo, String cor, String fornecedor) {
        this.epc = epc;
        this.nome = nome;
        this.categoria = categoria;
        this.tipo = tipo;
        this.cor = cor;
        this.fornecedor = fornecedor;
    }
}
