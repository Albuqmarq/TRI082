// Resultado de uma sessao de inventario finalizada: resumo e as tres categorias.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResultadoInventario {

    @SerializedName("resumo")
    public Resumo resumo;

    @SerializedName("esperados_encontrados")
    public List<ItemResumo> esperadosEncontrados;

    @SerializedName("esperados_ausentes")
    public List<ItemResumo> esperadosAusentes;

    @SerializedName("nao_identificados")
    public List<LeituraResumo> naoIdentificados;

    public static class Resumo {
        @SerializedName("total_esperado")
        public int totalEsperado;

        @SerializedName("total_encontrados")
        public int totalEncontrados;

        @SerializedName("total_ausentes")
        public int totalAusentes;

        @SerializedName("total_nao_identificados")
        public int totalNaoIdentificados;
    }

    public static class ItemResumo {
        @SerializedName("nome")
        public String nome;

        @SerializedName("epc")
        public String epc;

        @SerializedName("tipo")
        public String tipo;

        @SerializedName("cor")
        public String cor;
    }

    public static class LeituraResumo {
        @SerializedName("epc")
        public String epc;
    }
}
