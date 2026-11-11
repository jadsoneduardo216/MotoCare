package br.com.jadson.motocare.activities;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.MotoDao;
import br.com.jadson.motocare.dao.TrocaOleoDao;

public class HistoricoTrocasOleoActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";

    private TextView txtKmAtual;
    private TextView txtSituacaoAtual;
    private TextView txtHistoricoVazio;

    private LinearLayout containerHistorico;

    private TrocaOleoDao trocaOleoDao;
    private MotoDao motoDao;

    private final SimpleDateFormat formatoBanco =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final SimpleDateFormat formatoExibicao =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_trocas_oleo);

        ImageView btnVoltar = findViewById(R.id.btnVoltarHistoricoOleo);
        btnVoltar.setOnClickListener(v -> finish());

        txtKmAtual = findViewById(R.id.txtKmAtual);
        txtSituacaoAtual = findViewById(R.id.txtSituacaoAtual);
        txtHistoricoVazio = findViewById(R.id.txtHistoricoVazio);
        containerHistorico = findViewById(R.id.containerHistorico);

        trocaOleoDao = new TrocaOleoDao(this);
        motoDao = new MotoDao(this);

        carregarHistorico();
    }

    private void carregarHistorico() {

        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String idMoto = preferences.getString(KEY_MOTO_ATIVA, null);

        if (idMoto == null || idMoto.trim().isEmpty()) {
            txtKmAtual.setText("Nenhuma motocicleta selecionada.");
            txtSituacaoAtual.setText("");
            txtHistoricoVazio.setVisibility(View.VISIBLE);
            return;
        }

        Motocicleta moto = motoDao.buscarPorId(idMoto);

        if (moto == null) {
            txtKmAtual.setText("Não foi possível localizar a motocicleta.");
            txtSituacaoAtual.setText("");
            txtHistoricoVazio.setVisibility(View.VISIBLE);
            return;
        }

        String kmAtualTexto = moto.getQuilometragem();

        txtKmAtual.setText(
                "Quilometragem atual: " + kmAtualTexto + " km"
        );

        List<TrocaOleo> trocas =
                trocaOleoDao.listarPorMoto(idMoto);

        containerHistorico.removeAllViews();

        if (trocas.isEmpty()) {
            txtSituacaoAtual.setText("Situação da troca: sem registros");
            txtHistoricoVazio.setVisibility(View.VISIBLE);
            return;
        }

        txtHistoricoVazio.setVisibility(View.GONE);

        try {
            long kmAtual = Long.parseLong(kmAtualTexto);
            atualizarSituacaoAtual(trocas.get(0), kmAtual);
        } catch (NumberFormatException e) {
            txtSituacaoAtual.setText(
                    "Não foi possível calcular a situação da troca."
            );
        }

        for (TrocaOleo troca : trocas) {
            adicionarCartaoHistorico(troca);
        }
    }

    private void atualizarSituacaoAtual(
            TrocaOleo troca,
            long kmAtual
    ) {

        try {

            long kmTroca = Long.parseLong(troca.getQuilometragem());
            long proximaTrocaKm = kmTroca + troca.getIntervaloKm();

            Calendar vencimento = obterDataVencimento(troca.getData());
            Calendar hoje = Calendar.getInstance();

            boolean vencidaPorKm = kmAtual >= proximaTrocaKm;

            boolean vencidaPorData =
                    !hoje.before(vencimento);

            if (vencidaPorKm || vencidaPorData) {

                String motivo = vencidaPorKm
                        ? "quilometragem atingida"
                        : "prazo de seis meses atingido";

                txtSituacaoAtual.setText(
                        "Situação da troca: VENCIDA (" + motivo + ")."
                );

                txtSituacaoAtual.setTextColor(
                        getResources().getColor(R.color.error)
                );

            } else {

                txtSituacaoAtual.setText(
                        "Situação da troca: EM DIA."
                );

                txtSituacaoAtual.setTextColor(
                        getResources().getColor(R.color.success)
                );
            }

        } catch (Exception e) {

            txtSituacaoAtual.setText(
                    "Não foi possível calcular a situação da troca."
            );
        }
    }


    private void adicionarCartaoHistorico(TrocaOleo troca) {

        LinearLayout cartao = new LinearLayout(this);

        cartao.setOrientation(LinearLayout.VERTICAL);
        cartao.setPadding(18, 18, 18, 18);

        android.graphics.drawable.GradientDrawable fundo =
                new android.graphics.drawable.GradientDrawable();

        fundo.setColor(getResources().getColor(R.color.primary_dark));
        fundo.setCornerRadius(20f);
        fundo.setStroke(
                2,
                getResources().getColor(R.color.success)
        );

        cartao.setBackground(fundo);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, 16);

        cartao.setLayoutParams(parametros);

        String dataFormatada = formatarData(troca.getData());

        long kmTroca = Long.parseLong(troca.getQuilometragem());

        long proximaTrocaKm =
                kmTroca + troca.getIntervaloKm();

        String previsaoData;

        try {

            Calendar vencimento =
                    obterDataVencimento(troca.getData());

            previsaoData = "Previsão por tempo: "
                    + formatoExibicao.format(vencimento.getTime());

        } catch (ParseException e) {

            previsaoData = "Previsão por tempo indisponível.";
        }

        adicionarTexto(
                cartao,
                "Troca realizada em " + dataFormatada,
                17,
                true
        );

        adicionarTexto(
                cartao,
                "Quilometragem: " + kmTroca + " km",
                15,
                false
        );

        adicionarTexto(
                cartao,
                "Intervalo definido: "
                        + troca.getIntervaloKm() + " km",
                15,
                false
        );

        adicionarTexto(
                cartao,
                "Próxima troca: " + proximaTrocaKm + " km",
                15,
                true
        );

        adicionarTexto(
                cartao,
                previsaoData,
                14,
                false
        );

        containerHistorico.addView(cartao);
    }


    private void adicionarTexto(
            LinearLayout container,
            String texto,
            int tamanho,
            boolean negrito
    ) {

        TextView textView = new TextView(this);

        textView.setText(texto);
        textView.setTextSize(tamanho);
        textView.setTextColor(
                getResources().getColor(R.color.white)
        );

        if (negrito) {
            textView.setTypeface(null, Typeface.BOLD);
        }

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, 8);

        textView.setLayoutParams(parametros);

        container.addView(textView);
    }

    private Calendar obterDataVencimento(String data) throws ParseException {

        Date dataTroca = formatoBanco.parse(data);

        Calendar vencimento = Calendar.getInstance();
        vencimento.setTime(dataTroca);
        vencimento.add(Calendar.MONTH, 6);

        return vencimento;
    }

    private String formatarData(String data) {

        try {
            Date dataConvertida = formatoBanco.parse(data);
            return formatoExibicao.format(dataConvertida);

        } catch (ParseException e) {
            return data;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (trocaOleoDao != null) {
            trocaOleoDao.fechar();
        }

        if (motoDao != null) {
            motoDao.fechar();
        }
    }
}
