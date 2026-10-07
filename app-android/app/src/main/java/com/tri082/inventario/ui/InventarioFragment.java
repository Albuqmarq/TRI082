// Tela de inventario: inicia uma sessao, orienta a ler as tags na aba Leitura, finaliza e mostra o resultado.

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
import androidx.fragment.app.Fragment;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.EstadoLeitura;
import com.tri082.inventario.data.local.SessaoInventario;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.ResultadoInventario;
import com.tri082.inventario.data.remote.SessaoResposta;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventarioFragment extends Fragment {

    private View areaSemSessao;
    private View areaComSessao;
    private View areaResultado;
    private TextView valorEsperados;
    private TextView valorEncontrados;
    private TextView valorAusentes;
    private TextView valorNaoId;
    private TextView textoEncontrados;
    private TextView textoAusentes;
    private TextView textoNaoId;
    private LinearLayout containerSessoes;
    private TextView textoSemSessoes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventario, container, false);
        areaSemSessao = view.findViewById(R.id.area_sem_sessao);
        containerSessoes = view.findViewById(R.id.container_sessoes);
        textoSemSessoes = view.findViewById(R.id.texto_sem_sessoes);
        areaComSessao = view.findViewById(R.id.area_com_sessao);
        areaResultado = view.findViewById(R.id.area_resultado);
        valorEsperados = view.findViewById(R.id.valor_esperados);
        valorEncontrados = view.findViewById(R.id.valor_encontrados);
        valorAusentes = view.findViewById(R.id.valor_ausentes);
        valorNaoId = view.findViewById(R.id.valor_naoid);
        textoEncontrados = view.findViewById(R.id.texto_encontrados);
        textoAusentes = view.findViewById(R.id.texto_ausentes);
        textoNaoId = view.findViewById(R.id.texto_nao_id);

        view.findViewById(R.id.botao_iniciar).setOnClickListener(v -> iniciar());
        view.findViewById(R.id.botao_finalizar).setOnClickListener(v -> finalizar());
        view.findViewById(R.id.botao_novo).setOnClickListener(v -> novo());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderEstado();
    }

    private void renderEstado() {
        if (SessaoInventario.ativa()) {
            mostrarArea(areaComSessao);
        } else if (SessaoInventario.getFinalizado() != null) {
            carregarResultado(SessaoInventario.getFinalizado());
        } else {
            mostrarArea(areaSemSessao);
            carregarSessoes();
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
        int mostradas = 0;
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = sessoes.size() - 1; i >= 0 && mostradas < 6; i--) {
            SessaoResposta sessao = sessoes.get(i);
            View linha = inflater.inflate(R.layout.item_sessao, containerSessoes, false);
            ((TextView) linha.findViewById(R.id.sessao_data)).setText(formatarData(sessao.dataInicio));
            boolean finalizada = "finalizada".equalsIgnoreCase(sessao.status);
            TextView status = linha.findViewById(R.id.sessao_status);
            status.setText(finalizada ? R.string.inv_finalizado : R.string.inv_aberto);
            status.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(),
                    finalizada ? R.color.verde : R.color.texto_secundario));
            if (mostradas > 0) {
                View div = new View(requireContext());
                div.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) getResources().getDisplayMetrics().density));
                div.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.linha));
                containerSessoes.addView(div);
            }
            containerSessoes.addView(linha);
            mostradas++;
        }
        textoSemSessoes.setVisibility(mostradas == 0 ? View.VISIBLE : View.GONE);
        if (mostradas == 0) {
            containerSessoes.addView(textoSemSessoes);
        }
    }

    private String formatarData(String iso) {
        if (iso == null || iso.length() < 10) {
            return "-";
        }
        String dia = iso.substring(8, 10);
        String mes = iso.substring(5, 7);
        String ano = iso.substring(0, 4);
        return dia + "/" + mes + "/" + ano;
    }

    private void mostrarArea(View area) {
        areaSemSessao.setVisibility(area == areaSemSessao ? View.VISIBLE : View.GONE);
        areaComSessao.setVisibility(area == areaComSessao ? View.VISIBLE : View.GONE);
        areaResultado.setVisibility(area == areaResultado ? View.VISIBLE : View.GONE);
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

    private void finalizar() {
        Integer id = SessaoInventario.getAtivo();
        if (id == null) {
            return;
        }
        servico().finalizarInventario(id).enqueue(new Callback<ResultadoInventario>() {
            @Override
            public void onResponse(@NonNull Call<ResultadoInventario> call, @NonNull Response<ResultadoInventario> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    SessaoInventario.finalizar();
                    mostrarResultado(resposta.body());
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

    private void carregarResultado(int id) {
        servico().obterResultado(id).enqueue(new Callback<ResultadoInventario>() {
            @Override
            public void onResponse(@NonNull Call<ResultadoInventario> call, @NonNull Response<ResultadoInventario> resposta) {
                if (isAdded() && resposta.isSuccessful() && resposta.body() != null) {
                    mostrarResultado(resposta.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResultadoInventario> call, @NonNull Throwable erro) {
            }
        });
    }

    private void mostrarResultado(ResultadoInventario resultado) {
        mostrarArea(areaResultado);
        ResultadoInventario.Resumo r = resultado.resumo;
        valorEsperados.setText(String.valueOf(r.totalEsperado));
        valorEncontrados.setText(String.valueOf(r.totalEncontrados));
        valorAusentes.setText(String.valueOf(r.totalAusentes));
        valorNaoId.setText(String.valueOf(r.totalNaoIdentificados));
        textoEncontrados.setText(getString(R.string.inv_encontrados, tamanho(resultado.esperadosEncontrados))
                + "\n" + nomes(resultado.esperadosEncontrados));
        textoAusentes.setText(getString(R.string.inv_ausentes, tamanho(resultado.esperadosAusentes))
                + "\n" + nomes(resultado.esperadosAusentes));
        textoNaoId.setText(getString(R.string.inv_nao_identificados, tamanho(resultado.naoIdentificados))
                + "\n" + epcs(resultado.naoIdentificados));
    }

    private void novo() {
        SessaoInventario.limpar();
        EstadoLeitura.limpar();
        renderEstado();
    }

    private int tamanho(List<?> lista) {
        return lista == null ? 0 : lista.size();
    }

    private String nomes(List<ResultadoInventario.ItemResumo> itens) {
        if (itens == null || itens.isEmpty()) {
            return "-";
        }
        StringBuilder texto = new StringBuilder();
        for (ResultadoInventario.ItemResumo item : itens) {
            if (texto.length() > 0) {
                texto.append("\n");
            }
            texto.append(item.nome);
            if (item.cor != null && !item.cor.isEmpty()) {
                texto.append(" (").append(item.cor).append(")");
            }
        }
        return texto.toString();
    }

    private String epcs(List<ResultadoInventario.LeituraResumo> leituras) {
        if (leituras == null || leituras.isEmpty()) {
            return "-";
        }
        StringBuilder texto = new StringBuilder();
        for (ResultadoInventario.LeituraResumo leitura : leituras) {
            if (texto.length() > 0) {
                texto.append("\n");
            }
            texto.append(leitura.epc);
        }
        return texto.toString();
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
