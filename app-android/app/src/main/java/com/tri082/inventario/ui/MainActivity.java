// Tela principal: barra superior com botao de voltar, navegacao por abas, e volta ao login quando a sessao expira.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final Set<Integer> TECLAS_GATILHO =
            new HashSet<>(Arrays.asList(293, 294, 520, 521, 522));

    private NavController navController;
    private AppBarConfiguration configuracaoBarra;
    private Runnable gatilhoApertar;
    private Runnable gatilhoSoltar;

    public void registrarGatilho(Runnable apertar, Runnable soltar) {
        this.gatilhoApertar = apertar;
        this.gatilhoSoltar = soltar;
    }

    public void removerGatilho() {
        this.gatilhoApertar = null;
        this.gatilhoSoltar = null;
    }

    @Override
    public boolean onKeyDown(int codigo, KeyEvent evento) {
        if (gatilhoApertar != null && TECLAS_GATILHO.contains(codigo)) {
            if (evento.getRepeatCount() == 0) {
                gatilhoApertar.run();
            }
            return true;
        }
        return super.onKeyDown(codigo, evento);
    }

    @Override
    public boolean onKeyUp(int codigo, KeyEvent evento) {
        if (gatilhoSoltar != null && TECLAS_GATILHO.contains(codigo)) {
            gatilhoSoltar.run();
            return true;
        }
        return super.onKeyUp(codigo, evento);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        Set<Integer> telasPrincipais = new HashSet<>(Arrays.asList(
                R.id.loginFragment,
                R.id.painelFragment,
                R.id.leituraFragment,
                R.id.catalogoFragment,
                R.id.inventarioFragment,
                R.id.localizarFragment));
        configuracaoBarra = new AppBarConfiguration.Builder(telasPrincipais).build();
        NavigationUI.setupActionBarWithNavController(this, navController, configuracaoBarra);

        navController.addOnDestinationChangedListener((controlador, destino, argumentos) -> {
            int id = destino.getId();
            toolbar.setVisibility(id == R.id.loginFragment ? View.GONE : View.VISIBLE);
            boolean semAbas = id == R.id.loginFragment
                    || id == R.id.configuracoesFragment
                    || id == R.id.itemDetalheFragment
                    || id == R.id.itemFormFragment;
            bottomNav.setVisibility(semAbas ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, configuracaoBarra) || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Sessao.definirAoExpirar(this::voltarParaLogin);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Sessao.definirAoExpirar(null);
    }

    private void voltarParaLogin() {
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() != R.id.loginFragment) {
            androidx.navigation.NavOptions opcoes = new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build();
            navController.navigate(R.id.loginFragment, null, opcoes);
        }
    }
}
