// Corpo enviado ao registrar uma leitura numa sessao de inventario. O horario e o da leitura no coletor,
// para que uma leitura reenviada depois pela fila mantenha a hora em que foi feita.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class LeituraRequisicao {

    @SerializedName("epc")
    private final String epc;

    @SerializedName("dispositivo")
    private final String dispositivo;

    @SerializedName("timestamp")
    private final String timestamp;

    public LeituraRequisicao(String epc, String dispositivo, String timestamp) {
        this.epc = epc;
        this.dispositivo = dispositivo;
        this.timestamp = timestamp;
    }
}
