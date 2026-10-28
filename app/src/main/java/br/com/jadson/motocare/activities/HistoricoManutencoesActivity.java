
package br.com.jadson.motocare.activities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.ManutencaoDao;

public class HistoricoManutencoesActivity extends AppCompatActivity {

    private ImageView btnVoltarHistorico;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private TextView txtMotoHistorico;
    private TextView txtQuantidadeManutencoes;
    private TextView txtHistoricoVazio;
    private LinearLayout containerHistoricoManutencoes;

    private ManutencaoDao manutencaoDao;

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";
    private static final String EXTRA_ID_MANUTENCAO = "id_manutencao";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_manutencoes);

        inicializarViews();
        manutencaoDao = new ManutencaoDao(this);
        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");

        btnVoltarHistorico.setOnClickListener(v -> finish());

        carregarHistorico();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (manutencaoDao != null && containerHistoricoManutencoes != null) {
            carregarHistorico();
        }
    }

    private void inicializarViews() {
        btnVoltarHistorico = findViewById(R.id.btnVoltarHistorico);
        txtMotoHistorico = findViewById(R.id.txtMotoHistorico);
        txtQuantidadeManutencoes =
                findViewById(R.id.txtQuantidadeManutencoes);
        txtHistoricoVazio = findViewById(R.id.txtHistoricoVazio);
        containerHistoricoManutencoes =
                findViewById(R.id.containerHistoricoManutencoes);
    }

    private void carregarHistorico() {
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String idMotoAtiva =
                preferences.getString(KEY_MOTO_ATIVA, null);

        if (idMotoAtiva == null || idMotoAtiva.trim().isEmpty()) {
            txtMotoHistorico.setText("Nenhuma motocicleta selecionada");
            txtQuantidadeManutencoes.setText("Registros encontrados: 0");
            txtHistoricoVazio.setText(
                    "Selecione uma motocicleta para consultar seu histórico."
            );
            txtHistoricoVazio.setVisibility(View.VISIBLE);
            containerHistoricoManutencoes.removeAllViews();
            return;
        }

        List<Manutencao> manutencoes =
                manutencaoDao.listarPorMoto(idMotoAtiva);

        txtMotoHistorico.setText(
                "Manutenções da motocicleta selecionada"
        );

        txtQuantidadeManutencoes.setText(
                "Registros encontrados: " + manutencoes.size()
        );

        containerHistoricoManutencoes.removeAllViews();

        if (manutencoes.isEmpty()) {
            txtHistoricoVazio.setText(
                    "Nenhuma manutenção registrada para esta motocicleta."
            );
            txtHistoricoVazio.setVisibility(View.VISIBLE);
            return;
        }

        txtHistoricoVazio.setVisibility(View.GONE);

        for (Manutencao manutencao : manutencoes) {
            adicionarCartaoManutencao(manutencao);
        }
    }

    private void adicionarCartaoManutencao(Manutencao manutencao) {
        LinearLayout cartao = new LinearLayout(this);
        cartao.setOrientation(LinearLayout.VERTICAL);
        cartao.setPadding(dp(18), dp(16), dp(18), dp(16));
        cartao.setBackgroundResource(R.drawable.bg_card_action);

        LinearLayout.LayoutParams parametrosCartao =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosCartao.bottomMargin = dp(12);
        cartao.setLayoutParams(parametrosCartao);

        TextView tipo = criarTexto(
                manutencao.getTipo(),
                18,
                true,
                getColor(R.color.primary_green)
        );
        cartao.addView(tipo);

        TextView data = criarTexto(
                "Data: " + formatarData(manutencao.getData()),
                14,
                false,
                getColor(R.color.white)
        );
        data.setPadding(0, dp(8), 0, 0);
        cartao.addView(data);

        TextView quilometragem = criarTexto(
                "Quilometragem: " + manutencao.getQuilometragem() + " km",
                14,
                false,
                getColor(R.color.white)
        );
        quilometragem.setPadding(0, dp(6), 0, 0);
        cartao.addView(quilometragem);

        TextView valor = criarTexto(
                "Valor: R$ " + manutencao.getValor(),
                14,
                true,
                getColor(R.color.white)
        );
        valor.setPadding(0, dp(6), 0, 0);
        cartao.addView(valor);

        String descricaoTexto = manutencao.getDescricao();

        if (descricaoTexto != null && !descricaoTexto.trim().isEmpty()) {
            TextView descricao = criarTexto(
                    "Descrição: " + descricaoTexto,
                    14,
                    false,
                    getColor(R.color.white)
            );
            descricao.setPadding(0, dp(8), 0, 0);
            cartao.addView(descricao);
        }

        // Botão para editar a manutenção.
        TextView btnEditar = new TextView(this);
        btnEditar.setText("✎  EDITAR MANUTENÇÃO");
        btnEditar.setTextSize(14);
        btnEditar.setTextColor(getColor(R.color.white));
        btnEditar.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        btnEditar.setGravity(Gravity.CENTER);
        btnEditar.setBackgroundResource(R.drawable.bg_button_primary);
        btnEditar.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams parametrosEditar =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosEditar.topMargin = dp(16);
        btnEditar.setLayoutParams(parametrosEditar);

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HistoricoManutencoesActivity.this,
                    CadastroManutencaoActivity.class
            );

            intent.putExtra(
                    EXTRA_ID_MANUTENCAO,
                    manutencao.getId()
            );

            startActivity(intent);
        });

        cartao.addView(btnEditar);

        // Botão para excluir a manutenção.
        TextView btnExcluir = new TextView(this);
        btnExcluir.setText("✖  EXCLUIR MANUTENÇÃO");
        btnExcluir.setTextSize(14);
        btnExcluir.setTextColor(getColor(R.color.white));
        btnExcluir.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        btnExcluir.setGravity(Gravity.CENTER);
        btnExcluir.setBackgroundResource(R.drawable.bg_button_icon);
        btnExcluir.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams parametrosExcluir =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosExcluir.topMargin = dp(10);
        btnExcluir.setLayoutParams(parametrosExcluir);

        btnExcluir.setOnClickListener(v ->
                confirmarExclusao(manutencao)
        );

        cartao.addView(btnExcluir);

        containerHistoricoManutencoes.addView(cartao);
    }

    private void confirmarExclusao(Manutencao manutencao) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir manutenção")
                .setMessage(
                        "Tem certeza de que deseja excluir a manutenção \""
                                + manutencao.getTipo()
                                + "\"?\n\nEssa ação não poderá ser desfeita."
                )
                .setNegativeButton("CANCELAR", (dialog, which) ->
                        dialog.dismiss()
                )
                .setPositiveButton("EXCLUIR", (dialog, which) ->
                        excluirManutencao(manutencao)
                )
                .show();
    }


    private void excluirManutencao(Manutencao manutencao) {

        if (manutencao.getId() == null
                || manutencao.getId().trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Não foi possível identificar a manutenção.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        FirebaseUser usuario = firebaseAuth.getCurrentUser();

        if (usuario == null) {

            Toast.makeText(
                    this,
                    "Sua sessão expirou. Entre novamente.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        databaseReference
                .child(usuario.getUid())
                .child("manutencoes")
                .child(manutencao.getId())
                .removeValue()
                .addOnCompleteListener(task -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                HistoricoManutencoesActivity.this,
                                "Falha ao excluir do Firebase. "
                                        + "A manutenção continua no aparelho.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    boolean excluidaLocalmente =
                            manutencaoDao.excluir(manutencao.getId());

                    if (excluidaLocalmente) {

                        Toast.makeText(
                                HistoricoManutencoesActivity.this,
                                "Manutenção excluída com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                HistoricoManutencoesActivity.this,
                                "Removida do Firebase, mas houve um erro "
                                        + "ao excluir do aparelho.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    carregarHistorico();
                });
    }


    private TextView criarTexto(
            String texto,
            int tamanhoSp,
            boolean negrito,
            int cor
    ) {
        TextView textView = new TextView(this);
        textView.setText(texto == null ? "" : texto);
        textView.setTextSize(tamanhoSp);
        textView.setTextColor(cor);
        textView.setGravity(Gravity.START);

        if (negrito) {
            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        return textView;
    }

    private String formatarData(String dataOriginal) {
        if (dataOriginal == null || dataOriginal.trim().isEmpty()) {
            return "Não informada";
        }

        try {
            SimpleDateFormat entrada = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            );

            entrada.setLenient(false);

            Date data = entrada.parse(dataOriginal);

            if (data != null) {
                SimpleDateFormat saida = new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

                return saida.format(data);
            }
        } catch (ParseException e) {
            // Mantém a data original se o formato for diferente.
        }

        return dataOriginal;
    }

    private int dp(int valor) {
        return (int) (valor * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (manutencaoDao != null) {
            manutencaoDao.fechar();
        }
    }
}
