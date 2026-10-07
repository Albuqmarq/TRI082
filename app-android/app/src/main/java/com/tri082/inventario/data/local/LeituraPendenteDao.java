// Consultas da fila local de leituras pendentes de envio.

package com.tri082.inventario.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LeituraPendenteDao {

    @Insert
    void inserir(LeituraPendente leitura);

    @Delete
    void remover(LeituraPendente leitura);

    @Query("SELECT * FROM leituras_pendentes ORDER BY id")
    List<LeituraPendente> listar();

    @Query("SELECT COUNT(*) FROM leituras_pendentes")
    int contar();

    @Query("SELECT COUNT(*) FROM leituras_pendentes WHERE sessaoId = :sessaoId")
    int contarDaSessao(int sessaoId);
}
