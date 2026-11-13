
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

    // Botões da manutenção rápida
    private LinearLayout btnManutencao;
    private LinearLayout btnTrocaOleo;
    private LinearLayout btnAlertas;
    private LinearLayout btnHistorico;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usuariosReference;

    private MotoDao motoDao;
    private List<Motocicleta> motosUsuario;
    private SharedPreferences preferences;

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";

    private void sincronizarMotosPendentes() {

        FirebaseUser usuarioAtual =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual == null) {
            return;
        }

        MotoSyncManager syncManager =
                new MotoSyncManager(this);

        syncManager.sincronizarMotos(usuarioAtual.getUid());
    }

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

        // Primeiro inicializamos as referências do XML.
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

        // Só configuramos os cliques depois de inicializar as Views.
        configurarBotoes();

        sincronizarMotosPendentes();
        carregarNomeUsuario();
        carregarMotocicleta();
    }

    private void inicializarViews() {

        txtSaudacao = findViewById(R.id.txtSaudacao);
        txtNenhumaMoto = findViewById(R.id.txtNenhumaMoto);
        txtDescricaoMoto = findViewById(R.id.txtDescricaoMoto);

        btnAdicionarMoto = findViewById(R.id.btnAdicionarMoto);
        cardMotocicleta = findViewById(R.id.cardMotocicleta);

        // IDs dos quatro botões presentes no activity_main.xml
        btnManutencao = findViewById(R.id.btnManutencao);
        btnTrocaOleo = findViewById(R.id.btnTrocaOleo);
        btnAlertas = findViewById(R.id.btnAlertas);
        btnHistorico = findViewById(R.id.btnHistorico);
    }

    private void configurarBotoes() {

        // Cadastrar outra motocicleta.
        btnAdicionarMoto.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    CadastroMotoActivity.class
            );

            startActivity(intent);
        });

        // Abrir os detalhes e a seleção de motocicletas.
        cardMotocicleta.setOnClickListener(v -> {

            if (motosUsuario != null && !motosUsuario.isEmpty()) {

                Intent intent = new Intent(
                        MainActivity.this,
                        DetalhesMotoActivity.class
                );

                startActivity(intent);

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Cadastre uma motocicleta primeiro.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Histórico de manutenções.
        btnHistorico.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    HistoricoManutencoesActivity.class
            );

            startActivity(intent);
        });


        btnManutencao.setOnClickListener(v -> {

            if (motosUsuario == null || motosUsuario.isEmpty()) {
                Toast.makeText(
                        MainActivity.this,
                        "Cadastre uma motocicleta primeiro.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    MainActivity.this,
                    RelatorioManutencoesActivity.class
            );

            startActivity(intent);
        });

        btnTrocaOleo.setOnClickListener(v -> {

            if (motosUsuario == null || motosUsuario.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        "Cadastre uma motocicleta primeiro.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    MainActivity.this,
                    TrocaOleoActivity.class
            );

            startActivity(intent);
        });

        btnAlertas.setOnClickListener(v -> {

            if (motosUsuario == null || motosUsuario.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        "Cadastre uma motocicleta primeiro.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent = new Intent(
                    MainActivity.this,
                    AlertasActivity.class
            );

            startActivity(intent);
        });
    }

    /**
     * Carrega o nome do usuário autenticado.
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
     * Carrega as motocicletas do usuário e identifica
     * qual está selecionada como ativa.
     */
    private void carregarMotocicleta() {

        FirebaseUser usuarioAtual =
                firebaseAuth.getCurrentUser();

        if (usuarioAtual == null) {

            motosUsuario = null;

            preferences.edit()
                    .remove(KEY_MOTO_ATIVA)
                    .apply();

            mostrarSemMotocicleta();
            return;
        }

        String uid = usuarioAtual.getUid();

        motosUsuario = motoDao.listarPorUsuario(uid);

        if (motosUsuario.isEmpty()) {

            preferences.edit()
                    .remove(KEY_MOTO_ATIVA)
                    .apply();

            mostrarSemMotocicleta();
            return;
        }

        String idMotoAtiva = preferences.getString(
                KEY_MOTO_ATIVA,
                null
        );

        Motocicleta motoAtiva = null;

        if (idMotoAtiva != null) {

            for (Motocicleta moto : motosUsuario) {

                if (idMotoAtiva.equals(moto.getId())) {
                    motoAtiva = moto;
                    break;
                }
            }
        }

        if (motoAtiva == null) {

            motoAtiva = motosUsuario.get(0);
            salvarMotoAtiva(motoAtiva);
        }

        mostrarMotocicleta(motoAtiva);
    }

    /**
     * Exibe o seletor de motocicletas cadastradas.
     */
    private void mostrarSeletorDeMotocicleta() {

        if (motosUsuario == null || motosUsuario.isEmpty()) {
            return;
        }

        String[] nomesMotos = new String[motosUsuario.size()];

        String idMotoAtiva = preferences.getString(
                KEY_MOTO_ATIVA,
                null
        );

        int motoSelecionada = 0;

        for (int i = 0; i < motosUsuario.size(); i++) {

            Motocicleta moto = motosUsuario.get(i);

            nomesMotos[i] = moto.getApelido()
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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Selecionar motocicleta")
                .setSingleChoiceItems(
                        nomesMotos,
                        motoSelecionada,
                        null
                )
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            android.widget.ListView listView =
                    dialog.getListView();

            listView.setOnItemClickListener(
                    (parent, view, position, id) -> {

                        Motocicleta motoEscolhida =
                                motosUsuario.get(position);

                        salvarMotoAtiva(motoEscolhida);
                        mostrarMotocicleta(motoEscolhida);
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
     * Salva o ID da motocicleta ativa.
     */
    private void salvarMotoAtiva(Motocicleta moto) {

        if (moto == null
                || moto.getId() == null
                || moto.getId().trim().isEmpty()) {
            return;
        }

        preferences.edit()
                .putString(KEY_MOTO_ATIVA, moto.getId())
                .apply();
    }

    /**
     * Exibe a tela inicial quando não há motocicletas.
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

        txtNenhumaMoto.setText(moto.getApelido());

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
     * Atualiza os dados ao retornar à tela principal.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (motoDao != null) {
            carregarMotocicleta();
        }
    }

    /**
     * Fecha a conexão com o SQLite.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (motoDao != null) {
            motoDao.fechar();
        }
    }
}
