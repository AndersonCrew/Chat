package com.dazone.crewchatoff.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.dazone.crewchatoff.Tree.Dtos.TreeUserDTO;
import com.dazone.crewchatoff.utils.CrewChatApplication;

import java.util.ArrayList;
import java.util.List;

public class DepartmentDBHelper {
    public static final String TABLE_NAME = "DepartmentTbl";

    public static final String ID = "Id";
    public static final String DEPART_NO = "depart_no";
    public static final String DEPART_NAME = "depart_name";
    public static final String DEPART_ENABLE = "depart_enable";
    public static final String DEPART_MODATE = "depart_mod_date";
    public static final String DEPART_CHILD_DEPART = "depart_child_depart";
    public static final String DEPART_SHORT_NAME = "depart_short_name";
    public static final String DEPART_PARENT_NO = "depart_parent_number";
    public static final String DEPART_NAME_EN = "depart_name_en";
    public static final String DEPART_SORT_NO = "depart_sort_number";
    public static final String DEPART_MOD_USER_NO = "depart_mod_user_no";
    public static final String DEPART_NAME_DEFAULT = "depart_name_default";

    public static final String SQL_EXCUTE = "CREATE TABLE " + TABLE_NAME + "("
            + ID + " integer primary key autoincrement not null, "
            + DEPART_NO + " integer, "
            + DEPART_NAME + " text, "
            + DEPART_ENABLE + " integer, "
            + DEPART_MODATE + " text, "
            + DEPART_CHILD_DEPART + " text, "
            + DEPART_SHORT_NAME + " text, "
            + DEPART_PARENT_NO + " integer, "
            + DEPART_NAME_EN + " text, "
            + DEPART_SORT_NO + " integer, "
            + DEPART_MOD_USER_NO + " integer, "
            + DEPART_NAME_DEFAULT + " text);";


    public synchronized static ArrayList<TreeUserDTO> getDepartments_v2() {
        ArrayList<TreeUserDTO> departs = new ArrayList<>();

        String[] columns = new String[]{"*"};
        ContentResolver resolver = CrewChatApplication.getInstance()
                .getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(
                AppContentProvider.GET_DEPARTMENT_CONTENT_URI, columns, null,
                null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)));
                        int depart_no = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DEPART_NO)));
                        String name = cursor.getString(cursor.getColumnIndex(DEPART_NAME));
                        int status = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DEPART_ENABLE)));
                        int parent_id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DEPART_PARENT_NO)));
                        String name_en = cursor.getString(cursor.getColumnIndex(DEPART_NAME_EN));
                        int mNumSort = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DEPART_SORT_NO)));

                        TreeUserDTO depart = new TreeUserDTO(
                                id,
                                depart_no,
                                status,
                                null
                                ,
                                name,
                                name_en,
                                parent_id,
                                mNumSort
                        );
                        depart.setType(0); // value 0 = department type
                        departs.add(depart);
                    }

                } finally {
                    cursor.close();
                }

            }
            cursor.close();
        }
        return departs;
    }


    // this version just get one user
    public synchronized static ArrayList<TreeUserDTO> getDepartments() {
        ArrayList<TreeUserDTO> departs = new ArrayList<>();
        return departs;
    }

    public synchronized static boolean isExist(TreeUserDTO depart) {

        String[] columns = new String[]{"*"};
        ContentResolver resolver = CrewChatApplication.getInstance()
                .getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(AppContentProvider.GET_DEPARTMENT_CONTENT_URI, columns, DEPART_NO + "=" + depart.getId(), null, null);

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


    public static void updateDepart(TreeUserDTO depart) {
        try {
            ContentResolver resolver = CrewChatApplication.getInstance()
                    .getApplicationContext().getContentResolver();
            ContentValues values = new ContentValues();

            values.put(DEPART_NO, depart.getId());
            values.put(DEPART_NAME, depart.getName());
            if (depart.isEnabled()) {
                depart.setStatus(0);
            } else {
                depart.setStatus(1);
            }
            values.put(DEPART_ENABLE, depart.getStatus());
            values.put(DEPART_PARENT_NO, depart.getParent());
            values.put(DEPART_NAME_EN, depart.getNameEN());
            values.put(DEPART_SORT_NO, depart.getmSortNo());

            resolver.update(AppContentProvider.GET_DEPARTMENT_CONTENT_URI, values, DEPART_NO + "=" + depart.getId(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void addDepartment(List<TreeUserDTO> departs) {
        try {
            for (TreeUserDTO depart : departs) {
                if (!isExist(depart)) {
                    Log.d("addDepartment", "addDepartment");
                    ContentValues values = new ContentValues();
                    values.put(DEPART_NO, depart.getId());
                    values.put(DEPART_NAME, depart.getName());
                    values.put(DEPART_ENABLE, depart.getStatus());
                    values.put(DEPART_PARENT_NO, depart.getParent());
                    values.put(DEPART_NAME_EN, depart.getNameEN());
                    values.put(DEPART_SORT_NO, depart.getmSortNo());

                    ContentResolver resolver = CrewChatApplication.getInstance()
                            .getApplicationContext().getContentResolver();
                    resolver.insert(AppContentProvider.GET_DEPARTMENT_CONTENT_URI, values);
                } else {
                    updateDepart(depart);
                }
                if (depart.getSubordinates() != null && depart.getSubordinates().size() > 0) {
                    addDepartment(depart.getSubordinates());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean addDepartment(TreeUserDTO depart) {
        try {
            if (!isExist(depart)) {
                ContentValues values = new ContentValues();
                values.put(DEPART_NO, depart.getId());
                values.put(DEPART_NAME, depart.getName());
                values.put(DEPART_ENABLE, depart.getStatus());
                values.put(DEPART_PARENT_NO, depart.getParent());
                values.put(DEPART_NAME_EN, depart.getNameEN());
                values.put(DEPART_SORT_NO, depart.getmSortNo());

                ContentResolver resolver = CrewChatApplication.getInstance()
                        .getApplicationContext().getContentResolver();
                resolver.insert(AppContentProvider.GET_DEPARTMENT_CONTENT_URI, values);
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean clearDepartment() {
        try {

            ContentResolver resolver = CrewChatApplication.getInstance()
                    .getApplicationContext().getContentResolver();
            resolver.delete(AppContentProvider.GET_DEPARTMENT_CONTENT_URI, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
