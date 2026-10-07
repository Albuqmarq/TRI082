// Converte as datas da API (UTC, formato ISO 8601) para data e hora local, ex.: 07/10/2026 às 09:48.

package com.tri082.inventario.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Datas {

    private Datas() {
    }

    public static String paraTexto(String iso) {
        if (iso == null || iso.length() < 19) {
            return "-";
        }
        try {
            SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
            entrada.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date data = entrada.parse(iso.substring(0, 19));
            return new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", new Locale("pt", "BR")).format(data);
        } catch (ParseException invalida) {
            return iso.substring(0, 10);
        }
    }

}
