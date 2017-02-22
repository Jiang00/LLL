package com.suo.applock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.suo.applock.menu.AppBridge;
import com.suo.applock.menu.BackgData;
import com.suo.applock.menu.SharPre;
import com.suo.applock.view.MsgLinbo;
import com.suo.applock.db.MyAppBridge;
import com.suo.applock.menu.MyEveryDay;
import com.suo.applock.menu.MyMenu;
import com.suo.applock.view.MoreApp;

import java.util.List;

/**
 * Created by huale on 2015/2/2.
 */
public class MyAppBridgeImpl implements MyAppBridge {
    boolean unlockSelf;
    Context context;
    Drawable icon;
    private Context myContext;
    //有SYSTEM_ALERT_WINDOW的权限，小米系列默认禁止此权限
    boolean hasPermission;
    boolean unlockMe;
    String pkgName;
    CharSequence appName;
    Object[] addedToWindow = new Object[MENU_IDX_COUNT];

    static final MyAppBridgeImpl ins = new MyAppBridgeImpl();

    public static void reset(Context context, boolean unlockSelf, boolean hasPermission, String pkgName) {
        pkgName = pkgName == null ? context.getPackageName() : pkgName;
        ins.unlockSelf = unlockSelf;
        ins.context = context;
        ins.pkgName = pkgName;
        ins.icon = ins.getIcon(pkgName);
        AppBridge.bridge = ins;
        ins.hasPermission = hasPermission;
    }

    public Drawable getIcon(String pkgName) {
        try {
            this.myContext=context;
            PackageManager packageManager = context.getPackageManager();
            PackageInfo pi = packageManager.getPackageInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return pi.applicationInfo.loadIcon(packageManager);
        } catch (Exception e) {
            return context.getResources().getDrawable(R.drawable.suo_ic);
        }
    }

    @Override
    public CharSequence appName() {
        return "";
    }

    @Override
    public Drawable icon() {
        return icon;
    }

    @Override
    public boolean check(String passwd, boolean normal) {
        if (SharPre.checkPasswd(passwd, normal)) {
            if (context instanceof Activity) {
                ((MainActivity) context).unlockSuccess(unlockMe);
            } else {
                ((WorkService) context).unlockSuccess(unlockMe);
                SharedPreferences sp = Application.getSharedPreferences();
                if (sp.getInt(SharPre.PREF_BRIEF_SLOT, SharPre.PREF_DEFAULT) == SharPre.PREF_BRIEF_AFTER_SCREEN_OFF
                        && !sp.contains("PREF_BRIEF_AFTER_SCREEN_OFF")) {
                    MsgLinbo.Data data = new MsgLinbo.Data();
                    data.alert = true;
                    data.title = R.string.suo_brief;
                    data.msg = R.string.suo_be_soff;
                    MsgLinbo.show_(context, data);
                    sp.edit().putBoolean("PREF_BRIEF_AFTER_SCREEN_OFF", true).apply();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Resources res() {
        return context.getResources();
    }

    @Override
    public int resId(String name, String type) {
        return res().getIdentifier(name, type, context.getPackageName());
    }

    @Override
    public boolean random() {
        return Application.getSharedPreferences().getBoolean("random", false);
    }

    MyMenu[] menus;
    public static final int MENU_IDX_ALL = -1;
    public static final int MENU_IDX_BRIEF = 0;
    public static final int MENU_IDX_UNLOCKME = 1;
    //    public static final int MENU_IDX_FORGET = 2;
    public static final int MENU_IDX_THEME = 2;
    public static final int MENU_IDX_TOGGLE = 3;
    public static final int MENU_IDX_COUNT = 4;

    public void detachFromWindow(int idx) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (idx == MENU_IDX_ALL) {
            for (int i = 0; i < MENU_IDX_COUNT; ++i) {
                detatchSingleView(i, wm);
            }
        } else {
            detatchSingleView(idx, wm);
        }
    }

    public void detatchSingleView(int idx, WindowManager wm) {
        try {
            if (addedToWindow[idx] instanceof View) {
                wm.removeViewImmediate((View) addedToWindow[idx]);
            } else {
                ((AlertDialog) addedToWindow[idx]).dismiss();
            }
        } catch (Exception ignore) {

        } finally {
            addedToWindow[idx] = null;
        }
    }

    @Override
    public MyMenu[] menus() {
        if (menus == null) {

            MyMenu theme = new MyMenu() {
                @Override
                public MyMenu init() {
                    return this;
                }

                @Override
                public void onClick(View dummy) {
                    Log.e("abc","-----222");



                    AppBridge.requestTheme = true;
                    AppBridge.needUpdate = false;
                    MyTrack.sendEvent(MyTrack.CATE_OVERFLOW_MENU, MyTrack.ACT_OVERFLOW_MENU_THEME, MyTrack.ACT_OVERFLOW_MENU_THEME, 1L);
                    if (hasPermission) {
                        RunApp(context.getPackageName(),context);
                        ((WorkService) context).hideAlertImmediate();

                    } else {
//                            context.startActivity(new Intent(context, LockScreenThemes.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(LockScreenThemes.PATTERN_INTENT,true));
                    }
                }
            }.init();

            MyMenu brief = new MyMenu() {
                @Override
                public MyMenu init() {
                    title = R.string.suo_br_setting;
                    return this;
                }

                @Override
                public void onClick(View v) {
                    Log.e("abc","-----333");
                    try {
                        int idx = Application.getSharedPreferences().getInt(SharPre.PREF_BRIEF_SLOT, SharPre.PREF_DEFAULT);
                        final String[] stringArray = context.getResources().getStringArray(R.array.suo_setting_brief);
                        AlertDialog d = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert).setTitle(context.getResources().getString(R.string.suo_br_setting)).setSingleChoiceItems(stringArray, idx, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Application.getSharedPreferences().edit().putInt(SharPre.PREF_BRIEF_SLOT, i).commit();
                                MyTrack.sendEvent(MyTrack.CATE_OVERFLOW_MENU, MyTrack.ACT_OVERFLOW_MENU_BRIEF, stringArray[i], 1L);
                                dialogInterface.dismiss();
                            }
                        }).create();
                        Log.e("haha", "ehihei " + context);
                        if (hasPermission) {
                            d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        }
                        d.show();
                        addedToWindow[MENU_IDX_BRIEF] = d;
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                    }
                }
            }.init();

            final MyMenu unlockMeMenu = new MyMenu() {
                @Override
                public MyMenu init() {
                    title = R.string.suo_lock_the_app;
                    checkable = true;
                    return this;
                }

                @Override
                public void onClick(View v) {
                    Log.e("abc","-----444");

                    checked = !checked;
                    unlockMe = checked;
                    if (checked) {
                        MsgLinbo.Data data = new MsgLinbo.Data();
                        data.title = R.string.suo_lock_the_app;
                        data.msg = R.string.suo_unlock;
                        data.alert = hasPermission;
                        AlertDialog d = MsgLinbo.show_(context, data);
                        addedToWindow[MENU_IDX_UNLOCKME] = d;
                        MyTrack.sendEvent(MyTrack.CATE_OVERFLOW_MENU, MyTrack.ACT_OVERFLOW_MENU_UNLOCK_ME, MyTrack.ACT_OVERFLOW_MENU_UNLOCK_ME, 1L);
                    }
                }
            }.init();

            menus = new MyMenu[]{brief, unlockMeMenu, /*lockscreen_forget, */theme};
        }

        if (unlockSelf) {
            return new MyMenu[]{/*menus[2], */menus[2]};
        } else {
            unlockMe = false;
            menus[1].checked = false;
            return menus;
        }
    }
//
    @Override
    public MyEveryDay daily() {
        MyEveryDay d = new MyEveryDay();
        SharedPreferences sp = Application.getSharedPreferences();
        d.has = sp.getBoolean(BackgData.KEY_DAILY_UNLOCK, false);
        d.unread = sp.getBoolean(BackgData.KEY_DAILY_UNLOCK_NEW, false);
        d.iconUrl = BackgData.KEY_DAILY_ICON_PREF_KEY;
        d.iconPersistentUrl = BackgData.KEY_DAILY_ICON_PERSISTENT;
        return d;
    }

