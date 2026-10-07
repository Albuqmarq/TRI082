// Banco local do aparelho (Room). Hoje guarda apenas a fila de leituras pendentes de envio.

package com.tri082.inventario.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LeituraPendente.class}, version = 1, exportSchema = false)
public abstract class BancoLocal extends RoomDatabase {

    private static volatile BancoLocal instancia;

    public abstract LeituraPendenteDao leiturasPendentes();

    public static BancoLocal obter(Context context) {
        if (instancia == null) {
            synchronized (BancoLocal.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(context.getApplicationContext(),
                            BancoLocal.class, "tri082_local.db").build();
                }
            }
        }
        return instancia;
    }
}
