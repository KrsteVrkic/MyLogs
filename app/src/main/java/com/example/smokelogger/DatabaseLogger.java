package com.example.smokelogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseLogger extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "personal_logs";
    private static final int DATABASE_VERSION = 3;

    public DatabaseLogger(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS log_table (date DATE, event VARCHAR(25))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS log_table");
        onCreate(db);
    }
 }
