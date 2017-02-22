package com.lockscreen.api.liberal.liu;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.provider.MediaStore;

import com.lockscreen.api.liberal.BaseApplication;
import com.lockscreen.api.module.WenjianType;

/**
 * Created by SongHualin on 6/29/2015.
 */
public class LoadNorThum extends LoadAppIcon {

    private static LoadNorThum instance = new LoadNorThum();
    public static LoadNorThum Instance() {
        return instance;
    }

    public static int calcSampleSize(int width, int height, int requireWidth, int requireHeight) {
        if (width <= requireWidth && height <= requireHeight) return 1;
        int inSampleSize = 1;

        if (height > requireHeight || width > requireWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) requireHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) requireWidth);
            }
        }
        return inSampleSize;
    }

    @Override
    protected Bitmap getBitmap(String url, LoadingNotifiable notifiable) throws PackageManager.NameNotFoundException {
        Bitmap bmp = null;
        switch (notifiable.getFileType()) {
            case WenjianType.TYPE_VIDEO:
                bmp = MediaStore.Video.Thumbnails.getThumbnail(BaseApplication.getContext().getContentResolver(), notifiable.getIdLong(), MediaStore.Video.Thumbnails.MICRO_KIND, null);
                break;

            case WenjianType.TYPE_PIC:
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(url, opt);
                Point size = notifiable.getSize();
                opt.inSampleSize = calcSampleSize(opt.outWidth, opt.outHeight, size.x, size.y);
                opt.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeFile(url, opt);
                break;
        }
        return bmp;
    }
}
