
package br.com.jadson.motocare.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.jadson.motocare.R;
import br.com.jadson.motocare.dao.MotoDao;
import br.com.jadson.motocare.dao.TrocaOleoDao;
import br.com.jadson.motocare.dao.ManutencaoCorrenteDao;

public class AlertasActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MotoCarePrefs";
    private static final String KEY_MOTO_ATIVA = "moto_ativa";
    private static final long INTERVALO_CORRENTE_KM = 500;

    private TextView txtKmAtualAlerta;
    private TextView txtSituacaoOleo;
    private TextView txtUltimaTroca;
    private TextView txtProximaTroca;
    private TextView txtVencimentoOleo;
    private TextView txtAlertaVazio;

    private TextView txtSituacaoCorrente;
    private TextView txtUltimaCorrente;
    private TextView txtProximaCorrente;
    private TextView txtIntervaloCorrente;
    private TextView btnRegistrarCorrente;

    private TrocaOleoDao trocaOleoDao;
    private MotoDao motoDao;
    private ManutencaoCorrenteDao manutencaoCorrenteDao;

    private String idMotoAtiva;
    private String uidUsuario;
    private long quilometragemAtual;

    private final SimpleDateFormat formatoBanco =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final SimpleDateFormat formatoExibicao =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertas);

        ImageView btnVoltar = findViewById(R.id.btnVoltarAlertas);
        btnVoltar.setOnClickListener(v -> finish());

        txtKmAtualAlerta = findViewById(R.id.txtKmAtualAlerta);
        txtSituacaoOleo = findViewById(R.id.txtSituacaoOleo);
        txtUltimaTroca = findViewById(R.id.txtUltimaTroca);
        txtProximaTroca = findViewById(R.id.txtProximaTroca);
        txtVencimentoOleo = findViewById(R.id.txtVencimentoOleo);
        txtAlertaVazio = findViewById(R.id.txtAlertaVazio);

        txtSituacaoCorrente = findViewById(R.id.txtSituacaoCorrente);
        txtUltimaCorrente = findViewById(R.id.txtUltimaCorrente);
        txtProximaCorrente = findViewById(R.id.txtProximaCorrente);
        txtIntervaloCorrente = findViewById(R.id.txtIntervaloCorrente);
        btnRegistrarCorrente = findViewById(R.id.btnRegistrarCorrente);

        trocaOleoDao = new TrocaOleoDao(this);
        motoDao = new MotoDao(this);
        manutencaoCorrenteDao = new ManutencaoCorrenteDao(this);

        FirebaseUser usuarioAtual =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioAtual != null) {
            uidUsuario = usuarioAtual.getUid();
        }

        btnRegistrarCorrente.setOnClickListener(v ->
                registrarManutencaoCorrente()
        );

        carregarAlertas();
    }

    private void carregarAlertas() {

        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        idMotoAtiva = preferences.getString(KEY_MOTO_ATIVA, null);

        if (idMotoAtiva == null || idMotoAtiva.trim().isEmpty()) {
            mostrarMensagemVazia("Nenhuma motocicleta selecionada.");
            desabilitarRegistroCorrente();
            return;
        }

        Motocicleta moto = motoDao.buscarPorId(idMotoAtiva);

        if (moto == null) {
            mostrarMensagemVazia(
                    "Não foi possível localizar a motocicleta."
            );
            desabilitarRegistroCorrente();
            return;
        }

        String kmAtualTexto = moto.getQuilometragem();

        txtKmAtualAlerta.setText(
                "Quilometragem atual: " + kmAtualTexto + " km"
        );

        try {
            quilometragemAtual = Long.parseLong(kmAtualTexto);
        } catch (NumberFormatException e) {
            txtSituacaoOleo.setText(
                    "Não foi possível calcular a situação da troca."
            );

            txtAlertaVazio.setVisibility(View.VISIBLE);
            txtAlertaVazio.setText(
                    "Verifique a quilometragem cadastrada da motocicleta."
            );

            txtSituacaoCorrente.setText(
                    "Quilometragem inválida"
            );

            desabilitarRegistroCorrente();
            return;
        }

        carregarAlertaOleo(idMotoAtiva, quilometragemAtual);
        carregarAlertaCorrente(idMotoAtiva, quilometragemAtual);
    }

    // =====================================================
    // ALERTA DE TROCA DE ÓLEO
    // =====================================================

    private void carregarAlertaOleo(String idMoto, long kmAtual) {

        List<TrocaOleo> trocas =
                trocaOleoDao.listarPorMoto(idMoto);

        if (trocas.isEmpty()) {
            txtSituacaoOleo.setText("Situação: sem registros");
            txtUltimaTroca.setText("Última troca: nenhuma registrada");
            txtProximaTroca.setText("Próxima troca: aguardando registro");
            txtVencimentoOleo.setText("Prazo de seis meses: não disponível");

            txtAlertaVazio.setText(
                    "Você ainda não cadastrou nenhuma troca de óleo."
            );
            txtAlertaVazio.setVisibility(View.VISIBLE);
            return;
        }

        atualizarSituacaoOleo(trocas.get(0), kmAtual);
    }

    private void atualizarSituacaoOleo(
            TrocaOleo troca,
            long kmAtual
    ) {

        try {

            long kmTroca =
                    Long.parseLong(troca.getQuilometragem());

            long proximaTrocaKm =
                    kmTroca + troca.getIntervaloKm();

            Calendar vencimento =
                    obterDataVencimento(troca.getData());

            Calendar hoje = Calendar.getInstance();

            boolean vencidaPorKm =
                    kmAtual >= proximaTrocaKm;

            boolean vencidaPorData =
                    !hoje.before(vencimento);

            txtUltimaTroca.setText(
                    "Última troca: "
                            + formatarData(troca.getData())
                            + "\nQuilometragem: "
                            + kmTroca + " km"
            );

            txtProximaTroca.setText(
                    "Próxima troca: "
                            + proximaTrocaKm + " km"
            );

            txtVencimentoOleo.setText(
                    "Prazo de seis meses: "
                            + formatoExibicao.format(
                            vencimento.getTime()
                    )
            );

            if (vencidaPorKm || vencidaPorData) {

                String motivo = vencidaPorKm
                        ? "quilometragem atingida"
                        : "prazo de seis meses atingido";

                txtSituacaoOleo.setText(
                        "TROCA DE ÓLEO VENCIDA\n" + motivo
                );

                txtSituacaoOleo.setTextColor(
                        getResources().getColor(R.color.error)
                );

            } else {

                txtSituacaoOleo.setText(
                        "TROCA DE ÓLEO EM DIA"
                );

                txtSituacaoOleo.setTextColor(
                        getResources().getColor(R.color.success)
                );
            }

        } catch (NumberFormatException | ParseException e) {

            txtSituacaoOleo.setText(
                    "Não foi possível calcular a situação da troca."
            );

            txtAlertaVazio.setVisibility(View.VISIBLE);
            txtAlertaVazio.setText(
                    "Verifique os dados da última troca de óleo."
            );
        }
    }

    // =====================================================
    // ALERTA DE MANUTENÇÃO DA CORRENTE
    // =====================================================

    private void carregarAlertaCorrente(
            String idMoto,
            long kmAtual
    ) {

        ManutencaoCorrente ultima =
                manutencaoCorrenteDao.buscarUltimaPorMoto(idMoto);

        txtIntervaloCorrente.setText(
                "Intervalo recomendado: "
                        + INTERVALO_CORRENTE_KM + " km"
        );

        if (ultima == null) {

            txtSituacaoCorrente.setText(
                    "AGUARDANDO PRIMEIRA MANUTENÇÃO"
            );

            txtSituacaoCorrente.setTextColor(
                    getResources().getColor(R.color.warning)
            );

            txtUltimaCorrente.setText(
                    "Última manutenção: nenhuma registrada"
            );

            txtProximaCorrente.setText(
                    "Próxima manutenção: após o primeiro registro"
            );

            return;
        }

        try {

            long kmUltima =
                    Long.parseLong(ultima.getQuilometragem());

            long proximaManutencao =
                    kmUltima + INTERVALO_CORRENTE_KM;

            txtUltimaCorrente.setText(
                    "Última manutenção: "
                            + formatarData(ultima.getData())
                            + "\nQuilometragem: "
                            + kmUltima + " km"
            );

            txtProximaCorrente.setText(
                    "Próxima manutenção: "
                            + proximaManutencao + " km"
            );

            if (kmAtual >= proximaManutencao) {

                txtSituacaoCorrente.setText(
                        "MANUTENÇÃO NECESSÁRIA"
                );

                txtSituacaoCorrente.setTextColor(
                        getResources().getColor(R.color.error)
                );

            } else {

                txtSituacaoCorrente.setText(
                        "APERTO E LUBRIFICAÇÃO EM DIA"
                );

                txtSituacaoCorrente.setTextColor(
                        getResources().getColor(R.color.success)
                );
            }

        } catch (NumberFormatException e) {

            txtSituacaoCorrente.setText(
                    "Não foi possível calcular a manutenção."
            );

            txtSituacaoCorrente.setTextColor(
                    getResources().getColor(R.color.error)
            );
        }
    }

    // =====================================================
    // REGISTRAR MANUTENÇÃO DA CORRENTE
    // =====================================================

    private void registrarManutencaoCorrente() {

        if (idMotoAtiva == null || idMotoAtiva.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Nenhuma motocicleta selecionada.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (uidUsuario == null || uidUsuario.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Faça login para registrar a manutenção.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (quilometragemAtual < 0) {
            Toast.makeText(
                    this,
                    "Quilometragem inválida.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String dataAtual = formatoBanco.format(new Date());

        ManutencaoCorrente manutencao =
                new ManutencaoCorrente(
                        idMotoAtiva,
                        uidUsuario,
                        dataAtual,
                        String.valueOf(quilometragemAtual)
                );

        boolean inserido =
                manutencaoCorrenteDao.inserir(manutencao);

        if (!inserido) {
            Toast.makeText(
                    this,
                    "Não foi possível registrar a manutenção.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        carregarAlertaCorrente(
                idMotoAtiva,
                quilometragemAtual
        );

        Toast.makeText(
                this,
                "Manutenção da corrente registrada!",
                Toast.LENGTH_LONG
        ).show();

        btnRegistrarCorrente.setEnabled(false);
        btnRegistrarCorrente.setAlpha(0.6f);

        sincronizarCorrente();
    }

    private void sincronizarCorrente() {

        ManutencaoCorrenteSyncManager syncManager =
                new ManutencaoCorrenteSyncManager(this);

        syncManager.sincronizarManutencoesCorrente(
                uidUsuario,
                (sincronizadas, falhas) -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (sincronizadas > 0) {
                        Toast.makeText(
                                this,
                                "Manutenção sincronizada com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else if (falhas > 0) {
                        Toast.makeText(
                                this,
                                "Registro salvo no aparelho. "
                                        + "A sincronização será tentada novamente.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    else {
                        Toast.makeText(
                                this,
                                "Nenhum registro pendente de sincronização.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void desabilitarRegistroCorrente() {
        btnRegistrarCorrente.setEnabled(false);
        btnRegistrarCorrente.setAlpha(0.5f);
    }

    // =====================================================
    // DATAS
    // =====================================================

    private Calendar obterDataVencimento(String data)
            throws ParseException {

        Date dataTroca = formatoBanco.parse(data);

        if (dataTroca == null) {
            throw new ParseException("Data inválida", 0);
        }

        Calendar vencimento = Calendar.getInstance();
        vencimento.setTime(dataTroca);
        vencimento.add(Calendar.MONTH, 6);

        return vencimento;
    }

    private String formatarData(String data) {

        try {

            Date dataConvertida = formatoBanco.parse(data);

            if (dataConvertida != null) {
                return formatoExibicao.format(dataConvertida);
            }

            return data;

        } catch (ParseException e) {
            return data;
        }
    }

    private void mostrarMensagemVazia(String mensagem) {

        txtSituacaoOleo.setText("");
        txtSituacaoCorrente.setText("");
        txtAlertaVazio.setText(mensagem);
        txtAlertaVazio.setVisibility(View.VISIBLE);
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

        if (manutencaoCorrenteDao != null) {
            manutencaoCorrenteDao.fechar();
        }
    }
}
