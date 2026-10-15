package br.com.jadson.motocare.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MotoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "motocare.db";
    private static final int DATABASE_VERSION = 2;

    // ==============================
    // TABELA DE MOTOCICLETAS
    // ==============================

    public static final String TABLE_MOTOS = "motocicletas";

    public static final String COL_ID = "id";
    public static final String COL_UID_USUARIO = "uid_usuario";
    public static final String COL_APELIDO = "apelido";
    public static final String COL_MARCA = "marca";
    public static final String COL_MODELO = "modelo";
    public static final String COL_ANO = "ano";
    public static final String COL_PLACA = "placa";
    public static final String COL_QUILOMETRAGEM = "quilometragem";
    public static final String COL_SINCRONIZADO = "sincronizado";


    // ==============================
    // TABELA DE MANUTENÇÕES
    // ==============================

    public static final String TABLE_MANUTENCOES =
            "manutencoes";

    public static final String COL_MANUTENCAO_ID =
            "id";

    public static final String COL_MANUTENCAO_ID_MOTO =
            "id_moto";

    public static final String COL_MANUTENCAO_UID_USUARIO =
            "uid_usuario";

    public static final String COL_MANUTENCAO_TIPO =
            "tipo";

    public static final String COL_MANUTENCAO_DESCRICAO =
            "descricao";

    public static final String COL_MANUTENCAO_DATA =
            "data";

    public static final String COL_MANUTENCAO_QUILOMETRAGEM =
            "quilometragem";

    public static final String COL_MANUTENCAO_VALOR =
            "valor";

    public static final String COL_MANUTENCAO_SINCRONIZADO =
            "sincronizado";


    public MotoDatabaseHelper(Context context) {

        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // ==============================
        // CRIA TABELA DE MOTOS
        // ==============================

        String sqlMotos =
                "CREATE TABLE "
                        + TABLE_MOTOS
                        + " ("
                        + COL_ID
                        + " TEXT PRIMARY KEY, "

                        + COL_UID_USUARIO
                        + " TEXT NOT NULL, "

                        + COL_APELIDO
                        + " TEXT NOT NULL, "

                        + COL_MARCA
                        + " TEXT NOT NULL, "

                        + COL_MODELO
                        + " TEXT NOT NULL, "

                        + COL_ANO
                        + " TEXT NOT NULL, "

                        + COL_PLACA
                        + " TEXT NOT NULL, "

                        + COL_QUILOMETRAGEM
                        + " TEXT NOT NULL, "

                        + COL_SINCRONIZADO
                        + " INTEGER NOT NULL DEFAULT 0"
                        + ")";

        db.execSQL(sqlMotos);


        // ==============================
        // CRIA TABELA DE MANUTENÇÕES
        // ==============================

        String sqlManutencoes =
                "CREATE TABLE "
                        + TABLE_MANUTENCOES
                        + " ("

                        + COL_MANUTENCAO_ID
                        + " TEXT PRIMARY KEY, "

                        + COL_MANUTENCAO_ID_MOTO
                        + " TEXT NOT NULL, "

                        + COL_MANUTENCAO_UID_USUARIO
                        + " TEXT NOT NULL, "

                        + COL_MANUTENCAO_TIPO
                        + " TEXT NOT NULL, "

                        + COL_MANUTENCAO_DESCRICAO
                        + " TEXT, "

                        + COL_MANUTENCAO_DATA
                        + " TEXT NOT NULL, "

                        + COL_MANUTENCAO_QUILOMETRAGEM
                        + " TEXT NOT NULL, "

                        + COL_MANUTENCAO_VALOR
                        + " TEXT NOT NULL, "

                        + COL_MANUTENCAO_SINCRONIZADO
                        + " INTEGER NOT NULL DEFAULT 0"

                        + ")";

        db.execSQL(sqlManutencoes);
    }


    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        // Versão 1 -> 2
        //
        // Não apagamos a tabela de motos.
        // Apenas adicionamos a nova tabela
        // de manutenções.

        if (oldVersion < 2) {

            String sqlManutencoes =
                    "CREATE TABLE "
                            + TABLE_MANUTENCOES
                            + " ("

                            + COL_MANUTENCAO_ID
                            + " TEXT PRIMARY KEY, "

                            + COL_MANUTENCAO_ID_MOTO
                            + " TEXT NOT NULL, "

                            + COL_MANUTENCAO_UID_USUARIO
                            + " TEXT NOT NULL, "

                            + COL_MANUTENCAO_TIPO
                            + " TEXT NOT NULL, "

                            + COL_MANUTENCAO_DESCRICAO
                            + " TEXT, "

                            + COL_MANUTENCAO_DATA
                            + " TEXT NOT NULL, "

                            + COL_MANUTENCAO_QUILOMETRAGEM
                            + " TEXT NOT NULL, "

                            + COL_MANUTENCAO_VALOR
                            + " TEXT NOT NULL, "

                            + COL_MANUTENCAO_SINCRONIZADO
                            + " INTEGER NOT NULL DEFAULT 0"

                            + ")";

            db.execSQL(sqlManutencoes);
        }
    }
}