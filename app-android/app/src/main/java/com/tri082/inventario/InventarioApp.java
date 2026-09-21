// Classe de aplicacao. Carrega a sessao guardada no aparelho assim que o app inicia.

package com.tri082.inventario;

import android.app.Application;

import com.tri082.inventario.data.local.Sessao;

public class InventarioApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Sessao.inicializar(this);
    }
}
