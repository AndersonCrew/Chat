package com.dazone.crewchatoff.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.*;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.interfaces.OnClickViewCallback;
import com.dazone.crewchatoff.utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static com.dazone.crewchatoff.constant.Statics.CHOOSE_OPTION_IMAGE;


public class RotateImageActivity extends Activity implements View.OnClickListener {
    private RelativeLayout rlHeader;
    private LinearLayout lnFooter;
    private boolean showFull = false;
    private String imagePath;
    private String regDate;
    /**
     * VIEW
     */
    private ImageView btnBack;
    private ImageView btnDownload;
    private ImageView btnShare;
    private ImageView btnDelete;
    private ImageView imgAvatar;
    private ImageView ivTick;
    private TextView tvUserName;
    private TextView tvDate;
    private TextView tvQuality;
    private ImageView ivMain;
    private ImageView image_option;
    private Bitmap mMainBitmap;
    private ProgressBar mProgressBarLoadImage;
    private boolean isHide = false;
    private Prefs mPref;

    int xDim, yDim;        //stores ImageView dimensions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate_image);

        mPref = new Prefs();
        initView();
        initData();
    }

    @Override
    //Get the size of the Image view after the
    //Activity has completely loaded
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        xDim = ivMain.getWidth();
        yDim = ivMain.getHeight();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        btnDownload = findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(this);

        btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(this);

        imgAvatar = findViewById(R.id.img_avatar);
        tvUserName = findViewById(R.id.tv_username);
        tvQuality = findViewById(R.id.tv_quality);
        tvDate = findViewById(R.id.tv_date);

        rlHeader = findViewById(R.id.rl_header);
        lnFooter = findViewById(R.id.ln_footer);
        mProgressBarLoadImage = findViewById(R.id.progressBarLoadImage);

        ivMain = findViewById(R.id.iv_main_image);
        ivMain.setOnClickListener(this);
        ivTick = findViewById(R.id.iv_tick);
        ivTick.setOnClickListener(this);

        image_option = findViewById(R.id.image_option);

        tvQuality.setText(chooseOption());
        tvQuality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);

                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                setCheckMenu(popup);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.Standard:
                                mPref.putIntValue(CHOOSE_OPTION_IMAGE, Statics.STANDARD);
                                tvQuality.setText(chooseOption());
                                return true;
                            case R.id.High:
                                mPref.putIntValue(CHOOSE_OPTION_IMAGE, Statics.HIGH);
                                tvQuality.setText(chooseOption());
                                return true;
                            case R.id.Original:
                                mPref.putIntValue(CHOOSE_OPTION_IMAGE, Statics.ORIGINAL);
                                tvQuality.setText(chooseOption());
                                return true;
                            default:
                                return false;

                        }

                    }

                });
                popup.show();//showing popup menu
            }
        });
    }

    private String chooseOption() {
        int option = mPref.getIntValue(CHOOSE_OPTION_IMAGE, Statics.ORIGINAL);
        String fis = "";
        if (option == Statics.STANDARD) {
            fis = getString(R.string.standard_text);
        } else if (option == Statics.HIGH) {
            fis = getString(R.string.hight_text);
        } else if (option == Statics.ORIGINAL) {
            fis = getString(R.string.original_text);
        }
        return fis;
    }

    private void setCheckMenu(PopupMenu popupMenu) {
        int choose = mPref.getIntValue(CHOOSE_OPTION_IMAGE, Statics.ORIGINAL);
        if (choose == Statics.STANDARD) {
            popupMenu.getMenu().getItem(Statics.STANDARD).setChecked(true);
        } else if (choose == Statics.HIGH) {
            popupMenu.getMenu().getItem(Statics.HIGH).setChecked(true);
        } else if (choose == Statics.ORIGINAL) {
            popupMenu.getMenu().getItem(Statics.ORIGINAL).setChecked(true);
        }
    }

    private void initData() {
        Intent i = getIntent();
        imagePath = i.getStringExtra(Statics.CHATTING_DTO_GALLERY_SINGLE);
        regDate = i.getStringExtra(Statics.CHATTING_DTO_REG_DATE);

        if (showFull) {
            rlHeader.setVisibility(View.GONE);
            lnFooter.setVisibility(View.GONE);
        } else {
            rlHeader.setVisibility(View.VISIBLE);
            lnFooter.setVisibility(View.VISIBLE);
        }

        Prefs prefs = new Prefs();
        ImageUtils.showCycleImageFromLink(prefs.getServerSite() + prefs.getAvatarUrl(), imgAvatar, R.dimen.common_avatar);
        tvUserName.setText(prefs.getFullName());
        tvDate.setText(TimeUtils.displayTimeWithoutOffset(this, TimeUtils.convertTimeDeviceToTimeServerDefault(regDate), 0, TimeUtils.KEY_FROM_SERVER));
        new LoadbitMap().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class LoadbitMap extends AsyncTask<Void, Integer, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            if (imagePath != null && mProgressBarLoadImage != null) {
                mProgressBarLoadImage.setVisibility(View.VISIBLE);
                ivMain.setImageBitmap(null);
                ivMain.destroyDrawingCache();
                if (mMainBitmap != null) {
                    mMainBitmap.recycle();
                }
            }
        }

        protected String doInBackground(Void... arg0) {
            if (imagePath != null) {
                mMainBitmap = BitmapFactory.decodeFile(imagePath);
            }
            return "You are at PostExecute";
        }

        protected void onProgressUpdate(Integer... a) {
            super.onProgressUpdate(a);

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            int height = mMainBitmap.getHeight();
            int width = mMainBitmap.getWidth();

            // if size too large, need to reduce size before upload
            if (height > 1280 && width > 960) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap imgbitmap = BitmapFactory.decodeFile(imagePath, options);
                mMainBitmap = ExifUtil.rotateBitmap(imagePath, imgbitmap);
                ivMain.setImageBitmap(mMainBitmap);
                mProgressBarLoadImage.setVisibility(View.GONE);
            } else {
                ivMain.setImageBitmap(mMainBitmap);
                mProgressBarLoadImage.setVisibility(View.GONE);
            }

        }
    }

    private void toggleMenu() {
        rlHeader.setTag("Header");
        lnFooter.setTag("Footer");

        if (this.isHide) {
            slideToBottom(rlHeader);
            slideToTop(lnFooter);
            this.isHide = false;
        } else {
            slideToTop(rlHeader);
            slideToBottom(lnFooter);
            this.isHide = true;
        }
    }

    // To animate view slide out from top to bottom
    public void slideToBottom(View view) {
        TranslateAnimation animate;
        if (view.getTag().equals("Header")) {
            animate = new TranslateAnimation(0, 0, 0, 0);
        } else {
            animate = new TranslateAnimation(0, 0, 0, view.getHeight());
        }

        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    // To animate view slide out from bottom to top
    public void slideToTop(View view) {
        TranslateAnimation animate;
        if (view.getTag().equals("Header")) {
            animate = new TranslateAnimation(0, 0, 0, -view.getHeight());
        } else {
            animate = new TranslateAnimation(0, 0, 0, 0);
        }

        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;

            case R.id.iv_main_image:
                toggleMenu();
                break;

            case R.id.btn_download:
                Matrix matrix1 = new Matrix();
                matrix1.postRotate(90); // anti-clockwise by 90 degrees
                Bitmap rotatedBitmap1 = null;

                try {
                    rotatedBitmap1 = Bitmap.createBitmap(mMainBitmap, 0, 0, mMainBitmap.getWidth(), mMainBitmap.getHeight(), matrix1, true);
                } catch (OutOfMemoryError | Exception e) {
                    e.printStackTrace();
                }

                // if rotated then save it path = fOut
                if (rotatedBitmap1 != null) {
                    mMainBitmap.recycle();
                    mMainBitmap = rotatedBitmap1;
                    ivMain.setImageBitmap(mMainBitmap);
                }

                break;

            case R.id.btn_delete:
                break;

            case R.id.iv_tick:
                new AsyncClickTick().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
    }

    public class AsyncClickTick extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // Here you can show progress bar or something on the similar lines.
            // Since you are in a UI thread here.
            if (mProgressBarLoadImage != null) {
                mProgressBarLoadImage.setVisibility(View.VISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // After completing execution of given task, control will return here.
            // Hence if you want to populate UI elements with fetched data, do it here.
            if (mProgressBarLoadImage != null) {
                mProgressBarLoadImage.setVisibility(View.GONE);
            }
            finish();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            // You can track you progress update here
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Here you are in the worker thread and you are not allowed to access UI thread from here.
            // Here you can perform network operations or any heavy operations you want.
            Matrix matrix2 = ivMain.getImageMatrix();

            // Save to out put
            Bitmap rotatedBitmap2 = null;
            OutputStream fOut = null;
            File file = new File(imagePath);

            try {
                rotatedBitmap2 = Bitmap.createBitmap(mMainBitmap, 0, 0, mMainBitmap.getWidth(), mMainBitmap.getHeight(), matrix2, true);
                fOut = new FileOutputStream(file);
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
            }

            // if rotated then save it path = fOut
            if (rotatedBitmap2 != null) {
                rotatedBitmap2.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            }

            Intent intent = new Intent();
            intent.putExtra(Statics.CHATTING_DTO_GALLERY_SINGLE, imagePath);
            setResult(Activity.RESULT_OK, intent);
            return null;
        }
    }
}