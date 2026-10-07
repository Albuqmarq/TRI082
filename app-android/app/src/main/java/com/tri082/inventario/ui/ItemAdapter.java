// Adaptador da lista de itens do catalogo: nome, circulo com a cor, cor e tipo, e a categoria.

package com.tri082.inventario.ui;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.Item;
import com.tri082.inventario.util.Cores;

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
                .inflate(R.layout.item_catalogo, parent, false);
        return new Segurador(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Segurador segurador, int posicao) {
        Item item = itens.get(posicao);
        segurador.nome.setText(item.getNome());
        segurador.categoria.setText(item.getCategoria());

        String cor = item.getCor();
        boolean temCor = cor != null && !cor.isEmpty();
        Integer valorCor = Cores.paraCor(cor);
        if (valorCor != null) {
            segurador.swatch.setVisibility(View.VISIBLE);
            GradientDrawable fundo = (GradientDrawable) ContextCompat
                    .getDrawable(segurador.itemView.getContext(), R.drawable.swatch_cor).mutate();
            fundo.setColor(valorCor);
            segurador.swatch.setBackground(fundo);
        } else {
            segurador.swatch.setVisibility(View.GONE);
        }
        segurador.detalhe.setText(temCor ? cor + "  ·  " + item.getTipo() : item.getTipo());

        segurador.itemView.setOnClickListener(v -> aoClicar.accept(item));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Segurador extends RecyclerView.ViewHolder {
        final TextView nome;
        final TextView detalhe;
        final TextView categoria;
        final View swatch;

        Segurador(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.item_nome);
            detalhe = itemView.findViewById(R.id.item_detalhe);
            categoria = itemView.findViewById(R.id.item_categoria);
            swatch = itemView.findViewById(R.id.item_swatch);
        }
    }
}
