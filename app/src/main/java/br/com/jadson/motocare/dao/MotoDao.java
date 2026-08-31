package br.com.jadson.motocare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import br.com.jadson.motocare.activities.Motocicleta;
import br.com.jadson.motocare.database.MotoDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MotoDao {

    private final MotoDatabaseHelper databaseHelper;

    public MotoDao(Context context) {
        databaseHelper = new MotoDatabaseHelper(context);
    }

    /**
     * Insere uma motocicleta no banco local.
     *
     * A moto começa como NÃO sincronizada.
     */
    public boolean inserir(Motocicleta moto, String uidUsuario) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_ID,
                moto.getId()
        );

        values.put(
                MotoDatabaseHelper.COL_UID_USUARIO,
                uidUsuario
        );

        values.put(
                MotoDatabaseHelper.COL_APELIDO,
                moto.getApelido()
        );

        values.put(
                MotoDatabaseHelper.COL_MARCA,
                moto.getMarca()
        );

        values.put(
                MotoDatabaseHelper.COL_MODELO,
                moto.getModelo()
        );

        values.put(
                MotoDatabaseHelper.COL_ANO,
                moto.getAno()
        );

        values.put(
                MotoDatabaseHelper.COL_PLACA,
                moto.getPlaca()
        );

        values.put(
                MotoDatabaseHelper.COL_QUILOMETRAGEM,
                moto.getQuilometragem()
        );

        // 0 = ainda não confirmado no Firebase
        values.put(
                MotoDatabaseHelper.COL_SINCRONIZADO,
                0
        );

        long resultado = db.insert(
                MotoDatabaseHelper.TABLE_MOTOS,
                null,
                values
        );

        db.close();

        return resultado != -1;
    }

    /**
     * Marca a motocicleta como sincronizada.
     */
    public boolean marcarComoSincronizada(String idMoto) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_SINCRONIZADO,
                1
        );

        int resultado = db.update(
                MotoDatabaseHelper.TABLE_MOTOS,
                values,
                MotoDatabaseHelper.COL_ID + " = ?",
                new String[]{idMoto}
        );

        db.close();

        return resultado > 0;
    }

    /**
     * Busca todas as motocicletas de um usuário.
     */
    public List<Motocicleta> listarPorUsuario(String uidUsuario) {

        List<Motocicleta> lista = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_MOTOS,
                null,
                MotoDatabaseHelper.COL_UID_USUARIO + " = ?",
                new String[]{uidUsuario},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {

            do {

                String id = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_ID
                        )
                );

                String apelido = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_APELIDO
                        )
                );

                String marca = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MARCA
                        )
                );

                String modelo = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MODELO
                        )
                );

                String ano = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_ANO
                        )
                );

                String placa = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_PLACA
                        )
                );

                String quilometragem = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_QUILOMETRAGEM
                        )
                );

                Motocicleta moto = new Motocicleta(
                        id,
                        apelido,
                        marca,
                        modelo,
                        ano,
                        placa,
                        quilometragem
                );

                lista.add(moto);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return lista;
    }

    /**
     * Atualiza a quilometragem.
     */
    public boolean atualizarQuilometragem(
            String idMoto,
            String novaQuilometragem
    ) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_QUILOMETRAGEM,
                novaQuilometragem
        );

        // Alterou localmente, então precisa sincronizar novamente.
        values.put(
                MotoDatabaseHelper.COL_SINCRONIZADO,
                0
        );

        int resultado = db.update(
                MotoDatabaseHelper.TABLE_MOTOS,
                values,
                MotoDatabaseHelper.COL_ID + " = ?",
                new String[]{idMoto}
        );

        db.close();

        return resultado > 0;
    }

    /**
     * Exclui uma motocicleta.
     */
    public boolean excluir(String idMoto) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        int resultado = db.delete(
                MotoDatabaseHelper.TABLE_MOTOS,
                MotoDatabaseHelper.COL_ID + " = ?",
                new String[]{idMoto}
        );

        db.close();

        return resultado > 0;
    }

    /**
     * Fecha o banco.
     */
    public void fechar() {
        databaseHelper.close();
    }
}