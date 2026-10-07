// Tela de usuarios (somente Administrador): lista as contas e abre o cadastro ou a edicao de cada uma.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;
import com.tri082.inventario.data.remote.ClienteApi;
import com.tri082.inventario.data.remote.UsuarioResposta;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuariosFragment extends Fragment {

    private UsuariosAdapter adapter;
    private TextView textoContagem;
    private final List<UsuarioResposta> usuarios = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios, container, false);

        if (!Sessao.ehAdmin()) {
            Toast.makeText(requireContext(), R.string.acesso_restrito_admin, Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).popBackStack();
            return view;
        }

        textoContagem = view.findViewById(R.id.usu_contagem);
        adapter = new UsuariosAdapter(this::abrirFormulario);
        RecyclerView lista = view.findViewById(R.id.usu_lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        lista.setAdapter(adapter);

        view.findViewById(R.id.usu_voltar).setOnClickListener(
                v -> NavHostFragment.findNavController(this).popBackStack());
        view.findViewById(R.id.usu_novo).setOnClickListener(v -> abrirFormulario(null));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            carregar();
        }
    }

    private void carregar() {
        ClienteApi.criarServico(requireContext()).listarUsuarios().enqueue(new Callback<List<UsuarioResposta>>() {
            @Override
            public void onResponse(@NonNull Call<List<UsuarioResposta>> call,
                                   @NonNull Response<List<UsuarioResposta>> resposta) {
                if (!isAdded()) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    usuarios.clear();
                    usuarios.addAll(resposta.body());
                    adapter.definir(usuarios);
                    textoContagem.setText(getResources().getQuantityString(R.plurals.usu_contagem,
                            resposta.body().size(), resposta.body().size()));
                } else {
                    Toast.makeText(requireContext(), R.string.usu_erro_carregar, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UsuarioResposta>> call, @NonNull Throwable erro) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.usu_erro_carregar, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean ehAdminAtivo(UsuarioResposta usuario) {
        return "admin".equals(usuario.getPapel()) && usuario.isAtivo();
    }

    private int contarAdminsAtivos() {
        int total = 0;
        for (UsuarioResposta usuario : usuarios) {
            if (ehAdminAtivo(usuario)) {
                total++;
            }
        }
        return total;
    }

    private void abrirFormulario(@Nullable UsuarioResposta usuario) {
        Bundle argumentos = new Bundle();
        if (usuario != null) {
            argumentos.putInt("usuarioId", usuario.getId());
            argumentos.putString("nome", usuario.getNomeUsuario());
            argumentos.putString("papel", usuario.getPapel());
            argumentos.putBoolean("ativo", usuario.isAtivo());
            argumentos.putBoolean("unicoAdminAtivo", ehAdminAtivo(usuario) && contarAdminsAtivos() == 1);
        }
        NavHostFragment.findNavController(this).navigate(R.id.usuarioFormFragment, argumentos);
    }
}
