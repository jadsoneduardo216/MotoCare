package br.com.jadson.motocare.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.MotoDao;

public class CadastroMotoActivity extends AppCompatActivity {

    private EditText edtApelido;
    private EditText edtMarca;
    private EditText edtModelo;
    private EditText edtAno;
    private EditText edtPlaca;
    private EditText edtQuilometragem;

    private TextView btnSalvarMoto;
    private ImageView btnVoltar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private MotoDao motoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastro_moto);

        inicializarViews();

        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");

        motoDao = new MotoDao(this);

        configurarBotoes();
    }

    private void inicializarViews() {

        edtApelido = findViewById(R.id.edtApelido);
        edtMarca = findViewById(R.id.edtMarca);
        edtModelo = findViewById(R.id.edtModelo);
        edtAno = findViewById(R.id.edtAno);
        edtPlaca = findViewById(R.id.edtPlaca);
        edtQuilometragem = findViewById(R.id.edtQuilometragem);

        btnSalvarMoto = findViewById(R.id.btnSalvarMoto);
        btnVoltar = findViewById(R.id.btnVoltar);
    }

    private void configurarBotoes() {

        btnVoltar.setOnClickListener(v -> finish());

        btnSalvarMoto.setOnClickListener(v -> salvarMotocicleta());
    }

    private void salvarMotocicleta() {

        String apelido = edtApelido.getText().toString().trim();
        String marca = edtMarca.getText().toString().trim();
        String modelo = edtModelo.getText().toString().trim();
        String ano = edtAno.getText().toString().trim();
        String placa = edtPlaca.getText().toString().trim();
        String quilometragem = edtQuilometragem.getText().toString().trim();

        // =========================
        // VALIDAÇÕES
        // =========================

        if (apelido.isEmpty()) {
            edtApelido.setError("Informe um apelido para sua moto");
            edtApelido.requestFocus();
            return;
        }

        if (marca.isEmpty()) {
            edtMarca.setError("Informe a marca");
            edtMarca.requestFocus();
            return;
        }

        if (modelo.isEmpty()) {
            edtModelo.setError("Informe o modelo");
            edtModelo.requestFocus();
            return;
        }

        if (ano.isEmpty()) {
            edtAno.setError("Informe o ano");
            edtAno.requestFocus();
            return;
        }

        if (placa.isEmpty()) {
            edtPlaca.setError("Informe a placa");
            edtPlaca.requestFocus();
            return;
        }

        if (quilometragem.isEmpty()) {
            edtQuilometragem.setError(
                    "Informe a quilometragem atual"
            );
            edtQuilometragem.requestFocus();
            return;
        }

        // =========================
        // USUÁRIO LOGADO
        // =========================

        FirebaseUser usuarioAtual = firebaseAuth.getCurrentUser();

        if (usuarioAtual == null) {

            Toast.makeText(
                    this,
                    "Nenhum usuário está logado.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String uid = usuarioAtual.getUid();

        // =========================
        // GERA ID DA MOTOCICLETA
        // =========================

        String idMoto = databaseReference
                .child(uid)
                .child("motocicletas")
                .push()
                .getKey();

        if (idMoto == null) {

            Toast.makeText(
                    this,
                    "Não foi possível gerar o ID da motocicleta.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // =========================
        // CRIA OBJETO
        // =========================

        Motocicleta motocicleta = new Motocicleta(
                idMoto,
                apelido,
                marca,
                modelo,
                ano,
                placa,
                quilometragem
        );

        // =========================
        // SALVA PRIMEIRO NO SQLITE
        // =========================

        boolean salvaLocal = motoDao.inserir(
                motocicleta,
                uid
        );

        if (!salvaLocal) {

            Toast.makeText(
                    this,
                    "Não foi possível salvar a motocicleta localmente.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // =========================
        // DESABILITA BOTÃO
        // =========================

        btnSalvarMoto.setEnabled(false);

        // =========================
        // ENVIA PARA O FIREBASE
        // =========================

        databaseReference
                .child(uid)
                .child("motocicletas")
                .child(idMoto)
                .setValue(motocicleta)
                .addOnCompleteListener(task -> {

                    btnSalvarMoto.setEnabled(true);

                    if (task.isSuccessful()) {

                        // Firebase confirmou o salvamento.
                        // Agora marcamos a moto como sincronizada
                        // no SQLite.

                        motoDao.marcarComoSincronizada(idMoto);

                        Toast.makeText(
                                this,
                                "Motocicleta salva com sucesso!",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                    } else {

                        /*
                         * A moto continua no SQLite.
                         *
                         * Como ela foi criada com
                         * sincronizado = 0,
                         * poderemos enviá-la posteriormente.
                         */

                        Toast.makeText(
                                this,
                                "Moto salva no aparelho. "
                                        + "Não foi possível sincronizar com a nuvem.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (motoDao != null) {
            motoDao.fechar();
        }
    }
}