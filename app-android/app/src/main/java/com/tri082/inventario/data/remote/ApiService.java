// Mapa dos endpoints da API consumidos pelo aplicativo.

package com.tri082.inventario.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("auth/login")
    Call<TokenResposta> login(@Body LoginRequisicao requisicao);

    @GET("usuarios/me")
    Call<UsuarioResposta> obterMeusDados();

    @GET("itens")
    Call<List<Item>> listarItens();

    @GET("itens/{id}")
    Call<Item> obterItem(@Path("id") int id);

    @GET("itens/epc/{epc}")
    Call<Item> obterItemPorEpc(@Path("epc") String epc);

    @GET("itens/recomendacao")
    Call<List<Item>> obterRecomendacao();

    @POST("itens")
    Call<Item> criarItem(@Body ItemCriar item);

    @PATCH("itens/{id}")
    Call<Item> atualizarItem(@Path("id") int id, @Body ItemAtualizar item);

    @DELETE("itens/{id}")
    Call<Void> excluirItem(@Path("id") int id);
}
