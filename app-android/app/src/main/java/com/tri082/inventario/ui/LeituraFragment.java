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
import com.tri082.inventario.data.repository.FilaLeituras;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;
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
    private View areaFila;
    private TextView textoFila;
    private final Runnable aoMudarFila = this::atualizarFila;
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
        areaFila = view.findViewById(R.id.area_fila);
        textoFila = view.findViewById(R.id.texto_fila);
        view.findViewById(R.id.botao_sincronizar).setOnClickListener(v -> sincronizar());
        RecyclerView lista = view.findViewById(R.id.lista_leituras);

        adapter = new LeiturasAdapter(this::aoClicarTag, this::aoSegurarTag);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
                requireContext(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));
        lista.setAdapter(adapter);

        textoStatus.setText(R.string.leitura_verificando_curto);
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
        FilaLeituras.adicionarOuvinte(aoMudarFila);
        atualizarFila();
    }

    @Override
    public void onStop() {
        super.onStop();
        pararSeLendo();
        leitor.liberar();
        ((MainActivity) requireActivity()).removerGatilho();
        ui.removeCallbacks(atualizacao);
        FilaLeituras.removerOuvinte(aoMudarFila);
    }

    @Override
    public void aoMudarStatus(boolean disponivel) {
        this.disponivel = disponivel;
        textoStatus.setText(disponivel
                ? R.string.leitura_pausada
                : R.string.leitura_indisponivel_curto);
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
        textoStatus.setText(R.string.leitura_lendo);
    }

    private void pararLeitura() {
        lendo = false;
        leitor.desligarLeitura();
        botaoLer.setText(R.string.leitura_iniciar);
        textoStatus.setText(R.string.leitura_pausada);
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

    private void aoSegurarTag(EstadoLeitura.Registro registro) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.leitura_remover_titulo)
                .setMessage(getString(R.string.leitura_remover_msg, registro.epc))
                .setPositiveButton(R.string.leitura_remover_confirmar, (d, w) -> {
                    EstadoLeitura.remover(registro.epc);
                    atualizarLista();
                })
                .setNegativeButton(R.string.detalhe_cancelar, null)
                .show();
    }

    private void registrarNaSessao(String epc) {
        Integer id = SessaoInventario.getAtivo();
        if (id != null) {
            FilaLeituras.enviar(requireContext(), id, epc, "DT50P");
        }
    }

    private void atualizarFila() {
        FilaLeituras.contar(requireContext(), total -> {
            if (!isAdded()) {
                return;
            }
            areaFila.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
            textoFila.setText(getResources().getQuantityString(R.plurals.fila_pendentes, total, total));
        });
    }

    private void sincronizar() {
        FilaLeituras.sincronizar(requireContext(), resultado -> {
            if (isAdded()) {
                AvisoSincronizacao.mostrar(requireContext(), resultado);
            }
        });
    }

    private void resolverNomesPendentes() {
        for (EstadoLeitura.Registro registro : EstadoLeitura.lista()) {
            if (!registro.resolvida) {
                resolverDados(registro.epc);
            }
        }
    }

    private void resolverDados(String epc) {
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.obterItemPorEpc(epc).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(@NonNull Call<Item> call, @NonNull Response<Item> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    Item item = resposta.body();
                    EstadoLeitura.definirDados(epc, item.getId(), item.getNome(), item.getTipo(), item.getCor());
                } else {
                    EstadoLeitura.definirNaoCadastrada(epc);
                    avisarPendencia();
                }
                agendarAtualizacao();
            }

            @Override
            public void onFailure(@NonNull Call<Item> call, @NonNull Throwable erro) {
            }
        });
    }

    private void avisarPendencia() {
        if (isAdded() && !Sessao.ehAdmin() && SessaoInventario.ativa()) {
            Toast.makeText(requireContext(), R.string.leitura_pendencia_registrada, Toast.LENGTH_SHORT).show();
        }
    }

    private void agendarAtualizacao() {
        ui.removeCallbacks(atualizacao);
        ui.postDelayed(atualizacao, 150);
    }

    private void atualizarLista() {
        adapter.definir(EstadoLeitura.lista());
        textoContagem.setText(String.valueOf(EstadoLeitura.totalTags()));
    }
}
