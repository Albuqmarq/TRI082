// Interceptor que anexa o token de autenticacao em toda chamada, exceto no login.

package com.tri082.inventario.data.remote;

import androidx.annotation.NonNull;

import com.tri082.inventario.data.local.Sessao;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class InterceptadorToken implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request requisicao = chain.request();
        boolean ehLogin = requisicao.url().encodedPath().endsWith("/auth/login");
        if (!ehLogin && Sessao.ativa()) {
            requisicao = requisicao.newBuilder()
                    .header("Authorization", "Bearer " + Sessao.getToken())
                    .build();
        }
        return chain.proceed(requisicao);
    }
}
