package br.com.jadson.motocare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.com.jadson.motocare.activities.ManutencaoCorrente;
import br.com.jadson.motocare.database.MotoDatabaseHelper;

public class ManutencaoCorrenteDao {

    private final MotoDatabaseHelper databaseHelper;

    public ManutencaoCorrenteDao(Context context) {
        databaseHelper = new MotoDatabaseHelper(context);
    }

    // =========================================
    // INSERIR MANUTENÇÃO DA CORRENTE
    // =========================================

    public boolean inserir(ManutencaoCorrente manutencao) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_CORRENTE_ID,
                manutencao.getId()
        );

        values.put(
                MotoDatabaseHelper.COL_CORRENTE_ID_MOTO,
                manutencao.getIdMoto()
        );

        values.put(
                MotoDatabaseHelper.COL_CORRENTE_UID_USUARIO,
                manutencao.getUidUsuario()
        );

        values.put(
                MotoDatabaseHelper.COL_CORRENTE_DATA,
                manutencao.getData()
        );

        values.put(
                MotoDatabaseHelper.COL_CORRENTE_QUILOMETRAGEM,
                manutencao.getQuilometragem()
        );

        values.put(
                MotoDatabaseHelper.COL_CORRENTE_SINCRONIZADO,
                0
        );

        long resultado = db.insert(
                MotoDatabaseHelper.TABLE_MANUTENCOES_CORRENTE,
                null,
                values
        );

        return resultado != -1;
    }

    // =========================================
    // LISTAR HISTÓRICO DE UMA MOTO
    // =========================================

    public List<ManutencaoCorrente> listarPorMoto(String idMoto) {

        List<ManutencaoCorrente> lista = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_MANUTENCOES_CORRENTE,
                null,
                MotoDatabaseHelper.COL_CORRENTE_ID_MOTO + " = ?",
                new String[]{idMoto},
                null,
                null,
                MotoDatabaseHelper.COL_CORRENTE_DATA + " DESC"
        );

        try {

            while (cursor.moveToNext()) {

                ManutencaoCorrente manutencao =
                        new ManutencaoCorrente();

                manutencao.setId(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        MotoDatabaseHelper.COL_CORRENTE_ID
                                )
                        )
                );

                manutencao.setIdMoto(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        MotoDatabaseHelper.COL_CORRENTE_ID_MOTO
                                )
                        )
                );

                manutencao.setUidUsuario(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        MotoDatabaseHelper.COL_CORRENTE_UID_USUARIO
                                )
                        )
                );

                manutencao.setData(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        MotoDatabaseHelper.COL_CORRENTE_DATA
                                )
                        )
                );

                manutencao.setQuilometragem(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        MotoDatabaseHelper.COL_CORRENTE_QUILOMETRAGEM
                                )
                        )
                );

                int sincronizado = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_CORRENTE_SINCRONIZADO
                        )
                );

                manutencao.setSincronizado(sincronizado == 1);

                lista.add(manutencao);
            }

        } finally {
            cursor.close();
        }

        return lista;
    }

    // =========================================
    // BUSCAR A ÚLTIMA MANUTENÇÃO
    // =========================================

    public ManutencaoCorrente buscarUltimaPorMoto(String idMoto) {

        List<ManutencaoCorrente> lista = listarPorMoto(idMoto);

        if (lista.isEmpty()) {
            return null;
        }

        ManutencaoCorrente ultima = null;
        int maiorKm = -1;

        for (ManutencaoCorrente manutencao : lista) {

            try {

                int km = Integer.parseInt(
                        manutencao.getQuilometragem()
                );

                if (km > maiorKm) {
                    maiorKm = km;
                    ultima = manutencao;
                }

            } catch (NumberFormatException e) {
                // Ignora registros com quilometragem inválida
            }
        }

        return ultima;
    }

    // =========================================
    // MARCAR COMO SINCRONIZADA
    // =========================================

    public boolean marcarComoSincronizada(String id) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_CORRENTE_SINCRONIZADO,
                1
        );

        int linhas = db.update(
                MotoDatabaseHelper.TABLE_MANUTENCOES_CORRENTE,
                values,
                MotoDatabaseHelper.COL_CORRENTE_ID + " = ?",
                new String[]{id}
        );

        return linhas > 0;
    }

    // =========================================
    // LISTAR REGISTROS PENDENTES DE SINCRONIZAÇÃO
    // =========================================

    public List<ManutencaoCorrente> listarNaoSincronizadas(
            String uidUsuario
    ) {

        List<ManutencaoCorrente> lista = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_MANUTENCOES_CORRENTE,
                null,
                MotoDatabaseHelper.COL_CORRENTE_UID_USUARIO
                        + " = ? AND "
                        + MotoDatabaseHelper.COL_CORRENTE_SINCRONIZADO
                        + " = 0",
                new String[]{uidUsuario},
                null,
                null,
                null
        );

        try {

            while (cursor.moveToNext()) {

                ManutencaoCorrente manutencao =
                        new ManutencaoCorrente();

                manutencao.setId(cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_CORRENTE_ID
                        )
                ));

                manutencao.setIdMoto(cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_CORRENTE_ID_MOTO
                        )
                ));

                manutencao.setUidUsuario(cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_CORRENTE_UID_USUARIO
                        )
                ));

                manutencao.setData(cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_CORRENTE_DATA
                        )
                ));

                manutencao.setQuilometragem(cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_CORRENTE_QUILOMETRAGEM
                        )
                ));

                manutencao.setSincronizado(false);

                lista.add(manutencao);
            }

        } finally {
            cursor.close();
        }

        return lista;
    }

    // =========================================
    // FECHAR BANCO
    // =========================================

    public void fechar() {
        databaseHelper.close();
    }
}