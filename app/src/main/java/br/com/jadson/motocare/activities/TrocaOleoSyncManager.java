package br.com.jadson.motocare.activities;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.jadson.motocare.dao.TrocaOleoDao;

public class TrocaOleoSyncManager {

    private static final String TAG = "TrocaOleoSyncManager";

    private final TrocaOleoDao trocaOleoDao;
    private final DatabaseReference databaseReference;

    public interface SyncCallback {
        void onComplete(int sincronizadas, int falhas);
    }

    public TrocaOleoSyncManager(Context context) {

        trocaOleoDao = new TrocaOleoDao(context);

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");
    }

    public void sincronizarTrocasOleo(
            String uidUsuario,
            SyncCallback callback
    ) {

        if (uidUsuario == null || uidUsuario.trim().isEmpty()) {

            Log.e(TAG, "UID do usuário inválido.");
            trocaOleoDao.fechar();

            if (callback != null) {
                callback.onComplete(0, 1);
            }

            return;
        }

        List<TrocaOleo> trocasPendentes;

        try {

            trocasPendentes =
                    trocaOleoDao.listarNaoSincronizadas(uidUsuario);

        } catch (Exception e) {

            Log.e(TAG, "Erro ao buscar trocas de óleo pendentes.", e);
            trocaOleoDao.fechar();

            if (callback != null) {
                callback.onComplete(0, 1);
            }

            return;
        }

        if (trocasPendentes == null || trocasPendentes.isEmpty()) {

            trocaOleoDao.fechar();

            if (callback != null) {
                callback.onComplete(0, 0);
            }

            return;
        }

        AtomicInteger concluidas = new AtomicInteger(0);
        AtomicInteger sincronizadas = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        int total = trocasPendentes.size();

        for (TrocaOleo trocaOleo : trocasPendentes) {

            if (trocaOleo == null
                    || trocaOleo.getId() == null
                    || trocaOleo.getIdMoto() == null) {

                falhas.incrementAndGet();

                if (concluidas.incrementAndGet() == total) {
                    finalizarSincronizacao(
                            sincronizadas.get(),
                            falhas.get(),
                            callback
                    );
                }

                continue;
            }

            databaseReference
                    .child(uidUsuario)
                    .child("motocicletas")
                    .child(trocaOleo.getIdMoto())
                    .child("trocasOleo")
                    .child(trocaOleo.getId())
                    .setValue(trocaOleo)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            boolean marcado =
                                    trocaOleoDao.marcarComoSincronizada(
                                            trocaOleo.getId()
                                    );

                            if (marcado) {
                                sincronizadas.incrementAndGet();
                            } else {
                                falhas.incrementAndGet();

                                Log.e(
                                        TAG,
                                        "Troca enviada, mas não foi possível "
                                                + "marcar como sincronizada no SQLite: "
                                                + trocaOleo.getId()
                                );
                            }

                        } else {

                            falhas.incrementAndGet();

                            Log.e(
                                    TAG,
                                    "Falha ao enviar troca de óleo: "
                                            + trocaOleo.getId(),
                                    task.getException()
                            );
                        }

                        if (concluidas.incrementAndGet() == total) {

                            finalizarSincronizacao(
                                    sincronizadas.get(),
                                    falhas.get(),
                                    callback
                            );
                        }
                    });
        }
    }

    private void finalizarSincronizacao(
            int sincronizadas,
            int falhas,
            SyncCallback callback
    ) {

        trocaOleoDao.fechar();

        if (callback != null) {
            callback.onComplete(sincronizadas, falhas);
        }
    }

    public void fechar() {
        trocaOleoDao.fechar();
    }
}
