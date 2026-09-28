// Tela inicial apos o login: mostra os indicadores da operacao (itens, pendentes de envio, pendencias de cadastro)
// e atalhos que so aparecem para o Administrador.

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
    private TextView valorPendentesEnvio;
    private TextView valorPendencias;
    private View cardPendencias;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_painel, container, false);
        textoPapel = view.findViewById(R.id.texto_papel);
        valorItens = view.findViewById(R.id.valor_itens);
        valorPendentesEnvio = view.findViewById(R.id.valor_pendentes_envio);
        valorPendencias = view.findViewById(R.id.valor_pendencias);
        cardPendencias = view.findViewById(R.id.card_pendencias);

        boolean admin = Sessao.ehAdmin();
        textoPapel.setText(getString(R.string.painel_papel,
                admin ? getString(R.string.painel_papel_admin) : getString(R.string.painel_papel_funcionario)));
        view.findViewById(R.id.area_atalhos_admin).setVisibility(admin ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.botao_novo_item).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.itemFormFragment));
        view.findViewById(R.id.botao_config).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.configuracoesFragment));
        view.findViewById(R.id.botao_sair).setOnClickListener(this::sair);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // A fila offline (#15) ainda nao existe: as leituras vao direto para a API, entao nao ha nada na fila de envio.
        valorPendentesEnvio.setText("0");
        carregarItens();
        if (Sessao.ehAdmin()) {
            carregarPendencias();
        }
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
                if (isAdded() && resposta.isSuccessful() && resposta.body() != null) {
                    valorPendencias.setText(String.valueOf(resposta.body().size()));
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
