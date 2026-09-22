// Testes da deduplicacao de leituras dentro de uma mesma varredura.

package com.tri082.inventario.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DeduplicadorTest {

    @Test
    public void primeiraLeituraEhNova() {
        Deduplicador d = new Deduplicador();
        assertTrue(d.ehNova("EPC-A"));
    }

    @Test
    public void repetidaNaMesmaVarreduraNaoEhNova() {
        Deduplicador d = new Deduplicador();
        d.ehNova("EPC-A");
        assertFalse(d.ehNova("EPC-A"));
    }

    @Test
    public void tagsDiferentesSaoNovas() {
        Deduplicador d = new Deduplicador();
        assertTrue(d.ehNova("EPC-A"));
        assertTrue(d.ehNova("EPC-B"));
    }

    @Test
    public void reiniciarLiberaAsTags() {
        Deduplicador d = new Deduplicador();
        d.ehNova("EPC-A");
        d.reiniciar();
        assertTrue(d.ehNova("EPC-A"));
    }
}
