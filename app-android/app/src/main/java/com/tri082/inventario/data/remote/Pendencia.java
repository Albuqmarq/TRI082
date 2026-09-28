package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class Pendencia {
    public String epc;
    public int ocorrencias;

    @SerializedName("ultima_leitura")
    public String ultimaLeitura;
}
