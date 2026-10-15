
package br.com.jadson.motocare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executor;

import br.com.jadson.motocare.R;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtSenha;

    private TextView btnEntrar;
    private TextView btnCriarConta;
    private TextView btnEntrarComDigital;

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
        btnEntrarComDigital = findViewById(R.id.btnEntrarComDigital);
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

        btnEntrarComDigital.setOnClickListener(v -> entrarComDigital());
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

        firebaseAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    btnEntrar.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Login realizado com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                        abrirTelaPrincipal();
                    } else {
                        Toast.makeText(
                                this,
                                "E-mail ou senha incorretos.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void entrarComDigital() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Entre primeiro com e-mail e senha neste dispositivo.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        BiometricManager biometricManager =
                BiometricManager.from(this);

        int resultado = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        );

        if (resultado != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(
                    this,
                    "Biometria indisponível. Verifique o cadastro de digitais nas configurações do celular.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        abrirTelaPrincipal();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(
                                LoginActivity.this,
                                "Biometria não reconhecida. Tente novamente.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(
                                LoginActivity.this,
                                "Autenticação cancelada ou indisponível.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Entrar no MotoCare")
                        .setSubtitle("Confirme sua identidade para continuar")
                        .setAllowedAuthenticators(
                                BiometricManager.Authenticators.BIOMETRIC_WEAK
                        )
                        .setNegativeButtonText("Cancelar")
                        .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );
        startActivity(intent);
        finish();
    }
}
