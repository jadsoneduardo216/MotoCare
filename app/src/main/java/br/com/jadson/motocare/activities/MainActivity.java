package br.com.jadson.motocare.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.MotoDao;

public class MainActivity extends AppCompatActivity {

    private TextView btnAdicionarMoto;

    private TextView txtSaudacao;
    private TextView txtNenhumaMoto;
    private TextView txtDescricaoMoto;

    private LinearLayout cardMotocicleta;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usuariosReference;

    private MotoDao motoDao;

    private List<Motocicleta> motosUsuario;

    private SharedPreferences preferences;

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";

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

        firebaseAuth = FirebaseAuth.getInstance();

        usuariosReference = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");

        motoDao = new MotoDao(this);

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        configurarBotoes();

        carregarNomeUsuario();

        carregarMotocicleta();
    }

    private void inicializarViews() {

        txtSaudacao = findViewById(R.id.txtSaudacao);

        txtNenhumaMoto = findViewById(R.id.txtNenhumaMoto);

        txtDescricaoMoto = findViewById(R.id.txtDescricaoMoto);

        btnAdicionarMoto = findViewById(R.id.btnAdicionarMoto);

        cardMotocicleta = findViewById(R.id.cardMotocicleta);
    }

    private void configurarBotoes() {

        btnAdicionarMoto.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    CadastroMotoActivity.class
            );

            startActivity(intent);
        });

        /*
         * Ao tocar no card da motocicleta,
         * o usuário poderá escolher outra moto.
         */
        cardMotocicleta.setOnClickListener(v -> {

            if (motosUsuario != null && !motosUsuario.isEmpty()) {

                mostrarSeletorDeMotocicleta();

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Cadastre uma motocicleta primeiro.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    /**
     * Carrega o nome do usuário atualmente autenticado.
     */
    private void carregarNomeUsuario() {

        FirebaseUser usuarioAtual =
                firebaseAuth.getCurrentUser();

        if (usuarioAtual == null) {

            txtSaudacao.setText("Olá!");

            return;
        }

        String uid = usuarioAtual.getUid();

        usuariosReference
                .child(uid)
                .child("nome")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {

                                String nome =
                                        snapshot.getValue(String.class);

                                if (nome != null
                                        && !nome.trim().isEmpty()) {

                                    txtSaudacao.setText(
                                            "Olá, " + nome + "!"
                                    );

                                } else {

                                    txtSaudacao.setText("Olá!");
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {

                                txtSaudacao.setText("Olá!");

                                Toast.makeText(
                                        MainActivity.this,
                                        "Não foi possível carregar o nome do usuário.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    /**
     * Carrega todas as motocicletas do usuário.
     *
     * Depois identifica qual delas está marcada
     * como motocicleta ativa.
     */
    private void carregarMotocicleta() {

        FirebaseUser usuarioAtual =
                firebaseAuth.getCurrentUser();

        if (usuarioAtual == null) {

            motosUsuario = null;

            mostrarSemMotocicleta();

            return;
        }

        String uid = usuarioAtual.getUid();

        motosUsuario =
                motoDao.listarPorUsuario(uid);

        if (motosUsuario.isEmpty()) {

            mostrarSemMotocicleta();

            return;
        }

        String idMotoAtiva =
                preferences.getString(
                        KEY_MOTO_ATIVA,
                        null
                );

        Motocicleta motoAtiva = null;

        /*
         * Primeiro tentamos encontrar a moto
         * que o usuário escolheu anteriormente.
         */
        if (idMotoAtiva != null) {

            for (Motocicleta moto : motosUsuario) {

                if (idMotoAtiva.equals(moto.getId())) {

                    motoAtiva = moto;

                    break;
                }
            }
        }

        /*
         * Se não houver uma moto selecionada,
         * usamos a primeira cadastrada.
         */
        if (motoAtiva == null) {

            motoAtiva = motosUsuario.get(0);

            salvarMotoAtiva(motoAtiva);
        }

        mostrarMotocicleta(motoAtiva);
    }

    /**
     * Abre a caixa de seleção das motocicletas.
     */
    private void mostrarSeletorDeMotocicleta() {

        if (motosUsuario == null
                || motosUsuario.isEmpty()) {

            return;
        }

        String[] nomesMotos =
                new String[motosUsuario.size()];

        String idMotoAtiva =
                preferences.getString(
                        KEY_MOTO_ATIVA,
                        null
                );

        int motoSelecionada = 0;

        for (int i = 0; i < motosUsuario.size(); i++) {

            Motocicleta moto = motosUsuario.get(i);

            nomesMotos[i] =
                    moto.getApelido()
                            + "\n"
                            + moto.getMarca()
                            + " "
                            + moto.getModelo()
                            + " • "
                            + moto.getAno();

            if (idMotoAtiva != null
                    && idMotoAtiva.equals(moto.getId())) {

                motoSelecionada = i;
            }
        }

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Selecionar motocicleta")
                        .setSingleChoiceItems(
                                nomesMotos,
                                motoSelecionada,
                                null
                        )
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .create();

        dialog.setOnShowListener(dialogInterface -> {

            android.widget.ListView listView =
                    dialog.getListView();

            listView.setOnItemClickListener(
                    (parent, view, position, id) -> {

                        Motocicleta motoEscolhida =
                                motosUsuario.get(position);

                        salvarMotoAtiva(motoEscolhida);

                        mostrarMotocicleta(
                                motoEscolhida
                        );

                        dialog.dismiss();

                        Toast.makeText(
                                MainActivity.this,
                                "Motocicleta selecionada: "
                                        + motoEscolhida.getApelido(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );
        });

        dialog.show();
    }

    /**
     * Salva o ID da motocicleta atualmente ativa.
     */
    private void salvarMotoAtiva(Motocicleta moto) {

        if (moto == null
                || moto.getId() == null
                || moto.getId().trim().isEmpty()) {

            return;
        }

        preferences
                .edit()
                .putString(
                        KEY_MOTO_ATIVA,
                        moto.getId()
                )
                .apply();
    }

    /**
     * Exibe o estado da Home quando não existe
     * nenhuma motocicleta cadastrada.
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
     * Exibe os dados da motocicleta ativa.
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

    /**
     * Atualiza a motocicleta exibida quando
     * retornamos da tela de cadastro.
     */
    @Override
    protected void onResume() {

        super.onResume();

        if (motoDao != null) {

            carregarMotocicleta();
        }
    }

    /**
     * Fecha corretamente a conexão do SQLite.
     */
    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (motoDao != null) {

            motoDao.fechar();
        }
    }
}