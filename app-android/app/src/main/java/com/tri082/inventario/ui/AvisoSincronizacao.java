// Mensagem padrao mostrada ao operador depois de uma sincronizacao manual da fila de leituras.

package com.tri082.inventario.ui;

import android.content.Context;
import android.widget.Toast;

import com.tri082.inventario.R;
import com.tri082.inventario.data.repository.FilaLeituras;

final class AvisoSincronizacao {

    private AvisoSincronizacao() {
    }

    static void mostrar(Context contexto, FilaLeituras.Resultado resultado) {
        String texto;
        if (resultado.semConexao) {
            texto = contexto.getString(R.string.fila_sem_conexao);
        } else if (resultado.restantes > 0) {
            texto = contexto.getString(R.string.fila_servidor_indisponivel);
        } else if (resultado.enviadas == 0 && resultado.descartadas == 0) {
            texto = contexto.getString(R.string.fila_nada);
        } else {
            texto = contexto.getResources().getQuantityString(R.plurals.fila_enviadas,
                    resultado.enviadas, resultado.enviadas);
        }
        Toast.makeText(contexto, texto, Toast.LENGTH_LONG).show();
    }
}
