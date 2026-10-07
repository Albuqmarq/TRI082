// Tela de inventario: inicia uma sessao, orienta a ler as tags na aba Leitura, finaliza e mostra o resultado.

package com.tri082.inventario.ui;

import android.graphics.drawable.GradientDrawable;
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

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.EstadoLeitura;
import com.tri082.inventario.data.local.SessaoInventario;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.ResultadoInventario;
import com.tri082.inventario.data.remote.SessaoResposta;
import com.tri082.inventario.data.repository.FilaLeituras;
import com.tri082.inventario.util.Cores;
import com.tri082.inventario.util.Datas;

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
    private LinearLayout listaEncontrados;
    private LinearLayout listaAusentes;
    private LinearLayout listaNaoId;
    private TextView contadorEncontrados;
    private TextView contadorAusentes;
    private TextView contadorNaoId;
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
        listaEncontrados = view.findViewById(R.id.lista_encontrados);
        listaAusentes = view.findViewById(R.id.lista_ausentes);
        listaNaoId = view.findViewById(R.id.lista_naoid);
        contadorEncontrados = view.findViewById(R.id.contador_encontrados);
        contadorAusentes = view.findViewById(R.id.contador_ausentes);
        contadorNaoId = view.findViewById(R.id.contador_naoid);

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
            status.setText(R.string.inv_finalizado);
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.verde));
            adicionarLinha(containerSessoes, linha);
            mostradas++;
            if (mostradas == 6) {
                break;
            }
        }
        if (mostradas == 0) {
            textoSemSessoes.setVisibility(View.VISIBLE);
            containerSessoes.addView(textoSemSessoes);
        }
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
        preencherItens(listaAusentes, contadorAusentes, resultado.esperadosAusentes);
        preencherItens(listaEncontrados, contadorEncontrados, resultado.esperadosEncontrados);
        preencherNaoIdentificados(resultado.naoIdentificados);
    }

    private void novo() {
        SessaoInventario.limpar();
        EstadoLeitura.limpar();
        renderEstado();
    }

    private int tamanho(List<?> lista) {
        return lista == null ? 0 : lista.size();
    }

    private void preencherItens(LinearLayout lista, TextView contador, List<ResultadoInventario.ItemResumo> itens) {
        lista.removeAllViews();
        contador.setText(String.valueOf(tamanho(itens)));
        if (itens == null || itens.isEmpty()) {
            adicionarVazio(lista);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ResultadoInventario.ItemResumo item : itens) {
            View linha = inflater.inflate(R.layout.item_catalogo, lista, false);
            ((TextView) linha.findViewById(R.id.item_nome)).setText(item.nome);
            boolean temCor = item.cor != null && !item.cor.isEmpty();
            String tipo = item.tipo != null ? item.tipo : "";
            ((TextView) linha.findViewById(R.id.item_detalhe)).setText(
                    temCor ? item.cor + "  ·  " + tipo : tipo);
            linha.findViewById(R.id.item_categoria).setVisibility(View.GONE);
            View swatch = linha.findViewById(R.id.item_swatch);
            Integer valorCor = Cores.paraCor(item.cor);
            if (valorCor != null) {
                GradientDrawable fundo = (GradientDrawable) ContextCompat
                        .getDrawable(requireContext(), R.drawable.swatch_cor).mutate();
                fundo.setColor(valorCor);
                swatch.setBackground(fundo);
            } else {
                swatch.setVisibility(View.GONE);
            }
            adicionarLinha(lista, linha);
        }
    }

    private void preencherNaoIdentificados(List<ResultadoInventario.LeituraResumo> leituras) {
        listaNaoId.removeAllViews();
        contadorNaoId.setText(String.valueOf(tamanho(leituras)));
        if (leituras == null || leituras.isEmpty()) {
            adicionarVazio(listaNaoId);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ResultadoInventario.LeituraResumo leitura : leituras) {
            View linha = inflater.inflate(R.layout.item_leitura, listaNaoId, false);
            TextView nome = linha.findViewById(R.id.item_nome);
            nome.setText(R.string.leitura_tag_sem_cadastro);
            nome.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario));
            ((TextView) linha.findViewById(R.id.item_epc)).setText(leitura.epc);
            linha.findViewById(R.id.item_status).setVisibility(View.GONE);
            linha.setBackground(null);
            adicionarLinha(listaNaoId, linha);
        }
    }

    private void adicionarLinha(LinearLayout lista, View linha) {
        if (lista.getChildCount() > 0) {
            View divisoria = new View(requireContext());
            divisoria.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    Math.max(1, (int) getResources().getDisplayMetrics().density)));
            divisoria.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.linha));
            lista.addView(divisoria);
        }
        lista.addView(linha);
    }

    private void adicionarVazio(LinearLayout lista) {
        TextView vazio = new TextView(requireContext());
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        vazio.setPadding(pad, pad, pad, pad);
        vazio.setText(R.string.inv_lista_vazia);
        vazio.setTextSize(13);
        vazio.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario));
        lista.addView(vazio);
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
