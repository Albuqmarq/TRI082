// Adaptador da lista de itens do catalogo.

package com.tri082.inventario.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.Segurador> {

    private final List<Item> itens = new ArrayList<>();
    private final Consumer<Item> aoClicar;

    public ItemAdapter(Consumer<Item> aoClicar) {
        this.aoClicar = aoClicar;
    }

    public void definir(List<Item> novos) {
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
        Item item = itens.get(posicao);
        segurador.nome.setText(item.getNome());
        segurador.detalhe.setText(montarDetalhe(item));
        segurador.itemView.setOnClickListener(v -> aoClicar.accept(item));
    }

    private String montarDetalhe(Item item) {
        StringBuilder texto = new StringBuilder();
        texto.append(item.getCategoria()).append(" - ").append(item.getTipo());
        if (item.getCor() != null && !item.getCor().isEmpty()) {
            texto.append(" - ").append(item.getCor());
        }
        return texto.toString();
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
