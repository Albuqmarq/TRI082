// Tela de detalhe de um item. Mostra as especificacoes e, para o Administrador, permite editar e excluir.

package com.tri082.inventario.ui;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;
import com.tri082.inventario.util.Cores;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetalheFragment extends Fragment {

    private int itemId;
    private View raiz;
    private Item itemAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        raiz = inflater.inflate(R.layout.fragment_item_detalhe, container, false);
        itemId = getArguments() != null ? getArguments().getInt("itemId", -1) : -1;

        boolean admin = Sessao.ehAdmin();
        raiz.findViewById(R.id.botao_editar).setVisibility(admin ? View.VISIBLE : View.GONE);
        raiz.findViewById(R.id.botao_excluir).setVisibility(admin ? View.VISIBLE : View.GONE);

        raiz.findViewById(R.id.det_voltar).setOnClickListener(
                v -> NavHostFragment.findNavController(this).popBackStack());
        raiz.findViewById(R.id.botao_editar).setOnClickListener(v -> editar());
        raiz.findViewById(R.id.botao_excluir).setOnClickListener(v -> confirmarExclusao());
        raiz.findViewById(R.id.botao_localizar).setOnClickListener(v -> localizar());
        return raiz;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregar();
    }

    private void carregar() {
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.obterItem(itemId).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(@NonNull Call<Item> call, @NonNull Response<Item> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    preencher(resposta.body());
                } else {
                    Toast.makeText(requireContext(), R.string.catalogo_erro_carregar, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Item> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.catalogo_erro_carregar, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void preencher(Item item) {
        itemAtual = item;
        texto(R.id.det_nome, item.getNome());
        texto(R.id.det_epc, item.getEpc());
        texto(R.id.det_categoria, item.getCategoria());
        texto(R.id.det_tipo, item.getTipo());
        texto(R.id.det_fornecedor, ouVazio(item.getFornecedor()));

        String cor = item.getCor();
        View swatch = raiz.findViewById(R.id.det_swatch);
        texto(R.id.det_cor, cor != null && !cor.isEmpty() ? cor : "-");
        Integer valorCor = Cores.paraCor(cor);
        if (valorCor != null) {
            swatch.setVisibility(View.VISIBLE);
            GradientDrawable fundo = (GradientDrawable) ContextCompat
                    .getDrawable(requireContext(), R.drawable.swatch_cor).mutate();
            fundo.setColor(valorCor);
            swatch.setBackground(fundo);
        } else {
            swatch.setVisibility(View.GONE);
        }
    }

    private String ouVazio(String valor) {
        return valor == null || valor.isEmpty() ? "-" : valor;
    }

    private void texto(int id, String valor) {
        ((TextView) raiz.findViewById(id)).setText(valor);
    }

    private void editar() {
        Bundle argumentos = new Bundle();
        argumentos.putInt("itemId", itemId);
        NavHostFragment.findNavController(this).navigate(R.id.itemFormFragment, argumentos);
    }

    private void localizar() {
        if (itemAtual == null) {
            return;
        }
        Bundle argumentos = new Bundle();
        argumentos.putString("epc", itemAtual.getEpc());
        argumentos.putString("nome", itemAtual.getNome());
        NavHostFragment.findNavController(this).navigate(R.id.localizarFragment, argumentos);
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.detalhe_confirmar_exclusao)
                .setPositiveButton(R.string.detalhe_excluir, (dialogo, qual) -> excluir())
                .setNegativeButton(R.string.detalhe_cancelar, null)
                .show();
    }

    private void excluir() {
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.excluirItem(itemId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.detalhe_excluido, Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(ItemDetalheFragment.this).popBackStack();
                } else {
                    Toast.makeText(requireContext(), R.string.catalogo_erro_salvar, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.catalogo_erro_salvar, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
