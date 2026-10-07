// Sessao de inventario retornada pela API.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class SessaoResposta {

    @SerializedName("id")
    public int id;

    @SerializedName("status")
    public String status;

    @SerializedName("data_inicio")
    public String dataInicio;

    @SerializedName("data_fim")
    public String dataFim;

    @SerializedName("total_leituras")
    public int totalLeituras;
}
