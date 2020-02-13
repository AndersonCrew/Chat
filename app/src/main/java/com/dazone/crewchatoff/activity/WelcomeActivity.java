package com.dazone.crewchatoff.activity;

/**
 * Created by maidinh on 22/2/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.utils.Prefs;

public class WelcomeActivity extends AppCompatActivity {
    String TAG = "WelcomeActivity";
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnNext;
    //    private PrefManager prefManager;
    int exist_Id_Login = 0;
    int sumPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        // Checking for first time launch - before calling setContentView()
//        prefManager = new PrefManager(this);
//        if (!prefManager.isFirstTimeLaunch()) {
//            launchHomeScreen();
//            finish();
//        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);
        Prefs prefs = new Prefs();
//        exist_Id_Login = getIntent().getIntExtra(Cons.exist_Id_Login, 0);

//        String first_login = Statics.FIRST_LOGIN;
//        boolean isLogin = new Prefs().getBooleanValue(first_login, false);
//        if (isLogin) {
//            launchHomeScreen();
//        } else {
//            initView();
//        }

        String first_login = Statics.FIRST_LOGIN;
        boolean isLogin = prefs.getBooleanValue(first_login, false);
//        if (isLogin) {
//            launchHomeScreen();
//        } else {
//            initView();
//        }

        boolean is_first_login = prefs.get_login_install_app(); // default true
        Log.d(TAG, "is_first_login:" + is_first_login);
        if (is_first_login) {
            prefs.set_login_install_app(false);

            if (isLogin) {
                launchHomeScreen();
            } else {
                initView();
            }
        } else {
            launchHomeScreen();
        }

    }

    void initView() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnNext = (Button) findViewById(R.id.btn_next);


        // layouts of all welcome sliders
        // add few more layouts if you want
        if (exist_Id_Login == 0) {
            sumPage = 4;
            layouts = new int[]{
                    R.layout.welcome_slide1,
                    R.layout.welcome_slide2,
                    R.layout.welcome_slide3,
                    R.layout.welcome_slide4,
                    R.layout.welcome_slide5};
        } else {
            sumPage = 3;
            layouts = new int[]{
                    R.layout.welcome_slide1,
                    R.layout.welcome_slide2,
                    R.layout.welcome_slide3,
                    R.layout.welcome_slide4};
        }

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                int current = getItem(+1);
//                if (current < layouts.length) {
//                    // move to next screen
//                    viewPager.setCurrentItem(current);
//                } else {
//                    launchHomeScreen();
//                }

                launchHomeScreen();
            }
        });
    }


    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.rightMargin = 20;
            params.leftMargin = 20;
            dotsLayout.addView(dots[i], params);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
//        prefManager.setFirstTimeLaunch(false);
//        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
//        finish();

        startActivity(new Intent(WelcomeActivity.this, IntroActivity.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            if (position == sumPage)
                btnNext.setVisibility(View.VISIBLE);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
            } else {
                // still pages are left
//                btnNext.setText(getResources().getString(R.string.string_done));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            TextView login_btn_sign_up = (TextView) view.findViewById(R.id.tvNewGroup);
            if (login_btn_sign_up != null) {
                Log.d(TAG, "login_btn_sign_up != null");
                login_btn_sign_up.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(WelcomeActivity.this, SignUpActivity.class);
                        startActivity(intent);
                    }
                });
            } else {
                Log.d(TAG, "login_btn_sign_up null");
            }
//            ImageView img_2 = (ImageView) view.findViewById(R.id.img_2);
//            if (img_2 != null) {
//                if (Locale.getDefault().getLanguage().equalsIgnoreCase("vi")) {
//                    img_2.setImageResource(R.drawable.intro_ic_02_vi);
//                } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko")) {
//                    img_2.setImageResource(R.drawable.intro_ic_02_ko);
//                } else {
//                    img_2.setImageResource(R.drawable.intro_ic_02_en);
//                }
//            }
//
//            ImageView img_3 = (ImageView) view.findViewById(R.id.img_3);
//            if (img_3 != null) {
//                if (Locale.getDefault().getLanguage().equalsIgnoreCase("vi")) {
//                    img_3.setImageResource(R.drawable.intro_ic_03_vi);
//                } else if (Locale.getDefault().getLanguage().equalsIgnoreCase("ko")) {
//                    img_3.setImageResource(R.drawable.intro_ic_03_ko);
//                } else {
//                    img_3.setImageResource(R.drawable.intro_ic_03_en);
//                }
//            }

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}