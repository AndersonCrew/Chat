package com.dazone.crewchatoff.utils;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.activity.base.BaseActivity;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.customs.AlertDialogView;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.dto.ChattingDto;
import com.dazone.crewchatoff.dto.TreeUserDTOTemp;
import com.dazone.crewchatoff.dto.UserDto;
import com.dazone.crewchatoff.interfaces.DownLoadIMGFinish;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Utils {

    public static boolean isNetworkAvailable() {
        NetworkInfo networkInfo = getNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

    private static NetworkInfo getNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) CrewChatApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    public static String getString(int stringID) {
        return CrewChatApplication.getInstance().getApplicationContext().getResources().getString(stringID);
    }

    public static boolean checkStringValue(String... params) {
        for (String param : params) {
            if (TextUtils.isEmpty(param.trim())) {
                return false;
            }
            if (param.contains("\n") && TextUtils.isEmpty(param.replace("\n", ""))) {
                return false;
            }
        }
        return true;
    }

    public static int getDimenInPx(int id) {
        return (int) CrewChatApplication.getInstance().getApplicationContext().getResources().getDimension(id);
    }

    public static void addFragmentToActivity(FragmentManager fragmentManager, Fragment fragment, int frameLayout, boolean isSaveStack) {
        addFragmentToActivity(fragmentManager, fragment, frameLayout, isSaveStack, null);
    }

    public static void addFragmentToActivity(FragmentManager fragmentManager, Fragment fragment, int frameLayout, boolean isSaveStack, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (TextUtils.isEmpty(tag)) {
            transaction.add(frameLayout, fragment);
        } else {
            transaction.add(frameLayout, fragment, tag);
        }

        if (isSaveStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPathFromURI(Uri contentUri, Context context) {
        String result;
        try {
            Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentUri.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (idx == -1) {
                    result = Environment.getExternalStorageDirectory() + contentUri.getPath();
                    result = result.replace("/external_files", "");
                    if (result.contains("/emulated/0/emulated/0/")) {
                        result = result.replace("/emulated/0/emulated/0/", "/emulated/0/");
                    }
                } else {
                    result = cursor.getString(idx);
                }

                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        }
        return result;
    }

    public static String getPath(Activity context, Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public static String getPathImage (Activity context, Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public static void showMessage(String message) {
        Toast.makeText(CrewChatApplication.getInstance().getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public static void showMessageShort(String message) {
        Toast.makeText(CrewChatApplication.getInstance().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static TreeUserDTOTemp GetUserFromDatabase(List<TreeUserDTOTemp> list, int id) {
        for (TreeUserDTOTemp treeUserDTOTemp : list) {
            if (treeUserDTOTemp.getUserNo() == id) {
                return treeUserDTOTemp;
            }
        }
        return null;
    }

    public static TreeUserDTOTemp GetUserFromDatabase(ArrayList<TreeUserDTOTemp> list, int id) {
        for (TreeUserDTOTemp treeUserDTOTemp : list) {
            if (treeUserDTOTemp.getUserNo() == id) {
                return treeUserDTOTemp;
            }
        }
        return null;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private static boolean hasReadPermissions(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private static boolean hasWritePermissions(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public static void displayDownloadFileDialog(final Context context, final String url, final String name) {
        try {
            AlertDialogView.normalAlertDialogWithCancel(context, Utils.getString(R.string.app_name), Utils.getString(R.string.notice_download),
                    Utils.getString(R.string.no), Utils.getString(R.string.yes), new AlertDialogView.OnAlertDialogViewClickEvent() {
                        @Override
                        public void onOkClick(DialogInterface alertDialog) {
                            if (hasReadPermissions(context) && hasWritePermissions(context)) {
                                downloadEvent(context, url, name);
                            } else {
                                ActivityCompat.requestPermissions((Activity) context,
                                        new String[]{
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        }, Statics.REQUEST_CODE); // your request code
                            }

                        }

                        @Override
                        public void onCancelClick() {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(context.getString(R.string.download_file_error));
        }
    }

    public static void downloadEvent(final Context context, final String url, final String name) {

        File folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(folder, name);
        Uri uri =  FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.getPath());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        try{
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(name)
                    .setMimeType(mimeType) // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name.toLowerCase());
            dm.enqueue(request);
        }catch (Exception e){
            Toast.makeText(context, "Download failed.", Toast.LENGTH_SHORT).show();
        }

    }

    //1: Image
    //2: Video
    //3: Audio
    //4: DOC
    //5: XLS
    //6: PDF
    //7: PPT
    //8: zip
    //9: rar
    //10: apk
    //11: default
    public static int getTypeFile(String typeFile) {
        int type = 0;
        switch (typeFile) {
            case Statics.IMAGE_GIF:
            case Statics.IMAGE_JPEG:
            case Statics.IMAGE_JPG:
            case Statics.IMAGE_PNG:
                type = 1;
                break;
            case Statics.VIDEO_MP4:
            case Statics.VIDEO_MOV:
            case Statics.VIDEO_3GP:
                type = 2;
                break;
            case Statics.AUDIO_MP3:
            case Statics.AUDIO_AMR:
            case Statics.AUDIO_WMA:
                type = 3;
                break;
            case Statics.FILE_DOC:
            case Statics.FILE_DOCX:
                type = 4;
                break;
            case Statics.FILE_XLS:
            case Statics.FILE_XLSX:
                type = 5;
                break;
            case Statics.FILE_PDF:
                type = 6;
                break;
            case Statics.FILE_PPT:
            case Statics.FILE_PPTX:
                type = 7;
                break;
            case Statics.FILE_ZIP:
                type = 8;
                break;
            case Statics.FILE_RAR:
                type = 9;
                break;
            case Statics.FILE_APK:
                type = 10;
                break;
            default:
                type = 11;
                break;
        }
        return type;
    }

    public static Uri getImageContentUri(Context context, File imageFile, int flag) {
        String filePath = imageFile.getAbsolutePath();
        if (flag == Statics.MEDIA_TYPE_IMAGE) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse(Statics.NOTE_SUPPORT_URI_IMAGE);
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return context.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        } else {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media._ID},
                    MediaStore.Video.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse(Statics.NOTE_SUPPORT_URI_VIDEO);
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Video.Media.DATA, filePath);
                    return context.getContentResolver().insert(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        }

    }

    public static boolean isVideo(String fileName) {
        if (fileName == null || TextUtils.isEmpty(fileName)) {
            return false;
        }

        String fileType = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        int type = getTypeFile(fileType);

        return type == 2;
    }

    public static boolean isAudio(String fileType) {
        return fileType.equalsIgnoreCase(Statics.AUDIO_MP3) || fileType.equalsIgnoreCase(".wav") || fileType.equalsIgnoreCase(Statics.AUDIO_AMR)
                || fileType.equalsIgnoreCase(Statics.AUDIO_WMA) || fileType.equalsIgnoreCase(Statics.AUDIO_M4A);
    }

    public static int getTypeFileAttach(String fileType) {
        int type = 2;
        if (TextUtils.isEmpty(fileType))
            return type;
        else
            switch (fileType) {
                case Statics.IMAGE_JPEG:
                case Statics.IMAGE_JPG:
                case Statics.IMAGE_PNG:
                case Statics.IMAGE_GIF:
                    type = 1;
                    break;
                default:
                    type = 2;
                    break;
            }
        return type;
    }

    public static String getFileName(String path) {
        if (!TextUtils.isEmpty(path)) {
            return path.substring(path.lastIndexOf("/") + 1);
        } else {
            return "";
        }
    }

    public static String getFileType(String name) {
        try {
            if (!TextUtils.isEmpty(name)) {
                return name.substring(name.lastIndexOf(".")).toLowerCase();
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * chattingDto 의 글쓴이와 chattingDto2 의 글쓴이가 서로 다르고 chattingDto2의 채팅 타입이 1이 아닐 경우인지 체크한다.
     */
    public static boolean getChattingType(ChattingDto chattingDto, ChattingDto chattingDto2) {
        try {
            return (chattingDto.getWriterUser() == chattingDto2.getWriterUser()) && (chattingDto2.getType() != 1);
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean checkChatId198(ChattingDto chattingDto) {
        return false;
    }

    /**
     * CONVERT DP TO PX
     */
    public static int convertDipToPixels(Context context, float dips) {
        return (int) (dips * context.getResources().getDisplayMetrics().density + 0.5f);
    }


    /**
     * CHECK CALL VISIBLE
     */
    public static void addCallArray(ArrayList<Integer> userNos, ArrayAdapter<String> arrayAdapter, ArrayList<TreeUserDTOTemp> listUser) {
        for (int i : userNos) {
            if (i != UserDBHelper.getUser().Id) {
                TreeUserDTOTemp treeUserDTOTemp = Utils.GetUserFromDatabase(listUser, i);

                if (treeUserDTOTemp != null) {
                    String userName = treeUserDTOTemp.getName();
                    String phone = !TextUtils.isEmpty(treeUserDTOTemp.getCellPhone().trim()) ?
                            treeUserDTOTemp.getCellPhone() :
                            !TextUtils.isEmpty(treeUserDTOTemp.getCompanyPhone().trim()) ?
                                    treeUserDTOTemp.getCompanyPhone() :
                                    "";

                    if (!TextUtils.isEmpty(phone)) {
                        arrayAdapter.add(userName + " (" + phone + ")");
                    }
                }
            }
        }
    }

    /**
     * CALL PHONE CALL
     */
    public static void CallPhone(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }

    public static void sendSMS(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("vnd.android-dir/mms-sms");
        intent.setData(Uri.parse("sms:" + phoneNumber));
        context.startActivity(intent);
    }

    public static void sendMail(Context context, String eMail) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        String[] recipients = new String[]{eMail};
        Log.d("ssssssss", recipients[0]);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
        intent.setType("message/rfc822");
        context.startActivity(Intent.createChooser(intent, "Choose an Email client"));
    }

    public static int getCurrentId() {
        int myId = CrewChatApplication.currentId;

        if (myId == 0) {
            myId = new Prefs().getUserNo();
        }

        if (myId == 0) {
            myId = UserDBHelper.getUser().Id;
        }

        return myId;
    }

    public static UserDto getCurrentUser() {
        if (CrewChatApplication.currentUser != null) {
            return CrewChatApplication.currentUser;
        }

        return UserDBHelper.getUser();
    }

    /**
     * Remove duplicate item
     */
    public static void removeArrayDuplicate(ArrayList<Integer> ids) {
        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                if (ids.get(i).intValue() == ids.get(j).intValue()) {
                    ids.remove(j);
                    removeArrayDuplicate(ids);
                    break;
                }
            }
        }
    }

    public static void DownloadImage(final Context context, final String url, final String name) {
        String mimeType;
        String serviceString = Context.DOWNLOAD_SERVICE;
        String fileType = name.substring(name.lastIndexOf(".")).toLowerCase();
        final DownloadManager downloadmanager;
        downloadmanager = (DownloadManager) context.getSystemService(serviceString);
        Uri uri = Uri
                .parse(url);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Constant.pathDownload, name);
        int type = getTypeFile(fileType);
        switch (type) {
            case 1:
                request.setMimeType(Statics.MIME_TYPE_IMAGE);
                break;
            case 2:
                request.setMimeType(Statics.MIME_TYPE_VIDEO);
                break;
            case 3:
                request.setMimeType(Statics.MIME_TYPE_AUDIO);
                break;
            default:
                try {
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
                } catch (Exception e) {
                    e.printStackTrace();
                    mimeType = Statics.MIME_TYPE_ALL;
                }
                if (TextUtils.isEmpty(mimeType)) {
                    request.setMimeType(Statics.MIME_TYPE_ALL);
                } else {
                    request.setMimeType(mimeType);
                }
                break;
        }

        final Long reference = downloadmanager.enqueue(request);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    Cursor c = downloadmanager.query(query);

                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        }
                    }
                }
            }
        };

        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public static void DownloadImage_v2(final Context context, final String url, final String name, DownLoadIMGFinish callback) {
        String mimeType;
        String serviceString = Context.DOWNLOAD_SERVICE;
        String fileType = name.substring(name.lastIndexOf(".")).toLowerCase();
        final DownloadManager downloadmanager;
        downloadmanager = (DownloadManager) context.getSystemService(serviceString);
        Uri uri = Uri
                .parse(url);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Constant.pathDownload, name);
        int type = getTypeFile(fileType);
        switch (type) {
            case 1:
                request.setMimeType(Statics.MIME_TYPE_IMAGE);
                break;
            case 2:
                request.setMimeType(Statics.MIME_TYPE_VIDEO);
                break;
            case 3:
                request.setMimeType(Statics.MIME_TYPE_AUDIO);
                break;
            default:
                try {
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
                } catch (Exception e) {
                    e.printStackTrace();
                    mimeType = Statics.MIME_TYPE_ALL;
                }
                if (TextUtils.isEmpty(mimeType)) {
                    request.setMimeType(Statics.MIME_TYPE_ALL);
                } else {
                    request.setMimeType(mimeType);
                }
                break;
        }

        final Long reference = downloadmanager.enqueue(request);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    Cursor c = downloadmanager.query(query);

                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        }
                    }
                }
            }
        };

        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        callback.onSuccess();
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static int compareVersionNames(String oldVersionName, String newVersionName) {
        int res = 0;

        String[] oldNumbers = oldVersionName.split("\\.");
        String[] newNumbers = newVersionName.split("\\.");

        // To avoid IndexOutOfBounds
        int maxIndex = Math.min(oldNumbers.length, newNumbers.length);

        for (int i = 0; i < maxIndex; i++) {
            int oldVersionPart = Integer.valueOf(oldNumbers[i]);
            int newVersionPart = Integer.valueOf(newNumbers[i]);

            if (oldVersionPart < newVersionPart) {
                res = -1;
                break;
            } else if (oldVersionPart > newVersionPart) {
                res = 1;
                break;
            }
        }

        // If versions are the same so far, but they have different length...
        if (res == 0 && oldNumbers.length != newNumbers.length) {
            res = (oldNumbers.length > newNumbers.length) ? 1 : -1;
        }

        return res;
    }

    public static void oneButtonAlertDialog(final Activity context, String title, String message, String okButton) {
        // Build an AlertDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_common, null);

        // Set the custom layout as alert dialog view
        builder.setView(dialogView);

        // Get the custom alert dialog view widgets reference
        Button btn_positive = dialogView.findViewById(R.id.btn_yes);
        Button btn_negative = dialogView.findViewById(R.id.btn_no);
        TextView txtTitle = dialogView.findViewById(R.id.txt_dialog_title);
        TextView txtContent = dialogView.findViewById(R.id.txt_dialog_content);

        btn_negative.setVisibility(View.GONE);
        btn_positive.setText(okButton);

        txtTitle.setText(title);
        txtContent.setText(message);

        final android.app.AlertDialog dialog = builder.create();

        btn_positive.setOnClickListener(v -> {
            dialog.cancel();
        });

        if (!context.isFinishing()) {
            dialog.show();
        }
    }

    public static void customAlertDialog(final Activity context, String title, String message, String okButton, String noButton, final DialogUtils.OnAlertDialogViewClickEvent clickEvent) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_common, null);

        builder.setView(dialogView);
        Button btn_positive = dialogView.findViewById(R.id.btn_yes);
        Button btn_negative = dialogView.findViewById(R.id.btn_no);
        TextView txtTitle = dialogView.findViewById(R.id.txt_dialog_title);
        TextView txtContent = dialogView.findViewById(R.id.txt_dialog_content);

        btn_negative.setText(noButton);
        btn_positive.setText(okButton);
        txtTitle.setText(title);
        txtContent.setText(message);

        final android.app.AlertDialog dialog = builder.create();

        btn_positive.setOnClickListener(v -> {
            if (clickEvent != null) {
                clickEvent.onOkClick(dialog);
            }
            dialog.dismiss();
        });

        btn_negative.setOnClickListener(v -> {
            if (clickEvent != null) {
                clickEvent.onCancelClick();
            }
            dialog.cancel();
        });
        dialog.show();
    }

    public static void appendLog(String text) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String formatted = format1.format(Calendar.getInstance().getTime());
        String line = String.format("%s: %s", formatted, text);
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/logs.log";
        File logFile = new File(filePath);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Log.d("appendLog", e.getMessage());
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(line);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ChattingDto> checkExist(ChattingDto chattingDto, List<ChattingDto> list) {
        List<ChattingDto> tempList = new ArrayList<>();
        tempList.addAll(list);
        boolean isExist = false;
        for (int i = 0; i < list.size(); i++) {
            if (chattingDto.getMessageNo() == list.get(i).getMessageNo()) {
                tempList.remove(i);
                tempList.add(chattingDto);
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            list.add(chattingDto);
        }

        return tempList;
    }

    public static int getPosition(ChattingDto dto, List<ChattingDto> list) {
        for (ChattingDto chattingDto : list) {
            if (chattingDto.getMessageNo() == dto.getMessageNo()) {
                return list.indexOf(chattingDto);
            }
        }

        return list.size() - 1;
    }

    public static String getStrDate(ChattingDto dto) {
        if (TimeUtils.checkDateIsToday(dto.getRegDate())) {
            return Utils.getString(R.string.today);
        } else if (TimeUtils.checkDateIsYesterday(dto.getRegDate())) {
            return Utils.getString(R.string.yesterday);
        } else {
            long chatTime = new Date(TimeUtils.getTime(dto.getRegDate())).getTime();
            return TimeUtils.showTimeWithoutTimeZone(chatTime, Statics.DATE_FORMAT_YYYY_MM_DD);
        }
    }

    public static String setServerSite(String domain) {
        String[] domains = domain.split("[.]");
        if (domain.contains(".bizsw.co.kr") && !domain.contains("8080")) {
            domain = domain.replace(".bizsw.co.kr", ".bizsw.co.kr:8080");
        }

        if (domains.length == 1) {
            domain = domains[0] + ".crewcloud.net";
        }

        if(domain.startsWith("http://") || domain.startsWith("https://")){
            CrewChatApplication.getInstance().getPrefs().putStringValue(Constants.DOMAIN, domain);
            String companyName = domain.startsWith("http://") ? domain.replace("http://", ""): domain.startsWith("https://") ? domain.replace("https://", ""): domain;
            CrewChatApplication.getInstance().getPrefs().putStringValue(Constants.COMPANY_NAME, companyName);
            return domain;
        }

        String head = CrewChatApplication.getInstance().getPrefs().getBooleanValue(Constants.HAS_SSL, false) ? "https://" : "http://";
        String domainCompany = head + domain;
        CrewChatApplication.getInstance().getPrefs().putStringValue(Constants.DOMAIN, domainCompany);
        CrewChatApplication.getInstance().getPrefs().putStringValue(Constants.COMPANY_NAME, domain);
        return domainCompany;
    }

    public static String getTimeNewChat(long diffTime) {
        return TimeUtils.convertTimeDeviceToTimeServerDefault(CrewChatApplication.getInstance().getTimeLocal() + diffTime +"");
    }

    public static String getTimeFormat(long time) {
        return TimeUtils.showTimeWithoutTimeZone(time, Statics.yyyy_MM_dd_HH_mm_ss_SSS);
    }
}