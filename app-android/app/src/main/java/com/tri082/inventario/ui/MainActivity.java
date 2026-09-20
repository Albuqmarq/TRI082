// Tela principal que hospeda a navegacao por abas e esconde as abas nas telas de login e configuracoes.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tri082.inventario.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener((controlador, destino, argumentos) -> {
            int id = destino.getId();
            boolean semAbas = id == R.id.loginFragment || id == R.id.configuracoesFragment;
            bottomNav.setVisibility(semAbas ? View.GONE : View.VISIBLE);
        });
    }
}
