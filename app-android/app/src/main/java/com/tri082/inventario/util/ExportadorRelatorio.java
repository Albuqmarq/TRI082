// Gera o relatorio de um inventario em PDF (A4, com cabecalho, resumo e tabelas por categoria) ou em CSV
// no formato que o Excel em portugues abre direto (separador ponto e virgula e UTF-8 com BOM).

package com.tri082.inventario.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.ResultadoInventario;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ExportadorRelatorio {

    private static final int LARGURA = 595;
    private static final int ALTURA = 842;
    private static final int MARGEM = 40;
    private static final int LIMITE = ALTURA - 50;
    private static final int LINHA = 18;
    private static final int NAVY = Color.parseColor("#2B448C");
    private static final int VERDE = Color.parseColor("#2E9E5B");
    private static final int VERMELHO = Color.parseColor("#C62828");
    private static final int TEXTO = Color.parseColor("#1A1C1E");
    private static final int CINZA = Color.parseColor("#6B7178");
    private static final int FUNDO = Color.parseColor("#F4F6F9");
    private static final int LINHA_COR = Color.parseColor("#E6E8EC");
    private static final int[] COLUNAS = {MARGEM, MARGEM + 200, MARGEM + 310, MARGEM + 385};

    private ExportadorRelatorio() {
    }

    public static String nomeArquivo(ResultadoInventario r, String extensao) {
        String data = r.dataFim != null && r.dataFim.length() >= 10 ? r.dataFim.substring(0, 10) : "sem-data";
        return "inventario_" + r.sessaoId + "_" + data + "." + extensao;
    }

    // ---------- CSV ----------

    public static void gravarCsv(Context c, ResultadoInventario r, OutputStream saida) throws IOException {
        StringBuilder csv = new StringBuilder("﻿");
        linha(csv, c.getString(R.string.exp_titulo));
        linha(csv, c.getString(R.string.exp_inventario), "#" + r.sessaoId);
        linha(csv, c.getString(R.string.exp_inicio), Datas.paraTexto(r.dataInicio));
        linha(csv, c.getString(R.string.exp_fim), Datas.paraTexto(r.dataFim));
        csv.append("\r\n");
        linha(csv, c.getString(R.string.inv_esperados_label), String.valueOf(r.resumo.totalEsperado));
        linha(csv, c.getString(R.string.inv_encontrados_label), String.valueOf(r.resumo.totalEncontrados));
        linha(csv, c.getString(R.string.inv_ausentes_label), String.valueOf(r.resumo.totalAusentes));
        linha(csv, c.getString(R.string.inv_naoid_titulo), String.valueOf(r.resumo.totalNaoIdentificados));
        csv.append("\r\n");
        linha(csv, c.getString(R.string.exp_situacao), c.getString(R.string.form_nome), c.getString(R.string.form_categoria),
                c.getString(R.string.form_tipo), c.getString(R.string.form_cor), "EPC");
        for (ResultadoInventario.ItemResumo item : naoNula(r.esperadosAusentes)) {
            linha(csv, c.getString(R.string.exp_ausente), item.nome, item.categoria, item.tipo, item.cor, item.epc);
        }
        for (ResultadoInventario.ItemResumo item : naoNula(r.esperadosEncontrados)) {
            linha(csv, c.getString(R.string.exp_encontrado), item.nome, item.categoria, item.tipo, item.cor, item.epc);
        }
        for (ResultadoInventario.LeituraResumo leitura : naoNula(r.naoIdentificados)) {
            linha(csv, c.getString(R.string.exp_nao_identificado), "", "", "", "", leitura.epc);
        }
        saida.write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void linha(StringBuilder csv, String... campos) {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                csv.append(';');
            }
            String campo = campos[i] == null ? "" : campos[i];
            if (campo.contains(";") || campo.contains("\"") || campo.contains("\n")) {
                campo = "\"" + campo.replace("\"", "\"\"") + "\"";
            }
            csv.append(campo);
        }
        csv.append("\r\n");
    }

    // ---------- PDF ----------

    public static void gravarPdf(Context c, ResultadoInventario r, OutputStream saida) throws IOException {
        Pdf pdf = new Pdf(c);
        pdf.cabecalho(r);
        pdf.resumo(r.resumo);
        pdf.secaoItens(c.getString(R.string.inv_ausentes_label), VERMELHO, naoNula(r.esperadosAusentes));
        pdf.secaoNaoIdentificados(naoNula(r.naoIdentificados));
        pdf.secaoItens(c.getString(R.string.inv_encontrados_label), VERDE, naoNula(r.esperadosEncontrados));
        pdf.fechar(saida);
    }

    private static <T> List<T> naoNula(List<T> lista) {
        return lista == null ? new ArrayList<>() : lista;
    }

    private static final class Pdf {
        private final Context c;
        private final PdfDocument documento = new PdfDocument();
        private final Paint texto = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint forma = new Paint(Paint.ANTI_ALIAS_FLAG);
        private PdfDocument.Page pagina;
        private Canvas tela;
        private int numero;
        private float y;

        Pdf(Context c) {
            this.c = c;
            novaPagina();
        }

        private void novaPagina() {
            if (pagina != null) {
                rodape();
                documento.finishPage(pagina);
            }
            numero++;
            pagina = documento.startPage(new PdfDocument.PageInfo.Builder(LARGURA, ALTURA, numero).create());
            tela = pagina.getCanvas();
            y = MARGEM;
        }

        private void garantirEspaco(float altura) {
            if (y + altura > LIMITE) {
                novaPagina();
            }
        }

        void cabecalho(ResultadoInventario r) {
            forma.setColor(NAVY);
            tela.drawRect(0, 0, LARGURA, 86, forma);
            escrever(c.getString(R.string.exp_titulo), MARGEM, 44, 20, Color.WHITE, true);
            escrever(c.getString(R.string.exp_inventario) + " #" + r.sessaoId, MARGEM, 66, 11, Color.WHITE, false);
            y = 112;
            escrever(c.getString(R.string.exp_inicio) + ": " + Datas.paraTexto(r.dataInicio), MARGEM, y, 10, CINZA, false);
            escrever(c.getString(R.string.exp_fim) + ": " + Datas.paraTexto(r.dataFim), MARGEM + 250, y, 10, CINZA, false);
            y += 16;
            escrever(c.getString(R.string.exp_gerado) + ": " + Datas.paraTexto(Datas.agoraIso()), MARGEM, y, 10, CINZA, false);
            y += 22;
        }

        void resumo(ResultadoInventario.Resumo resumo) {
            String[] rotulos = {c.getString(R.string.inv_esperados_label), c.getString(R.string.inv_encontrados_label),
                    c.getString(R.string.inv_ausentes_label), c.getString(R.string.inv_naoid_titulo)};
            int[] valores = {resumo.totalEsperado, resumo.totalEncontrados, resumo.totalAusentes,
                    resumo.totalNaoIdentificados};
            int[] cores = {TEXTO, VERDE, VERMELHO, NAVY};
            float largura = (LARGURA - 2 * MARGEM - 3 * 10) / 4f;
            for (int i = 0; i < 4; i++) {
                float x = MARGEM + i * (largura + 10);
                forma.setColor(FUNDO);
                tela.drawRoundRect(new RectF(x, y, x + largura, y + 58), 8, 8, forma);
                escrever(String.valueOf(valores[i]), x + 12, y + 28, 20, cores[i], true);
                escrever(rotulos[i], x + 12, y + 46, 9, CINZA, false);
            }
            y += 80;
        }

        void secaoItens(String titulo, int cor, List<ResultadoInventario.ItemResumo> itens) {
            tituloSecao(titulo + " (" + itens.size() + ")", cor);
            if (itens.isEmpty()) {
                vazio();
                return;
            }
            cabecalhoTabela(c.getString(R.string.form_nome), c.getString(R.string.form_tipo),
                    c.getString(R.string.form_cor), "EPC");
            int indice = 0;
            for (ResultadoInventario.ItemResumo item : itens) {
                linhaTabela(indice++, item.nome, item.tipo, item.cor, item.epc);
            }
            y += 14;
        }

        void secaoNaoIdentificados(List<ResultadoInventario.LeituraResumo> leituras) {
            tituloSecao(c.getString(R.string.inv_naoid_titulo) + " (" + leituras.size() + ")", NAVY);
            if (leituras.isEmpty()) {
                vazio();
                return;
            }
            cabecalhoTabela("EPC", "", "", c.getString(R.string.exp_lida_em));
            int indice = 0;
            for (ResultadoInventario.LeituraResumo leitura : leituras) {
                linhaTabela(indice++, leitura.epc, "", "", Datas.paraTexto(leitura.timestamp));
            }
            y += 14;
        }

        private void tituloSecao(String titulo, int cor) {
            garantirEspaco(70);
            y += 12;
            forma.setColor(cor);
            tela.drawRect(MARGEM, y - 11, MARGEM + 4, y + 3, forma);
            escrever(titulo, MARGEM + 10, y, 13, TEXTO, true);
            y += 14;
        }

        private void cabecalhoTabela(String... colunas) {
            garantirEspaco(LINHA * 2);
            forma.setColor(LINHA_COR);
            tela.drawRect(MARGEM, y, LARGURA - MARGEM, y + LINHA, forma);
            for (int i = 0; i < colunas.length; i++) {
                escrever(colunas[i], COLUNAS[i] + 6, y + 12.5f, 8.5f, CINZA, true);
            }
            y += LINHA;
        }

        private void linhaTabela(int indice, String... colunas) {
            if (y + LINHA > LIMITE) {
                novaPagina();
            }
            if (indice % 2 == 1) {
                forma.setColor(FUNDO);
                tela.drawRect(MARGEM, y, LARGURA - MARGEM, y + LINHA, forma);
            }
            for (int i = 0; i < colunas.length; i++) {
                int fim = i + 1 < COLUNAS.length ? COLUNAS[i + 1] : LARGURA - MARGEM;
                texto.setTypeface(i == 3 || (colunas[1].isEmpty() && i == 0) ? Typeface.MONOSPACE : Typeface.DEFAULT);
                texto.setTextSize(8.5f);
                String valor = cortar(colunas[i] == null ? "" : colunas[i], fim - COLUNAS[i] - 10);
                texto.setColor(TEXTO);
                tela.drawText(valor, COLUNAS[i] + 6, y + 12.5f, texto);
            }
            y += LINHA;
        }

        private void vazio() {
            garantirEspaco(LINHA);
            escrever(c.getString(R.string.inv_lista_vazia), MARGEM + 10, y + 10, 9, CINZA, false);
            y += 30;
        }

        private void rodape() {
            escrever(c.getString(R.string.exp_pagina, numero), LARGURA - MARGEM - 50, ALTURA - 24, 8, CINZA, false);
            escrever("TRI082", MARGEM, ALTURA - 24, 8, CINZA, false);
        }

        private void escrever(String valor, float x, float base, float tamanho, int cor, boolean negrito) {
            texto.setTypeface(negrito ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            texto.setTextSize(tamanho);
            texto.setColor(cor);
            tela.drawText(valor, x, base, texto);
        }

        private String cortar(String valor, float largura) {
            if (texto.measureText(valor) <= largura) {
                return valor;
            }
            String resultado = valor;
            while (resultado.length() > 1 && texto.measureText(resultado + "...") > largura) {
                resultado = resultado.substring(0, resultado.length() - 1);
            }
            return resultado + "...";
        }

        void fechar(OutputStream saida) throws IOException {
            rodape();
            documento.finishPage(pagina);
            try {
                documento.writeTo(saida);
            } finally {
                documento.close();
            }
        }
    }
}
