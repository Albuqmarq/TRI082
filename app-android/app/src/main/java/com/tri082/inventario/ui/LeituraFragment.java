// Tela de leitura RFID: inicia e para a leitura pelo botao ou pelo gatilho do coletor, listando as tags lidas ja deduplicadas.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.rfid.LeitorRfid;

public class LeituraFragment extends Fragment implements LeitorRfid.Ouvinte {

    private final LeitorRfid leitor = new LeitorRfid();
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

        adapter = new LeiturasAdapter();
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);

        textoStatus.setText(R.string.leitura_status_verificando);
        botaoLer.setEnabled(false);
        botaoLer.setOnClickListener(v -> alternar());
        atualizarContagem();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        leitor.iniciar(requireContext(), this);
        ((MainActivity) requireActivity()).registrarGatilho(this::gatilhoApertar, this::gatilhoSoltar);
    }

    @Override
    public void onStop() {
        super.onStop();
        pararSeLendo();
        leitor.liberar();
        ((MainActivity) requireActivity()).removerGatilho();
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
        adapter.adicionar(epc, sinal);
        atualizarContagem();
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
        adapter.limpar();
        atualizarContagem();
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

    private void gatilhoApertar() {
        iniciarLeitura();
    }

    private void gatilhoSoltar() {
        pararSeLendo();
    }

    private void atualizarContagem() {
        textoContagem.setText(getString(R.string.leitura_contagem, adapter.total()));
    }
}
