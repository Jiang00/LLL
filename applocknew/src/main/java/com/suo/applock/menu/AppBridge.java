package com.suo.applock.menu;

import android.content.Context;

import com.suo.applock.db.MyAppBridge;

/**
 * Created by huale on 2014/11/27.
 */
public class AppBridge {
    public static MyAppBridge bridge;
    public static Context themeContext;
    public static boolean needUpdate = false;
    public static boolean requestTheme = false;
}