    @Override
    public void visitDaily(boolean persistent) {
        MyTrack.sendEvent(MyTrack.CATE_DEFAULT, MyTrack.ACT_DLY_UNLOCK, MyTrack.ACT_DLY_UNLOCK, 1L);
        if (persistent) {
            menus[MENU_IDX_THEME].onClick(null);
        } else {
            SharedPreferences sp = Application.getSharedPreferences();
            sp.edit().putBoolean(BackgData.KEY_DAILY_UNLOCK_NEW, false).commit();
            if (hasPermission) {
                ((WorkService) context).hideAlertIfPossible(false);
//                detachFromWindow(MENU_IDX_THEME);
            }
            final String url = sp.getString(BackgData.KEY_DAILY_UNLOCK_URL, context.getPackageName());
            /**
             * @design if url has https:// http:// ftp:// etc. will use MoreGameWeb.show()
             * otherwise use google play
             */
            if (!url.contains("://")) {
                Log.e("abc", "-----666");

                    Toolcls.openPlayStore(context, url);
                    detachFromWindow(MENU_IDX_THEME);
            } else {
                Log.e("abc","-----777");
                MoreApp.show(context, url);
            }
        }
    }

    @Override
    public void toggle(boolean normal) {
        if (unlockSelf) {
            ((MainActivity) context).toggle(normal);
        }
    }

    @Override
    public boolean hasPattern() {
        return unlockSelf && SharPre.isPasswdSet(false);
    }

    @Override
    public boolean hasPasswd() {
        return unlockSelf && SharPre.isPasswdSet(true);
    }

    @Override
    public void back() {
        if (!hasPermission || context instanceof Activity) {
            ((MainActivity) context).onBackPressed();
        } else {
            ((WorkService) context).backHome();
        }
    }

    @Override
    /**
     * 无用
     */
    public void switchTheme() {

    }

    private void RunApp(String packageName,Context c) {
        PackageInfo pi;
        try {
            pi =c. getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);
            PackageManager pManager = c.getPackageManager();
            List<ResolveInfo> apps = pManager.queryIntentActivities(
                    resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;

                Intent intent = new Intent();

                ComponentName cn = new ComponentName(packageName, "LockScreenThemes");
                intent.setComponent(cn);
                intent.setAction("android.intent.action.VIEW");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(intent);

                Log.e("run","run-----------------");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String currentPkg() {
        return pkgName;
    }

    public static void clear() {
        ins.context = null;
        ins.icon = null;
    }
}
