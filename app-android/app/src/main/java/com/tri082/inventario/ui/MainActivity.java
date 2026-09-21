// Tela principal: barra superior com botao de voltar, navegacao por abas, e volta ao login quando a sessao expira.

package com.tri082.inventario.ui;

import android.os.Bundle;
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

    private NavController navController;
    private AppBarConfiguration configuracaoBarra;

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
