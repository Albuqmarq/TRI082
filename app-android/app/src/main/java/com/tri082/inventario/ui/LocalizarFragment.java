// Tela de localizacao de item por sinal RFID: filtra pela tag escolhida e mostra o sinal em tempo real,
// como uma bussola de proximidade. A leitura para ao sair da tela.
// Avisa na tela quando a internet cai e quando o sinal da tag some por alguns segundos (com vibracao curta).

package com.tri082.inventario.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.Item;
import com.tri082.inventario.rfid.LeitorRfid;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocalizarFragment extends Fragment implements LeitorRfid.Ouvinte {

    private static final long TEMPO_SEM_SINAL = 1200;
    private static final long TEMPO_SINAL_PERDIDO = 3000;

    private final LeitorRfid leitor = new LeitorRfid();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable semSinal = this::marcarProcurando;
    private final Runnable sinalPerdido = this::marcarSinalPerdido;

    private EditText campoEpc;
    private Button botaoLocalizar;
    private TextView textoStatus;
    private TextView textoNomeAlvo;
    private TextView textoSinal;
    private TextView textoProximidade;
    private TextView textoMelhor;
    private TextView textoProcurando;
    private View cardSinal;
    private MedidorSinal medidor;
    private View avisoInternet;
    private View avisoSinal;
    private ConnectivityManager conectividade;
    private final ConnectivityManager.NetworkCallback aoMudarRede = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network rede) {
            ui.post(LocalizarFragment.this::atualizarInternet);
        }

        @Override
        public void onLost(@NonNull Network rede) {
            ui.post(LocalizarFragment.this::atualizarInternet);
        }
    };

    private final List<Item> itensCatalogo = new ArrayList<>();
    private String alvo;
    private boolean disponivel = false;
    private boolean localizando = false;
    private int maisPerto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_localizar, container, false);
        campoEpc = view.findViewById(R.id.campo_epc);
        botaoLocalizar = view.findViewById(R.id.botao_localizar);
        textoStatus = view.findViewById(R.id.texto_status);
        textoNomeAlvo = view.findViewById(R.id.texto_nome_alvo);
        textoSinal = view.findViewById(R.id.texto_sinal);
        textoProximidade = view.findViewById(R.id.texto_proximidade);
        textoMelhor = view.findViewById(R.id.texto_melhor);
        textoProcurando = view.findViewById(R.id.texto_procurando);
        cardSinal = view.findViewById(R.id.card_sinal);
        medidor = view.findViewById(R.id.medidor);
        avisoInternet = view.findViewById(R.id.aviso_internet);
        avisoSinal = view.findViewById(R.id.aviso_sinal);
        conectividade = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        textoStatus.setText(R.string.leitura_status_verificando);
        botaoLocalizar.setOnClickListener(v -> alternar());
        view.findViewById(R.id.botao_escolher).setOnClickListener(v -> escolherDoCatalogo());

        if (getArguments() != null) {
            String epcArg = getArguments().getString("epc");
            String nomeArg = getArguments().getString("nome");
            if (epcArg != null && !epcArg.isEmpty()) {
                campoEpc.setText(epcArg);
            }
            if (nomeArg != null && !nomeArg.isEmpty()) {
                textoNomeAlvo.setText(nomeArg);
            }
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        leitor.iniciar(requireContext(), this);
        ((MainActivity) requireActivity()).registrarGatilho(this::iniciarLocalizacao, this::pararLocalizacao);
        conectividade.registerDefaultNetworkCallback(aoMudarRede);
        atualizarInternet();
    }

    @Override
    public void onStop() {
        super.onStop();
        pararLocalizacao();
        leitor.liberar();
        ((MainActivity) requireActivity()).removerGatilho();
        ui.removeCallbacks(semSinal);
        ui.removeCallbacks(sinalPerdido);
        conectividade.unregisterNetworkCallback(aoMudarRede);
    }

    @Override
    public void aoMudarStatus(boolean disponivel) {
        this.disponivel = disponivel;
        textoStatus.setText(disponivel
                ? R.string.localizar_status_pronto
                : R.string.leitura_status_indisponivel);
        botaoLocalizar.setEnabled(disponivel);
    }

    @Override
    public void aoLerTag(String epc, int sinal) {
        if (localizando && alvo != null && alvo.equalsIgnoreCase(epc)) {
            atualizarSinal(sinal);
        }
    }

    private void alternar() {
        if (localizando) {
            pararLocalizacao();
        } else {
            iniciarLocalizacao();
        }
    }

    private void iniciarLocalizacao() {
        if (!disponivel || localizando) {
            return;
        }
        String epc = campoEpc.getText().toString().trim();
        if (epc.isEmpty()) {
            Toast.makeText(requireContext(), R.string.localizar_informe_epc, Toast.LENGTH_LONG).show();
            return;
        }
        alvo = epc;
        localizando = true;
        maisPerto = 0;
        cardSinal.setVisibility(View.VISIBLE);
        textoSinal.setText("--");
        pintar(textoSinal, R.color.texto_secundario);
        textoProximidade.setText("");
        textoMelhor.setText("");
        medidor.definir(0, ContextCompat.getColor(requireContext(), R.color.linha));
        marcarProcurando();
        leitor.ligarLeitura();
        botaoLocalizar.setText(R.string.localizar_parar);
    }

    private void pararLocalizacao() {
        if (!localizando) {
            return;
        }
        localizando = false;
        leitor.desligarLeitura();
        ui.removeCallbacks(semSinal);
        ui.removeCallbacks(sinalPerdido);
        avisoSinal.setVisibility(View.GONE);
        botaoLocalizar.setText(R.string.localizar_iniciar);
    }

    private void atualizarSinal(int sinal) {
        // Validado no coletor: o valor entregue pelo leitor cresce quando o coletor se aproxima da tag.
        int intensidade = Math.abs(sinal);
        if (intensidade > maisPerto) {
            maisPerto = intensidade;
        }
        int perto = proximidade(intensidade);
        textoSinal.setText(getString(R.string.localizar_sinal, intensidade));
        textoMelhor.setText(getString(R.string.localizar_melhor, maisPerto));
        textoProcurando.setVisibility(View.GONE);
        int cor = aplicarRotulo(perto);
        medidor.definir(perto, ContextCompat.getColor(requireContext(), cor));
        avisoSinal.setVisibility(View.GONE);
        ui.removeCallbacks(semSinal);
        ui.postDelayed(semSinal, TEMPO_SEM_SINAL);
        ui.removeCallbacks(sinalPerdido);
        ui.postDelayed(sinalPerdido, TEMPO_SINAL_PERDIDO);
    }

    // Escala fixa do arco: 20 (bem longe) fica quase vazio, 90 (encostado na tag) fica cheio.
    private int proximidade(int intensidade) {
        int valor = Math.round((intensidade - 20) * 100f / 70f);
        return Math.max(0, Math.min(100, valor));
    }

    private int aplicarRotulo(int perto) {
        int texto;
        int cor;
        if (perto >= 75) {
            texto = R.string.localizar_muito_perto;
            cor = R.color.verde;
        } else if (perto >= 45) {
            texto = R.string.localizar_perto;
            cor = R.color.verde_escuro;
        } else if (perto >= 20) {
            texto = R.string.localizar_medio;
            cor = R.color.azul;
        } else {
            texto = R.string.localizar_fraco;
            cor = R.color.texto_secundario;
        }
        textoProximidade.setText(texto);
        pintar(textoProximidade, cor);
        pintar(textoSinal, cor);
        return cor;
    }

    private void pintar(TextView alvo, @ColorRes int cor) {
        alvo.setTextColor(ContextCompat.getColor(requireContext(), cor));
    }

    private void marcarProcurando() {
        if (localizando) {
            textoProcurando.setVisibility(View.VISIBLE);
        }
    }

    // So avisa quando a tag ja tinha sido encontrada nessa busca e o sinal sumiu.
    private void marcarSinalPerdido() {
        if (!localizando || maisPerto == 0 || !isAdded()) {
            return;
        }
        avisoSinal.setVisibility(View.VISIBLE);
        textoSinal.setText("--");
        textoProximidade.setText(R.string.localizar_sem_sinal);
        pintar(textoSinal, R.color.texto_secundario);
        pintar(textoProximidade, R.color.erro);
        medidor.definir(0, ContextCompat.getColor(requireContext(), R.color.linha));
        vibrar();
    }

    private void vibrar() {
        Vibrator vibrador = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrador == null || !vibrador.hasVibrator()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrador.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrador.vibrate(300);
        }
    }

    private void atualizarInternet() {
        if (!isAdded()) {
            return;
        }
        Network rede = conectividade.getActiveNetwork();
        NetworkCapabilities capacidades = rede == null ? null : conectividade.getNetworkCapabilities(rede);
        boolean conectado = capacidades != null
                && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        avisoInternet.setVisibility(conectado ? View.GONE : View.VISIBLE);
    }

    private void escolherDoCatalogo() {
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.listarItens().enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(@NonNull Call<List<Item>> call, @NonNull Response<List<Item>> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null && !resposta.body().isEmpty()) {
                    itensCatalogo.clear();
                    itensCatalogo.addAll(resposta.body());
                    mostrarEscolha();
                } else {
                    Toast.makeText(requireContext(), R.string.localizar_sem_itens, Toast.LENGTH_LONG).show();
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

    private void mostrarEscolha() {
        BottomSheetDialog folha = new BottomSheetDialog(requireContext());
        View conteudo = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_escolher_item, null);
        folha.setContentView(conteudo);

        ItemAdapter adapterEscolha = new ItemAdapter(item -> {
            campoEpc.setText(item.getEpc());
            textoNomeAlvo.setText(item.getNome());
            folha.dismiss();
        });
        RecyclerView lista = conteudo.findViewById(R.id.lista_escolha);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        lista.setAdapter(adapterEscolha);
        adapterEscolha.definir(itensCatalogo);

        EditText busca = conteudo.findViewById(R.id.campo_busca_escolha);
        busca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterEscolha.definir(filtrarCatalogo(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        conteudo.findViewById(R.id.botao_fechar).setOnClickListener(v -> folha.dismiss());

        folha.getBehavior().setSkipCollapsed(true);
        folha.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        folha.show();
    }

    private List<Item> filtrarCatalogo(String texto) {
        String busca = texto.trim().toLowerCase();
        if (busca.isEmpty()) {
            return itensCatalogo;
        }
        List<Item> filtrados = new ArrayList<>();
        for (Item item : itensCatalogo) {
            if (contem(item.getNome(), busca) || contem(item.getCor(), busca) || contem(item.getEpc(), busca)) {
                filtrados.add(item);
            }
        }
        return filtrados;
    }

    private boolean contem(String valor, String busca) {
        return valor != null && valor.toLowerCase().contains(busca);
    }
}
