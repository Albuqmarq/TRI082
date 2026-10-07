// Adaptador da lista de pendencias: EPC da tag, quantas vezes foi lida e quando foi a ultima leitura.

package com.tri082.inventario.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.Pendencia;
import com.tri082.inventario.util.Datas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PendenciasAdapter extends RecyclerView.Adapter<PendenciasAdapter.Segurador> {

    private final List<Pendencia> pendencias = new ArrayList<>();
    private final Consumer<Pendencia> aoClicar;

    public PendenciasAdapter(Consumer<Pendencia> aoClicar) {
        this.aoClicar = aoClicar;
    }

    public void definir(List<Pendencia> novas) {
        pendencias.clear();
        pendencias.addAll(novas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Segurador onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leitura, parent, false);
        return new Segurador(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Segurador segurador, int posicao) {
        Pendencia pendencia = pendencias.get(posicao);
        Context contexto = segurador.itemView.getContext();

        segurador.titulo.setText(pendencia.epc);
        segurador.titulo.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        segurador.detalhe.setTypeface(Typeface.DEFAULT);
        segurador.detalhe.setText(contexto.getResources().getQuantityString(R.plurals.pend_leituras,
                pendencia.ocorrencias, pendencia.ocorrencias, Datas.paraTexto(pendencia.ultimaLeitura)));
        segurador.acao.setText(R.string.pend_resolver);
        segurador.acao.setTextColor(ContextCompat.getColor(contexto, R.color.navy));

        segurador.itemView.setOnClickListener(v -> aoClicar.accept(pendencia));
    }

    @Override
    public int getItemCount() {
        return pendencias.size();
    }

    static class Segurador extends RecyclerView.ViewHolder {
        final TextView titulo;
        final TextView detalhe;
        final TextView acao;

        Segurador(View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.item_nome);
            detalhe = itemView.findViewById(R.id.item_epc);
            acao = itemView.findViewById(R.id.item_status);
        }
    }
}
