
package br.com.jadson.motocare.activities;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.jadson.motocare.dao.TrocaOleoDao;

public class TrocaOleoSyncManager {

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

        List<TrocaOleo> trocasPendentes =
                trocaOleoDao.listarNaoSincronizadas(uidUsuario);

        if (trocasPendentes.isEmpty()) {
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

            databaseReference
                    .child(uidUsuario)
                    .child("motocicletas")
                    .child(trocaOleo.getIdMoto())
                    .child("trocasOleo")
                    .child(trocaOleo.getId())
                    .setValue(trocaOleo)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            trocaOleoDao.marcarComoSincronizada(
                                    trocaOleo.getId()
                            );

                            sincronizadas.incrementAndGet();

                        } else {
                            falhas.incrementAndGet();
                        }

                        if (concluidas.incrementAndGet() == total) {

                            trocaOleoDao.fechar();

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
        trocaOleoDao.fechar();
    }
}
