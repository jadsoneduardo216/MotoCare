package br.com.jadson.motocare.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    }

    /**
     * Configura os botões da tela.
     */
    private void configurarBotoes() {

        btnVoltarDetalhes.setOnClickListener(v -> finish());

        btnTrocarMoto.setOnClickListener(
                v -> mostrarSeletorDeMotocicleta()
        );
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

        java.util.List<Motocicleta> motos =
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

            Motocicleta moto = motos.get(i);

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

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this)
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