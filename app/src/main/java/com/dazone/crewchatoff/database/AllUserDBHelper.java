package com.dazone.crewchatoff.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.dazone.crewchatoff.dto.BelongDepartmentDTO;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AllUserDBHelper {
    public static final String TAG = "AllUserDBHelper";
    public static final String TABLE_NAME = "AllAccountTbl";
    public static final String ID = "Id";
    public static final String ALL_DEPART_NO = "depart_no";
    public static final String ALL_USER_ID = "user_id";
    public static final String ALL_USER_NO = "user_no";
    public static final String ALL_POSITION = "position";
    public static final String ALL_USER_NAME = "name";
    public static final String ALL_USER_NAME_EN = "name_en";
    public static final String ALL_AVATAR_URL = "avatar_url";
    public static final String ALL_CELL_PHONE = "cell_phone";
    public static final String ALL_COMPANY_NUMBER = "company_number";
    public static final String ALL_USER_STATUS = "user_status";
    public static final String ALL_USER_ENABLE = "Enabled";
    public static final String ALL_USER_STATUS_STRING = "user_status_string";

    public static final String SQL_EXECUTE = "CREATE TABLE " + TABLE_NAME + "("
            + ID + " integer primary key autoincrement not null," + ALL_DEPART_NO + " integer," + ALL_USER_ID + " integer," + ALL_POSITION
            + " text, " + ALL_USER_NAME + " text, " + ALL_USER_NAME_EN + " text, "
            + ALL_USER_STATUS + " integer, "
            + ALL_USER_STATUS_STRING + " text, "
            + ALL_USER_NO + " integer, "
            + ALL_AVATAR_URL + " text, "
            + ALL_CELL_PHONE + " text,"
            + ALL_COMPANY_NUMBER + " text );";

    public static ArrayList<TreeUserDTOTemp> getUser_v2() {
        ArrayList<TreeUserDTOTemp> arrayList = new ArrayList<>();
        List<BelongDepartmentDTO> allOfBelongs = BelongsToDBHelper.getAllOfBelongs();
        String[] columns = new String[]{"*"};
        ContentResolver resolver = CrewChatApplication.getInstance().getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(AppContentProvider.GET_ALL_CONTENT_URI, columns, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    int _ID = cursor.getColumnIndex(ID);
                    int _ALL_USER_NO = cursor.getColumnIndex(ALL_USER_NO);
                    int _ALL_USER_ID = cursor.getColumnIndex(ALL_USER_ID);
                    int _ALL_DEPART_NO = cursor.getColumnIndex(ALL_DEPART_NO);
                    int _ALL_POSITION = cursor.getColumnIndex(ALL_POSITION);
                    int _ALL_AVATAR_URL = cursor.getColumnIndex(ALL_AVATAR_URL);
                    int _ALL_CELL_PHONE = cursor.getColumnIndex(ALL_CELL_PHONE);
                    int _ALL_COMPANY_NUMBER = cursor.getColumnIndex(ALL_COMPANY_NUMBER);
                    int _ALL_USER_NAME = cursor.getColumnIndex(ALL_USER_NAME);
                    int _ALL_USER_NAME_EN = cursor.getColumnIndex(ALL_USER_NAME_EN);
                    int _ALL_USER_STATUS = cursor.getColumnIndex(ALL_USER_STATUS);
                    int _ALL_USER_STATUS_STRING = cursor.getColumnIndex(ALL_USER_STATUS_STRING);

                    while (!cursor.isLast()) {
                        cursor.moveToNext();

                        TreeUserDTOTemp userDto = new TreeUserDTOTemp();
                        userDto.setDBId(Integer.parseInt(cursor.getString(_ID)));

                        int userNo = Integer.parseInt(cursor.getString(_ALL_USER_NO));
                        userDto.UserNo = userNo;
                        userDto.UserID = cursor.getString(_ALL_USER_ID);
                        userDto.DepartNo = Integer.parseInt(cursor.getString(_ALL_DEPART_NO));
                        userDto.Position = cursor.getString(_ALL_POSITION);
                        userDto.AvatarUrl = cursor.getString(_ALL_AVATAR_URL);
                        userDto.CellPhone = cursor.getString(_ALL_CELL_PHONE);
                        userDto.CompanyPhone = cursor.getString(_ALL_COMPANY_NUMBER);
                        userDto.Name = cursor.getString(_ALL_USER_NAME);
                        userDto.NameEN = cursor.getString(_ALL_USER_NAME_EN);

                        String statusStr = cursor.getString(_ALL_USER_STATUS) == null ? "0" : cursor.getString(_ALL_USER_STATUS);
                        userDto.setStatus(Integer.parseInt(statusStr));

                        ArrayList<BelongDepartmentDTO> _belongs = new ArrayList<>();

                        for (BelongDepartmentDTO belong : allOfBelongs) {
                            if (belong.UserNo == userNo) {
                                _belongs.add(belong);
                            }
                        }
                        userDto.setBelongs(_belongs);
                        userDto.setUserStatusString(cursor.getString(_ALL_USER_STATUS_STRING));

                        arrayList.add(userDto);
                    }
                } finally {
                    cursor.close();
                }
            }

            cursor.close();
        }

        return arrayList;
    }

    public static ArrayList<TreeUserDTOTemp> getUser() {
        TinyDB tinyDB = new TinyDB(CrewChatApplication.getInstance());
        ArrayList<TreeUserDTOTemp> arrayList;
        arrayList = (tinyDB.getListTreeTmp("user", TreeUserDTOTemp.class));
        return arrayList;
    }

    public static TreeUserDTOTemp getAUser(long userNo) {
        String[] columns = new String[]{"*"};
        ContentResolver resolver = CrewChatApplication.getInstance().getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(AppContentProvider.GET_ALL_CONTENT_URI, columns, ALL_USER_NO + "=" + userNo, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    cursor.moveToFirst();
                    TreeUserDTOTemp userDto = new TreeUserDTOTemp();
                    userDto.setDBId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID))));
                    userDto.setUserNo(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ALL_USER_NO))));
                    userDto.setUserID(cursor.getString(cursor.getColumnIndex(ALL_USER_ID)));
                    userDto.setDepartNo(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ALL_DEPART_NO))));
                    userDto.setPosition(cursor.getString(cursor.getColumnIndex(ALL_POSITION)));
                    userDto.setAvatarUrl(cursor.getString(cursor.getColumnIndex(ALL_AVATAR_URL)));
                    userDto.setCellPhone(cursor.getString(cursor.getColumnIndex(ALL_CELL_PHONE)));
                    userDto.setCompanyPhone(cursor.getString(cursor.getColumnIndex(ALL_COMPANY_NUMBER)));
                    userDto.setName(cursor.getString(cursor.getColumnIndex(ALL_USER_NAME)));
                    userDto.setNameEN(cursor.getString(cursor.getColumnIndex(ALL_USER_NAME_EN)));
                    userDto.setStatus(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ALL_USER_STATUS))));

                    String statusString = cursor.getString(cursor.getColumnIndex(ALL_USER_STATUS_STRING));

                    // Get belong to here
                    userDto.setBelongs(BelongsToDBHelper.getBelongs(userNo));
                    userDto.setUserStatusString(statusString);
                    return userDto;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }

            cursor.close();
        }

        return null;
    }

    public static String getAUserStatus(int userNo) {
        String[] columns = new String[]{ALL_USER_STATUS_STRING};
        ContentResolver resolver = CrewChatApplication.getInstance().getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(AppContentProvider.GET_ALL_CONTENT_URI, columns, ALL_USER_NO + "=" + userNo, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndex(ALL_USER_STATUS_STRING));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }

            cursor.close();
        }

        return null;
    }

    public static boolean updateStatus(int userId, int status) {

        try {
            ContentResolver resolver = CrewChatApplication.getInstance().getApplicationContext().getContentResolver();
            ContentValues conValues = new ContentValues();
            conValues.put(ALL_USER_STATUS, status);
            resolver.update(AppContentProvider.GET_ALL_CONTENT_URI, conValues, ID + "=" + userId, null);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updateStatusString(int userId, String status) {
        try {
            ContentResolver resolver = CrewChatApplication.getInstance().getApplicationContext().getContentResolver();
            ContentValues conValues = new ContentValues();
            conValues.put(ALL_USER_STATUS_STRING, status);
            resolver.update(AppContentProvider.GET_ALL_CONTENT_URI, conValues, ID + "=" + userId, null);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updateUser(TreeUserDTOTemp treeUserDTOTemp) {
        try {
            ContentResolver resolver = CrewChatApplication.getInstance().getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(ALL_DEPART_NO, treeUserDTOTemp.getDepartNo());
            values.put(ALL_USER_ID, treeUserDTOTemp.getUserID());
            values.put(ALL_USER_NO, treeUserDTOTemp.getUserNo());
            values.put(ALL_AVATAR_URL, treeUserDTOTemp.getAvatarUrl());
            values.put(ALL_POSITION, treeUserDTOTemp.getBelongs().get(0).getPositionName());
            values.put(ALL_CELL_PHONE, treeUserDTOTemp.getCellPhone());
            values.put(ALL_COMPANY_NUMBER, treeUserDTOTemp.getCompanyPhone());
            values.put(ALL_USER_NAME, treeUserDTOTemp.getName());
            values.put(ALL_USER_NAME_EN, treeUserDTOTemp.getNameEN());
            values.put(ALL_USER_STATUS_STRING, treeUserDTOTemp.getUserStatusString());
            if (treeUserDTOTemp.getEnabled()) {
                treeUserDTOTemp.setStatus(0);
            } else {
                treeUserDTOTemp.setStatus(1);
            }
            values.put(ALL_USER_STATUS, treeUserDTOTemp.getStatus());
            resolver.update(AppContentProvider.GET_ALL_CONTENT_URI, values, ALL_USER_NO + "=" + treeUserDTOTemp.getUserNo(), null);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public synchronized static boolean isExist(TreeUserDTOTemp temp) {
        String[] columns = new String[]{"*"};
        ContentResolver resolver = CrewChatApplication.getInstance().getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(AppContentProvider.GET_ALL_CONTENT_URI, columns, ALL_USER_NO + "=" + temp.getUserNo(), null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            } else {
                cursor.close();
            }
        }

        return false;
    }

    public synchronized static boolean addUser(List<TreeUserDTOTemp> list) {
        for (TreeUserDTOTemp treeUserDTOTemp : list) {
            Log.d(TAG, "isAddUser" + CrewChatApplication.isAddUser);
            if (CrewChatApplication.isAddUser) {
                // Check user before insert
                if (!isExist(treeUserDTOTemp)) {
                    Log.d(TAG, "!isExist(treeUserDTOTemp)");
                    // perform insert to belongs to
                    BelongsToDBHelper.clearBelongWithUserNo(treeUserDTOTemp.getUserNo());
                    boolean isSuccess = BelongsToDBHelper.addDepartment(treeUserDTOTemp.getBelongs());
                    if (isSuccess) {
                        // perform insert to database

                        Log.d(TAG, "addUser" + "isSuccess");
                        ContentValues values = new ContentValues();
                        values.put(ALL_DEPART_NO, treeUserDTOTemp.getDepartNo());
                        values.put(ALL_USER_ID, treeUserDTOTemp.getUserID());
                        values.put(ALL_USER_NO, treeUserDTOTemp.getUserNo());
                        values.put(ALL_AVATAR_URL, treeUserDTOTemp.getAvatarUrl());

                        values.put(ALL_POSITION, treeUserDTOTemp.getPosition());
                        values.put(ALL_CELL_PHONE, treeUserDTOTemp.getCellPhone());
                        values.put(ALL_COMPANY_NUMBER, treeUserDTOTemp.getCompanyPhone());
                        values.put(ALL_USER_NAME, treeUserDTOTemp.getName());
                        values.put(ALL_USER_NAME_EN, treeUserDTOTemp.getNameEN());
                        values.put(ALL_USER_STATUS_STRING, treeUserDTOTemp.getUserStatusString());
                        values.put(ALL_USER_STATUS, treeUserDTOTemp.getStatus());
                        try {
                            //  values.put(ALL_USER_ENABLE, treeUserDTOTemp.getEnabled());
                            Log.d("printStackTrace", "values" + values.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("printStackTrace", "printStackTrace");
                        }
                        ContentResolver resolver = CrewChatApplication.getInstance()
                                .getApplicationContext().getContentResolver();
                        resolver.insert(AppContentProvider.GET_ALL_CONTENT_URI, values);
                    } else {
                        return false;
                    }
                } else { // Perform update user to database
                    Log.d(TAG, "updateUser:" + new Gson().toJson(treeUserDTOTemp));
                    try {
                        BelongsToDBHelper.clearBelongWithUserNo(treeUserDTOTemp.getUserNo());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateUser(treeUserDTOTemp);
                }
            } else {
                Log.d(TAG, "not addUser");
                break;
            }

        }

        return true;
    }

    public static boolean clearUser() {
        try {
            ContentResolver resolver = CrewChatApplication.getInstance().getApplicationContext().getContentResolver();
            resolver.delete(AppContentProvider.GET_ALL_CONTENT_URI, null, null);
            Log.d(TAG, "clearUser");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}