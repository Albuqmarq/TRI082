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
    private final Consumer<EstadoLeitura.Registro> aoSegurar;

    public LeiturasAdapter(Consumer<EstadoLeitura.Registro> aoClicar,
                           Consumer<EstadoLeitura.Registro> aoSegurar) {
        this.aoClicar = aoClicar;
        this.aoSegurar = aoSegurar;
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
        android.content.Context contexto = segurador.itemView.getContext();
        boolean cadastrada = registro.resolvida && registro.cadastrada;

        segurador.nome.setText(cadastrada ? registro.nome : registro.epc);

        StringBuilder detalhe = new StringBuilder();
        if (cadastrada) {
            if (registro.tipo != null && !registro.tipo.isEmpty()) {
                detalhe.append(registro.tipo);
            }
            if (registro.cor != null && !registro.cor.isEmpty()) {
                if (detalhe.length() > 0) {
                    detalhe.append(" - ");
                }
                detalhe.append(registro.cor);
            }
        } else {
            detalhe.append(contexto.getString(R.string.leitura_epc_curto, registro.epc));
        }
        if (detalhe.length() > 0) {
            detalhe.append(" - ");
        }
        detalhe.append(contexto.getString(R.string.leitura_lida, registro.contagem));
        segurador.detalhe.setText(detalhe.toString());

        if (!registro.resolvida) {
            segurador.status.setVisibility(View.GONE);
        } else if (!registro.cadastrada) {
            segurador.status.setText(R.string.leitura_nao_cadastrada);
            segurador.status.setVisibility(View.VISIBLE);
        } else {
            segurador.status.setVisibility(View.GONE);
        }

        segurador.itemView.setOnClickListener(v -> aoClicar.accept(registro));
        segurador.itemView.setOnLongClickListener(v -> {
            aoSegurar.accept(registro);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Segurador extends RecyclerView.ViewHolder {
        final TextView nome;
        final TextView detalhe;
        final TextView status;

        Segurador(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.item_nome);
            detalhe = itemView.findViewById(R.id.item_detalhe);
            status = itemView.findViewById(R.id.item_status);
        }
    }
}
