// Descarta leituras repetidas da mesma tag numa mesma varredura. Reiniciar limpa o conjunto ao iniciar uma nova leitura.

package com.tri082.inventario.util;

import java.util.HashSet;
import java.util.Set;

public class Deduplicador {

    private final Set<String> vistos = new HashSet<>();

    public void reiniciar() {
        vistos.clear();
    }

    public boolean ehNova(String epc) {
        return vistos.add(epc);
    }
}
