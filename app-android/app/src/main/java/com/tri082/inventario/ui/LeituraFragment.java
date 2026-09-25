// Tela de leitura RFID: le pelo gatilho ou botao, mantem as leituras ao trocar de aba (limpa so no botao Limpar),
// mostra o nome do item quando ja cadastrado e conta as leituras de cada tag (apoio de teste).

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.EstadoLeitura;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.local.SessaoInventario;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;
import com.tri082.inventario.data.remote.LeituraRequisicao;
import com.tri082.inventario.rfid.LeitorRfid;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeituraFragment extends Fragment implements LeitorRfid.Ouvinte {

    private final LeitorRfid leitor = new LeitorRfid();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable atualizacao = this::atualizarLista;
    private LeiturasAdapter adapter;
    private TextView textoStatus;
    private TextView textoContagem;
    private Button botaoLer;
    private boolean disponivel = false;
    private boolean lendo = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leitura, container, false);
        textoStatus = view.findViewById(R.id.texto_status);
        textoContagem = view.findViewById(R.id.texto_contagem);
        botaoLer = view.findViewById(R.id.botao_ler);
        RecyclerView lista = view.findViewById(R.id.lista_leituras);

        adapter = new LeiturasAdapter(this::aoClicarTag);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);

        textoStatus.setText(R.string.leitura_status_verificando);
        botaoLer.setEnabled(false);
        botaoLer.setOnClickListener(v -> alternar());
        view.findViewById(R.id.botao_limpar).setOnClickListener(v -> limpar());

        atualizarLista();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        leitor.iniciar(requireContext(), this);
        ((MainActivity) requireActivity()).registrarGatilho(this::iniciarLeitura, this::pararSeLendo);
        resolverNomesPendentes();
    }

    @Override
    public void onStop() {
        super.onStop();
        pararSeLendo();
        leitor.liberar();
        ((MainActivity) requireActivity()).removerGatilho();
        ui.removeCallbacks(atualizacao);
    }

    @Override
    public void aoMudarStatus(boolean disponivel) {
        this.disponivel = disponivel;
        textoStatus.setText(disponivel
                ? R.string.leitura_status_pronto
                : R.string.leitura_status_indisponivel);
        botaoLer.setEnabled(disponivel);
    }

    @Override
    public void aoLerTag(String epc, int sinal) {
        boolean nova = !EstadoLeitura.contem(epc);
        EstadoLeitura.registrar(epc);
        agendarAtualizacao();
        if (nova) {
            resolverDados(epc);
            if (SessaoInventario.ativa()) {
                registrarNaSessao(epc);
            }
        }
    }

    private void alternar() {
        if (lendo) {
            pararLeitura();
        } else {
            iniciarLeitura();
        }
    }

    private void iniciarLeitura() {
        if (!disponivel || lendo) {
            return;
        }
        lendo = true;
        leitor.ligarLeitura();
        botaoLer.setText(R.string.leitura_parar);
    }

    private void pararLeitura() {
        lendo = false;
        leitor.desligarLeitura();
        botaoLer.setText(R.string.leitura_iniciar);
    }

    private void pararSeLendo() {
        if (lendo) {
            pararLeitura();
        }
    }

    private void limpar() {
        EstadoLeitura.limpar();
        atualizarLista();
    }

    private void aoClicarTag(EstadoLeitura.Registro registro) {
        if (!Sessao.ehAdmin()) {
            return;
        }
        Bundle argumentos = new Bundle();
        if (registro.itemId != null) {
            argumentos.putInt("itemId", registro.itemId);
            NavHostFragment.findNavController(this).navigate(R.id.itemDetalheFragment, argumentos);
        } else {
            argumentos.putInt("itemId", -1);
            argumentos.putString("epc", registro.epc);
            NavHostFragment.findNavController(this).navigate(R.id.itemFormFragment, argumentos);
        }
    }

    private void registrarNaSessao(String epc) {
        Integer id = SessaoInventario.getAtivo();
        if (id == null) {
            return;
        }
        ClienteApi.criarServico(requireContext())
                .registrarLeitura(id, new LeituraRequisicao(epc, "DT50P"))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> resposta) {
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable erro) {
                    }
                });
    }

    private void resolverNomesPendentes() {
        for (EstadoLeitura.Registro registro : EstadoLeitura.lista()) {
            if (registro.itemId == null) {
                resolverDados(registro.epc);
            }
        }
    }

    private void resolverDados(String epc) {
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.obterItemPorEpc(epc).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(@NonNull Call<Item> call, @NonNull Response<Item> resposta) {
                if (isAdded() && resposta.isSuccessful() && resposta.body() != null) {
                    Item item = resposta.body();
                    EstadoLeitura.definirDados(epc, item.getId(), item.getNome(), item.getTipo(), item.getCor());
                    agendarAtualizacao();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Item> call, @NonNull Throwable erro) {
            }
        });
    }

    private void agendarAtualizacao() {
        ui.removeCallbacks(atualizacao);
        ui.postDelayed(atualizacao, 150);
    }

    private void atualizarLista() {
        adapter.definir(EstadoLeitura.lista());
        textoContagem.setText(getString(R.string.leitura_contagem, EstadoLeitura.totalTags()));
    }
}
