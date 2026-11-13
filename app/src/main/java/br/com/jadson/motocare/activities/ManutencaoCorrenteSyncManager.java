package br.com.jadson.motocare.activities;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.jadson.motocare.dao.ManutencaoCorrenteDao;

public class ManutencaoCorrenteSyncManager {

    private static final String TAG = "CorrenteSync";

    private final ManutencaoCorrenteDao manutencaoCorrenteDao;
    private final DatabaseReference databaseReference;

    public interface SyncCallback {
        void onComplete(int sincronizadas, int falhas);
    }

    public ManutencaoCorrenteSyncManager(Context context) {

        manutencaoCorrenteDao =
                new ManutencaoCorrenteDao(context);

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");
    }

    public void sincronizarManutencoesCorrente(
            String uidUsuario,
            SyncCallback callback
    ) {

        if (uidUsuario == null || uidUsuario.trim().isEmpty()) {

            manutencaoCorrenteDao.fechar();

            if (callback != null) {
                callback.onComplete(0, 1);
            }

            return;
        }

        List<ManutencaoCorrente> pendentes;

        try {

            pendentes =
                    manutencaoCorrenteDao.listarNaoSincronizadas(
                            uidUsuario
                    );

        } catch (Exception e) {

            Log.e(TAG, "Erro ao consultar manutenções pendentes", e);

            manutencaoCorrenteDao.fechar();

            if (callback != null) {
                callback.onComplete(0, 1);
            }

            return;
        }

        if (pendentes == null || pendentes.isEmpty()) {

            manutencaoCorrenteDao.fechar();

            if (callback != null) {
                callback.onComplete(0, 0);
            }

            return;
        }

        AtomicInteger concluidas = new AtomicInteger(0);
        AtomicInteger sincronizadas = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        int total = pendentes.size();

        for (ManutencaoCorrente manutencao : pendentes) {

            databaseReference
                    .child(uidUsuario)
                    .child("motocicletas")
                    .child(manutencao.getIdMoto())
                    .child("manutencoesCorrente")
                    .child(manutencao.getId())
                    .setValue(manutencao)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            try {

                                boolean atualizado =
                                        manutencaoCorrenteDao
                                                .marcarComoSincronizada(
                                                        manutencao.getId()
                                                );

                                if (atualizado) {

                                    sincronizadas.incrementAndGet();

                                    Log.d(
                                            TAG,
                                            "Manutenção sincronizada: "
                                                    + manutencao.getId()
                                    );

                                } else {

                                    falhas.incrementAndGet();

                                    Log.e(
                                            TAG,
                                            "Firebase recebeu o registro, "
                                                    + "mas o SQLite não confirmou "
                                                    + "a atualização: "
                                                    + manutencao.getId()
                                    );
                                }

                            } catch (Exception e) {

                                falhas.incrementAndGet();

                                Log.e(
                                        TAG,
                                        "Erro ao atualizar sincronização local",
                                        e
                                );
                            }

                        } else {

                            falhas.incrementAndGet();

                            Log.e(
                                    TAG,
                                    "Falha ao enviar manutenção ao Firebase",
                                    task.getException()
                            );
                        }

                        if (concluidas.incrementAndGet() == total) {

                            manutencaoCorrenteDao.fechar();

                            if (callback != null) {
                                callback.onComplete(
                                        sincronizadas.get(),
                                        falhas.get()
                                );
                            }
                        }
                    });
        }
    }

    public void fechar() {
        manutencaoCorrenteDao.fechar();
    }
}
