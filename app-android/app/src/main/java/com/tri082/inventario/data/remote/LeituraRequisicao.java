// Corpo enviado ao registrar uma leitura numa sessao de inventario.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class LeituraRequisicao {

    @SerializedName("epc")
    private final String epc;

    @SerializedName("dispositivo")
    private final String dispositivo;

    public LeituraRequisicao(String epc, String dispositivo) {
        this.epc = epc;
        this.dispositivo = dispositivo;
    }
}
