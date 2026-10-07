// Adaptador da lista de usuarios: nome, papel e se a conta esta ativa.

package com.tri082.inventario.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.UsuarioResposta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.Segurador> {

    private final List<UsuarioResposta> usuarios = new ArrayList<>();
    private final Consumer<UsuarioResposta> aoClicar;

    public UsuariosAdapter(Consumer<UsuarioResposta> aoClicar) {
        this.aoClicar = aoClicar;
    }

    public void definir(List<UsuarioResposta> novos) {
        usuarios.clear();
        usuarios.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Segurador onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalogo, parent, false);
        return new Segurador(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Segurador segurador, int posicao) {
        UsuarioResposta usuario = usuarios.get(posicao);
        Context contexto = segurador.itemView.getContext();

        segurador.nome.setText(usuario.getNomeUsuario());
        segurador.swatch.setVisibility(View.GONE);
        segurador.papel.setText("admin".equals(usuario.getPapel())
                ? R.string.painel_papel_admin : R.string.painel_papel_funcionario);
        segurador.situacao.setText(usuario.isAtivo() ? R.string.usu_ativo : R.string.usu_inativo);
        segurador.situacao.setTextColor(ContextCompat.getColor(contexto,
                usuario.isAtivo() ? R.color.verde : R.color.erro));
        segurador.nome.setTextColor(ContextCompat.getColor(contexto,
                usuario.isAtivo() ? R.color.texto : R.color.texto_secundario));

        segurador.itemView.setOnClickListener(v -> aoClicar.accept(usuario));
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    static class Segurador extends RecyclerView.ViewHolder {
        final TextView nome;
        final TextView papel;
        final TextView situacao;
        final View swatch;

        Segurador(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.item_nome);
            papel = itemView.findViewById(R.id.item_detalhe);
            situacao = itemView.findViewById(R.id.item_categoria);
            swatch = itemView.findViewById(R.id.item_swatch);
        }
    }
}
