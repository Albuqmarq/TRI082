// Tela de login: autentica na API, guarda a sessao e leva ao Painel. Permite abrir as Configuracoes antes de entrar.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.ConfiguracaoApi;
import com.tri082.inventario.data.remote.LoginRequisicao;
import com.tri082.inventario.data.remote.TokenResposta;
import com.tri082.inventario.data.remote.UsuarioResposta;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText campoUsuario;
    private EditText campoSenha;
    private TextView textoErro;
    private Button botaoEntrar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        campoUsuario = view.findViewById(R.id.campo_usuario);
        campoSenha = view.findViewById(R.id.campo_senha);
        textoErro = view.findViewById(R.id.texto_erro);
        botaoEntrar = view.findViewById(R.id.botao_entrar);

        botaoEntrar.setOnClickListener(v -> entrar());
        view.findViewById(R.id.botao_configurar).setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.configuracoesFragment));
        return view;
    }

    private void entrar() {
        String usuario = campoUsuario.getText().toString().trim();
        String senha = campoSenha.getText().toString();

        if (usuario.isEmpty() || senha.isEmpty()) {
            mostrarErro(getString(R.string.login_erro_campos));
            return;
        }
        ConfiguracaoApi configuracao = new ConfiguracaoApi(requireContext());
        if (!configuracao.estaConfigurado()) {
            mostrarErro(getString(R.string.login_erro_config));
            return;
        }

        esconderErro();
        botaoEntrar.setEnabled(false);
        ApiService servico = ClienteApi.criarServico(requireContext());
        servico.login(new LoginRequisicao(usuario, senha)).enqueue(new Callback<TokenResposta>() {
            @Override
            public void onResponse(@NonNull Call<TokenResposta> call, @NonNull Response<TokenResposta> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    Sessao.definirToken(resposta.body().getTokenAcesso());
                    carregarPerfil(servico);
                } else {
                    botaoEntrar.setEnabled(true);
                    if (resposta.code() == 401) {
                        mostrarErro(getString(R.string.login_erro_credenciais));
                    } else {
                        mostrarErro(getString(R.string.login_erro_generico, resposta.code()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenResposta> call, @NonNull Throwable erro) {
                if (!isAdded()) {
                    return;
                }
                botaoEntrar.setEnabled(true);
                mostrarErro(getString(R.string.login_erro_conexao));
            }
        });
    }

    private void carregarPerfil(ApiService servico) {
        servico.obterMeusDados().enqueue(new Callback<UsuarioResposta>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResposta> call, @NonNull Response<UsuarioResposta> resposta) {
                if (!isAdded()) {
                    return;
                }
                botaoEntrar.setEnabled(true);
                if (resposta.isSuccessful() && resposta.body() != null) {
                    Sessao.definirPapel(resposta.body().getPapel());
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.acao_login_para_painel);
                } else {
                    Sessao.limpar();
                    mostrarErro(getString(R.string.login_erro_perfil));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResposta> call, @NonNull Throwable erro) {
                if (!isAdded()) {
                    return;
                }
                botaoEntrar.setEnabled(true);
                Sessao.limpar();
                mostrarErro(getString(R.string.login_erro_perfil));
            }
        });
    }

    private void mostrarErro(String mensagem) {
        textoErro.setText(mensagem);
        textoErro.setVisibility(View.VISIBLE);
    }

    private void esconderErro() {
        textoErro.setVisibility(View.GONE);
    }
}
