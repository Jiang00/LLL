package com.suo.applock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.android.dev.SafeDB;
import com.lockscreen.api.liberal.liu.SafeIDB;
import com.lockscreen.api.liberal.liu.TuMaster;
import com.suo.applock.menu.SharPre;
import com.suo.applock.yibuasync.ImageTManager;
import com.suo.gallery.labary.TileBitmapDrawable;
import com.lockscreen.api.FileOp;
import com.suo.applock.menu.MyYingys;
import com.suo.applock.menu.MyProfiles;
import com.lockscreen.api.liberal.BaseApplication;
import com.lockscreen.api.liberal.data.DataType;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.Locale;

/**
 * Created by SongHualin on 5/6/2015.
 */
public class Application extends BaseApplication {

    public static Context lockPageContext;

    @Override
    public void onCreate() {
        super.onCreate();
        lockPageContext = getApplicationContext();

        if (requireEarlyReturn()) {
            return;
        }


        watcher = LeakCanary.install(this);
        DataType.init(this);

        if (SharPre.isEnglish()) {
            if (getResources().getConfiguration().locale != Locale.ENGLISH) {
                Configuration cfg = getResources().getConfiguration();
                cfg.locale = Locale.ENGLISH;
                getResources().updateConfiguration(cfg, getResources().getDisplayMetrics());
            }
        }
        Preference.initialize(this);
        FileOp.init(this, ImageTManager.ROOT);
        SafeDB.initialize(this, getHandler());
//        TrackerApi.initialize(this,getGa() );//getString(R.string.ga_trackingId)
//        TCAgent.init(this, BuildConfig.TALKING_DATA_ID, "google");

        TuMaster.imageCache = TileBitmapDrawable.initCache(this);
        ImageTManager.cache = TuMaster.imageCache;


        startService(new Intent(this, WorkService.class));

//        AdMaster.onCreate(this, com.privacy.data.Configda.CACHE);

        MyYingys.init();
        MyProfiles.init();
    }




    public static Context getContextObject() {
        return lockPageContext;
    }

    static RefWatcher watcher;

    public static RefWatcher getWatcher() {
        return watcher;
    }

    public static SharedPreferences getSharedPreferences() {
        return Application.getContext().getSharedPreferences("cf", MODE_MULTI_PROCESS);
    }
}

