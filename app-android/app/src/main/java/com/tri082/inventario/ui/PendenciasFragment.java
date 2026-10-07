// Tela de pendencias de cadastro (somente Administrador): lista as tags lidas sem item no catalogo.
// Cada tag pode ser cadastrada (abre o cadastro com o EPC preenchido) ou ignorada, quando nao e um produto.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendenciasFragment extends Fragment {

    private PendenciasAdapter adapter;
    private TextView textoContagem;
    private View cartao;
    private View textoVazio;

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
        adapter.definir(pendencias);
        boolean vazia = pendencias.isEmpty();
        cartao.setVisibility(vazia ? View.GONE : View.VISIBLE);
        textoContagem.setVisibility(vazia ? View.GONE : View.VISIBLE);
        textoVazio.setVisibility(vazia ? View.VISIBLE : View.GONE);
        textoContagem.setText(getResources().getQuantityString(R.plurals.pend_contagem,
                pendencias.size(), pendencias.size()));
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
