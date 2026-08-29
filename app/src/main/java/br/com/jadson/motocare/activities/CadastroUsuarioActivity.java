package br.com.jadson.motocare.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import br.com.jadson.motocare.R;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private EditText edtNome;
    private EditText edtEmail;
    private EditText edtSenha;
    private EditText edtConfirmarSenha;

    private TextView btnCriarConta;
    private TextView btnVoltarLogin;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastro_usuario);

        inicializarViews();

        firebaseAuth = FirebaseAuth.getInstance();

        configurarBotoes();
    }

    private void inicializarViews() {

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);

        btnCriarConta = findViewById(R.id.btnCriarConta);
        btnVoltarLogin = findViewById(R.id.btnVoltarLogin);
    }

    private void configurarBotoes() {

        btnCriarConta.setOnClickListener(v -> criarConta());

        btnVoltarLogin.setOnClickListener(v -> finish());
    }

    private void criarConta() {

        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();
        String confirmarSenha = edtConfirmarSenha.getText().toString().trim();

        // Verifica o nome
        if (TextUtils.isEmpty(nome)) {
            edtNome.setError("Informe seu nome");
            edtNome.requestFocus();
            return;
        }

        // Verifica o e-mail
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Informe seu e-mail");
            edtEmail.requestFocus();
            return;
        }

        // Verifica a senha
        if (TextUtils.isEmpty(senha)) {
            edtSenha.setError("Informe uma senha");
            edtSenha.requestFocus();
            return;
        }

        // Firebase exige pelo menos 6 caracteres
        if (senha.length() < 6) {
            edtSenha.setError("A senha deve ter pelo menos 6 caracteres");
            edtSenha.requestFocus();
            return;
        }

        // Confirma a senha
        if (TextUtils.isEmpty(confirmarSenha)) {
            edtConfirmarSenha.setError("Confirme sua senha");
            edtConfirmarSenha.requestFocus();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            edtConfirmarSenha.setError("As senhas não coincidem");
            edtConfirmarSenha.requestFocus();
            return;
        }

        btnCriarConta.setEnabled(false);

        firebaseAuth
                .createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {

                    btnCriarConta.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                this,
                                "Conta criada com sucesso!",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                    } else {

                        Toast.makeText(
                                this,
                                "Não foi possível criar a conta.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}