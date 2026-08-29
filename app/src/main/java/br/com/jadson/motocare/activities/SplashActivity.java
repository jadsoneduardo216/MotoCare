package br.com.jadson.motocare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.com.jadson.motocare.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Aguarda 2,5 segundos e abre o Login.
        // Durante o desenvolvimento, o aplicativo
        // sempre começará pela tela de Login.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            abrirLoginActivity();

        }, SPLASH_DURATION);
    }

    private void abrirLoginActivity() {

        Intent intent = new Intent(
                SplashActivity.this,
                LoginActivity.class
        );

        startActivity(intent);

        // Encerra a Splash para que ela não fique
        // na pilha de navegação.
        finish();
    }
}