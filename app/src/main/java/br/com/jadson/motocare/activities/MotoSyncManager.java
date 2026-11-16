
package br.com.jadson.motocare.activities;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.jadson.motocare.dao.MotoDao;

public class MotoSyncManager {

    private static final String TAG = "MotoSyncManager";

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
    public void sincronizarMotos(String uidUsuario) {

        if (uidUsuario == null || uidUsuario.trim().isEmpty()) {
            Log.e(TAG, "UID do usuário inválido.");
            motoDao.fechar();
            return;
        }

        List<Motocicleta> motosPendentes;

        try {
            motosPendentes = motoDao.listarNaoSincronizadas(uidUsuario);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar motos pendentes.", e);
            motoDao.fechar();
            return;
        }

        if (motosPendentes == null || motosPendentes.isEmpty()) {
            motoDao.fechar();
            return;
        }

        for (Motocicleta moto : motosPendentes) {
            sincronizarMoto(uidUsuario, moto);
        }
    }

    /**
     * Atualiza somente os campos da motocicleta.
     * Os históricos e demais nós filhos são preservados.
     */
    private void sincronizarMoto(
            String uidUsuario,
            Motocicleta moto
    ) {

        if (moto == null || moto.getId() == null) {
            Log.e(TAG, "Motocicleta ou ID inválido.");
            return;
        }

        Map<String, Object> dadosMoto = new HashMap<>();

        adicionarSeNaoNulo(dadosMoto, "id", moto.getId());
        adicionarSeNaoNulo(dadosMoto, "apelido", moto.getApelido());
        adicionarSeNaoNulo(dadosMoto, "marca", moto.getMarca());
        adicionarSeNaoNulo(dadosMoto, "modelo", moto.getModelo());
        adicionarSeNaoNulo(dadosMoto, "ano", moto.getAno());
        adicionarSeNaoNulo(dadosMoto, "placa", moto.getPlaca());
        adicionarSeNaoNulo(
                dadosMoto,
                "quilometragem",
                moto.getQuilometragem()
        );

        databaseReference
                .child(uidUsuario)
                .child("motocicletas")
                .child(moto.getId())
                .updateChildren(dadosMoto)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        boolean marcado = motoDao.marcarComoSincronizada(
                                moto.getId()
                        );

                        if (!marcado) {
                            Log.e(
                                    TAG,
                                    "Moto enviada, mas não foi possível atualizar "
                                            + "o status local: " + moto.getId()
                            );
                        }

                    } else {

                        Log.e(
                                TAG,
                                "Falha ao sincronizar moto: " + moto.getId(),
                                task.getException()
                        );
                    }
                });
    }

    private void adicionarSeNaoNulo(
            Map<String, Object> dados,
            String campo,
            String valor
    ) {
        if (valor != null) {
            dados.put(campo, valor);
        }
    }

    /**
     * Fecha o banco local.
     */
    public void fechar() {
        motoDao.fechar();
    }
}
