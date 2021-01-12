package com.dazone.crewchatoff.activity;

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
import android.widget.Toast;

import com.dazone.crewchatoff.BuildConfig;
import com.dazone.crewchatoff.R;
import com.dazone.crewchatoff.constant.Constants;
import com.dazone.crewchatoff.constant.Statics;
import com.dazone.crewchatoff.database.AllUserDBHelper;
import com.dazone.crewchatoff.database.BelongsToDBHelper;
import com.dazone.crewchatoff.database.ChatMessageDBHelper;
import com.dazone.crewchatoff.database.ChatRoomDBHelper;
import com.dazone.crewchatoff.database.DepartmentDBHelper;
import com.dazone.crewchatoff.database.FavoriteGroupDBHelper;
import com.dazone.crewchatoff.database.FavoriteUserDBHelper;
import com.dazone.crewchatoff.database.UserDBHelper;
import com.dazone.crewchatoff.utils.Constant;
import com.dazone.crewchatoff.utils.CrewChatApplication;
import com.dazone.crewchatoff.utils.Prefs;

import static com.dazone.crewchatoff.utils.Utils.compareVersionNames;

public class WelcomeActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnNext;
    int exist_Id_Login = 0;
    int sumPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);

        /** Check Logout if current version <= 3.1.1 becase Table of DB has changed*/
        checkLogout();
        String first_login = Statics.FIRST_LOGIN;
        boolean isLogin = CrewChatApplication.getInstance().getPrefs().getBooleanValue(first_login, false);
        boolean isFirstLogin = CrewChatApplication.getInstance().getPrefs().get_login_install_app(); // default true

        if (isFirstLogin) {
            CrewChatApplication.getInstance().getPrefs().set_login_install_app(false);
            if (isLogin) {
                launchHomeScreen();
            } else {
                initView();
            }
        } else {
            launchHomeScreen();
        }
    }

    private void checkLogout() {
        String appVersion = BuildConfig.VERSION_NAME;
        if (!CrewChatApplication.getInstance().getPrefs().getBooleanValue(Constants.IS_FIRST_INSTALL_VER, false)) {
            BelongsToDBHelper.clearBelong();
            AllUserDBHelper.clearUser();
            ChatRoomDBHelper.clearChatRooms();
            ChatMessageDBHelper.clearMessages();
            DepartmentDBHelper.clearDepartment();
            UserDBHelper.clearUser();
            FavoriteGroupDBHelper.clearGroups();
            FavoriteUserDBHelper.clearFavorites();
            CrewChatApplication.resetValue();
            CrewChatApplication.isLoggedIn = false;
            CrewChatApplication.getInstance().getPrefs().putBooleanValue(Statics.FIRST_LOGIN, false);
            CrewChatApplication.getInstance().getPrefs().set_login_install_app(false);
            CrewChatApplication.getInstance().getPrefs().setDataComplete(false);
            CrewChatApplication.getInstance().getPrefs().putBooleanValue(Constants.IS_FIRST_INSTALL_VER, true);

            if (!CrewChatApplication.getInstance().getPrefs().getStringValue("serversite", "").isEmpty()
                    && CrewChatApplication.getInstance().getPrefs().getServerSite().isEmpty()) {
                CrewChatApplication.getInstance().getPrefs().putStringValue(Constants.DOMAIN, CrewChatApplication.getInstance().getPrefs().getStringValue("serversite", ""));
            }
        }
    }

    void initView() {
        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.btn_next);

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

        addBottomDots(0);
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        btnNext.setOnClickListener(v -> launchHomeScreen());
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

    private void launchHomeScreen() {
        startActivity(new Intent(WelcomeActivity.this, IntroActivity.class));
        finish();
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            if (position == sumPage)
                btnNext.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            TextView tvSignUp = view.findViewById(R.id.tvNewGroup);
            if (tvSignUp != null) {
                tvSignUp.setOnClickListener(v -> {
                    Intent intent = new Intent(WelcomeActivity.this, SignUpActivity.class);
                    startActivity(intent);
                });
            }

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