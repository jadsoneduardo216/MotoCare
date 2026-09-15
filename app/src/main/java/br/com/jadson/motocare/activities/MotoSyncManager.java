package br.com.jadson.motocare.activities;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import br.com.jadson.motocare.dao.MotoDao;

public class MotoSyncManager {

    private final MotoDao motoDao;
    private final DatabaseReference databaseReference;

    public MotoSyncManager(Context context) {

        motoDao = new MotoDao(context);

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("usuarios");
    }

    /**
     * Sincroniza todas as motocicletas pendentes
     * de um determinado usuário.
     */
    public void sincronizarMotos(
            String uidUsuario
    ) {

        List<Motocicleta> motosPendentes =
                motoDao.listarNaoSincronizadas(
                        uidUsuario
                );

        if (motosPendentes.isEmpty()) {
            return;
        }

        for (Motocicleta moto : motosPendentes) {

            sincronizarMoto(
                    uidUsuario,
                    moto
            );
        }
    }

    /**
     * Envia uma motocicleta específica
     * para o Firebase.
     */
    private void sincronizarMoto(
            String uidUsuario,
            Motocicleta moto
    ) {

        databaseReference
                .child(uidUsuario)
                .child("motocicletas")
                .child(moto.getId())
                .setValue(moto)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        motoDao.marcarComoSincronizada(
                                moto.getId()
                        );
                    }
                });
    }

    /**
     * Fecha o banco local.
     */
    public void fechar() {

        motoDao.fechar();
    }
}