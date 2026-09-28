// Tela de cadastro e edicao de item (somente Administrador). No cadastro, sugere os ultimos itens para reaproveitar.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;
import com.tri082.inventario.data.remote.ItemAtualizar;
import com.tri082.inventario.data.remote.ItemCriar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemFormFragment extends Fragment {

    private static final int MAXIMO_RECOMENDACOES = 3;

    private int itemId;
    private boolean edicao;
    private EditText campoEpc;
    private EditText campoNome;
    private EditText campoCategoria;
    private EditText campoTipo;
    private EditText campoCor;
    private EditText campoFornecedor;
    private Button botaoSalvar;
    private TextView textoRecomendar;
    private LinearLayout containerRecomendacoes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_form, container, false);
        itemId = getArguments() != null ? getArguments().getInt("itemId", -1) : -1;
        edicao = itemId > 0;

        campoEpc = view.findViewById(R.id.campo_epc);
        campoNome = view.findViewById(R.id.campo_nome);
        campoCategoria = view.findViewById(R.id.campo_categoria);
        campoTipo = view.findViewById(R.id.campo_tipo);
        campoCor = view.findViewById(R.id.campo_cor);
        campoFornecedor = view.findViewById(R.id.campo_fornecedor);
        botaoSalvar = view.findViewById(R.id.botao_salvar);
        textoRecomendar = view.findViewById(R.id.texto_recomendar);
        containerRecomendacoes = view.findViewById(R.id.container_recomendacoes);
        TextView titulo = view.findViewById(R.id.form_titulo);

        if (edicao) {
            titulo.setText(R.string.form_titulo_editar);
            campoEpc.setVisibility(View.GONE);
            carregarItem();
        } else {
            titulo.setText(R.string.form_titulo_novo);
            String epcDaTag = getArguments() != null ? getArguments().getString("epc") : null;
            if (epcDaTag != null && !epcDaTag.isEmpty()) {
                campoEpc.setText(epcDaTag);
                campoEpc.setEnabled(false);
            }
            carregarRecomendacoes();
        }

        botaoSalvar.setOnClickListener(v -> salvar());
        return view;
    }

    private void carregarItem() {
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.obterItem(itemId).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(@NonNull Call<Item> call, @NonNull Response<Item> resposta) {
                if (isAdded() && resposta.isSuccessful() && resposta.body() != null) {
                    preencher(resposta.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Item> call, @NonNull Throwable erro) {
            }
        });
    }

    private void carregarRecomendacoes() {
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.obterRecomendacao().enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(@NonNull Call<List<Item>> call, @NonNull Response<List<Item>> resposta) {
                if (!isAdded() || resposta.body() == null || resposta.body().isEmpty()) {
                    return;
                }
                mostrarRecomendacoes(resposta.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Item>> call, @NonNull Throwable erro) {
            }
        });
    }

    private void mostrarRecomendacoes(List<Item> itens) {
        containerRecomendacoes.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int total = Math.min(itens.size(), MAXIMO_RECOMENDACOES);
        for (int i = 0; i < total; i++) {
            Item item = itens.get(i);
            View cartao = inflater.inflate(R.layout.item_recomendacao, containerRecomendacoes, false);
            ((TextView) cartao.findViewById(R.id.rec_nome)).setText(item.getNome());
            ((TextView) cartao.findViewById(R.id.rec_detalhe)).setText(descrever(item));
            cartao.setOnClickListener(v -> preencher(item));
            containerRecomendacoes.addView(cartao);
        }
        textoRecomendar.setVisibility(View.VISIBLE);
        containerRecomendacoes.setVisibility(View.VISIBLE);
    }

    private String descrever(Item item) {
        StringBuilder texto = new StringBuilder(item.getTipo());
        if (item.getCor() != null && !item.getCor().isEmpty()) {
            texto.append(" - ").append(item.getCor());
        }
        return texto.toString();
    }

    private void preencher(Item item) {
        campoNome.setText(item.getNome());
        campoCategoria.setText(item.getCategoria());
        campoTipo.setText(item.getTipo());
        campoCor.setText(item.getCor());
        campoFornecedor.setText(item.getFornecedor());
    }

    private void salvar() {
        String epc = campoEpc.getText().toString().trim();
        String nome = campoNome.getText().toString().trim();
        String categoria = campoCategoria.getText().toString().trim();
        String tipo = campoTipo.getText().toString().trim();
        String cor = campoCor.getText().toString().trim();
        String fornecedor = campoFornecedor.getText().toString().trim();

        if (nome.isEmpty() || categoria.isEmpty() || tipo.isEmpty()
                || (!edicao && epc.isEmpty())) {
            Toast.makeText(requireContext(), R.string.form_erro_campos, Toast.LENGTH_LONG).show();
            return;
        }

        botaoSalvar.setEnabled(false);
        ApiService servico = ClienteApi.criarServico(requireContext());
        if (edicao) {
            servico.atualizarItem(itemId, new ItemAtualizar(nome, categoria, tipo, cor, fornecedor))
                    .enqueue(retornoSalvar());
        } else {
            servico.criarItem(new ItemCriar(epc, nome, categoria, tipo, cor, fornecedor))
                    .enqueue(retornoSalvar());
        }
    }

    private Callback<Item> retornoSalvar() {
        return new Callback<Item>() {
            @Override
            public void onResponse(@NonNull Call<Item> call, @NonNull Response<Item> resposta) {
                if (!isAdded()) {
                    return;
                }
                botaoSalvar.setEnabled(true);
                if (resposta.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.form_salvo, Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(ItemFormFragment.this).popBackStack();
                } else if (resposta.code() == 409) {
                    Toast.makeText(requireContext(), R.string.form_erro_epc, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), R.string.catalogo_erro_salvar, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Item> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    botaoSalvar.setEnabled(true);
                    Toast.makeText(requireContext(), R.string.catalogo_erro_salvar, Toast.LENGTH_LONG).show();
                }
            }
        };
    }
}
