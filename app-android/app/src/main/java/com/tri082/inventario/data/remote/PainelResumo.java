// Numeros gerais da operacao num periodo, mostrados no dashboard do Painel.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class PainelResumo {

    @SerializedName("periodo_dias")
    public int periodoDias;

    @SerializedName("itens_total")
    public int itensTotal;

    @SerializedName("itens_novos")
    public int itensNovos;

    @SerializedName("inventarios_abertos")
    public int inventariosAbertos;

    @SerializedName("inventarios_finalizados")
    public int inventariosFinalizados;

    @SerializedName("inventarios_finalizados_total")
    public int inventariosFinalizadosTotal;

    @SerializedName("leituras")
    public int leituras;

    @SerializedName("ultima_contagem")
    public UltimaContagem ultimaContagem;

    public static class UltimaContagem {
        @SerializedName("sessao_id")
        public int sessaoId;

        @SerializedName("data_fim")
        public String dataFim;

        @SerializedName("total_esperado")
        public int totalEsperado;

        @SerializedName("total_encontrados")
        public int totalEncontrados;

        @SerializedName("total_ausentes")
        public int totalAusentes;

        @SerializedName("total_nao_identificados")
        public int totalNaoIdentificados;
    }
}
