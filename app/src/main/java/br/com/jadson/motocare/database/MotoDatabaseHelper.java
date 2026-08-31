package br.com.jadson.motocare.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MotoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "motocare.db";
    private static final int DATABASE_VERSION = 1;

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

    public MotoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + TABLE_MOTOS + " (" +
                COL_ID + " TEXT PRIMARY KEY, " +
                COL_UID_USUARIO + " TEXT NOT NULL, " +
                COL_APELIDO + " TEXT NOT NULL, " +
                COL_MARCA + " TEXT NOT NULL, " +
                COL_MODELO + " TEXT NOT NULL, " +
                COL_ANO + " TEXT NOT NULL, " +
                COL_PLACA + " TEXT NOT NULL, " +
                COL_QUILOMETRAGEM + " TEXT NOT NULL, " +
                COL_SINCRONIZADO + " INTEGER NOT NULL DEFAULT 0" +
                ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        db.execSQL(
                "DROP TABLE IF EXISTS " + TABLE_MOTOS
        );

        onCreate(db);
    }
}