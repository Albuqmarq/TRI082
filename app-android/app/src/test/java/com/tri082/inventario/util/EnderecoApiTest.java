// Testes da logica de endereco da API: validacao de IP e porta e montagem da URL base.

package com.tri082.inventario.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EnderecoApiTest {

    @Test
    public void montaUrlBaseComIpEPorta() {
        assertEquals("http://192.168.0.10:8000/",
                EnderecoApi.montarUrlBase("192.168.0.10", "8000"));
    }

    @Test
    public void montaUrlBaseRemovendoEspacos() {
        assertEquals("http://192.168.0.10:8000/",
                EnderecoApi.montarUrlBase("  192.168.0.10 ", " 8000 "));
    }

    @Test
    public void ipValidoAceitaEnderecoEHostname() {
        assertTrue(EnderecoApi.ipValido("192.168.0.10"));
        assertTrue(EnderecoApi.ipValido("servidor-fabrica"));
    }

    @Test
    public void ipInvalidoRejeitaVazioEComEspaco() {
        assertFalse(EnderecoApi.ipValido(""));
        assertFalse(EnderecoApi.ipValido("   "));
        assertFalse(EnderecoApi.ipValido("meu servidor"));
        assertFalse(EnderecoApi.ipValido(null));
    }

    @Test
    public void portaValidaAceitaFaixaPermitida() {
        assertTrue(EnderecoApi.portaValida("1"));
        assertTrue(EnderecoApi.portaValida("8000"));
        assertTrue(EnderecoApi.portaValida("65535"));
    }

    @Test
    public void portaInvalidaRejeitaForaDaFaixaENaoNumerica() {
        assertFalse(EnderecoApi.portaValida("0"));
        assertFalse(EnderecoApi.portaValida("70000"));
        assertFalse(EnderecoApi.portaValida("abc"));
        assertFalse(EnderecoApi.portaValida(""));
        assertFalse(EnderecoApi.portaValida(null));
    }
}
