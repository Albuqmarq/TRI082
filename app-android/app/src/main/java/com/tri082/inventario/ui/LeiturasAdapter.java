// Adaptador da lista de tags lidas: mostra o nome do item (quando conhecido) ou o EPC, e a contagem de leituras.

package com.tri082.inventario.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.EstadoLeitura;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LeiturasAdapter extends RecyclerView.Adapter<LeiturasAdapter.Segurador> {

    private final List<EstadoLeitura.Registro> itens = new ArrayList<>();
    private final Consumer<EstadoLeitura.Registro> aoClicar;

    public LeiturasAdapter(Consumer<EstadoLeitura.Registro> aoClicar) {
        this.aoClicar = aoClicar;
    }

    public void definir(List<EstadoLeitura.Registro> novos) {
        itens.clear();
        itens.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Segurador onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista, parent, false);
        return new Segurador(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Segurador segurador, int posicao) {
        EstadoLeitura.Registro registro = itens.get(posicao);
        View contexto = segurador.itemView;
        boolean temNome = registro.nome != null && !registro.nome.isEmpty();
        segurador.nome.setText(temNome ? registro.nome : registro.epc);
        if (temNome) {
            segurador.detalhe.setText(contexto.getContext()
                    .getString(R.string.leitura_linha_com_nome, registro.epc, registro.contagem));
        } else {
            segurador.detalhe.setText(contexto.getContext()
                    .getString(R.string.leitura_linha_sem_nome, registro.contagem));
        }
        segurador.itemView.setOnClickListener(v -> aoClicar.accept(registro));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Segurador extends RecyclerView.ViewHolder {
        final TextView nome;
        final TextView detalhe;

        Segurador(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.item_nome);
            detalhe = itemView.findViewById(R.id.item_detalhe);
        }
    }
}
