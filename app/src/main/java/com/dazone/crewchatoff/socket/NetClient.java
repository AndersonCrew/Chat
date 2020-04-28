package com.dazone.crewchatoff.socket;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ProgressBar;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.dto.AttachDTO;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;
import com.dazone.crewchatoff.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static com.dazone.crewchatoff.constant.Statics.CHOOSE_OPTION_IMAGE;

public class NetClient {

    private static NotificationCompat.Builder notificationBuilder;
    private static NotificationManager notificationManager;
    private static Integer notificationIDUpload = 200;
    private static int sdk = android.os.Build.VERSION.SDK_INT;

    /**
     * Maximum size of buffer
     */
    private Socket socket = null;
    private OutputStream out = null;
    private InputStream in = null;

    private String host = null;
    private int port = 9999;

    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private String domainName = new Prefs().getServerSite().replace("http://", "");
    private int deviceType = 2;
    public long m_SendFileSize = 0;
    public String responseLine = null;
    private Prefs prefs;

    public class AsyncObject {
        public int BufferSize = 4096;
        public byte[] buffer = new byte[BufferSize];

        public AsyncObject(int bufferSize) {
            this.buffer = new byte[bufferSize];
        }
    }


    /**
     * Constructor with Host, Port and MAC Address
     *
     * @param host
     * @param port
     */
    public NetClient(String host, int port) {
        this.host = host;
        this.port = port;
        prefs = CrewChatApplication.getInstance().getPrefs();
    }

