package br.com.jadson.motocare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.MotoDao;

public class MainActivity extends AppCompatActivity {

    private TextView btnAdicionarMoto;

    private TextView txtSaudacao;
    private TextView txtNenhumaMoto;
    private TextView txtDescricaoMoto;

    private MotoDao motoDao;

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

        motoDao = new MotoDao(this);

        configurarBotoes();

        carregarUsuario();

        carregarMotocicleta();
    }

    private void inicializarViews() {

        btnAdicionarMoto = findViewById(R.id.btnAdicionarMoto);

        txtSaudacao = findViewById(R.id.txtSaudacao);
        txtNenhumaMoto = findViewById(R.id.txtNenhumaMoto);
        txtDescricaoMoto = findViewById(R.id.txtDescricaoMoto);
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

    /**
     * Carrega o nome do usuário logado.
     */
    private void carregarUsuario() {

        FirebaseUser usuarioAtual =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual != null) {

            String nome = usuarioAtual.getDisplayName();

            if (nome != null && !nome.trim().isEmpty()) {

                txtSaudacao.setText(
                        "Olá, " + nome + "!"
                );

            } else {

                txtSaudacao.setText("Olá!");
            }
        }
    }

    /**
     * Busca as motocicletas do usuário no SQLite.
     */
    private void carregarMotocicleta() {

        FirebaseUser usuarioAtual =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual == null) {
            return;
        }

        String uid = usuarioAtual.getUid();

        List<Motocicleta> motos =
                motoDao.listarPorUsuario(uid);

        if (motos.isEmpty()) {

            mostrarSemMotocicleta();

        } else {

            // Por enquanto mostramos a primeira moto.
            mostrarMotocicleta(motos.get(0));
        }
    }

    /**
     * Mostra o estado quando não existe moto cadastrada.
     */
    private void mostrarSemMotocicleta() {

        txtNenhumaMoto.setText(
                "Nenhuma motocicleta\ncadastrada"
        );

        txtDescricaoMoto.setText(
                "Cadastre sua moto para acompanhar suas manutenções."
        );

        btnAdicionarMoto.setText(
                "+  ADICIONAR MOTOCICLETA"
        );
    }

    /**
     * Mostra os dados da motocicleta.
     */
    private void mostrarMotocicleta(Motocicleta moto) {

        txtNenhumaMoto.setText(
                moto.getApelido()
        );

        txtDescricaoMoto.setText(
                moto.getMarca()
                        + " "
                        + moto.getModelo()
                        + "\n"
                        + moto.getAno()
                        + " • "
                        + moto.getPlaca()
                        + "\n"
                        + moto.getQuilometragem()
                        + " km"
        );

        btnAdicionarMoto.setText(
                "+  ADICIONAR OUTRA MOTOCICLETA"
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Atualiza o card quando voltamos do cadastro.
        if (motoDao != null) {
            carregarMotocicleta();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (motoDao != null) {
            motoDao.fechar();
        }
    }
}