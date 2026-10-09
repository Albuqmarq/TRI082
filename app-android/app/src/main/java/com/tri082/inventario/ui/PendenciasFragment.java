// Tela de pendencias de cadastro (somente Administrador): lista as tags lidas sem item no catalogo.
// Cada tag pode ser cadastrada (abre o cadastro com o EPC preenchido) ou ignorada, quando nao e um produto.
// O campo de busca filtra a lista pelo EPC enquanto o usuario digita.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Pendencia;
import com.tri082.inventario.util.Datas;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendenciasFragment extends Fragment {

    private PendenciasAdapter adapter;
    private TextView textoContagem;
    private View cartao;
    private TextView textoVazio;
    private EditText campoBusca;
    private final List<Pendencia> todas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pendencias, container, false);

        if (!Sessao.ehAdmin()) {
            Toast.makeText(requireContext(), R.string.acesso_restrito_admin, Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).popBackStack();
            return view;
        }

        textoContagem = view.findViewById(R.id.pend_contagem);
        cartao = view.findViewById(R.id.pend_cartao);
        textoVazio = view.findViewById(R.id.pend_vazio);
        campoBusca = view.findViewById(R.id.pend_busca);
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

        adapter = new PendenciasAdapter(this::abrirOpcoes);
        RecyclerView lista = view.findViewById(R.id.pend_lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        lista.setAdapter(adapter);

        view.findViewById(R.id.pend_voltar).setOnClickListener(
                v -> NavHostFragment.findNavController(this).popBackStack());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            carregar();
        }
    }

    private void carregar() {
        ClienteApi.criarServico(requireContext()).listarPendencias().enqueue(new Callback<List<Pendencia>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pendencia>> call, @NonNull Response<List<Pendencia>> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    mostrar(resposta.body());
                } else {
                    Toast.makeText(requireContext(), R.string.pend_erro, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Pendencia>> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.pend_erro, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void mostrar(List<Pendencia> pendencias) {
        todas.clear();
        todas.addAll(pendencias);
        filtrar();
    }

    private void filtrar() {
        String termo = campoBusca.getText().toString().replace(" ", "").toUpperCase(Locale.ROOT);
        List<Pendencia> filtradas = new ArrayList<>();
        for (Pendencia pendencia : todas) {
            if (pendencia.epc != null && pendencia.epc.toUpperCase(Locale.ROOT).contains(termo)) {
                filtradas.add(pendencia);
            }
        }
        adapter.definir(filtradas);

        boolean semPendencias = todas.isEmpty();
        boolean semResultado = filtradas.isEmpty();
        campoBusca.setVisibility(semPendencias ? View.GONE : View.VISIBLE);
        textoContagem.setVisibility(semPendencias ? View.GONE : View.VISIBLE);
        cartao.setVisibility(semResultado ? View.GONE : View.VISIBLE);
        textoVazio.setVisibility(semResultado ? View.VISIBLE : View.GONE);
        textoVazio.setText(semPendencias ? R.string.pend_vazio : R.string.pend_busca_vazia);
        textoContagem.setText(getResources().getQuantityString(R.plurals.pend_contagem,
                filtradas.size(), filtradas.size()));
    }

    private void abrirOpcoes(Pendencia pendencia) {
        BottomSheetDialog folha = new BottomSheetDialog(requireContext());
        View conteudo = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_pendencia, null);
        folha.setContentView(conteudo);
        ((TextView) conteudo.findViewById(R.id.folha_epc)).setText(pendencia.epc);
        ((TextView) conteudo.findViewById(R.id.folha_detalhe)).setText(getResources().getQuantityString(
                R.plurals.pend_leituras, pendencia.ocorrencias, pendencia.ocorrencias,
                Datas.paraTexto(pendencia.ultimaLeitura)));
        conteudo.findViewById(R.id.folha_cadastrar).setOnClickListener(v -> {
            folha.dismiss();
            cadastrar(pendencia);
        });
        conteudo.findViewById(R.id.folha_ignorar).setOnClickListener(v -> {
            folha.dismiss();
            ignorar(pendencia);
        });
        folha.show();
    }

    private void ignorar(Pendencia pendencia) {
        ClienteApi.criarServico(requireContext()).ignorarTag(pendencia.epc).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.pend_ignorada, Toast.LENGTH_SHORT).show();
                    carregar();
                } else {
                    Toast.makeText(requireContext(), R.string.pend_erro_ignorar, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.pend_erro_ignorar, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cadastrar(Pendencia pendencia) {
        Bundle argumentos = new Bundle();
        argumentos.putInt("itemId", -1);
        argumentos.putString("epc", pendencia.epc);
        NavHostFragment.findNavController(this).navigate(R.id.itemFormFragment, argumentos);
    }
}
