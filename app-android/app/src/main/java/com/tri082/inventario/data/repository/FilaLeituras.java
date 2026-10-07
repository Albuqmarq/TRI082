// Envio das leituras de inventario com fila local: se o envio falhar por rede ou servidor, a leitura vai para o Room
// e so sai de la quando o operador sincroniza. A API ignora leitura repetida da mesma tag na sessao, entao reenviar nao duplica.

package com.tri082.inventario.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.tri082.inventario.data.local.BancoLocal;
import com.tri082.inventario.data.local.LeituraPendente;
import com.tri082.inventario.data.local.LeituraPendenteDao;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.LeituraRequisicao;
import com.tri082.inventario.util.Datas;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class FilaLeituras {

    public static class Resultado {
        public int enviadas;
        public int descartadas;
        public int restantes;
        public boolean semConexao;
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());
    private static final List<Runnable> OUVINTES = new CopyOnWriteArrayList<>();

    private FilaLeituras() {
    }

    public static void adicionarOuvinte(Runnable ouvinte) {
        OUVINTES.add(ouvinte);
    }

    public static void removerOuvinte(Runnable ouvinte) {
        OUVINTES.remove(ouvinte);
    }

    public static void enviar(Context context, int sessaoId, String epc, String dispositivo) {
        Context app = context.getApplicationContext();
        String lidaEm = Datas.agoraIso();
        ClienteApi.criarServico(app)
                .registrarLeitura(sessaoId, new LeituraRequisicao(epc, dispositivo, lidaEm))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> resposta) {
                        if (deveGuardar(resposta.code())) {
                            guardar(app, sessaoId, epc, dispositivo, lidaEm);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable erro) {
                        guardar(app, sessaoId, epc, dispositivo, lidaEm);
                    }
                });
    }

    public static void sincronizar(Context context, Consumer<Resultado> aoTerminar) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            LeituraPendenteDao dao = BancoLocal.obter(app).leiturasPendentes();
            ApiService servico = ClienteApi.criarServico(app);
            Resultado resultado = new Resultado();
            for (LeituraPendente leitura : dao.listar()) {
                try {
                    Response<Void> resposta = servico.registrarLeitura(leitura.sessaoId,
                            new LeituraRequisicao(leitura.epc, leitura.dispositivo, leitura.lidaEm)).execute();
                    if (resposta.isSuccessful()) {
                        dao.remover(leitura);
                        resultado.enviadas++;
                    } else if (deveGuardar(resposta.code())) {
                        break;
                    } else {
                        dao.remover(leitura);
                        resultado.descartadas++;
                    }
                } catch (IOException semRede) {
                    resultado.semConexao = true;
                    break;
                }
            }
            resultado.restantes = dao.contar();
            PRINCIPAL.post(() -> {
                aoTerminar.accept(resultado);
                notificar();
            });
        });
    }

    public static void contar(Context context, Consumer<Integer> aoContar) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            int total = BancoLocal.obter(app).leiturasPendentes().contar();
            PRINCIPAL.post(() -> aoContar.accept(total));
        });
    }

    public static void contarDaSessao(Context context, int sessaoId, Consumer<Integer> aoContar) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            int total = BancoLocal.obter(app).leiturasPendentes().contarDaSessao(sessaoId);
            PRINCIPAL.post(() -> aoContar.accept(total));
        });
    }

    // Erros que podem se resolver depois (servidor fora ou login expirado) mantem a leitura na fila.
    // Os demais (sessao finalizada ou inexistente, dado invalido) nunca vao dar certo e a leitura e descartada.
    private static boolean deveGuardar(int codigo) {
        return codigo == 401 || codigo >= 500;
    }

    private static void guardar(Context app, int sessaoId, String epc, String dispositivo, String lidaEm) {
        EXECUTOR.execute(() -> {
            LeituraPendente leitura = new LeituraPendente();
            leitura.sessaoId = sessaoId;
            leitura.epc = epc;
            leitura.dispositivo = dispositivo;
            leitura.lidaEm = lidaEm;
            BancoLocal.obter(app).leiturasPendentes().inserir(leitura);
            PRINCIPAL.post(FilaLeituras::notificar);
        });
    }

    private static void notificar() {
        for (Runnable ouvinte : OUVINTES) {
            ouvinte.run();
        }
    }
}
