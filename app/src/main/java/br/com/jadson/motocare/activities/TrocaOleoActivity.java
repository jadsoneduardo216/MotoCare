
package br.com.jadson.motocare.activities;

import android.app.DatePickerDialog;
import android.widget.ImageView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.TrocaOleoDao;

public class TrocaOleoActivity extends AppCompatActivity {

    private Button btnSelecionarData;
    private Button btnVerHistoricoOleo;
    private Button btnSalvarTrocaOleo;

    private EditText edtQuilometragem;
    private EditText edtIntervaloKm;

    private TextView txtDataSelecionada;
    private TextView txtPrevisaoKm;
    private TextView txtPrevisaoData;

    private Calendar dataSelecionada;

    private TrocaOleoDao trocaOleoDao;

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";

    private final SimpleDateFormat formatoData =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

    private final SimpleDateFormat formatoBanco =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_troca_oleo);
        ImageView btnVoltar = findViewById(R.id.btnVoltarTrocaOleo);
        btnVoltar.setOnClickListener(v -> finish());

        inicializarViews();

        trocaOleoDao = new TrocaOleoDao(this);

        configurarSelecionarData();
        configurarCalculos();
        configurarBotaoSalvar();
        btnVerHistoricoOleo.setOnClickListener(v -> {
            startActivity(
                    new android.content.Intent(
                            TrocaOleoActivity.this,
                            HistoricoTrocasOleoActivity.class
                    )
            );
        });
    }



    private void inicializarViews() {

        btnSelecionarData = findViewById(R.id.btnSelecionarData);
        btnSalvarTrocaOleo = findViewById(R.id.btnSalvarTrocaOleo);

        edtQuilometragem = findViewById(R.id.edtQuilometragem);
        edtIntervaloKm = findViewById(R.id.edtIntervaloKm);

        txtDataSelecionada = findViewById(R.id.txtDataSelecionada);
        txtPrevisaoKm = findViewById(R.id.txtPrevisaoKm);
        txtPrevisaoData = findViewById(R.id.txtPrevisaoData);
        btnVerHistoricoOleo = findViewById(R.id.btnVerHistoricoOleo);
    }

    private void configurarSelecionarData() {

        btnSelecionarData.setOnClickListener(v -> {

            Calendar hoje = Calendar.getInstance();

            DatePickerDialog seletorData = new DatePickerDialog(
                    TrocaOleoActivity.this,
                    (view, ano, mes, dia) -> {

                        dataSelecionada = Calendar.getInstance();

                        dataSelecionada.set(
                                ano, mes, dia, 0, 0, 0
                        );

                        dataSelecionada.set(
                                Calendar.MILLISECOND, 0
                        );

                        txtDataSelecionada.setText(
                                formatoData.format(dataSelecionada.getTime())
                        );

                        calcularPrevisoes();
                    },
                    hoje.get(Calendar.YEAR),
                    hoje.get(Calendar.MONTH),
                    hoje.get(Calendar.DAY_OF_MONTH)
            );

            seletorData.getDatePicker().setMaxDate(
                    System.currentTimeMillis()
            );

            seletorData.show();
        });
    }

    private void configurarCalculos() {

        TextWatcher observador = new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count
            ) {
                calcularPrevisoes();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        edtQuilometragem.addTextChangedListener(observador);
        edtIntervaloKm.addTextChangedListener(observador);
    }

    private void calcularPrevisoes() {

        String kmTexto = edtQuilometragem.getText().toString().trim();
        String intervaloTexto = edtIntervaloKm.getText().toString().trim();

        if (!kmTexto.isEmpty() && !intervaloTexto.isEmpty()) {

            try {

                long km = Long.parseLong(kmTexto);
                long intervalo = Long.parseLong(intervaloTexto);

                if (km >= 0 && intervalo > 0
                        && intervalo <= Integer.MAX_VALUE
                        && km <= Long.MAX_VALUE - intervalo) {

                    txtPrevisaoKm.setText(
                            "Próxima troca prevista: "
                                    + (km + intervalo) + " km"
                    );

                } else {

                    txtPrevisaoKm.setText(
                            "Informe valores válidos para calcular."
                    );
                }

            } catch (NumberFormatException e) {

                txtPrevisaoKm.setText("Valores inválidos.");
            }

        } else {

            txtPrevisaoKm.setText("Próxima troca prevista: -");
        }

        calcularPrevisaoData();
    }

    private void calcularPrevisaoData() {

        if (dataSelecionada == null) {

            txtPrevisaoData.setText(
                    "Vencimento por tempo: seis meses após a troca"
            );

            return;
        }

        Calendar vencimento = (Calendar) dataSelecionada.clone();
        vencimento.add(Calendar.MONTH, 6);

        txtPrevisaoData.setText(
                "Vencimento por tempo: "
                        + formatoData.format(vencimento.getTime())
        );
    }

    private void configurarBotaoSalvar() {

        btnSalvarTrocaOleo.setOnClickListener(v -> salvarTrocaOleo());
    }

    private void salvarTrocaOleo() {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {

            Toast.makeText(
                    this,
                    "Faça login para registrar a troca de óleo.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String idMoto = preferences.getString(KEY_MOTO_ATIVA, null);

        if (idMoto == null || idMoto.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Nenhuma motocicleta selecionada.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (dataSelecionada == null) {

            Toast.makeText(
                    this,
                    "Selecione a data da troca.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String kmTexto = edtQuilometragem.getText().toString().trim();
        String intervaloTexto = edtIntervaloKm.getText().toString().trim();

        if (kmTexto.isEmpty()) {
            edtQuilometragem.setError("Informe a quilometragem.");
            edtQuilometragem.requestFocus();
            return;
        }

        if (intervaloTexto.isEmpty()) {
            edtIntervaloKm.setError("Informe o intervalo em quilômetros.");
            edtIntervaloKm.requestFocus();
            return;
        }

        try {

            long km = Long.parseLong(kmTexto);
            long intervalo = Long.parseLong(intervaloTexto);

            if (km < 0 || intervalo <= 0
                    || intervalo > Integer.MAX_VALUE
                    || km > Long.MAX_VALUE - intervalo) {

                Toast.makeText(
                        this,
                        "Confira os valores informados.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String id = UUID.randomUUID().toString();
            String uidUsuario = usuario.getUid();

            String data = formatoBanco.format(
                    dataSelecionada.getTime()
            );

            TrocaOleo troca = new TrocaOleo(
                    id,
                    idMoto,
                    uidUsuario,
                    data,
                    kmTexto,
                    (int) intervalo,
                    false
            );

            boolean salvo = trocaOleoDao.inserir(troca);

            if (salvo) {

                Toast.makeText(
                        this,
                        "Troca de óleo registrada! Tentando sincronizar.",
                        Toast.LENGTH_LONG
                ).show();

                // Tenta enviar ao Firebase todas as trocas pendentes.
                // Usa o contexto da aplicação para não depender
                // do ciclo de vida desta tela.
                TrocaOleoSyncManager syncManager =
                        new TrocaOleoSyncManager(getApplicationContext());

                syncManager.sincronizarTrocasOleo(
                        uidUsuario,
                        (sincronizadas, falhas) -> {
                            // Os resultados serão utilizados futuramente
                            // para apresentar o status da sincronização.
                        }
                );

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Não foi possível salvar a troca de óleo.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Informe valores numéricos válidos.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (trocaOleoDao != null) {
            trocaOleoDao.fechar();
        }
    }
}
