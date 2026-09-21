// Tela de catalogo: lista e busca itens para todos, e oferece cadastro ao Administrador.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogoFragment extends Fragment {

    private EditText campoBusca;
    private TextView textoVazio;
    private ItemAdapter adapter;
    private final List<Item> todos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogo, container, false);
        campoBusca = view.findViewById(R.id.campo_busca);
        textoVazio = view.findViewById(R.id.texto_vazio);
        RecyclerView lista = view.findViewById(R.id.lista_itens);
        Button botaoNovo = view.findViewById(R.id.botao_novo);

        adapter = new ItemAdapter(this::abrirDetalhe);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);

        botaoNovo.setVisibility(Sessao.ehAdmin() ? View.VISIBLE : View.GONE);
        botaoNovo.setOnClickListener(v -> abrirFormulario(-1));

        campoBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregar();
    }

    private void carregar() {
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.listarItens().enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(@NonNull Call<List<Item>> call, @NonNull Response<List<Item>> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    todos.clear();
                    todos.addAll(resposta.body());
                    filtrar();
                } else {
                    Toast.makeText(requireContext(), R.string.catalogo_erro_carregar, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Item>> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.catalogo_erro_carregar, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void filtrar() {
        String busca = campoBusca.getText().toString().trim().toLowerCase();
        List<Item> filtrados = new ArrayList<>();
        for (Item item : todos) {
            if (busca.isEmpty() || item.getNome().toLowerCase().contains(busca)) {
                filtrados.add(item);
            }
        }
        adapter.definir(filtrados);
        textoVazio.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void abrirDetalhe(Item item) {
        Bundle argumentos = new Bundle();
        argumentos.putInt("itemId", item.getId());
        NavHostFragment.findNavController(this).navigate(R.id.itemDetalheFragment, argumentos);
    }

    private void abrirFormulario(int itemId) {
        Bundle argumentos = new Bundle();
        argumentos.putInt("itemId", itemId);
        NavHostFragment.findNavController(this).navigate(R.id.itemFormFragment, argumentos);
    }
}
