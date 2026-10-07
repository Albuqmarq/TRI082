// Tela de configuracao do endereco da API. Le, valida e salva o IP e a porta do servidor.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tri082.inventario.R;
import com.tri082.inventario.data.remote.ConfiguracaoApi;
import com.tri082.inventario.util.EnderecoApi;

public class ConfiguracoesFragment extends Fragment {

    private ConfiguracaoApi configuracao;
    private EditText campoIp;
    private EditText campoPorta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracoes, container, false);

        configuracao = new ConfiguracaoApi(requireContext());
        campoIp = view.findViewById(R.id.campo_ip);
        campoPorta = view.findViewById(R.id.campo_porta);
        Button botaoSalvar = view.findViewById(R.id.botao_salvar);

        campoIp.setText(configuracao.obterIp());
        campoPorta.setText(configuracao.obterPorta());

        view.findViewById(R.id.config_voltar).setOnClickListener(
                v -> androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack());
        botaoSalvar.setOnClickListener(v -> salvar());
        return view;
    }

    private void salvar() {
        String ip = campoIp.getText().toString().trim();
        String porta = campoPorta.getText().toString().trim();

        if (!EnderecoApi.ipValido(ip)) {
            campoIp.setError(getString(R.string.config_erro_ip));
            return;
        }
        if (!EnderecoApi.portaValida(porta)) {
            campoPorta.setError(getString(R.string.config_erro_porta));
            return;
        }

        configuracao.salvar(ip, porta);
        String mensagem = getString(R.string.config_salvo, configuracao.obterUrlBase());
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
    }
}
