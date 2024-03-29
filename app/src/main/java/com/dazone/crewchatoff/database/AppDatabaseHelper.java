package com.dazone.crewchatoff.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.dazone.crewchatoff.HTTPs.HttpOauthRequest;
import com.dazone.crewchatoff.HTTPs.HttpRequest;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.ErrorDto;
import com.dazone.crewchatoff.interfaces.BaseHTTPCallBack;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;


public class AppDatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = Statics.DATABASE_NAME;
    public static final int DB_VERSION = Statics.DATABASE_VERSION;
    private Context mContext;
    public static final String[] TABLE_NAMES = new String[]{
            UserDBHelper.TABLE_NAME,
            ServerSiteDBHelper.TABLE_NAME,
            AllUserDBHelper.TABLE_NAME,
            ChatMessageDBHelper.TABLE_NAME,
            ChatRoomDBHelper.TABLE_NAME,
            DepartmentDBHelper.TABLE_NAME,
            FavoriteUserDBHelper.TABLE_NAME,
            FavoriteGroupDBHelper.TABLE_NAME,
            BelongsToDBHelper.TABLE_NAME
    };
    public static final String[] EXECUTE = new String[]{
            UserDBHelper.SQL_EXECUTE,
            ServerSiteDBHelper.SQL_EXECUTE,
            AllUserDBHelper.SQL_EXECUTE,
            ChatMessageDBHelper.SQL_EXECUTE,
            ChatRoomDBHelper.SQL_EXECUTE,
            DepartmentDBHelper.SQL_EXCUTE,
            FavoriteUserDBHelper.SQL_EXECUTE,
            FavoriteGroupDBHelper.SQL_EXECUTE,
            BelongsToDBHelper.SQL_EXECUTE
    };
    public static final String[] EXECUTE1 = new String[]{
            UserDBHelper.SQL_EXECUTE,
            ServerSiteDBHelper.SQL_EXECUTE,
            // AllUserDBHelper.SQL_EXECUTE1,
            ChatMessageDBHelper.SQL_EXECUTE,
            ChatRoomDBHelper.SQL_EXECUTE,
            DepartmentDBHelper.SQL_EXCUTE,
            FavoriteUserDBHelper.SQL_EXECUTE,
            FavoriteGroupDBHelper.SQL_EXECUTE,
            BelongsToDBHelper.SQL_EXECUTE
    };

    public AppDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AppDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            execMultipleSQL(db, EXECUTE);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            throw e;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            dropMultipleSQL(db, TABLE_NAMES);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.d("dbh", "oldVersion" + oldVersion);
        try {
            if (oldVersion < newVersion) {
                Log.d("dbh", "create");
                // execSQL(db,EXECUTE1[2]);
                //db.execSQL(EXECUTE1[2]);
                execMultipleSQL(db, EXECUTE1);
                doLogout();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doLogout() {
        new Prefs().putIntValue("PAGE", 0);
        String ids = new Prefs().getGCMregistrationid();
        if (!TextUtils.isEmpty(ids)) {
            HttpRequest.getInstance().DeleteDevice(ids, new BaseHTTPCallBack() {
                @Override
                public void onHTTPSuccess() {

                    HttpOauthRequest.getInstance().logout(new BaseHTTPCallBack() {
                        @Override
                        public void onHTTPSuccess() {
                            // New thread to clear all cache
                            CrewChatApplication.isAddUser = false;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    BelongsToDBHelper.clearBelong();
//                                    Log.d(TAG,"before delete AllUserDBHelper.getUser():"+AllUserDBHelper.getUser().size());
                                    AllUserDBHelper.clearUser();
//                                    Log.d(TAG,"after delete AllUserDBHelper.getUser():"+AllUserDBHelper.getUser().size());
                                    ChatRoomDBHelper.clearChatRooms();
                                    ChatMessageDBHelper.clearMessages();
                                    DepartmentDBHelper.clearDepartment();
                                    UserDBHelper.clearUser();
                                    FavoriteGroupDBHelper.clearGroups();
                                    FavoriteUserDBHelper.clearFavorites();
                                    // CrewChatApplication.getInstance().getPrefs().clear();
                                    CrewChatApplication.resetValue();
                                    CrewChatApplication.isLoggedIn = false;


                                }
                            }).start();
                        }

                        @Override
                        public void onHTTPFail(ErrorDto errorDto) {

                        }
                    });

                }

                @Override
                public void onHTTPFail(ErrorDto errorDto) {

                }
            });
        }


    }

    private void execMultipleSQL(SQLiteDatabase db, String[] sql) throws android.database.SQLException {
        for (String s : sql) {
            if (s.trim().length() > 0) {
                try {
                    db.execSQL(s);
                } catch (android.database.SQLException e) {
                    throw new android.database.SQLException();
                }
            }
        }
    }

    private void execSQL(SQLiteDatabase db, String s) throws android.database.SQLException {
        if (s.trim().length() > 0) {
            try {
                db.execSQL(s);
            } catch (android.database.SQLException e) {
                throw new android.database.SQLException();
            }
        }
    }

    private void dropMultipleSQL(SQLiteDatabase db, String[] tablename) throws android.database.SQLException {
        for (String s : tablename) {
            if (s.trim().length() > 0) {
                try {
                    db.execSQL("DROP TABLE IF EXISTS " + s + ";");
                } catch (android.database.SQLException e) {
                    throw new android.database.SQLException();
                }
            }
        }
    }
}