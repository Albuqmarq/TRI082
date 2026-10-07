// Tela inicial apos o login: indicadores da operacao, dashboard por periodo, aviso de pendencias (admin)
// e acoes rapidas por papel.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;
import com.tri082.inventario.data.remote.PainelResumo;
import com.tri082.inventario.data.remote.Pendencia;
import com.tri082.inventario.data.repository.FilaLeituras;
import com.tri082.inventario.util.Datas;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PainelFragment extends Fragment {

    private TextView textoPapel;
    private TextView valorItens;
    private TextView textoPendenciasTitulo;
    private View cardPendencias;
    private TextView valorPendentesEnvio;
    private TextView textoSincronia;
    private View botaoSincronizar;
    private final Runnable aoMudarFila = this::atualizarFila;
    private View raiz;
    private int periodoDias = 30;
    private PainelResumo.UltimaContagem ultimaContagem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_painel, container, false);
        raiz = view;
        view.findViewById(R.id.dash_7).setOnClickListener(v -> escolherPeriodo(7));
        view.findViewById(R.id.dash_30).setOnClickListener(v -> escolherPeriodo(30));
        view.findViewById(R.id.dash_90).setOnClickListener(v -> escolherPeriodo(90));
        view.findViewById(R.id.dash_card_ultima).setOnClickListener(v -> abrirUltimaContagem());
        marcarPeriodo();
        textoPapel = view.findViewById(R.id.texto_papel);
        valorItens = view.findViewById(R.id.valor_itens);
        textoPendenciasTitulo = view.findViewById(R.id.texto_pendencias_titulo);
        cardPendencias = view.findViewById(R.id.card_pendencias);
        valorPendentesEnvio = view.findViewById(R.id.valor_pendentes_envio);
        textoSincronia = view.findViewById(R.id.texto_sincronia);
        botaoSincronizar = view.findViewById(R.id.botao_sincronizar_painel);
        botaoSincronizar.setOnClickListener(v -> sincronizar());

        boolean admin = Sessao.ehAdmin();
        textoPapel.setText(getString(R.string.painel_papel,
                admin ? getString(R.string.painel_papel_admin) : getString(R.string.painel_papel_funcionario)));

        view.findViewById(R.id.linha_cadastrar).setVisibility(admin ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.div_cadastrar).setVisibility(admin ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.area_administracao).setVisibility(admin ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.linha_usuarios).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.usuariosFragment));

        view.findViewById(R.id.linha_cadastrar).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.itemFormFragment));
        view.findViewById(R.id.linha_leitura).setOnClickListener(v -> irPara(v, R.id.leituraFragment));
        view.findViewById(R.id.linha_inventario).setOnClickListener(v -> irPara(v, R.id.inventarioFragment));
        view.findViewById(R.id.botao_revisar).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.pendenciasFragment));
        view.findViewById(R.id.botao_config).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.configuracoesFragment));
        view.findViewById(R.id.botao_sair).setOnClickListener(this::sair);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        FilaLeituras.adicionarOuvinte(aoMudarFila);
        atualizarFila();
        carregarResumo();
        carregarItens();
        if (Sessao.ehAdmin()) {
            carregarPendencias();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        FilaLeituras.removerOuvinte(aoMudarFila);
    }

    private void escolherPeriodo(int dias) {
        periodoDias = dias;
        marcarPeriodo();
        carregarResumo();
    }

    private void marcarPeriodo() {
        raiz.findViewById(R.id.dash_7).setSelected(periodoDias == 7);
        raiz.findViewById(R.id.dash_30).setSelected(periodoDias == 30);
        raiz.findViewById(R.id.dash_90).setSelected(periodoDias == 90);
    }

    private void carregarResumo() {
        int pedido = periodoDias;
        servico().obterResumo(pedido).enqueue(new Callback<PainelResumo>() {
            @Override
            public void onResponse(@NonNull Call<PainelResumo> call, @NonNull Response<PainelResumo> resposta) {
                if (isAdded() && pedido == periodoDias && resposta.isSuccessful() && resposta.body() != null) {
                    mostrarResumo(resposta.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PainelResumo> call, @NonNull Throwable erro) {
            }
        });
    }

    private void mostrarResumo(PainelResumo resumo) {
        NumberFormat numero = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));
        ((TextView) raiz.findViewById(R.id.dash_itens)).setText(numero.format(resumo.itensNovos));
        ((TextView) raiz.findViewById(R.id.dash_contagens)).setText(numero.format(resumo.inventariosFinalizados));
        ((TextView) raiz.findViewById(R.id.dash_leituras)).setText(numero.format(resumo.leituras));

        ultimaContagem = resumo.ultimaContagem;
        boolean tem = ultimaContagem != null;
        raiz.findViewById(R.id.dash_ultima_conteudo).setVisibility(tem ? View.VISIBLE : View.GONE);
        raiz.findViewById(R.id.dash_sem_contagem).setVisibility(tem ? View.GONE : View.VISIBLE);
        if (!tem) {
            ((TextView) raiz.findViewById(R.id.dash_ultima_data)).setText("");
            return;
        }
        int percentual = ultimaContagem.totalEsperado > 0
                ? Math.round(100f * ultimaContagem.totalEncontrados / ultimaContagem.totalEsperado) : 0;
        ((TextView) raiz.findViewById(R.id.dash_ultima_data)).setText(Datas.paraTexto(ultimaContagem.dataFim));
        ((TextView) raiz.findViewById(R.id.dash_percentual)).setText(getString(R.string.dash_percentual, percentual));
        ((TextView) raiz.findViewById(R.id.dash_ultima_detalhe)).setText(getString(R.string.dash_ultima_detalhe,
                ultimaContagem.totalEncontrados, ultimaContagem.totalEsperado));
        ((TextView) raiz.findViewById(R.id.dash_ultima_extra)).setText(getString(R.string.dash_ultima_extra,
                ultimaContagem.totalAusentes, ultimaContagem.totalNaoIdentificados));
        ((LinearProgressIndicator) raiz.findViewById(R.id.dash_progresso)).setProgressCompat(percentual, true);
    }

    private void abrirUltimaContagem() {
        if (ultimaContagem == null) {
            return;
        }
        Bundle argumentos = new Bundle();
        argumentos.putInt("sessaoId", ultimaContagem.sessaoId);
        Navigation.findNavController(raiz).navigate(R.id.relatorioFragment, argumentos);
    }

    private void atualizarFila() {
        FilaLeituras.contar(requireContext(), total -> {
            if (!isAdded()) {
                return;
            }
            valorPendentesEnvio.setText(String.valueOf(total));
            boolean temPendentes = total > 0;
            textoSincronia.setText(temPendentes ? R.string.painel_aguardando : R.string.painel_sincronizado);
            textoSincronia.setTextColor(ContextCompat.getColor(requireContext(),
                    temPendentes ? R.color.alerta_texto : R.color.verde));
            botaoSincronizar.setVisibility(temPendentes ? View.VISIBLE : View.GONE);
        });
    }

    private void sincronizar() {
        FilaLeituras.sincronizar(requireContext(), resultado -> {
            if (isAdded()) {
                AvisoSincronizacao.mostrar(requireContext(), resultado);
            }
        });
    }

    private void irPara(View origem, int destino) {
        Navigation.findNavController(origem).navigate(destino);
    }

    private void carregarItens() {
        servico().listarItens().enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(@NonNull Call<List<Item>> call, @NonNull Response<List<Item>> resposta) {
                if (isAdded() && resposta.isSuccessful() && resposta.body() != null) {
                    valorItens.setText(String.valueOf(resposta.body().size()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Item>> call, @NonNull Throwable erro) {
            }
        });
    }

    private void carregarPendencias() {
        servico().listarPendencias().enqueue(new Callback<List<Pendencia>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pendencia>> call, @NonNull Response<List<Pendencia>> resposta) {
                if (isAdded() && resposta.isSuccessful() && resposta.body() != null && !resposta.body().isEmpty()) {
                    textoPendenciasTitulo.setText(getString(R.string.painel_pendencias_titulo, resposta.body().size()));
                    cardPendencias.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Pendencia>> call, @NonNull Throwable erro) {
            }
        });
    }

    private void sair(View view) {
        Sessao.limpar();
        NavOptions opcoes = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        Navigation.findNavController(view).navigate(R.id.loginFragment, null, opcoes);
    }

    private ApiService servico() {
        return ClienteApi.criarServico(requireContext());
    }
}
