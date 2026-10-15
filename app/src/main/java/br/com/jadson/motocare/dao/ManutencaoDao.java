package br.com.jadson.motocare.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.com.jadson.motocare.activities.Manutencao;
import br.com.jadson.motocare.database.MotoDatabaseHelper;

public class ManutencaoDao {

    private final MotoDatabaseHelper databaseHelper;

    public ManutencaoDao(Context context) {

        databaseHelper =
                new MotoDatabaseHelper(context);
    }

    /**
     * Insere uma manutenção no banco local.
     *
     * A manutenção começa como NÃO sincronizada.
     */
    public boolean inserir(
            Manutencao manutencao
    ) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_ID,
                manutencao.getId()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_ID_MOTO,
                manutencao.getIdMoto()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_UID_USUARIO,
                manutencao.getUidUsuario()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_TIPO,
                manutencao.getTipo()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_DESCRICAO,
                manutencao.getDescricao()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_DATA,
                manutencao.getData()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_QUILOMETRAGEM,
                manutencao.getQuilometragem()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_VALOR,
                manutencao.getValor()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_SINCRONIZADO,
                0
        );

        long resultado =
                db.insert(
                        MotoDatabaseHelper.TABLE_MANUTENCOES,
                        null,
                        values
                );

        db.close();

        return resultado != -1;
    }

    /**
     * Busca uma manutenção pelo ID.
     */
    public Manutencao buscarPorId(
            String idManutencao
    ) {

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_MANUTENCOES,
                null,
                MotoDatabaseHelper.COL_MANUTENCAO_ID
                        + " = ?",
                new String[]{
                        idManutencao
                },
                null,
                null,
                null,
                "1"
        );

        Manutencao manutencao = null;

        if (cursor.moveToFirst()) {

            manutencao =
                    criarManutencaoAPartirDoCursor(
                            cursor
                    );
        }

        cursor.close();
        db.close();

        return manutencao;
    }

    /**
     * Lista todas as manutenções
     * de uma determinada motocicleta.
     */
    public List<Manutencao> listarPorMoto(
            String idMoto
    ) {

        List<Manutencao> lista =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_MANUTENCOES,
                null,
                MotoDatabaseHelper.COL_MANUTENCAO_ID_MOTO
                        + " = ?",
                new String[]{
                        idMoto
                },
                null,
                null,
                MotoDatabaseHelper.COL_MANUTENCAO_DATA
                        + " DESC"
        );

        if (cursor.moveToFirst()) {

            do {

                Manutencao manutencao =
                        criarManutencaoAPartirDoCursor(
                                cursor
                        );

                lista.add(manutencao);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return lista;
    }

    /**
     * Atualiza uma manutenção existente.
     *
     * Depois da alteração, ela volta para
     * NÃO sincronizada.
     */
    public boolean atualizar(
            Manutencao manutencao
    ) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_ID_MOTO,
                manutencao.getIdMoto()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_UID_USUARIO,
                manutencao.getUidUsuario()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_TIPO,
                manutencao.getTipo()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_DESCRICAO,
                manutencao.getDescricao()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_DATA,
                manutencao.getData()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_QUILOMETRAGEM,
                manutencao.getQuilometragem()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_VALOR,
                manutencao.getValor()
        );

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_SINCRONIZADO,
                0
        );

        int resultado =
                db.update(
                        MotoDatabaseHelper.TABLE_MANUTENCOES,
                        values,
                        MotoDatabaseHelper.COL_MANUTENCAO_ID
                                + " = ?",
                        new String[]{
                                manutencao.getId()
                        }
                );

        db.close();

        return resultado > 0;
    }

    /**
     * Exclui uma manutenção.
     */
    public boolean excluir(
            String idManutencao
    ) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        int resultado =
                db.delete(
                        MotoDatabaseHelper.TABLE_MANUTENCOES,
                        MotoDatabaseHelper.COL_MANUTENCAO_ID
                                + " = ?",
                        new String[]{
                                idManutencao
                        }
                );

        db.close();

        return resultado > 0;
    }

    /**
     * Busca as manutenções que ainda
     * precisam ser sincronizadas.
     */
    public List<Manutencao> listarNaoSincronizadas(
            String uidUsuario
    ) {

        List<Manutencao> lista =
                new ArrayList<>();

        SQLiteDatabase db =
                databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MotoDatabaseHelper.TABLE_MANUTENCOES,
                null,
                MotoDatabaseHelper.COL_MANUTENCAO_UID_USUARIO
                        + " = ? AND "
                        + MotoDatabaseHelper.COL_MANUTENCAO_SINCRONIZADO
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

                Manutencao manutencao =
                        criarManutencaoAPartirDoCursor(
                                cursor
                        );

                lista.add(manutencao);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return lista;
    }

    /**
     * Marca uma manutenção como sincronizada.
     */
    public boolean marcarComoSincronizada(
            String idManutencao
    ) {

        SQLiteDatabase db =
                databaseHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                MotoDatabaseHelper.COL_MANUTENCAO_SINCRONIZADO,
                1
        );

        int resultado =
                db.update(
                        MotoDatabaseHelper.TABLE_MANUTENCOES,
                        values,
                        MotoDatabaseHelper.COL_MANUTENCAO_ID
                                + " = ?",
                        new String[]{
                                idManutencao
                        }
                );

        db.close();

        return resultado > 0;
    }

    /**
     * Cria um objeto Manutencao
     * a partir do Cursor.
     */
    private Manutencao criarManutencaoAPartirDoCursor(
            Cursor cursor
    ) {

        String id =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_ID
                        )
                );

        String idMoto =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_ID_MOTO
                        )
                );

        String uidUsuario =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_UID_USUARIO
                        )
                );

        String tipo =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_TIPO
                        )
                );

        String descricao =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_DESCRICAO
                        )
                );

        String data =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_DATA
                        )
                );

        String quilometragem =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_QUILOMETRAGEM
                        )
                );

        String valor =
                cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_VALOR
                        )
                );

        int sincronizado =
                cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                MotoDatabaseHelper.COL_MANUTENCAO_SINCRONIZADO
                        )
                );

        return new Manutencao(
                id,
                idMoto,
                uidUsuario,
                tipo,
                descricao,
                data,
                quilometragem,
                valor,
                sincronizado == 1
        );
    }

    /**
     * Fecha o banco.
     */
    public void fechar() {

        databaseHelper.close();
    }
}