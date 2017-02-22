package com.suo.applock.db;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.suo.applock.menu.MyEveryDay;
import com.suo.applock.menu.MyMenu;

/**
 * Created by huale on 2014/11/20.
 */
public interface MyAppBridge {
    CharSequence appName();

    Drawable icon();

    boolean check(String passwd, boolean normal);

    Resources res();

    int resId(String name, String type);

    boolean random();

    MyMenu[] menus();

    MyEveryDay daily();

    void visitDaily(boolean persistent);

    void toggle(boolean normal);

    boolean hasPattern();

    boolean hasPasswd();

    void back();

    void switchTheme();

    String currentPkg();
}
