package com.dazone.crewchatoff.sqlite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataBaseDAO {
    protected SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context mContext;

    public DataBaseDAO(Context context) {
        this.mContext = context;
        dbHelper = DataBaseHelper.getInstance(mContext);
        open();

    }

    public void open() throws SQLException {
        if (dbHelper == null)
            dbHelper = DataBaseHelper.getInstance(mContext);
        database = dbHelper.getWritableDatabase();
    }
}