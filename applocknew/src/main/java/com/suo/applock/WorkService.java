package com.suo.applock;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.client.AndroidSdk;
import com.suo.applock.menu.SharPre;
import com.suo.libra.view.AppWidgetContainer;
import com.suo.applock.menu.BackgData;
import com.suo.theme.MyFrameLayout;
import com.suo.applock.view.PattFragment;
import com.suo.applock.db.MyfileDBHelper;
import com.suo.applock.menu.MyProfiles;
import com.suo.applock.menu.AppBridge;
import com.suo.applock.view.Flow;
import com.suo.applock.view.PassFragment;
import com.suo.applock.view.AppFragment;
import com.lockscreen.api.liberal.Utils;
import com.lockscreen.api.liberal.liu.SafeIDB;
import com.privacy.lock.aidl.IWorker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by superjoy on 2014/8/25.
 */
public class WorkService extends Service {
    boolean home = false;
    HashMap<String, Long> briefTimes = new HashMap<>();
    public static Context context;
    private Runnable removeRunner = new Runnable() {
        @Override
        public void run() {
            hideAlertImmediate();
            removing = false;
        }
    };
    private String lunchapp="";

    public String getTopPackageName() {
        String packageName = null;
        if (Build.VERSION.SDK_INT > 19) {
            try {
                packageName = getActivePackages();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (packageName == null) {
                    packageName = getTopPackage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            List<ActivityManager.RunningTaskInfo> lst = mActivityManager.getRunningTasks(1);
            if (lst != null && lst.size() > 0) {
                ActivityManager.RunningTaskInfo runningTaskInfo = lst.get(0);
                if (runningTaskInfo.numRunning > 0 && runningTaskInfo.topActivity != null) {
                    packageName = runningTaskInfo.topActivity.getPackageName();
                }
            }
        }
        return packageName;
    }

    private ActivityManager mActivityManager;

    String getActivePackages() throws NoSuchFieldException, IllegalAccessException {
        if (processState == null) {
            processState = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            processState.setAccessible(true);
        }
        final List<ActivityManager.RunningAppProcessInfo> processInfos = mActivityManager.getRunningAppProcesses();
        if (processInfos != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
                int anInt = processState.getInt(processInfo);
                if (anInt == 2) return processInfo.pkgList[0];
            }
        }
        return null;
    }


    /** first app user */
    public static final int AID_APP = 10000;

    /** offset for uid ranges for each user */
    public static final int AID_USER = 100000;

    static HashMap<String, Boolean> excludes = new HashMap<>();
    static {
        excludes.put("com.android.systemui", true);
        excludes.put("android.process.acore", true);
        excludes.put("android.process.media", true);
        excludes.put("com.android.soundrecorder", true);
    }

    public static String getForegroundApp() {
        File[] files = new File("/proc").listFiles();
        int lowestOomScore = Integer.MAX_VALUE;
        String foregroundProcess = null;

        for (File file : files) {
            int pid;
            String name = file.getName();
            try {
                pid = Integer.parseInt(name);
            } catch (NumberFormatException e) {
                continue;
            }

            try {
                String cgroup = read(String.format("/proc/%d/cgroup", pid));

                if (cgroup.contains("bg_non_interactive")) {
                    continue;
                }

                if (!cgroup.endsWith(name)) {
                    continue;
                }

                int uid = Integer.parseInt(cgroup.substring(cgroup.indexOf("uid_") + 4, cgroup.lastIndexOf("/")));
                if (uid >= 1000 && uid <= 1038) {
                    // system process
                    continue;
                }

                int appId = uid - AID_APP;
                // loop until we get the correct user id.
                // 100000 is the offset for each user.
                while (appId > AID_USER) {
                    appId -= AID_USER;
                }

                if (appId < 0) {
                    continue;
                }

                String cmdline = read(String.format("/proc/%d/cmdline", pid));
                if (excludes.containsKey(cmdline)) {
                    continue;
                }

                // u{user_id}_a{app_id} is used on API 17+ for multiple user account support.
                // String uidName = String.format("u%d_a%d", userId, appId);

                File oomScoreAdj = new File(String.format("/proc/%d/oom_score_adj", pid));
                if (oomScoreAdj.canRead()) {
                    int oomAdj = Integer.parseInt(read(oomScoreAdj.getAbsolutePath()));
                    if (oomAdj != 0) {
                        continue;
                    }
                }

                int oomscore = Integer.parseInt(read(String.format("/proc/%d/oom_score", pid)));
                if (oomscore < lowestOomScore) {
                    lowestOomScore = oomscore;
                    foregroundProcess = cmdline;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return foregroundProcess;
    }

    private static String read(String path) throws IOException {
        BufferedReader reader = null;
        try {
            StringBuilder output = new StringBuilder();
            reader = new BufferedReader(new FileReader(path));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                output.append(line);
            }
            return output.toString().trim();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }



    Field processState = null;
    boolean sleep = false;

    public void backHome() {
        backHome_();
        hideAlertIfPossible(true);
    }

    private void backHome_() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(setIntent);
    }

    public void hideAlertImmediate() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (alertContainer != null) {
            wm.removeViewImmediate(alertContainer);
            if (alertView != null) {
                ((ViewGroup)alertView).removeAllViews();
            }
            alertContainer.removeAllViews();
            alertContainer = null;
        }
        alertView = null;
        alert = false;
    }

    int delayTime = 0;
    public void hideAlertIfPossible(boolean home) {
        if (alert && !removing) {
            removing = true;
            r.home = home;
            handler.postDelayed(r, delayTime);
        }
    }

    class MyRunnable implements Runnable {
        boolean home = false;

        @Override
        public void run() {
            if (alertView != null) {
                alertView.startAnimation(fadeout);
            } else {
                alert = false;
                removing = false;
            }
            if (home) {
                if (MyAppBridgeImpl.ins.context == null) {
                    MyAppBridgeImpl.ins.context = WorkService.this;
                }
                MyAppBridgeImpl.ins.detachFromWindow(MyAppBridgeImpl.MENU_IDX_ALL);
            }
        }
    }

    boolean unlocked = false;
    boolean alert = false;
    boolean removing = false;
    public View alertView;
    public FrameLayout alertContainer;
    private MyRunnable r = new MyRunnable();
    Map<String, Boolean> asks = new HashMap<>();

    class LockTask extends Thread {
        private Context mContext;
        String lastPackageName = "";

        public boolean running;

        public LockTask(Context context) {
            mContext = context;
            mActivityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            start();
        }

        static final int sleeptime = 100;

        @Override
        public void run() {
            running = true;
            while (running) {
                /**
                 * @design
                 *  because this thread run 5 times for each second
                 *
                 *  therefore, 300 seconds will run 300 * 5 times
                 */
                if (++lastAsyncTime > 1500) {
                    lastAsyncTime = 0;
                    BackgData.fetchIfNecessary(getApplicationContext());
                }

                final String packageName = getTopPackageName();
                if (packageName == null) {
                    try {
                        Thread.sleep(sleeptime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if (!lastPackageName.equals(packageName)) {
                    if (homes.containsKey(packageName)) {
                        home = true;
                        showWidgetIfNecessary(true);
                        if (unlocked) {
                            unlocked = false;
                            showAd();
                        }
                        int slot = Application.getSharedPreferences().getInt(SharPre.PREF_BRIEF_SLOT, SharPre.PREF_DEFAULT);
                        switch (slot) {
                            case SharPre.PREF_BRIEF_EVERY_TIME:
                                tmpUnlockedApps.clear();
                                getSharedPreferences("tmp", MODE_PRIVATE).edit().remove(SharPre.PREF_TMP_UNLOCK).commit();
                                break;

                            default:
                                if (tmpUnlockedApps.containsKey(lastPackageName)) {
                                    briefTimes.put(lastPackageName, System.currentTimeMillis());
                                    unlockLastApplication(lastPackageName, true);
                                }
                                break;
                        }
                        hideAlertIfPossible(true);
                    } else if (homes.containsKey(lastPackageName)) {
                        showWidgetIfNecessary(false);
                    }
                    lastPackageName = packageName;
                }

                if (sleep) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if (!alert) {
                    if (!lockApps.containsKey(packageName)) {
                        if (asks.containsKey(packageName) && SharPre.requireAsk()) {
                            Application.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    protect(packageName);
                                }
                            });
                        }
                    } else if (!tmpUnlockedApps.containsKey(packageName)) {
                        if (briefTimes.containsKey(packageName)) {
                            int slot = Application.getSharedPreferences().getInt(SharPre.PREF_BRIEF_SLOT, SharPre.PREF_DEFAULT);
                            if (slot != SharPre.PREF_BRIEF_EVERY_TIME) {
                                int time = 300;
                                if (slot != SharPre.PREF_BRIEF_5_MIN) {
                                    time = Integer.MAX_VALUE;
                                }
                                long t = briefTimes.get(packageName);
                                try {
                                    if ((System.currentTimeMillis() - t) / 1000 < time) {
                                        unlockLastApplication(packageName, false);
                                        handler.post(briefToast);
                                    } else
                                        alertForUnlock(packageName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            briefTimes.remove(packageName);
                        } else
                            alertForUnlock(packageName);
                    }
                }

                try {
                    if (home) {
                        Thread.sleep(50);
                    } else {
                        Thread.sleep(sleeptime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void attachToWindow(WindowManager wm, View v) {
            AppFragment.afterViewCreated(v, ctrl);
            ((MyFrameLayout) v).setOnBackListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backHome();
                }
            });
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT ,
                    0,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.CENTER;
            lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            try {
                if (alertContainer != null) {
                    wm.removeViewImmediate(alertContainer);
                    alertContainer.removeAllViews();
                }
                alertContainer = null;
                alertView = null;
            } catch (Exception ignore) {}
            if (Build.VERSION.SDK_INT >= 21) {
                v.setPadding(0, Utils.getDimens(WorkService.this, 16), 0, 0);
            }
            wm.addView(v, lp);

            alertContainer = (FrameLayout) v;
            v = ((FrameLayout) v).getChildAt(0);
            alertView = v;
        }

        public int lastAsyncTime;
        Flow ctrl = new Flow();
        Runnable dismissRunner = new Runnable() {
            @Override
            public void run() {
                delayTime = 500;
                backHome_();
            }
        };
        Runnable alertRunner = new Runnable() {
            AppFragment.ICheckResult callback = new AppFragment.ICheckResult() {
                @Override
                public void onSuccess() {
                    unlockLastApplication(lastApp, false);
                    hideAlertIfPossible(false);
                }
            };

            @Override
            public void run() {
                alert = true;
                MyTrack.sendEvent(MyTrack.CATE_DEFAULT, MyTrack.ACT_UNLOCK, MyTrack.ACT_UNLOCK, 1L);
                final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                LayoutInflater from = LayoutInflater.from(AppBridge.themeContext == null ? Application.getContext() : AppBridge.themeContext);
                if (SharPre.isUseNormalPasswd()) {
                    View v= PassFragment.getView(from, null, ctrl, callback);
                    attachToWindow(wm, v);

                } else {
                    View v= PattFragment.getView(from, null, ctrl, callback);
                    attachToWindow(wm, v);
                }


            }
        };

        private void alertForUnlock(final String packageName) {
            lastApp = packageName;
            if (Utils.hasSystemAlertPermission(Application.getContext())) {
                alert = true;
                MyAppBridgeImpl.reset(WorkService.this, false, true, packageName);
                    handler.post(alertRunner);
                    lunchapp=packageName;
            } else {
                Intent intent = new Intent(mContext.getApplicationContext(), Unlockactivity.class).
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION).
                        putExtra("action", Unlockactivity.ACTION_UNLOCK_OTHER).putExtra("pkg", packageName);
                mContext.startActivity(intent);
            }
        }
    }

    private void showAd() {
//        if (AdConfig.canShowFullAd()) {
            handler.post(adRunner);
//        }
    }

    private final Runnable adRunner = new Runnable() {
        @Override
        public void run() {
//            FullAdMaster.master().showFullScreenAd();
            AndroidSdk.showFullAd(AndroidSdk.FULL_TAG_PAUSE);
        }
    };

    public String lastApp;
    HashMap<String, Boolean> homes = new HashMap<>();
    HashMap<String, Boolean> tmpUnlockedApps = new HashMap<>();
    IWorker.Stub binder = new IWorker.Stub() {
        @Override
        public void notifyApplockUpdate() throws RemoteException {
            notifyLockedAppsUpdate();
        }

        @Override
        public void updateProtectStatus() throws RemoteException {
            sleep = SharPre.isProtectStopped();
            if (sleep) {
                onStopProtect();
            }
        }

        @Override
        public void toggleProtectStatus() throws RemoteException {
            toggleProtect();
        }

        @Override
        public void showNotification(boolean yes) throws RemoteException {
        }

        @Override
        public boolean unlockApp(String pkg) throws RemoteException {
            lockApps.remove(pkg);
            return true;
        }

        @Override
        public boolean unlockLastApp(boolean unlockAlways) throws RemoteException {
            unlockLastApplication(lastApp, false);
            unlockSuccess(unlockAlways);
            return true;
        }

        @Override
        public boolean homeDisplayed() throws RemoteException {
            if (home) {
                home = false;
                return true;
            } else
                return false;
        }

        @Override
        public void notifyShowWidget() throws RemoteException {
        }
    };

    public void toggleProtect() {
        sleep = !sleep;
        SharPre.stopProtect(sleep);
        if (sleep) {
            onStopProtect();
        }
        if (Application.getSharedPreferences().getBoolean("sn", false)) {
            showNotification(true);
        } else {
            if (sleep) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.suo_stopped_l, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.suo_app_open, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    public void showWidgetIfNecessary(boolean show) {
        if (Application.getSharedPreferences().getBoolean(SharPre.PREF_SHOW_WIDGET, false)) {
            final int visible = show ? View.VISIBLE : View.GONE;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (widgetContainer != null) {
                        widgetContainer.setVisibility(visible);
                    }
                }
            });
        }
    }



    AppWidgetContainer widgetContainer ;

    public void notifyLockedAppsUpdate() {
        try {
            long id = SafeIDB.defaultDB().getLong(SharPre.PREF_ACTIVE_PROFILE_ID, 0L);
            SQLiteDatabase db = MyfileDBHelper.singleton(Application.getContext()).getReadableDatabase();
            Map<String, Boolean> apps = MyfileDBHelper.ProfileEntry.getLockedApps(db, id);
            apps.remove("com.lockscreen_setting_bat.Settings");
            apps.remove("");
            lockApps.clear();
            lockApps.putAll(apps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unlockSuccess(boolean unlockMe) {
        unlocked = true;
        if (unlockMe) {
            try {
                long profileId = SafeIDB.defaultDB().getLong(SharPre.PREF_ACTIVE_PROFILE_ID, 1L);
                lockApps.remove(lastApp);
                MyfileDBHelper.ProfileEntry.deleteLockedApp(MyProfiles.getDB(), profileId, lastApp);
                MyProfiles.updateProfiles();
            } catch (Exception e) {
                e.printStackTrace();
//                TCAgent.onError(Application.getContext(), e);
            }
        }
    }

    public void unlockLastApplication(String app, boolean remove) {
        if (remove) {
            tmpUnlockedApps.remove(app);
        } else {
            tmpUnlockedApps.put(app, true);
        }
        StringBuilder sb = new StringBuilder();
        for (String key : tmpUnlockedApps.keySet()) {
            sb.append(key).append(';');
        }
        getSharedPreferences("tmp", MODE_PRIVATE).edit().putString(SharPre.PREF_TMP_UNLOCK, sb.toString()).commit();
    }

    public void showNotification(boolean yes) {
        if (true) return;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    String getTopPackage() {
        long ts = System.currentTimeMillis();

        if (mUsageStatsManager == null) {
            mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
        }

//        UsageEvents ue = mUsageStatsManager.queryEvents(ts - 1000, ts);
//        UsageEvents.Event event = new UsageEvents.Event();
//        while (ue.hasNextEvent()) {
//            ue.getNextEvent(event);
//        }
//        return event.getPackageName();
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 10000, ts);

        if (usageStats == null || usageStats.size() == 0) {
            return getForegroundApp();
        } else {
            Collections.sort(usageStats, mRecentComp);
            return usageStats.get(0).getPackageName();
        }
    }

    static UsageStatsManager mUsageStatsManager;

    Comparator<UsageStats> mRecentComp = new Comparator<UsageStats>() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : ((lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1);
        }
    };

    LockTask task;

    void startTimer() {
        if (task == null || !task.running) {
            task = new LockTask(getApplicationContext());
        }
    }

    void stopTimer() {
        if (task != null) {
            task.running = false;
            task.interrupt();
            task = null;
        }
    }

    HashMap<String, Boolean> lockApps = new HashMap<>();
    Handler handler;
    Runnable briefToast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), R.string.suo_brief_detai, Toast.LENGTH_SHORT).show();
        }
    };

    Animation fadeout;
    boolean create = true;

    boolean hasAccessUsagePermission = false;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidSdk.onCreate(this);
        context=getApplicationContext();


        Utils.init();
        c.a(this);
        Log.d("daa","suoping11-------");
        Intent serviceIntent=new Intent(this,c.class);
        startService(serviceIntent);
        handler = new Handler(getMainLooper());

        if (Build.VERSION.SDK_INT >= 21) {
            hasAccessUsagePermission = Utils.checkPermissionIsGrant(Application.getContext(), Utils.OP_GET_USAGE_STATS) == AppOpsManager.MODE_ALLOWED;
        }

        String[] top25 = new String[]{
                /*
                "com.facebook.orca",
                "com.google.android.apps.photos",
                "com.facebook.katana",
                "com.amazon.mShop.android.shopping",
                "com.instagram.android",
                "com.snapchat.android",
                "com.netflix.mediaclient",
                "com.whatsapp",
                "com.skype.raider",
                "com.pinterest",
                "com.instagram.layout",
                "com.twitter.android",
                "com.yahoo.mobile.client.android.mail",
                "com.ebay.mobile",
                "com.badoo.mobile",
                "jp.naver.suo_invade_li.android",
                "com.google.android.apps.docs.editors.docs",
                "com.viber.voip",
                "com.google.android.youtube",
                "com.dropbox.android",
                "com.google.android.gm",
                "com.paypal.android.p2pmobile",
                "com.tencent.mm",
                "com.kakao.talk",
                "com.flipkart.android",
                "in.amazon.mShop.android.shopping",
                "com.facebook.lite",
                "com.vkontakte.android",
                "ru.ok.android",
                "kik.android",
                "com.ubercab",
                "com.okcupid.okcupid",
                "com.bbm",
                "com.mxtech.videoplayer.ad",

                "com.android.vending"
                */
        };
        for (String top : top25) {
            if (!SafeIDB.defaultDB().getBool("dontask_" + top, false)) {
                asks.put(top, true);
            }
        }
        PackageManager pm = getPackageManager();
//        TrackerApi.initialize(getApplicationContext(), Application.getGa());//R.string.ga_trackingId
        SharedPreferences sp = Application.getSharedPreferences();
        sleep = sp.getBoolean("stop_service", false);

        new Thread() {
            @Override
            public void run() {
                try {
                    notifyLockedAppsUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        if (Build.VERSION.SDK_INT >= 20) {
//            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//            PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this, WorkService.class).putExtra("alarm", true), PendingIntent.FLAG_UPDATE_CURRENT);
//            am.setRepeating(AlarmManager.RTC_WAKEUP, 1000, 1000, pi);
        }

        if (fadeout == null) {
            fadeout = AnimationUtils.loadAnimation(this, R.anim.activi_out);
            fadeout.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    handler.post(removeRunner);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        String[] unlocked = getSharedPreferences("tmp", MODE_PRIVATE).getString(SharPre.PREF_TMP_UNLOCK, "").split(";");
        for (String unlock : unlocked) {
            tmpUnlockedApps.put(unlock, true);
        }

        showNotification(sp.getBoolean("sn", true));

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> lst = pm.queryIntentActivities(i, 0);
        if (lst != null) {
            for (ResolveInfo resolveInfo : lst) {
                homes.put(resolveInfo.activityInfo.packageName, true);
            }
        }

        startTimer();

        BroadcastReceiver sOnBroadcastReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!Application.getSharedPreferences().getBoolean("stop_service", false)) {
                    if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                        sleep = true;
                    } else {
                        sleep = false;
                        onWakeUp();
                    }
                }
            }
        };
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Intent.ACTION_SCREEN_ON);
        receiverFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(sOnBroadcastReciver, receiverFilter);

//        excludesClasses.put(Main1Activity.class.getName(), true);
        excludesClasses.put(MainActivity.class.getName(), true);
        excludesClasses.put(TogActivity.class.getName(), true);
        excludesClasses.put(Unlockactivity.class.getName(), true);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public static final int WORK_IDLE = 0;
    public static final int WORK_LOCK_NEW = 1;
    public static final int WORK_TURN_ON_PROTECT = 2;
    public static final String WORK_EXTRA_KEY = "works";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        if (intent != null) {
            if (intent.getBooleanExtra("on", false)) {
                onWakeUp();
            } else if (intent.getBooleanExtra("alarm", false)) {
                return START_STICKY;
            } else {
                switch (intent.getIntExtra(WORK_EXTRA_KEY, WORK_IDLE)) {
                    case WORK_LOCK_NEW:
                        lockNew(intent.getStringExtra("pkg"));
                        break;

                    case WORK_TURN_ON_PROTECT:
                        toggleProtect();
                        return START_STICKY;
                }
            }
        }

        return START_STICKY;
    }

    boolean dontaskagain = false;
    public void protect(final String pkg) {
    }

    public void lockNew(final String pkg) {
        try {
            if (lockApps.containsKey(pkg)) return;
            PackageInfo pi = getPackageManager().getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
            String label = pi.applicationInfo.loadLabel(getPackageManager()).toString();
            String format = getResources().getString(R.string.suo_lock_tishi);
            label = String.format(format, label);
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert).setTitle(R.string.app_name).setIcon(R.drawable.suo_dia_ic).setMessage(label)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    lockApps.put(pkg, true);
                                    long profileId = SafeIDB.defaultDB().getLong(SharPre.PREF_ACTIVE_PROFILE_ID, 1L);
                                    MyfileDBHelper.ProfileEntry.addLockedApp(MyProfiles.getDB(), profileId, pkg);
                                    MyProfiles.updateProfiles();
                                }
                            }
                    ).setCancelable(false).create();
            Utils.showDialog(dialog, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    HashMap<String, Boolean> excludesClasses = new HashMap<>();

    public void onWakeUp() {
        String pkgName = getTopPackageName();
        if (getPackageName().equals(pkgName)) {
            String className = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
            if (!excludesClasses.containsKey(className)) {
                Intent i = new Intent(getApplicationContext(), TogActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        }
        int slot = Application.getSharedPreferences().getInt(SharPre.PREF_BRIEF_SLOT, SharPre.PREF_DEFAULT);
        if (slot != SharPre.PREF_BRIEF_5_MIN) {
            tmpUnlockedApps.clear();
            unlockLastApplication("", true);
            briefTimes.clear();
        }
    }

    public void onStopProtect() {
        tmpUnlockedApps.clear();
        briefTimes.clear();
        getSharedPreferences("tmp", MODE_PRIVATE).edit().remove(SharPre.PREF_TMP_UNLOCK).commit();
    }

    @Override
    public void onDestroy() {
        Utils.LOGE("WorkService is destroying...");
        handler = null;
        try {
            hideAlertImmediate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopForeground(true);
        stopTimer();
        super.onDestroy();
    }

    public static void startService(Context context) {
        context.startService(new Intent(context, WorkService.class));
    }
}
