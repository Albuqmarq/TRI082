// Leitura que nao conseguiu ser enviada a API (rede fora ou servidor indisponivel) e aguarda o reenvio manual.

package com.tri082.inventario.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "leituras_pendentes")
public class LeituraPendente {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public int sessaoId;

    @NonNull
    public String epc = "";

    public String dispositivo;

    public String lidaEm;
}
