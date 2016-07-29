package com.genisky.account;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private  static final String DatabaseName = "clocking.db";
    private static final int DatabaseVersion = 1;

    public DatabaseHelper(Context context) {
        super(context, DatabaseName, null, DatabaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS account (" +
                "id INTEGER PRIMARY KEY, " +
                "services VARCHAR, " +
                "token VARCHAR, " +
                "phone VARCHAR, " +
                "password VARCHAR, " +
                "imei VARCHAR" +
//                "name VARCHAR, " +
//                "title VARCHAR, " +
//                "picture VARCHAR," +
//                "department VARCHAR, " +
//                "company VARCHAR, " +
//                "companyPicture VARCHAR, " +
//                "state VARCHAR" +
                ")");
        database.execSQL("CREATE TABLE IF NOT EXISTS message (" +
                "id INTEGER PRIMARY KEY, " +
                "datetime VARCHAR, " +
                "title VARCHAR, " +
                "content VARCHAR, " +
                "read INTEGER " +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