    private void connectWithServer() {
        try {
            if (socket == null) {
                socket = new Socket(this.host, this.port);
                socket.setKeepAlive(true);
                out = socket.getOutputStream();
                in = socket.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileInputStream chooseOption(File file) {
        int option = prefs.getIntValue(CHOOSE_OPTION_IMAGE, Statics.ORIGINAL);
        FileInputStream fis = null;

        if (option == Statics.STANDARD) {
            try {
                fis = new FileInputStream(compressImage(file.getPath(), 100));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (option == Statics.HIGH) {
            try {
                fis = new FileInputStream(compressImageHigh(file.getPath(), 100));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (option == Statics.ORIGINAL) {
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return fis;
    }

    private void disConnectWithServer() {
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 파일서버로 해당 파일 정보를 전송합니다.
    public void sendDataWithStringTest(AttachDTO attachDTO, ProgressBar progressBar) {
        if (attachDTO != null) {
            connectWithServer();
            senData(attachDTO, progressBar);
        }
    }

    public int receiveDataFromServer() {
        try {
            byte[] bytes = new byte[4];
            int countRead = in.read(bytes, 0, 4);

            if (countRead != 4) {
                disConnectWithServer();
                return 0;
            }

            byte[] reserveBytes = new byte[4];
            reserveBytes[0] = bytes[3];
            reserveBytes[1] = bytes[2];
            reserveBytes[2] = bytes[1];
            reserveBytes[3] = bytes[0];

            ByteBuffer wrapped = ByteBuffer.wrap(reserveBytes);
            int attachNo = wrapped.getInt();

            disConnectWithServer(); // disconnect server
            return attachNo;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }
    public void senData(AttachDTO attachDTO, ProgressBar progressBar) {
        try {
            if (socket != null && socket.isConnected()) {
                AsyncObject ao = new AsyncObject(4096);
                byte[] fileByteData = new byte[409];

                Boolean isFirst = true;
                int nBytes = 0;
                int nCurPercent = 0;
                File myFile = new File(attachDTO.getFullPath());
                FileInputStream fis = null;
                if (!isImageFile(myFile.getPath())) {
                    fis = new FileInputStream(myFile);
                } else {
                    fis = chooseOption(myFile);
                }
                if (fis == null) {
                    //TODO get file from SD card
                    File myFileSpecial = new File(Environment.getExternalStorageDirectory() + "/Download/" + attachDTO.getFileName());
                    if (!isImageFile(myFileSpecial.getPath())) {
                        fis = new FileInputStream(myFileSpecial);
                    } else {
                        fis = chooseOption(myFileSpecial);
                    }
                }

                long dataLeng = fis.available();
                while ((nBytes = fis.read(fileByteData, 0, fileByteData.length)) > 0) {

                    if (isFirst) {
                        isFirst = false;
                        int attachType = Utils.getTypeFileAttach(attachDTO.getFileType());
                        byte[] fType = BitConverter.getBytes(attachType);
                        byte[] fName = attachDTO.getFileName().getBytes(UTF8_CHARSET);
                        byte[] fNameLen = BitConverter.getBytes(fName.length);
                        byte[] fDomain = domainName.getBytes(UTF8_CHARSET);
                        byte[] fDomainLen = BitConverter.getBytes(fDomain.length);
                        byte[] fSessionID = CrewChatApplication.getInstance().getPrefs().getaccesstoken().getBytes(UTF8_CHARSET);
                        byte[] fSessionIDLen = BitConverter.getBytes(fSessionID.length);
                        byte[] fDeviceType = BitConverter.getBytes(deviceType);
                        byte[] fData = new byte[nBytes];
                        System.arraycopy(fileByteData, 0, fData, 0, nBytes);
                        byte[] fDataLen = BitConverter.getBytes(dataLeng);

                        byte[] firstSendData = new byte[fType.length + fNameLen.length + fName.length + fDomainLen.length + fDomain.length + fSessionIDLen.length + fSessionID.length + fDeviceType.length + fDataLen.length + fData.length];

                        System.arraycopy(fType, 0, firstSendData, 0, fType.length);
                        System.arraycopy(fNameLen, 0, firstSendData, fType.length, fNameLen.length);
                        System.arraycopy(fName, 0, firstSendData, fType.length + fNameLen.length, fName.length);
                        System.arraycopy(fDomainLen, 0, firstSendData, fType.length + fNameLen.length + fName.length, fDomainLen.length);
                        System.arraycopy(fDomain, 0, firstSendData, fType.length + fNameLen.length + fName.length + fDomainLen.length, fDomain.length);
                        System.arraycopy(fSessionIDLen, 0, firstSendData, fType.length + fNameLen.length + fName.length + fDomainLen.length + fDomain.length, fSessionIDLen.length);
                        System.arraycopy(fSessionID, 0, firstSendData, fType.length + fNameLen.length + fName.length + fDomainLen.length + fDomain.length + fSessionIDLen.length, fSessionID.length);
                        System.arraycopy(fDeviceType, 0, firstSendData, fType.length + fNameLen.length + fName.length + fDomainLen.length + fDomain.length + fSessionIDLen.length + fSessionID.length, fDeviceType.length);
                        System.arraycopy(fDataLen, 0, firstSendData, fType.length + fNameLen.length + fName.length + fDomainLen.length + fDomain.length + fSessionIDLen.length + fSessionID.length + fDeviceType.length, fDataLen.length);
                        System.arraycopy(fData, 0, firstSendData, fType.length + fNameLen.length + fName.length + fDomainLen.length + fDomain.length + fSessionIDLen.length + fSessionID.length + fDeviceType.length + fDataLen.length, fData.length);
                        m_SendFileSize += nBytes;
                        System.out.println("Sending...");
                        out.write(firstSendData, 0, firstSendData.length);
                    } else {

                        m_SendFileSize += nBytes;
                        ao.buffer = new byte[nBytes];
                        System.arraycopy(fileByteData, 0, ao.buffer, 0, nBytes);
                        out.write(ao.buffer, 0, ao.buffer.length);
                    }
                    int percent = (int) (((double) m_SendFileSize / (double) dataLeng) * 100);

                    if (percent != nCurPercent && percent > 1) {
                        nCurPercent = percent;
                    }
                    if (progressBar != null)
                        progressBar.setProgress(nCurPercent);
                }
                out.flush();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            responseLine = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            responseLine = "IOException: " + e.toString();
        } finally {

        }
    }

    public String compressImage(String filePath, int size) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 612.0f;
        float maxWidth = 408.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, size, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String compressImageHigh(String filePath, int size) {


        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 1020.0f;
        float maxWidth = 816.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, size, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "CrewChat/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
}
