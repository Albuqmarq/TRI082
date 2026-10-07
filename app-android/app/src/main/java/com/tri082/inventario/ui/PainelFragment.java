// Tela inicial apos o login: indicadores da operacao, aviso de pendencias (admin) e acoes rapidas por papel.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;
import com.tri082.inventario.data.remote.Pendencia;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PainelFragment extends Fragment {

    private TextView textoPapel;
    private TextView valorItens;
    private TextView textoPendenciasTitulo;
    private View cardPendencias;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_painel, container, false);
        textoPapel = view.findViewById(R.id.texto_papel);
        valorItens = view.findViewById(R.id.valor_itens);
        textoPendenciasTitulo = view.findViewById(R.id.texto_pendencias_titulo);
        cardPendencias = view.findViewById(R.id.card_pendencias);

        boolean admin = Sessao.ehAdmin();
        textoPapel.setText(getString(R.string.painel_papel,
                admin ? getString(R.string.painel_papel_admin) : getString(R.string.painel_papel_funcionario)));

        view.findViewById(R.id.linha_cadastrar).setVisibility(admin ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.div_cadastrar).setVisibility(admin ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.linha_cadastrar).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.itemFormFragment));
        view.findViewById(R.id.linha_leitura).setOnClickListener(v -> irPara(v, R.id.leituraFragment));
        view.findViewById(R.id.linha_inventario).setOnClickListener(v -> irPara(v, R.id.inventarioFragment));
        view.findViewById(R.id.botao_revisar).setOnClickListener(v -> irPara(v, R.id.leituraFragment));
        view.findViewById(R.id.botao_config).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.configuracoesFragment));
        view.findViewById(R.id.botao_sair).setOnClickListener(this::sair);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarItens();
        if (Sessao.ehAdmin()) {
            carregarPendencias();
        }
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
