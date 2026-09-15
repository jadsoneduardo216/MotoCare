package br.com.jadson.motocare.activities;

import android.content.SharedPreferences;
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

public class EditarMotoActivity extends AppCompatActivity {

    private ImageView btnVoltarEditarMoto;

    private EditText edtApelidoEditar;
    private EditText edtMarcaEditar;
    private EditText edtModeloEditar;
    private EditText edtAnoEditar;
    private EditText edtPlacaEditar;
    private EditText edtQuilometragemEditar;

    private TextView btnSalvarAlteracoesMoto;

    private MotoDao motoDao;

    private SharedPreferences preferences;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";

    private Motocicleta motoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editar_moto);

        inicializarViews();

        motoDao = new MotoDao(this);

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");

        configurarBotoes();

        carregarMotocicleta();
    }

    private void inicializarViews() {

        btnVoltarEditarMoto =
                findViewById(R.id.btnVoltarEditarMoto);

        edtApelidoEditar =
                findViewById(R.id.edtApelidoEditar);

        edtMarcaEditar =
                findViewById(R.id.edtMarcaEditar);

        edtModeloEditar =
                findViewById(R.id.edtModeloEditar);

        edtAnoEditar =
                findViewById(R.id.edtAnoEditar);

        edtPlacaEditar =
                findViewById(R.id.edtPlacaEditar);

        edtQuilometragemEditar =
                findViewById(R.id.edtQuilometragemEditar);

        btnSalvarAlteracoesMoto =
                findViewById(R.id.btnSalvarAlteracoesMoto);
    }

    private void configurarBotoes() {

        btnVoltarEditarMoto.setOnClickListener(
                v -> finish()
        );

        btnSalvarAlteracoesMoto.setOnClickListener(
                v -> salvarAlteracoes()
        );
    }

    private void carregarMotocicleta() {

        String idMotoAtiva =
                preferences.getString(
                        KEY_MOTO_ATIVA,
                        null
                );

        if (idMotoAtiva == null
                || idMotoAtiva.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Nenhuma motocicleta selecionada.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        motoAtual =
                motoDao.buscarPorId(idMotoAtiva);

        if (motoAtual == null) {

            Toast.makeText(
                    this,
                    "Não foi possível encontrar a motocicleta.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        preencherCampos();
    }

    private void preencherCampos() {

        edtApelidoEditar.setText(
                motoAtual.getApelido()
        );

        edtMarcaEditar.setText(
                motoAtual.getMarca()
        );

        edtModeloEditar.setText(
                motoAtual.getModelo()
        );

        edtAnoEditar.setText(
                motoAtual.getAno()
        );

        edtPlacaEditar.setText(
                motoAtual.getPlaca()
        );

        edtQuilometragemEditar.setText(
                motoAtual.getQuilometragem()
        );
    }

    private void salvarAlteracoes() {

        String apelido =
                edtApelidoEditar.getText()
                        .toString()
                        .trim();

        String marca =
                edtMarcaEditar.getText()
                        .toString()
                        .trim();

        String modelo =
                edtModeloEditar.getText()
                        .toString()
                        .trim();

        String ano =
                edtAnoEditar.getText()
                        .toString()
                        .trim();

        String placa =
                edtPlacaEditar.getText()
                        .toString()
                        .trim()
                        .toUpperCase();

        String quilometragem =
                edtQuilometragemEditar.getText()
                        .toString()
                        .trim();

        // =========================
        // VALIDAÇÕES
        // =========================

        if (apelido.isEmpty()) {

            edtApelidoEditar.setError(
                    "Informe um apelido para sua moto"
            );

            edtApelidoEditar.requestFocus();

            return;
        }

        if (marca.isEmpty()) {

            edtMarcaEditar.setError(
                    "Informe a marca"
            );

            edtMarcaEditar.requestFocus();

            return;
        }

        if (modelo.isEmpty()) {

            edtModeloEditar.setError(
                    "Informe o modelo"
            );

            edtModeloEditar.requestFocus();

            return;
        }

        if (ano.isEmpty()) {

            edtAnoEditar.setError(
                    "Informe o ano"
            );

            edtAnoEditar.requestFocus();

            return;
        }

        if (placa.isEmpty()) {

            edtPlacaEditar.setError(
                    "Informe a placa"
            );

            edtPlacaEditar.requestFocus();

            return;
        }

        if (quilometragem.isEmpty()) {

            edtQuilometragemEditar.setError(
                    "Informe a quilometragem atual"
            );

            edtQuilometragemEditar.requestFocus();

            return;
        }

        // =========================
        // USUÁRIO LOGADO
        // =========================

        FirebaseUser usuarioAtual =
                firebaseAuth.getCurrentUser();

        if (usuarioAtual == null) {

            Toast.makeText(
                    this,
                    "Nenhum usuário está logado.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String uid =
                usuarioAtual.getUid();

        // =========================
        // ATUALIZA OBJETO
        // =========================

        motoAtual.setApelido(apelido);
        motoAtual.setMarca(marca);
        motoAtual.setModelo(modelo);
        motoAtual.setAno(ano);
        motoAtual.setPlaca(placa);
        motoAtual.setQuilometragem(quilometragem);

        // =========================
        // SALVA NO SQLITE
        // =========================

        boolean atualizado =
                motoDao.atualizar(motoAtual);

        if (!atualizado) {

            Toast.makeText(
                    this,
                    "Não foi possível atualizar a motocicleta.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // =========================
        // DESABILITA BOTÃO
        // =========================

        btnSalvarAlteracoesMoto.setEnabled(false);

        // =========================
        // ENVIA PARA O FIREBASE
        // =========================

        databaseReference
                .child(uid)
                .child("motocicletas")
                .child(motoAtual.getId())
                .setValue(motoAtual)
                .addOnCompleteListener(task -> {

                    btnSalvarAlteracoesMoto.setEnabled(true);

                    if (task.isSuccessful()) {

                        // Firebase confirmou a alteração.
                        // Agora marcamos a moto como sincronizada.

                        motoDao.marcarComoSincronizada(
                                motoAtual.getId()
                        );

                        Toast.makeText(
                                this,
                                "Motocicleta atualizada com sucesso!",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                    } else {

                        /*
                         * A alteração continua salva no SQLite.
                         *
                         * Como MotoDao.atualizar()
                         * colocou sincronizado = 0,
                         * poderemos sincronizar posteriormente.
                         */

                        Toast.makeText(
                                this,
                                "Alteração salva no aparelho. "
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