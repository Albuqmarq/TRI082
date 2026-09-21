// Tela principal que hospeda a navegacao por abas, esconde as abas no login e configuracoes, e volta ao login quando a sessao expira.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tri082.inventario.R;
import com.tri082.inventario.data.local.Sessao;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener((controlador, destino, argumentos) -> {
            int id = destino.getId();
            boolean semAbas = id == R.id.loginFragment || id == R.id.configuracoesFragment;
            bottomNav.setVisibility(semAbas ? View.GONE : View.VISIBLE);
        });
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
            NavOptions opcoes = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build();
            navController.navigate(R.id.loginFragment, null, opcoes);
        }
    }
}
