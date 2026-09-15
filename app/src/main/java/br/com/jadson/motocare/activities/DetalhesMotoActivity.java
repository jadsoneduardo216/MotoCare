package br.com.jadson.motocare.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.MotoDao;

public class DetalhesMotoActivity extends AppCompatActivity {

    private ImageView btnVoltarDetalhes;

    private TextView txtApelidoMoto;
    private TextView txtMarcaModelo;
    private TextView txtAnoMoto;
    private TextView txtPlacaMoto;
    private TextView txtQuilometragemMoto;
    private TextView btnTrocarMoto;
    private TextView btnExcluirMoto;

    private MotoDao motoDao;

    private SharedPreferences preferences;

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalhes_moto);

        inicializarViews();

        motoDao = new MotoDao(this);

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        configurarBotoes();

        carregarMotocicleta();
    }

    /**
     * Inicializa os componentes da tela.
     */
    private void inicializarViews() {

        btnVoltarDetalhes =
                findViewById(R.id.btnVoltarDetalhes);

        txtApelidoMoto =
                findViewById(R.id.txtApelidoMoto);

        txtMarcaModelo =
                findViewById(R.id.txtMarcaModelo);

        txtAnoMoto =
                findViewById(R.id.txtAnoMoto);

        txtPlacaMoto =
                findViewById(R.id.txtPlacaMoto);

        txtQuilometragemMoto =
                findViewById(R.id.txtQuilometragemMoto);

        btnTrocarMoto =
                findViewById(R.id.btnTrocarMoto);

        btnExcluirMoto =
                findViewById(R.id.btnExcluirMoto);
    }

    /**
     * Configura os botões da tela.
     */
    private void configurarBotoes() {

        btnVoltarDetalhes.setOnClickListener(
                v -> finish()
        );

        btnTrocarMoto.setOnClickListener(
                v -> mostrarSeletorDeMotocicleta()
        );

        TextView btnEditarMoto =
                findViewById(R.id.btnEditarMoto);

        btnEditarMoto.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DetalhesMotoActivity.this,
                            EditarMotoActivity.class
                    );

            startActivity(intent);
        });

        btnExcluirMoto.setOnClickListener(
                v -> confirmarExclusao()
        );
    }

    /**
     * Exibe uma confirmação antes de excluir
     * a motocicleta.
     */
    private void confirmarExclusao() {

        new AlertDialog.Builder(this)
                .setTitle("Excluir motocicleta")
                .setMessage(
                        "Tem certeza que deseja excluir esta motocicleta?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Excluir",
                        (dialog, which) -> excluirMotocicleta()
                )
                .show();
    }

    /**
     * Exclui a motocicleta do banco local
     * e da nuvem.
     */
    private void excluirMotocicleta() {

        FirebaseUser usuarioAtual =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual == null) {

            Toast.makeText(
                    this,
                    "Usuário não autenticado.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uidUsuario =
                usuarioAtual.getUid();

        String idMotoAtiva =
                preferences.getString(
                        KEY_MOTO_ATIVA,
                        null
                );

        if (idMotoAtiva == null
                || idMotoAtiva.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Não foi possível identificar a motocicleta.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Primeiro excluímos do banco local.
         */
        boolean excluidaLocalmente =
                motoDao.excluir(idMotoAtiva);

        if (!excluidaLocalmente) {

            Toast.makeText(
                    this,
                    "Não foi possível excluir a motocicleta.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Verificamos se ainda existem outras
         * motocicletas cadastradas.
         */
        List<Motocicleta> motosRestantes =
                motoDao.listarPorUsuario(uidUsuario);

        if (!motosRestantes.isEmpty()) {

            /*
             * Escolhe automaticamente a primeira
             * motocicleta restante como ativa.
             */
            Motocicleta novaMotoAtiva =
                    motosRestantes.get(0);

            preferences
                    .edit()
                    .putString(
                            KEY_MOTO_ATIVA,
                            novaMotoAtiva.getId()
                    )
                    .apply();

        } else {

            /*
             * Não existem mais motocicletas.
             */
            preferences
                    .edit()
                    .remove(KEY_MOTO_ATIVA)
                    .apply();
        }

        /*
         * Agora excluímos a motocicleta do Firebase.
         */
        DatabaseReference referenciaMoto =
                FirebaseDatabase
                        .getInstance()
                        .getReference("usuarios")
                        .child(uidUsuario)
                        .child("motocicletas")
                        .child(idMotoAtiva);

        referenciaMoto.removeValue()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                DetalhesMotoActivity.this,
                                "Motocicleta excluída com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                DetalhesMotoActivity.this,
                                "Motocicleta removida do aparelho. "
                                        + "A sincronização com a nuvem "
                                        + "será concluída quando houver conexão.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    /*
                     * Volta para a Home.
                     */
                    Intent intent =
                            new Intent(
                                    DetalhesMotoActivity.this,
                                    MainActivity.class
                            );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );

                    startActivity(intent);

                    finish();
                });
    }

    /**
     * Busca a motocicleta atualmente selecionada
     * como ativa na Home.
     */
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

        Motocicleta moto =
                motoDao.buscarPorId(idMotoAtiva);

        if (moto == null) {

            Toast.makeText(
                    this,
                    "Não foi possível encontrar a motocicleta.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        mostrarDadosMotocicleta(moto);
    }

    /**
     * Exibe os dados da motocicleta na tela.
     */
    private void mostrarDadosMotocicleta(
            Motocicleta moto
    ) {

        txtApelidoMoto.setText(
                moto.getApelido()
        );

        txtMarcaModelo.setText(
                moto.getMarca()
                        + " "
                        + moto.getModelo()
        );

        txtAnoMoto.setText(
                moto.getAno()
        );

        txtPlacaMoto.setText(
                moto.getPlaca()
        );

        txtQuilometragemMoto.setText(
                moto.getQuilometragem()
                        + " km"
        );
    }

    /**
     * Exibe o seletor de motocicletas.
     */
    private void mostrarSeletorDeMotocicleta() {

        FirebaseUser usuarioAtual =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual == null) {

            Toast.makeText(
                    this,
                    "Usuário não autenticado.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uidUsuario =
                usuarioAtual.getUid();

        List<Motocicleta> motos =
                motoDao.listarPorUsuario(uidUsuario);

        if (motos.isEmpty()) {

            Toast.makeText(
                    this,
                    "Nenhuma motocicleta cadastrada.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] nomesMotos =
                new String[motos.size()];

        String idMotoAtual =
                preferences.getString(
                        KEY_MOTO_ATIVA,
                        null
                );

        int motoSelecionada = 0;

        for (int i = 0; i < motos.size(); i++) {

            Motocicleta moto =
                    motos.get(i);

            nomesMotos[i] =
                    moto.getApelido()
                            + "\n"
                            + moto.getMarca()
                            + " "
                            + moto.getModelo()
                            + " • "
                            + moto.getAno();

            if (idMotoAtual != null
                    && idMotoAtual.equals(moto.getId())) {

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

        dialog.setOnShowListener(
                dialogInterface -> {

                    android.widget.ListView listView =
                            dialog.getListView();

                    listView.setOnItemClickListener(
                            (parent, view, position, id) -> {

                                Motocicleta motoEscolhida =
                                        motos.get(position);

                                preferences
                                        .edit()
                                        .putString(
                                                KEY_MOTO_ATIVA,
                                                motoEscolhida.getId()
                                        )
                                        .apply();

                                mostrarDadosMotocicleta(
                                        motoEscolhida
                                );

                                dialog.dismiss();

                                Toast.makeText(
                                        DetalhesMotoActivity.this,
                                        "Motocicleta selecionada: "
                                                + motoEscolhida.getApelido(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                    );
                }
        );

        dialog.show();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (motoDao != null) {

            motoDao.fechar();
        }
    }
}