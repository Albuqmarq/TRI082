// Adaptador da lista de tags lidas na tela de leitura.

package com.tri082.inventario.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;

import java.util.ArrayList;
import java.util.List;

public class LeiturasAdapter extends RecyclerView.Adapter<LeiturasAdapter.Segurador> {

    private static class Leitura {
        final String epc;
        final int sinal;

        Leitura(String epc, int sinal) {
            this.epc = epc;
            this.sinal = sinal;
        }
    }

    private final List<Leitura> itens = new ArrayList<>();

    public void adicionar(String epc, int sinal) {
        itens.add(0, new Leitura(epc, sinal));
        notifyItemInserted(0);
    }

    public void limpar() {
        itens.clear();
        notifyDataSetChanged();
    }

    public int total() {
        return itens.size();
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
        Leitura leitura = itens.get(posicao);
        segurador.nome.setText(leitura.epc);
        segurador.detalhe.setText(segurador.itemView.getContext()
                .getString(R.string.leitura_sinal, leitura.sinal));
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
