package br.com.jadson.motocare.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.ManutencaoDao;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class RelatorioManutencoesActivity extends AppCompatActivity {

    private Spinner spinnerAno;
    private TextView txtTotalAnual;
    private TextView txtQuantidadeManutencoes;
    private TextView txtMediaMensal;
    private LinearLayout layoutGrafico;

    private ManutencaoDao manutencaoDao;
    private List<Manutencao> manutencoes;

    private String idMoto;
    private int anoSelecionado;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final String[] meses = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio_manutencoes);
        ImageView btnVoltar = findViewById(R.id.btnVoltarRelatorio);
        btnVoltar.setOnClickListener(v -> finish());

        inicializarComponentes();

        manutencaoDao = new ManutencaoDao(this);

        carregarDados();
    }

    private void inicializarComponentes() {
        spinnerAno = findViewById(R.id.spinnerAno);
        txtTotalAnual = findViewById(R.id.txtTotalAnual);
        txtQuantidadeManutencoes = findViewById(R.id.txtQuantidadeManutencoes);
        txtMediaMensal = findViewById(R.id.txtMediaMensal);
        layoutGrafico = findViewById(R.id.layoutGrafico);
    }

    private void carregarDados() {
        idMoto = getSharedPreferences(
                "MotoCarePrefs",
                MODE_PRIVATE
        ).getString("moto_ativa", null);

        if (idMoto == null || idMoto.isEmpty()) {
            txtTotalAnual.setText(formatoMoeda.format(0));
            txtQuantidadeManutencoes.setText("0");
            txtMediaMensal.setText(formatoMoeda.format(0));
            return;
        }

        manutencoes = manutencaoDao.listarPorMoto(idMoto);

        if (manutencoes == null) {
            manutencoes = new ArrayList<>();
        }

        configurarFiltroAno();
    }

    private void configurarFiltroAno() {
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);

        TreeSet<Integer> anosDisponiveis = new TreeSet<>();
        anosDisponiveis.add(anoAtual);

        for (Manutencao manutencao : manutencoes) {
            if (manutencao.getData() != null && manutencao.getData().length() >= 4) {
                try {
                    int ano = Integer.parseInt(manutencao.getData().substring(0, 4));
                    anosDisponiveis.add(ano);
                } catch (NumberFormatException ignored) {
                    // Ignora datas com ano inválido.
                }
            }
        }

        List<Integer> listaAnos = new ArrayList<>(anosDisponiveis);
        Collections.sort(listaAnos, Collections.reverseOrder());

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                listaAnos
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAno.setAdapter(adapter);

        anoSelecionado = anoAtual;

        spinnerAno.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        anoSelecionado = (Integer) parent.getItemAtPosition(position);
                        atualizarRelatorio();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );
    }

    private void atualizarRelatorio() {
        double totalAnual = 0;
        int quantidade = 0;

        double[] gastosMensais = new double[12];

        for (Manutencao manutencao : manutencoes) {
            String data = manutencao.getData();

            if (data == null || data.length() < 7) {
                continue;
            }

            try {
                int ano = Integer.parseInt(data.substring(0, 4));
                int mes = Integer.parseInt(data.substring(5, 7));

                if (ano != anoSelecionado || mes < 1 || mes > 12) {
                    continue;
                }

                quantidade++;
                double valor = converterValor(manutencao.getValor());

                totalAnual += valor;
                gastosMensais[mes - 1] += valor;

            } catch (NumberFormatException ignored) {
                // Ignora registros com data inválida.
            }
        }

        double mediaMensal = totalAnual / 12.0;

        txtTotalAnual.setText(formatoMoeda.format(totalAnual));
        txtQuantidadeManutencoes.setText(String.valueOf(quantidade));
        txtMediaMensal.setText(formatoMoeda.format(mediaMensal));

        montarGrafico(gastosMensais);
    }

    private double converterValor(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }

        try {
            return Double.parseDouble(valor.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void montarGrafico(double[] gastosMensais) {
        layoutGrafico.removeAllViews();

        layoutGrafico.setOrientation(LinearLayout.VERTICAL);
        layoutGrafico.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        double maiorValor = 0;
        for (double valor : gastosMensais) {
            if (valor > maiorValor) {
                maiorValor = valor;
            }
        }

        int larguraMaximaBarraPx = dpParaPx(150);

        for (int i = 0; i < 12; i++) {
            // Container para cada mês (Linha: [Mês] [Barra] [Valor])
            LinearLayout linhaMes = new LinearLayout(this);
            linhaMes.setOrientation(LinearLayout.HORIZONTAL);
            linhaMes.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams paramsLinha =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
            paramsLinha.bottomMargin = dpParaPx(8);
            linhaMes.setLayoutParams(paramsLinha);

            // 1. Nome do mês
            TextView txtMes = new TextView(this);
            txtMes.setText(meses[i]);
            txtMes.setTextColor(Color.WHITE);
            txtMes.setTextSize(12);
            txtMes.setGravity(Gravity.START);

            LinearLayout.LayoutParams paramsMes =
                    new LinearLayout.LayoutParams(
                            dpParaPx(35),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
            txtMes.setLayoutParams(paramsMes);
            linhaMes.addView(txtMes);

            // 2. Barra horizontal proporcional ao valor
            View barra = new View(this);
            int larguraBarra = 0;

            if (maiorValor > 0) {
                larguraBarra = (int) ((gastosMensais[i] / maiorValor) * larguraMaximaBarraPx);
            }

            if (gastosMensais[i] > 0 && larguraBarra < dpParaPx(4)) {
                larguraBarra = dpParaPx(4);
            }

            LinearLayout.LayoutParams paramsBarra =
                    new LinearLayout.LayoutParams(
                            larguraBarra,
                            dpParaPx(16)
                    );
            paramsBarra.setMargins(dpParaPx(4), 0, dpParaPx(8), 0);
            barra.setLayoutParams(paramsBarra);
            barra.setBackgroundColor(Color.parseColor("#6BCB77"));
            linhaMes.addView(barra);

            // 3. Valor gasto no mês
            TextView txtValor = new TextView(this);
            txtValor.setText(formatoMoeda.format(gastosMensais[i]));
            txtValor.setTextColor(Color.WHITE);
            txtValor.setTextSize(11);
            txtValor.setSingleLine(true);
            txtValor.setGravity(Gravity.START);

            LinearLayout.LayoutParams paramsValor =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
            txtValor.setLayoutParams(paramsValor);
            linhaMes.addView(txtValor);

            layoutGrafico.addView(linhaMes);
        }
    }

    private int dpParaPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (manutencaoDao != null) {
            manutencaoDao.fechar();
        }
    }
}