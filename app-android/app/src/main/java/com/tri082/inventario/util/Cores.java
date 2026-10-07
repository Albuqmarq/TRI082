// Converte o nome da cor de um item (texto livre) em uma cor para o circulo da lista.
// Aceita nomes comuns ou um codigo hex (#RRGGBB). Cor desconhecida devolve null e o circulo nao aparece.

package com.tri082.inventario.util;

import android.graphics.Color;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Cores {

    private static final Map<String, String> NOMES = new HashMap<>();

    static {
        NOMES.put("preto", "#1A1C1E");
        NOMES.put("branco", "#FFFFFF");
        NOMES.put("azul", "#2B448C");
        NOMES.put("azul marinho", "#1F2A55");
        NOMES.put("azul claro", "#85B7EB");
        NOMES.put("vermelho", "#C62828");
        NOMES.put("vinho", "#6E1E2B");
        NOMES.put("verde", "#2E9E5B");
        NOMES.put("verde claro", "#97C459");
        NOMES.put("verde escuro", "#27500A");
        NOMES.put("amarelo", "#F2B705");
        NOMES.put("laranja", "#D85A30");
        NOMES.put("roxo", "#7F77DD");
        NOMES.put("lilas", "#AFA9EC");
        NOMES.put("rosa", "#D4537E");
        NOMES.put("cinza", "#9AA0A6");
        NOMES.put("marrom", "#7A4B2B");
        NOMES.put("bege", "#D8C9A3");
        NOMES.put("creme", "#F2E8CF");
        NOMES.put("dourado", "#C9A227");
        NOMES.put("prata", "#C0C4C8");
        NOMES.put("turquesa", "#1D9E9E");
    }

    private Cores() {
    }

    public static Integer paraCor(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        String texto = nome.trim();
        if (texto.startsWith("#")) {
            try {
                return Color.parseColor(texto);
            } catch (IllegalArgumentException invalido) {
                return null;
            }
        }
        String chave = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String hex = NOMES.get(chave);
        return hex == null ? null : Color.parseColor(hex);
    }
}
