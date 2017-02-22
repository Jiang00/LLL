package com.lockscreen.api.liberal.liu;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lockscreen.api.FileOp;

/**
 * Created by SongHualin on 6/29/2015.
 */
public class LoadSafe extends LoadAppIcon {
    private static LoadSafe instance = new LoadSafe();
    public static LoadSafe Instance() {
        return instance;
    }

    @Override
    protected Bitmap getBitmap(String url, LoadingNotifiable notifiable) throws PackageManager.NameNotFoundException {
        return BitmapFactory.decodeFile(FileOp.s(url, true));
    }
}
