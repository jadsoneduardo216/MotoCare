package br.com.jadson.motocare.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.ManutencaoDao;
import br.com.jadson.motocare.dao.MotoDao;

public class CadastroManutencaoActivity extends AppCompatActivity {

    private TextView txtMotoManutencao;
    private TextView txtInfoMotoManutencao;

    private Spinner spinnerTipoManutencao;

    private EditText editDescricaoManutencao;
    private EditText editDataManutencao;
    private EditText editQuilometragemManutencao;
    private EditText editValorManutencao;

    private MotoDao motoDao;
    private ManutencaoDao manutencaoDao;

    private SharedPreferences preferences;

    private FirebaseAuth firebaseAuth;

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastro_manutencao);

        inicializarViews();

        motoDao = new MotoDao(this);
        manutencaoDao = new ManutencaoDao(this);

        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        firebaseAuth = FirebaseAuth.getInstance();

        configurarBotaoVoltar();
        configurarSpinnerTipoManutencao();
        configurarSeletorDeData();
        configurarBotaoSalvar();

        carregarMotocicleta();
    }

    private void inicializarViews() {

        txtMotoManutencao =
                findViewById(R.id.txtMotoManutencao);

        txtInfoMotoManutencao =
                findViewById(R.id.txtInfoMotoManutencao);

        spinnerTipoManutencao =
                findViewById(R.id.spinnerTipoManutencao);

        editDescricaoManutencao =
                findViewById(R.id.editDescricaoManutencao);

        editDataManutencao =
                findViewById(R.id.editDataManutencao);

        editQuilometragemManutencao =
                findViewById(R.id.editQuilometragemManutencao);

        editValorManutencao =
                findViewById(R.id.editValorManutencao);
    }

    private void configurarBotaoVoltar() {

        findViewById(R.id.btnVoltarManutencao)
                .setOnClickListener(v -> finish());
    }

    private void configurarSpinnerTipoManutencao() {

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.tipos_manutencao,
                        R.layout.item_spinner_tipo
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTipoManutencao.setAdapter(adapter);
        spinnerTipoManutencao.setSelection(0);
    }

    private void configurarSeletorDeData() {

        editDataManutencao.setOnClickListener(v -> {

            Calendar calendario = Calendar.getInstance();

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            this,
                            (view, year, month, dayOfMonth) -> {

                                String dataExibicao =
                                        String.format(
                                                Locale.getDefault(),
                                                "%02d/%02d/%04d",
                                                dayOfMonth,
                                                month + 1,
                                                year
                                        );

                                editDataManutencao.setText(
                                        dataExibicao
                                );
                            },
                            calendario.get(Calendar.YEAR),
                            calendario.get(Calendar.MONTH),
                            calendario.get(Calendar.DAY_OF_MONTH)
                    );

            datePickerDialog.show();
        });
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

    private void mostrarDadosMotocicleta(Motocicleta moto) {

        txtMotoManutencao.setText(moto.getApelido());

        txtInfoMotoManutencao.setText(
                moto.getMarca()
                        + " "
                        + moto.getModelo()
                        + " • "
                        + moto.getAno()
        );
    }

    private void configurarBotaoSalvar() {

        findViewById(R.id.btnSalvarManutencao)
                .setOnClickListener(v -> salvarManutencao());
    }

    private void salvarManutencao() {

        FirebaseUser usuario =
                firebaseAuth.getCurrentUser();

        if (usuario == null) {

            Toast.makeText(
                    this,
                    "Sua sessão expirou. Entre novamente.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String idMoto =
                preferences.getString(KEY_MOTO_ATIVA, null);

        if (idMoto == null || idMoto.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Selecione uma motocicleta antes de continuar.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int posicaoTipo = spinnerTipoManutencao.getSelectedItemPosition();

        if (posicaoTipo == 0) {

            Toast.makeText(
                    this,
                    "Selecione o tipo de manutenção.",
                    Toast.LENGTH_SHORT
            ).show();

            spinnerTipoManutencao.performClick();
            return;
        }

        String tipo =
                spinnerTipoManutencao.getSelectedItem().toString();

        String descricao =
                editDescricaoManutencao.getText().toString().trim();

        String dataDigitada =
                editDataManutencao.getText().toString().trim();

        String quilometragem =
                editQuilometragemManutencao.getText().toString().trim();

        String valorDigitado =
                editValorManutencao.getText().toString().trim();

        if (TextUtils.isEmpty(dataDigitada)) {

            editDataManutencao.setError("Informe a data");
            editDataManutencao.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(quilometragem)) {

            editQuilometragemManutencao.setError(
                    "Informe a quilometragem"
            );

            editQuilometragemManutencao.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(valorDigitado)) {

            editValorManutencao.setError("Informe o valor");

            editValorManutencao.requestFocus();
            return;
        }

        String dataBanco;

        try {

            SimpleDateFormat formatoExibicao =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            formatoExibicao.setLenient(false);

            Date data = formatoExibicao.parse(dataDigitada);

            if (data == null) {
                throw new ParseException("Data inválida", 0);
            }

            SimpleDateFormat formatoBanco =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            dataBanco = formatoBanco.format(data);

        } catch (ParseException e) {

            editDataManutencao.setError(
                    "Selecione uma data válida"
            );

            editDataManutencao.requestFocus();
            return;
        }

        String valorNormalizado =
                valorDigitado.replace(",", ".");

        try {

            double valorNumerico =
                    Double.parseDouble(valorNormalizado);

            if (!Double.isFinite(valorNumerico)
                    || valorNumerico < 0) {

                editValorManutencao.setError(
                        "Informe um valor válido"
                );

                editValorManutencao.requestFocus();
                return;
            }

            valorNormalizado =
                    String.format(Locale.US, "%.2f", valorNumerico);

        } catch (NumberFormatException e) {

            editValorManutencao.setError(
                    "Informe um valor válido"
            );

            editValorManutencao.requestFocus();
            return;
        }

        try {

            long quilometragemNumerica =
                    Long.parseLong(quilometragem);

            if (quilometragemNumerica < 0) {

                editQuilometragemManutencao.setError(
                        "A quilometragem não pode ser negativa"
                );

                editQuilometragemManutencao.requestFocus();
                return;
            }

        } catch (NumberFormatException e) {

            editQuilometragemManutencao.setError(
                    "Informe uma quilometragem válida"
            );

            editQuilometragemManutencao.requestFocus();
            return;
        }

        Manutencao manutencao = new Manutencao(
                UUID.randomUUID().toString(),
                idMoto,
                usuario.getUid(),
                tipo,
                descricao,
                dataBanco,
                quilometragem,
                valorNormalizado,
                false
        );

        boolean salvo = manutencaoDao.inserir(manutencao);

        if (salvo) {

            Toast.makeText(
                    this,
                    "Manutenção salva com sucesso!",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Não foi possível salvar a manutenção.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (motoDao != null) {
            motoDao.fechar();
        }

        if (manutencaoDao != null) {
            manutencaoDao.fechar();
        }
    }
}