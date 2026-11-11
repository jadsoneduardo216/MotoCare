
package br.com.jadson.motocare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.com.jadson.motocare.activities.TrocaOleo;
import br.com.jadson.motocare.database.MotoDatabaseHelper;

public class TrocaOleoDao {

    private final MotoDatabaseHelper databaseHelper;

    public TrocaOleoDao(Context context) {
        databaseHelper = new MotoDatabaseHelper(context);
    }

    /**
     * Insere uma troca de óleo no banco local.
     * A troca começa como não sincronizada.
     */
    public boolean inserir(TrocaOleo trocaOleo) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_OLEO_ID,
                trocaOleo.getId()
        );

        values.put(
                MotoDatabaseHelper.COL_OLEO_ID_MOTO,
                trocaOleo.getIdMoto()
        );

        values.put(
                MotoDatabaseHelper.COL_OLEO_UID_USUARIO,
                trocaOleo.getUidUsuario()
        );

        values.put(
                MotoDatabaseHelper.COL_OLEO_DATA,
                trocaOleo.getData()
        );

        values.put(
                MotoDatabaseHelper.COL_OLEO_QUILOMETRAGEM,
                trocaOleo.getQuilometragem()
        );

        values.put(
                MotoDatabaseHelper.COL_OLEO_INTERVALO_KM,
                trocaOleo.getIntervaloKm()
        );

        values.put(
                MotoDatabaseHelper.COL_OLEO_SINCRONIZADO,
                0
        );

        long resultado = db.insert(
                MotoDatabaseHelper.TABLE_TROCAS_OLEO,
                null,
                values
        );

        db.close();

        return resultado != -1;
    }

    /**
     * Busca uma troca de óleo pelo ID.
     */
    public TrocaOleo buscarPorId(String idTroca) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_TROCAS_OLEO,
                null,
                MotoDatabaseHelper.COL_OLEO_ID + " = ?",
                new String[]{idTroca},
                null,
                null,
                null,
                "1"
        );

        TrocaOleo trocaOleo = null;

        if (cursor.moveToFirst()) {
            trocaOleo = criarTrocaOleoAPartirDoCursor(cursor);
        }

        cursor.close();
        db.close();

        return trocaOleo;
    }

    /**
     * Lista o histórico de trocas de óleo
     * de uma motocicleta.
     */
    public List<TrocaOleo> listarPorMoto(String idMoto) {

        List<TrocaOleo> lista = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_TROCAS_OLEO,
                null,
                MotoDatabaseHelper.COL_OLEO_ID_MOTO + " = ?",
                new String[]{idMoto},
                null,
                null,
                MotoDatabaseHelper.COL_OLEO_DATA + " DESC"
        );

        if (cursor.moveToFirst()) {

            do {
                TrocaOleo trocaOleo =
                        criarTrocaOleoAPartirDoCursor(cursor);

                lista.add(trocaOleo);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return lista;
    }

    /**
     * Lista as trocas de óleo que ainda
     * precisam ser sincronizadas.
     */
    public List<TrocaOleo> listarNaoSincronizadas(String uidUsuario) {

        List<TrocaOleo> lista = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_TROCAS_OLEO,
                null,
                MotoDatabaseHelper.COL_OLEO_UID_USUARIO + " = ? AND "
                        + MotoDatabaseHelper.COL_OLEO_SINCRONIZADO + " = ?",
                new String[]{
                        uidUsuario,
                        "0"
                },
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {

            do {
                TrocaOleo trocaOleo =
                        criarTrocaOleoAPartirDoCursor(cursor);

                lista.add(trocaOleo);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return lista;
    }

    /**
     * Marca uma troca de óleo como sincronizada.
     */
    public boolean marcarComoSincronizada(String idTroca) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_OLEO_SINCRONIZADO,
                1
        );

        int resultado = db.update(
                MotoDatabaseHelper.TABLE_TROCAS_OLEO,
                values,
                MotoDatabaseHelper.COL_OLEO_ID + " = ?",
                new String[]{idTroca}
        );

        db.close();

        return resultado > 0;
    }

    /**
     * Cria um objeto TrocaOleo a partir do Cursor.
     */
    private TrocaOleo criarTrocaOleoAPartirDoCursor(Cursor cursor) {

        String id = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        MotoDatabaseHelper.COL_OLEO_ID
                )
        );

        String idMoto = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        MotoDatabaseHelper.COL_OLEO_ID_MOTO
                )
        );

        String uidUsuario = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        MotoDatabaseHelper.COL_OLEO_UID_USUARIO
                )
        );

        String data = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        MotoDatabaseHelper.COL_OLEO_DATA
                )
        );

        String quilometragem = cursor.getString(
                cursor.getColumnIndexOrThrow(
                        MotoDatabaseHelper.COL_OLEO_QUILOMETRAGEM
                )
        );

        int intervaloKm = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                        MotoDatabaseHelper.COL_OLEO_INTERVALO_KM
                )
        );

        int sincronizado = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                        MotoDatabaseHelper.COL_OLEO_SINCRONIZADO
                )
        );

        return new TrocaOleo(
                id,
                idMoto,
                uidUsuario,
                data,
                quilometragem,
                intervaloKm,
                sincronizado == 1
        );
    }

    /**
     * Fecha o banco de dados.
     */
    public void fechar() {
        databaseHelper.close();
    }
}
