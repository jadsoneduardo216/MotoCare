package br.com.jadson.motocare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.com.jadson.motocare.R;

public class MainActivity extends AppCompatActivity {

    private TextView btnAdicionarMoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        inicializarViews();
        configurarBotoes();
    }

    private void inicializarViews() {

        btnAdicionarMoto = findViewById(R.id.btnAdicionarMoto);
    }

    private void configurarBotoes() {

        btnAdicionarMoto.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    CadastroMotoActivity.class
            );

            startActivity(intent);
        });
    }
}