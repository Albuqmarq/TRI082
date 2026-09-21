// Item do catalogo retornado pela API.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class Item {

    @SerializedName("id")
    private int id;

    @SerializedName("epc")
    private String epc;

    @SerializedName("nome")
    private String nome;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("cor")
    private String cor;

    @SerializedName("fornecedor")
    private String fornecedor;

    public int getId() {
        return id;
    }

    public String getEpc() {
        return epc;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCor() {
        return cor;
    }

    public String getFornecedor() {
        return fornecedor;
    }
}
