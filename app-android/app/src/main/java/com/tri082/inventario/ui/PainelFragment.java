// Tela de painel. Estrutura inicial, com atalho para Configuracoes e o botao de sair da conta.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;

public class PainelFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_painel, container, false);
        view.findViewById(R.id.botao_config).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.configuracoesFragment));
        view.findViewById(R.id.botao_sair).setOnClickListener(v -> sair(v));
        return view;
    }

    private void sair(View view) {
        Sessao.limpar();
        NavOptions opcoes = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        Navigation.findNavController(view).navigate(R.id.loginFragment, null, opcoes);
    }
}
