// Adaptador da lista de tags lidas: mostra o nome (quando conhecido) ou que a tag esta sem cadastro, com o EPC.

package com.tri082.inventario.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.EstadoLeitura;
import com.tri082.inventario.data.local.Sessao;

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
                .inflate(R.layout.item_leitura, parent, false);
        return new Segurador(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Segurador segurador, int posicao) {
        EstadoLeitura.Registro registro = itens.get(posicao);
        android.content.Context contexto = segurador.itemView.getContext();
        boolean cadastrada = registro.resolvida && registro.cadastrada;

        if (cadastrada) {
            segurador.nome.setText(registro.nome);
            pintar(segurador.nome, R.color.texto);
            segurador.epc.setText(contexto.getString(R.string.leitura_epc_curto, registro.epc));
            segurador.status.setText(contexto.getString(R.string.leitura_lida, registro.contagem));
            pintar(segurador.status, R.color.texto_secundario);
        } else if (registro.resolvida) {
            segurador.nome.setText(R.string.leitura_tag_sem_cadastro);
            pintar(segurador.nome, R.color.texto_secundario);
            segurador.epc.setText(registro.epc);
            if (Sessao.ehAdmin()) {
                segurador.status.setText(R.string.leitura_cadastrar);
                pintar(segurador.status, R.color.navy);
            } else {
                segurador.status.setText(R.string.leitura_pendencia_curta);
                pintar(segurador.status, R.color.texto_secundario);
            }
        } else {
            segurador.nome.setText(registro.epc);
            pintar(segurador.nome, R.color.texto);
            segurador.epc.setText(R.string.leitura_verificando);
            segurador.status.setText(contexto.getString(R.string.leitura_lida, registro.contagem));
            pintar(segurador.status, R.color.texto_secundario);
        }

        segurador.itemView.setOnClickListener(v -> aoClicar.accept(registro));
        segurador.itemView.setOnLongClickListener(v -> {
            aoSegurar.accept(registro);
            return true;
        });
    }

    private void pintar(TextView alvo, int cor) {
        alvo.setTextColor(ContextCompat.getColor(alvo.getContext(), cor));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Segurador extends RecyclerView.ViewHolder {
        final TextView nome;
        final TextView epc;
        final TextView status;

        Segurador(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.item_nome);
            epc = itemView.findViewById(R.id.item_epc);
            status = itemView.findViewById(R.id.item_status);
        }
    }
}
