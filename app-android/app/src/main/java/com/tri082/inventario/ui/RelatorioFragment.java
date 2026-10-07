// Relatorio de um inventario finalizado: datas, resumo e as listas de ausentes, nao identificados e encontrados.
// Abre ao finalizar uma contagem ou ao tocar numa sessao do historico, e exporta o relatorio em PDF ou CSV.

package com.tri082.inventario.ui;

import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.ResultadoInventario;
import com.tri082.inventario.util.Cores;
import com.tri082.inventario.util.Datas;
import com.tri082.inventario.util.ExportadorRelatorio;

import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RelatorioFragment extends Fragment {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private View raiz;
    private View botaoExportar;
    private ResultadoInventario resultado;

    private final ActivityResultLauncher<String> salvarPdf = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"), uri -> gravar(uri, true));
    private final ActivityResultLauncher<String> salvarCsv = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"), uri -> gravar(uri, false));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        raiz = inflater.inflate(R.layout.fragment_relatorio, container, false);
        botaoExportar = raiz.findViewById(R.id.rel_exportar);
        botaoExportar.setOnClickListener(v -> escolherFormato());
        raiz.findViewById(R.id.rel_voltar).setOnClickListener(
                v -> NavHostFragment.findNavController(this).popBackStack());

        int sessaoId = getArguments() != null ? getArguments().getInt("sessaoId", -1) : -1;
        carregar(sessaoId);
        return raiz;
    }

    private void carregar(int sessaoId) {
        ClienteApi.criarServico(requireContext()).obterResultado(sessaoId).enqueue(new Callback<ResultadoInventario>() {
            @Override
            public void onResponse(@NonNull Call<ResultadoInventario> call, @NonNull Response<ResultadoInventario> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    mostrar(resposta.body());
                } else {
                    Toast.makeText(requireContext(), R.string.rel_erro, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResultadoInventario> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.rel_erro, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void mostrar(ResultadoInventario r) {
        resultado = r;
        botaoExportar.setEnabled(true);
        ((TextView) raiz.findViewById(R.id.rel_titulo)).setText(getString(R.string.rel_titulo_numero, r.sessaoId));
        ((TextView) raiz.findViewById(R.id.rel_periodo)).setText(getString(R.string.rel_periodo,
                Datas.paraTexto(r.dataInicio), Datas.paraTexto(r.dataFim)));
        texto(R.id.valor_esperados, r.resumo.totalEsperado);
        texto(R.id.valor_encontrados, r.resumo.totalEncontrados);
        texto(R.id.valor_ausentes, r.resumo.totalAusentes);
        texto(R.id.valor_naoid, r.resumo.totalNaoIdentificados);
        preencherItens(R.id.lista_ausentes, R.id.contador_ausentes, r.esperadosAusentes);
        preencherItens(R.id.lista_encontrados, R.id.contador_encontrados, r.esperadosEncontrados);
        preencherNaoIdentificados(r.naoIdentificados);
    }

    private void texto(int id, int valor) {
        ((TextView) raiz.findViewById(id)).setText(String.valueOf(valor));
    }

    private void preencherItens(int idLista, int idContador, List<ResultadoInventario.ItemResumo> itens) {
        LinearLayout lista = raiz.findViewById(idLista);
        lista.removeAllViews();
        ((TextView) raiz.findViewById(idContador)).setText(String.valueOf(itens == null ? 0 : itens.size()));
        if (itens == null || itens.isEmpty()) {
            adicionarVazio(lista);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ResultadoInventario.ItemResumo item : itens) {
            View linha = inflater.inflate(R.layout.item_catalogo, lista, false);
            ((TextView) linha.findViewById(R.id.item_nome)).setText(item.nome);
            boolean temCor = item.cor != null && !item.cor.isEmpty();
            String tipo = item.tipo != null ? item.tipo : "";
            ((TextView) linha.findViewById(R.id.item_detalhe)).setText(temCor ? item.cor + "  ·  " + tipo : tipo);
            linha.findViewById(R.id.item_categoria).setVisibility(View.GONE);
            View swatch = linha.findViewById(R.id.item_swatch);
            Integer valorCor = Cores.paraCor(item.cor);
            if (valorCor != null) {
                GradientDrawable fundo = (GradientDrawable) ContextCompat
                        .getDrawable(requireContext(), R.drawable.swatch_cor).mutate();
                fundo.setColor(valorCor);
                swatch.setBackground(fundo);
            } else {
                swatch.setVisibility(View.GONE);
            }
            adicionarLinha(lista, linha);
        }
    }

    private void preencherNaoIdentificados(List<ResultadoInventario.LeituraResumo> leituras) {
        LinearLayout lista = raiz.findViewById(R.id.lista_naoid);
        lista.removeAllViews();
        ((TextView) raiz.findViewById(R.id.contador_naoid)).setText(String.valueOf(leituras == null ? 0 : leituras.size()));
        if (leituras == null || leituras.isEmpty()) {
            adicionarVazio(lista);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ResultadoInventario.LeituraResumo leitura : leituras) {
            View linha = inflater.inflate(R.layout.item_leitura, lista, false);
            TextView nome = linha.findViewById(R.id.item_nome);
            nome.setText(R.string.leitura_tag_sem_cadastro);
            nome.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario));
            ((TextView) linha.findViewById(R.id.item_epc)).setText(leitura.epc);
            linha.findViewById(R.id.item_status).setVisibility(View.GONE);
            linha.setBackground(null);
            adicionarLinha(lista, linha);
        }
    }

    private void adicionarLinha(LinearLayout lista, View linha) {
        if (lista.getChildCount() > 0) {
            View divisoria = new View(requireContext());
            divisoria.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    Math.max(1, (int) getResources().getDisplayMetrics().density)));
            divisoria.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.linha));
            lista.addView(divisoria);
        }
        lista.addView(linha);
    }

    private void adicionarVazio(LinearLayout lista) {
        TextView vazio = new TextView(requireContext());
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        vazio.setPadding(pad, pad, pad, pad);
        vazio.setText(R.string.inv_lista_vazia);
        vazio.setTextSize(13);
        vazio.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_secundario));
        lista.addView(vazio);
    }

    private void escolherFormato() {
        String[] opcoes = {getString(R.string.rel_formato_pdf), getString(R.string.rel_formato_csv)};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.rel_exportar_titulo)
                .setItems(opcoes, (dialogo, qual) -> {
                    if (qual == 0) {
                        salvarPdf.launch(ExportadorRelatorio.nomeArquivo(resultado, "pdf"));
                    } else {
                        salvarCsv.launch(ExportadorRelatorio.nomeArquivo(resultado, "csv"));
                    }
                })
                .show();
    }

    // O arquivo e gerado fora da thread principal para nao travar a tela com relatorios grandes.
    private void gravar(@Nullable Uri destino, boolean pdf) {
        if (destino == null || resultado == null) {
            return;
        }
        android.content.Context contexto = requireContext().getApplicationContext();
        Handler principal = new Handler(Looper.getMainLooper());
        EXECUTOR.execute(() -> {
            boolean ok;
            try (OutputStream saida = contexto.getContentResolver().openOutputStream(destino)) {
                if (saida == null) {
                    throw new java.io.IOException("destino indisponivel");
                }
                if (pdf) {
                    ExportadorRelatorio.gravarPdf(contexto, resultado, saida);
                } else {
                    ExportadorRelatorio.gravarCsv(contexto, resultado, saida);
                }
                ok = true;
            } catch (Exception falha) {
                ok = false;
            }
            boolean sucesso = ok;
            principal.post(() -> Toast.makeText(contexto,
                    sucesso ? R.string.rel_exportado : R.string.rel_erro_exportar, Toast.LENGTH_LONG).show());
        });
    }
}
