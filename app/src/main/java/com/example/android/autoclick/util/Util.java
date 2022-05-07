package com.example.android.autoclick.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.core.view.WindowInsetsControllerCompat;


public class Util {

    public static void transparency_statusBar(Activity acticity) {
        acticity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    public static void transparency_statusBarOn(Activity acticity) {
        acticity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        acticity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    }

    // 휴대폰의 하단 네비게이션바가 있는지 검사
    public static boolean isUseBottomNavigation(Context context) {
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        boolean useSoftNavigation = context.getResources().getBoolean(id);
        return useSoftNavigation;
    }

    // 휴대폰의 하단 네비게이션 바가 존재한다면 높이를 반환
    public static int getBottomNavigationHeight(Context context) {
        int bottomNavigation = 0;
        int screenSizeType = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        if (screenSizeType != Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            //태블릿 예외처리
            int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                bottomNavigation = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        if (!isUseBottomNavigation(context)) bottomNavigation = 0;
        return bottomNavigation;
    }

    // 상태바 글자 색상 변경
    public static void setStatusBarColor(Activity activity, int mode) {
        Window window = activity.getWindow();
        WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (mode == 1) {
            // 상태바 글자 색상 white
            windowInsetsControllerCompat.setAppearanceLightStatusBars(false);
        } else if (mode == 2) {
            // 상태바 글자 색상 black
            windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
        }
    }

    // 상태바 색상 변경
    public static void setStatusBarColor(Activity activity) {
        //흰색으로 바꾸기
        Window window = activity.getWindow();
        window.setStatusBarColor(Color.WHITE);
    }

    // 터치 막기
    public static void setTouchOff(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    // 터치 재활성화
    public static void setTouchOn(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    // 키보드 내리기
    public static void keyboardOff(Activity activity){
        InputMethodManager manager = (InputMethodManager)activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
