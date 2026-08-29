package br.com.jadson.motocare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import br.com.jadson.motocare.R;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtSenha;

    private TextView btnEntrar;
    private TextView btnCriarConta;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        inicializarViews();

        firebaseAuth = FirebaseAuth.getInstance();

        configurarBotoes();
    }

    private void inicializarViews() {

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        btnEntrar = findViewById(R.id.btnEntrar);
        btnCriarConta = findViewById(R.id.btnCriarConta);
    }

    private void configurarBotoes() {

        btnEntrar.setOnClickListener(v -> realizarLogin());

        btnCriarConta.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    CadastroUsuarioActivity.class
            );

            startActivity(intent);
        });
    }

    private void realizarLogin() {

        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Informe seu e-mail");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            edtSenha.setError("Informe sua senha");
            edtSenha.requestFocus();
            return;
        }

        btnEntrar.setEnabled(false);

        firebaseAuth
                .signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {

                    btnEntrar.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                this,
                                "Login realizado com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Abre a tela principal do MotoCare
                        Intent intent = new Intent(
                                LoginActivity.this,
                                MainActivity.class
                        );

                        startActivity(intent);

                        // Impede que o usuário volte para o Login
                        // usando o botão Voltar do celular.
                        finish();

                    } else {

                        Toast.makeText(
                                this,
                                "E-mail ou senha incorretos.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}