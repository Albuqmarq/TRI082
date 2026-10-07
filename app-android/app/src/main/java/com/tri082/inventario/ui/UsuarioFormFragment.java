// Cadastro e edicao de conta de usuario (somente Administrador). Na edicao a senha e opcional e a conta pode
// ser desativada. O unico administrador ativo nao pode ser rebaixado nem desativado (a API tambem bloqueia),
// e quem altera o proprio papel ou desativa a propria conta volta para o login.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ApiService;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.UsuarioAtualizar;
import com.tri082.inventario.data.remote.UsuarioCriar;
import com.tri082.inventario.data.remote.UsuarioResposta;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioFormFragment extends Fragment {

    private static final int SENHA_MINIMA = 6;
    private static final String ADMIN = "admin";
    private static final String FUNCIONARIO = "funcionario";

    private int usuarioId;
    private boolean edicao;
    private String papelOriginal;
    private boolean ativoOriginal;
    private String papelEscolhido = FUNCIONARIO;
    private boolean propriaConta;

    private EditText campoNome;
    private EditText campoSenha;
    private TextView chipAdmin;
    private TextView chipFuncionario;
    private MaterialSwitch interruptorAtivo;
    private TextView avisoPropria;
    private Button botaoSalvar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuario_form, container, false);

        if (!Sessao.ehAdmin()) {
            Toast.makeText(requireContext(), R.string.acesso_restrito_admin, Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).popBackStack();
            return view;
        }

        Bundle argumentos = getArguments();
        usuarioId = argumentos != null ? argumentos.getInt("usuarioId", -1) : -1;
        edicao = usuarioId > 0;

        campoNome = view.findViewById(R.id.uf_campo_nome);
        campoSenha = view.findViewById(R.id.uf_campo_senha);
        chipAdmin = view.findViewById(R.id.uf_chip_admin);
        chipFuncionario = view.findViewById(R.id.uf_chip_funcionario);
        interruptorAtivo = view.findViewById(R.id.uf_ativo);
        avisoPropria = view.findViewById(R.id.uf_aviso_propria);
        botaoSalvar = view.findViewById(R.id.uf_salvar);
        TextView titulo = view.findViewById(R.id.uf_titulo);

        chipAdmin.setOnClickListener(v -> escolherPapel(ADMIN));
        chipFuncionario.setOnClickListener(v -> escolherPapel(FUNCIONARIO));

        if (edicao) {
            papelOriginal = argumentos.getString("papel", FUNCIONARIO);
            ativoOriginal = argumentos.getBoolean("ativo", true);
            titulo.setText(argumentos.getString("nome", ""));
            view.findViewById(R.id.uf_area_nome).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.uf_rotulo_senha)).setText(R.string.uf_nova_senha);
            ((TextView) view.findViewById(R.id.uf_ajuda_senha)).setText(R.string.uf_senha_ajuda_edicao);
            interruptorAtivo.setVisibility(View.VISIBLE);
            interruptorAtivo.setChecked(ativoOriginal);
            botaoSalvar.setText(R.string.form_salvar_editar);
            escolherPapel(papelOriginal);
            if (argumentos.getBoolean("unicoAdminAtivo", false)) {
                travarPapelESituacao();
            }
            verificarPropriaConta();
        } else {
            titulo.setText(R.string.uf_titulo_novo);
            botaoSalvar.setText(R.string.uf_criar);
            escolherPapel(FUNCIONARIO);
        }

        view.findViewById(R.id.uf_voltar).setOnClickListener(v -> voltar());
        view.findViewById(R.id.uf_cancelar).setOnClickListener(v -> voltar());
        botaoSalvar.setOnClickListener(v -> salvar());
        return view;
    }

    private void escolherPapel(String papel) {
        papelEscolhido = ADMIN.equals(papel) ? ADMIN : FUNCIONARIO;
        chipAdmin.setSelected(ADMIN.equals(papelEscolhido));
        chipFuncionario.setSelected(FUNCIONARIO.equals(papelEscolhido));
    }

    // O sistema precisa de pelo menos um administrador ativo, entao o unico nao pode perder esse papel.
    private void travarPapelESituacao() {
        chipAdmin.setEnabled(false);
        chipFuncionario.setEnabled(false);
        chipFuncionario.setAlpha(0.4f);
        interruptorAtivo.setEnabled(false);
        avisoPropria.setVisibility(View.VISIBLE);
    }

    private void verificarPropriaConta() {
        servico().obterMeusDados().enqueue(new Callback<UsuarioResposta>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResposta> call, @NonNull Response<UsuarioResposta> resposta) {
                if (isAdded() && resposta.isSuccessful() && resposta.body() != null) {
                    propriaConta = resposta.body().getId() == usuarioId;
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResposta> call, @NonNull Throwable erro) {
            }
        });
    }

    private void salvar() {
        String senha = campoSenha.getText().toString();
        if (edicao) {
            if (!senha.isEmpty() && senha.length() < SENHA_MINIMA) {
                campoSenha.setError(getString(R.string.uf_erro_senha));
                return;
            }
            String papel = papelEscolhido.equals(papelOriginal) ? null : papelEscolhido;
            Boolean ativo = interruptorAtivo.isChecked() == ativoOriginal ? null : interruptorAtivo.isChecked();
            String novaSenha = senha.isEmpty() ? null : senha;
            if (papel == null && ativo == null && novaSenha == null) {
                voltar();
                return;
            }
            boolean perdeAcesso = Boolean.FALSE.equals(ativo) || FUNCIONARIO.equals(papel);
            if (propriaConta && perdeAcesso) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.uf_confirmar_propria_titulo)
                        .setMessage(R.string.uf_confirmar_propria_msg)
                        .setPositiveButton(R.string.uf_confirmar, (dialogo, qual) -> enviarEdicao(papel, novaSenha, ativo))
                        .setNegativeButton(R.string.form_cancelar, null)
                        .show();
            } else {
                enviarEdicao(papel, novaSenha, ativo);
            }
        } else {
            String nome = campoNome.getText().toString().trim();
            if (nome.isEmpty()) {
                campoNome.setError(getString(R.string.uf_erro_nome));
                return;
            }
            if (senha.length() < SENHA_MINIMA) {
                campoSenha.setError(getString(R.string.uf_erro_senha));
                return;
            }
            botaoSalvar.setEnabled(false);
            servico().criarUsuario(new UsuarioCriar(nome, senha, papelEscolhido)).enqueue(retorno());
        }
    }

    private void enviarEdicao(String papel, String novaSenha, Boolean ativo) {
        botaoSalvar.setEnabled(false);
        boolean saiDaConta = propriaConta && (Boolean.FALSE.equals(ativo) || FUNCIONARIO.equals(papel));
        servico().atualizarUsuario(usuarioId, new UsuarioAtualizar(papel, novaSenha, ativo))
                .enqueue(retorno(saiDaConta));
    }

    private Callback<UsuarioResposta> retorno() {
        return retorno(false);
    }

    private Callback<UsuarioResposta> retorno(boolean saiDaConta) {
        return new Callback<UsuarioResposta>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioResposta> call, @NonNull Response<UsuarioResposta> resposta) {
                if (!isAdded()) {
                    return;
                }
                botaoSalvar.setEnabled(true);
                if (resposta.isSuccessful() && saiDaConta) {
                    Toast.makeText(requireContext(), R.string.uf_saiu, Toast.LENGTH_LONG).show();
                    sairDaConta();
                } else if (resposta.isSuccessful()) {
                    Toast.makeText(requireContext(), edicao ? R.string.uf_salvo : R.string.uf_criado,
                            Toast.LENGTH_SHORT).show();
                    voltar();
                } else if (resposta.code() == 409) {
                    Toast.makeText(requireContext(), edicao ? R.string.uf_erro_propria : R.string.uf_erro_duplicado,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), R.string.uf_erro_salvar, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioResposta> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    botaoSalvar.setEnabled(true);
                    Toast.makeText(requireContext(), R.string.uf_erro_salvar, Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void sairDaConta() {
        Sessao.limpar();
        NavOptions opcoes = new NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build();
        NavHostFragment.findNavController(this).navigate(R.id.loginFragment, null, opcoes);
    }

    private void voltar() {
        NavHostFragment.findNavController(this).popBackStack();
    }

    private ApiService servico() {
        return ClienteApi.criarServico(requireContext());
    }
}
