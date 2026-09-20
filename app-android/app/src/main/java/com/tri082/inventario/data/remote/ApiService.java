// Mapa dos endpoints da API consumidos pelo aplicativo.

package com.tri082.inventario.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<TokenResposta> login(@Body LoginRequisicao requisicao);

    @GET("usuarios/me")
    Call<UsuarioResposta> obterMeusDados();
}
