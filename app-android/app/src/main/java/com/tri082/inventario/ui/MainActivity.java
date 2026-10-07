// Tela principal: navegacao por abas na barra inferior e volta ao login quando a sessao expira.

package com.tri082.inventario.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

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
    private BottomNavigationView bottomNav;
    private View linhaNav;
    private View conteudo;
    private int margemInferior;
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
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        bottomNav = findViewById(R.id.bottom_nav);
        linhaNav = findViewById(R.id.linha_nav);
        conteudo = findViewById(R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNav, navController);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raiz), (raiz, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            raiz.setPadding(0, barras.top, 0, 0);
            margemInferior = Math.max(barras.bottom, teclado.bottom);
            ajustarRodape();
            return insets;
        });

        navController.addOnDestinationChangedListener((controlador, destino, argumentos) -> {
            int id = destino.getId();
            boolean semAbas = id == R.id.loginFragment
                    || id == R.id.configuracoesFragment
                    || id == R.id.itemDetalheFragment
                    || id == R.id.itemFormFragment;
            bottomNav.setVisibility(semAbas ? View.GONE : View.VISIBLE);
            linhaNav.setVisibility(semAbas ? View.GONE : View.VISIBLE);
            ajustarRodape();
        });
    }

    private void ajustarRodape() {
        boolean comAbas = bottomNav.getVisibility() == View.VISIBLE;
        conteudo.setPadding(0, 0, 0, comAbas ? 0 : margemInferior);
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
