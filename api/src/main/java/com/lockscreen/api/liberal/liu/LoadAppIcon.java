package com.lockscreen.api.liberal.liu;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;

import com.lockscreen.api.liberal.BaseApplication;
import com.lockscreen.api.liberal.yibuas.LoadingAsync;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by SongHualin on 6/12/2015.
 */
public class LoadAppIcon extends LoadingAsync {
    public interface LoadingNotifiable {
        String getUrl();

        Point getSize();

        int getFileType();

        long getIdLong();

        void offer(Bitmap bitmap);
    }

    private static LoadAppIcon instance = new LoadAppIcon();
    public static LoadAppIcon Instance() {
        return instance;
    }

    LinkedBlockingQueue<LoadingNotifiable> queue = new LinkedBlockingQueue<>();

    public void execute(LoadingNotifiable notifiable) {
        try {
            if (queue.contains(notifiable)) return;
            queue.put(notifiable);
            super.restart(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doInBackground() {
        try {
            while (!isCanceled() && !queue.isEmpty()) {
                LoadingNotifiable notifiable = queue.poll();
                String pkgName = notifiable.getUrl();
                if (pkgName == null) continue;
                if (TuMaster.hasImage(pkgName)) {
                    notifiable.offer(TuMaster.getImage(pkgName));
                } else {
                    Bitmap drawable = getBitmap(pkgName, notifiable);
                    if (drawable != null) {
                        TuMaster.addImage(pkgName, drawable);
                    }
                    if (pkgName.equals(notifiable.getUrl())) {
                        notifiable.offer(drawable);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Bitmap getBitmap(String url, LoadingNotifiable notifiable) throws PackageManager.NameNotFoundException {
        PackageManager pm = BaseApplication.getContext().getPackageManager();
        PackageInfo p = pm.getPackageInfo(url, PackageManager.GET_UNINSTALLED_PACKAGES);
        BitmapDrawable drawable = (BitmapDrawable) p.applicationInfo.loadIcon(pm);
        return drawable.getBitmap();
    }
}
