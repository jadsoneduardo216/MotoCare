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
    public boolean inserir(
            Motocicleta moto,
            String uidUsuario
    ) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

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

        long resultado =
                db.insert(
                        MotoDatabaseHelper.TABLE_MOTOS,
                        null,
                        values
                );

        db.close();

        return resultado != -1;
    }

    /**
     * Busca uma motocicleta específica pelo ID.
     */
    public Motocicleta buscarPorId(String idMoto) {

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_MOTOS,
                null,
                MotoDatabaseHelper.COL_ID + " = ?",
                new String[]{idMoto},
                null,
                null,
                null,
                "1"
        );

        Motocicleta moto = null;

        if (cursor.moveToFirst()) {

            moto = criarMotocicletaAPartirDoCursor(cursor);
        }

        cursor.close();
        db.close();

        return moto;
    }

    /**
     * Busca todas as motocicletas de um usuário.
     */
    public List<Motocicleta> listarPorUsuario(
            String uidUsuario
    ) {

        List<Motocicleta> lista =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();

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

                Motocicleta moto =
                        criarMotocicletaAPartirDoCursor(cursor);

                lista.add(moto);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return lista;
    }

    /**
     * Atualiza todos os dados de uma motocicleta.
     *
     * Depois de qualquer alteração local,
     * a moto volta para NÃO sincronizada.
     */
    public boolean atualizar(Motocicleta moto) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

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

        // A alteração precisa ser sincronizada novamente.
        values.put(
                MotoDatabaseHelper.COL_SINCRONIZADO,
                0
        );

        int resultado =
                db.update(
                        MotoDatabaseHelper.TABLE_MOTOS,
                        values,
                        MotoDatabaseHelper.COL_ID + " = ?",
                        new String[]{moto.getId()}
                );

        db.close();

        return resultado > 0;
    }

    /**
     * Marca a motocicleta como sincronizada.
     */
    public boolean marcarComoSincronizada(
            String idMoto
    ) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_SINCRONIZADO,
                1
        );

        int resultado =
                db.update(
                        MotoDatabaseHelper.TABLE_MOTOS,
                        values,
                        MotoDatabaseHelper.COL_ID + " = ?",
                        new String[]{idMoto}
                );

        db.close();

        return resultado > 0;
    }

    /**
     * Atualiza somente a quilometragem.
     */
    public boolean atualizarQuilometragem(
            String idMoto,
            String novaQuilometragem
    ) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_QUILOMETRAGEM,
                novaQuilometragem
        );

        // Alterou localmente.
        values.put(
                MotoDatabaseHelper.COL_SINCRONIZADO,
                0
        );

        int resultado =
                db.update(
                        MotoDatabaseHelper.TABLE_MOTOS,
                        values,
                        MotoDatabaseHelper.COL_ID + " = ?",
                        new String[]{idMoto}
                );

        db.close();

        return resultado > 0;
    }

    /**
     * Busca todas as motocicletas que ainda
     * precisam ser sincronizadas com o Firebase.
     *
     * sincronizado = 0 significa que houve
     * alguma alteração local ainda não enviada
     * ou confirmada pelo Firebase.
     */
    public List<Motocicleta> listarNaoSincronizadas(
            String uidUsuario
    ) {

        List<Motocicleta> lista =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_MOTOS,
                null,
                MotoDatabaseHelper.COL_UID_USUARIO
                        + " = ? AND "
                        + MotoDatabaseHelper.COL_SINCRONIZADO
                        + " = ?",
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

                Motocicleta moto =
                        criarMotocicletaAPartirDoCursor(cursor);

                lista.add(moto);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return lista;
    }

    /**
     * Exclui uma motocicleta.
     */
    public boolean excluir(String idMoto) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        int resultado =
                db.delete(
                        MotoDatabaseHelper.TABLE_MOTOS,
                        MotoDatabaseHelper.COL_ID + " = ?",
                        new String[]{idMoto}
                );

        db.close();

        return resultado > 0;
    }

    /**
     * Cria um objeto Motocicleta a partir
     * dos dados encontrados no Cursor.
     */
    private Motocicleta criarMotocicletaAPartirDoCursor(
            Cursor cursor
    ) {

        String id =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_ID
                        )
                );

        String apelido =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_APELIDO
                        )
                );

        String marca =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MARCA
                        )
                );

        String modelo =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MODELO
                        )
                );

        String ano =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_ANO
                        )
                );

        String placa =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_PLACA
                        )
                );

        String quilometragem =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_QUILOMETRAGEM
                        )
                );

        return new Motocicleta(
                id,
                apelido,
                marca,
                modelo,
                ano,
                placa,
                quilometragem
        );
    }

    /**
     * Fecha o banco.
     */
    public void fechar() {

        databaseHelper.close();
    }
}