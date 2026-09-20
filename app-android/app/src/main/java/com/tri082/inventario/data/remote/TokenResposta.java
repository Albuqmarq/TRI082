// Resposta do login: o token de acesso e o tipo do token.

package com.tri082.inventario.data.remote;

import com.google.gson.annotations.SerializedName;

public class TokenResposta {

    @SerializedName("token_acesso")
    private String tokenAcesso;

    @SerializedName("tipo_token")
    private String tipoToken;

    public String getTokenAcesso() {
        return tokenAcesso;
    }

    public String getTipoToken() {
        return tipoToken;
    }
}
