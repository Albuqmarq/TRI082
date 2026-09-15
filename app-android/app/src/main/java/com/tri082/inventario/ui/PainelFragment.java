// Tela de painel. Estrutura inicial, com atalho temporario para as Configuracoes ate a tela ser desenvolvida.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.tri082.inventario.R;

public class PainelFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_painel, container, false);
        view.findViewById(R.id.botao_config).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.configuracoesFragment));
        return view;
    }
}
