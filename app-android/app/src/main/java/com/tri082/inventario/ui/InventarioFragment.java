// Tela de inventario. Sem sessao aberta, mostra os passos e o historico (cada contagem abre o seu relatorio).
// Com sessao aberta, mostra a propria tela de leitura embutida e o botao de finalizar; ao finalizar, abre o relatorio.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.EstadoLeitura;
import com.tri082.inventario.data.local.SessaoInventario;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.ResultadoInventario;
import com.tri082.inventario.data.remote.SessaoResposta;
import com.tri082.inventario.data.repository.FilaLeituras;
import com.tri082.inventario.util.Datas;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventarioFragment extends Fragment {

    private static final int MAXIMO_HISTORICO = 10;

    private View rolagemSemSessao;
    private View areaComSessao;
    private TextView textoSessaoAtiva;
    private LinearLayout containerSessoes;
    private TextView textoSemSessoes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventario, container, false);
        rolagemSemSessao = view.findViewById(R.id.rolagem_sem_sessao);
        areaComSessao = view.findViewById(R.id.area_com_sessao);
        textoSessaoAtiva = view.findViewById(R.id.texto_sessao_ativa);
        containerSessoes = view.findViewById(R.id.container_sessoes);
        textoSemSessoes = view.findViewById(R.id.texto_sem_sessoes);

        view.findViewById(R.id.botao_iniciar).setOnClickListener(v -> iniciar());
        view.findViewById(R.id.botao_finalizar).setOnClickListener(v -> finalizar());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderEstado();
    }

    private void renderEstado() {
        Integer ativa = SessaoInventario.getAtivo();
        boolean comSessao = ativa != null;
        rolagemSemSessao.setVisibility(comSessao ? View.GONE : View.VISIBLE);
        areaComSessao.setVisibility(comSessao ? View.VISIBLE : View.GONE);
        if (comSessao) {
            textoSessaoAtiva.setText(getString(R.string.inv_sessao_numero, ativa));
            mostrarLeitura();
        } else {
            removerLeitura();
            carregarSessoes();
        }
    }

    private void mostrarLeitura() {
        if (getChildFragmentManager().findFragmentById(R.id.leitura_embutida) != null) {
            return;
        }
        LeituraFragment leitura = new LeituraFragment();
        Bundle argumentos = new Bundle();
        argumentos.putBoolean("embutida", true);
        leitura.setArguments(argumentos);
        getChildFragmentManager().beginTransaction().replace(R.id.leitura_embutida, leitura).commitNow();
    }

    private void removerLeitura() {
        Fragment leitura = getChildFragmentManager().findFragmentById(R.id.leitura_embutida);
        if (leitura != null) {
            getChildFragmentManager().beginTransaction().remove(leitura).commitNow();
        }
    }

    private void carregarSessoes() {
        servico().listarSessoes().enqueue(new Callback<List<SessaoResposta>>() {
            @Override
            public void onResponse(@NonNull Call<List<SessaoResposta>> call, @NonNull Response<List<SessaoResposta>> resposta) {
                if (isAdded() && resposta.isSuccessful() && resposta.body() != null) {
                    mostrarSessoes(resposta.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SessaoResposta>> call, @NonNull Throwable erro) {
            }
        });
    }

    private void mostrarSessoes(List<SessaoResposta> sessoes) {
        containerSessoes.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int mostradas = 0;
        for (SessaoResposta sessao : sessoes) {
            boolean real = "finalizada".equalsIgnoreCase(sessao.status) && sessao.totalLeituras > 0;
            if (!real) {
                continue;
            }
            View linha = inflater.inflate(R.layout.item_sessao, containerSessoes, false);
            ((TextView) linha.findViewById(R.id.sessao_data)).setText(Datas.paraTexto(sessao.dataInicio));
            ((TextView) linha.findViewById(R.id.sessao_tags)).setText(
                    getResources().getQuantityString(R.plurals.inv_tags, sessao.totalLeituras, sessao.totalLeituras));
            TextView status = linha.findViewById(R.id.sessao_status);
            status.setText(R.string.inv_ver_relatorio);
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.navy));
            linha.setBackgroundResource(R.drawable.fundo_clicavel);
            linha.setOnClickListener(v -> abrirRelatorio(sessao.id));
            if (mostradas > 0) {
                View divisoria = new View(requireContext());
                divisoria.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Math.max(1, (int) getResources().getDisplayMetrics().density)));
                divisoria.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.linha));
                containerSessoes.addView(divisoria);
            }
            containerSessoes.addView(linha);
            mostradas++;
            if (mostradas == MAXIMO_HISTORICO) {
                break;
            }
        }
        if (mostradas == 0) {
            textoSemSessoes.setVisibility(View.VISIBLE);
            containerSessoes.addView(textoSemSessoes);
        }
    }

    private void abrirRelatorio(int sessaoId) {
        Bundle argumentos = new Bundle();
        argumentos.putInt("sessaoId", sessaoId);
        NavHostFragment.findNavController(this).navigate(R.id.relatorioFragment, argumentos);
    }

    private void iniciar() {
        servico().criarSessao().enqueue(new Callback<SessaoResposta>() {
            @Override
            public void onResponse(@NonNull Call<SessaoResposta> call, @NonNull Response<SessaoResposta> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    SessaoInventario.definirAtivo(resposta.body().id);
                    EstadoLeitura.limpar();
                    Toast.makeText(requireContext(), R.string.inv_iniciado, Toast.LENGTH_SHORT).show();
                    renderEstado();
                } else {
                    erro();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SessaoResposta> call, @NonNull Throwable erro) {
                erro();
            }
        });
    }

    // Antes de finalizar, tenta enviar as leituras desta sessao que ficaram na fila; sem isso o resultado sairia incompleto.
    private void finalizar() {
        Integer id = SessaoInventario.getAtivo();
        if (id == null) {
            return;
        }
        FilaLeituras.contarDaSessao(requireContext(), id, pendentes -> {
            if (!isAdded()) {
                return;
            }
            if (pendentes == 0) {
                enviarFinalizacao(id);
                return;
            }
            FilaLeituras.sincronizar(requireContext(), resultado ->
                    FilaLeituras.contarDaSessao(requireContext(), id, restantes -> {
                        if (!isAdded()) {
                            return;
                        }
                        if (restantes == 0) {
                            enviarFinalizacao(id);
                        } else {
                            Toast.makeText(requireContext(), getResources().getQuantityString(
                                    R.plurals.inv_finalizar_pendentes, restantes, restantes), Toast.LENGTH_LONG).show();
                        }
                    }));
        });
    }

    private void enviarFinalizacao(int id) {
        servico().finalizarInventario(id).enqueue(new Callback<ResultadoInventario>() {
            @Override
            public void onResponse(@NonNull Call<ResultadoInventario> call, @NonNull Response<ResultadoInventario> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful()) {
                    SessaoInventario.limpar();
                    EstadoLeitura.limpar();
                    removerLeitura();
                    abrirRelatorio(id);
                } else {
                    erro();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResultadoInventario> call, @NonNull Throwable erro) {
                erro();
            }
        });
    }

    private void erro() {
        if (isAdded()) {
            Toast.makeText(requireContext(), R.string.inv_erro, Toast.LENGTH_LONG).show();
        }
    }

    private ApiService servico() {
        return ClienteApi.criarServico(requireContext());
    }
}
