// Monta o cliente HTTP (Retrofit) usando o endereco configurado e o interceptor de token.

package com.tri082.inventario.data.remote;

import android.content.Context;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ClienteApi {

    private ClienteApi() {
    }

    public static ApiService criarServico(Context context) {
        ConfiguracaoApi configuracao = new ConfiguracaoApi(context);

        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient cliente = new OkHttpClient.Builder()
                .addInterceptor(new InterceptadorToken())
                .addInterceptor(log)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(configuracao.obterUrlBase())
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();

        return retrofit.create(ApiService.class);
    }
}
