// Tela de catalogo: lista e busca os itens, filtra por categoria e, para o Administrador, abre o cadastro.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogoFragment extends Fragment {

    private EditText campoBusca;
    private TextView textoVazio;
    private TextView textoContagem;
    private LinearLayout containerFiltros;
    private ItemAdapter adapter;
    private final List<Item> todos = new ArrayList<>();
    private String categoriaSelecionada;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogo, container, false);
        campoBusca = view.findViewById(R.id.campo_busca);
        textoVazio = view.findViewById(R.id.texto_vazio);
        textoContagem = view.findViewById(R.id.texto_contagem);
        containerFiltros = view.findViewById(R.id.container_filtros);
        RecyclerView lista = view.findViewById(R.id.lista_itens);
        Button botaoNovo = view.findViewById(R.id.botao_novo);

        adapter = new ItemAdapter(this::abrirDetalhe);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        lista.setAdapter(adapter);

        categoriaSelecionada = getString(R.string.catalogo_filtro_todos);
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
                    textoContagem.setText(getString(R.string.catalogo_contagem, todos.size()));
                    montarFiltros();
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

    private void montarFiltros() {
        Set<String> categorias = new LinkedHashSet<>();
        categorias.add(getString(R.string.catalogo_filtro_todos));
        for (Item item : todos) {
            if (item.getCategoria() != null && !item.getCategoria().isEmpty()) {
                categorias.add(item.getCategoria());
            }
        }
        if (!categorias.contains(categoriaSelecionada)) {
            categoriaSelecionada = getString(R.string.catalogo_filtro_todos);
        }
        containerFiltros.removeAllViews();
        for (String categoria : categorias) {
            containerFiltros.addView(criarChip(categoria));
        }
    }

    private TextView criarChip(String categoria) {
        TextView chip = new TextView(requireContext());
        chip.setText(categoria);
        chip.setTextSize(13);
        chip.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
        chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_texto));
        chip.setBackgroundResource(R.drawable.chip_fundo);
        int px = (int) (14 * getResources().getDisplayMetrics().density);
        int py = (int) (7 * getResources().getDisplayMetrics().density);
        chip.setPadding(px, py, px, py);
        chip.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
        chip.setLayoutParams(lp);
        chip.setSelected(categoria.equals(categoriaSelecionada));
        chip.setOnClickListener(v -> {
            categoriaSelecionada = categoria;
            atualizarSelecaoChips();
            filtrar();
        });
        return chip;
    }

    private void atualizarSelecaoChips() {
        for (int i = 0; i < containerFiltros.getChildCount(); i++) {
            TextView chip = (TextView) containerFiltros.getChildAt(i);
            chip.setSelected(chip.getText().toString().equals(categoriaSelecionada));
        }
    }

    private void filtrar() {
        String busca = campoBusca.getText().toString().trim().toLowerCase();
        String todosRotulo = getString(R.string.catalogo_filtro_todos);
        List<Item> filtrados = new ArrayList<>();
        for (Item item : todos) {
            boolean bateCategoria = categoriaSelecionada.equals(todosRotulo)
                    || categoriaSelecionada.equals(item.getCategoria());
            if (bateCategoria && bateBusca(item, busca)) {
                filtrados.add(item);
            }
        }
        adapter.definir(filtrados);
        textoVazio.setVisibility(filtrados.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean bateBusca(Item item, String busca) {
        if (busca.isEmpty()) {
            return true;
        }
        return contem(item.getNome(), busca) || contem(item.getCor(), busca) || contem(item.getEpc(), busca);
    }

    private boolean contem(String valor, String busca) {
        return valor != null && valor.toLowerCase().contains(busca);
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
