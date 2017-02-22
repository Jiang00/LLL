package com.lockscreen.api;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.lockscreen.api.liberal.BaseApplication;
import com.lockscreen.api.liberal.Utils;

import java.io.File;

/**
 * Created by superjoy on 2014/9/1.
 */
public class FileOp {
    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_NOT_FOUND = 1;
    public static final int ERROR_RENAME_FAILS = 2;

    public static final int DATA_PATH = 0;
    public static final int THUMB_PATH = 1;
    public static final int INFO_PATH = 2;
    public static final int BAK_PATH = 3;

    /**
     * lock file
     * type is defined in
     *
     * @param file
     * @param type
     * @return
     */
    public static native boolean en(String file, byte type);

    public static boolean e(String file, int type) {
        if (file == null) return false;
        return en(file, (byte) type);
    }

    /**
     * get last error code
     *
     * @return
     */
    public static native int e();

    /**
     * unlock file
     *
     * @param file
     * @return
     */
    public static native boolean de(String file);

    public static boolean d(String file) {
        if (file == null) return false;
        return de(file);
    }

    /**
     * get file name that encrypted
     * eg. 29281829292893829298299238892893892893
     *
     * @param file
     * @return
     */
    public static native String sj(String file);

    public static String s(String file) {
        if (file == null) return null;
        return sj(file);
    }

    /**
     * get file name that encrypted with thumb/data dir name
     * eg. /sdcard/.superlock/d/019292838272919202002891828983
     *
     * @param file
     * @param thumb
     * @return
     */
    public static native String sj(String file, boolean thumb);

    public static String s(String file, boolean thumb) {
        if (file == null) return null;
        return sj(file, thumb);
    }

    /**
     * initialize
     *
     * @param file root dir of sdcard
     */
    public static native void i(String file);

    /**
     * try found lost files
     * @param filePath
     * @param fileSha256
     * @return
     */
    public static native boolean dd(String filePath, String fileSha256);

    /**
     * get all paths that will be create
     *
     * @return
     */
    public static native String[] p();

    /**
     * get thumb/data/info/bak dir path in sdcard
     *
     * @param type
     * @return
     */
    public static native String p(int type);

    /**
     * file name that will preview
     *
     * @param file
     * @param start
     * @return
     */
    public static native String pr(String file, boolean start);

    public static String p(String file, boolean start) {
        if (file == null) return null;
        return pr(file, start);
    }

    /**
     * get encrypted file info
     *
     * @param file
     * @return
     */
    public static native String fi(String file);

    public static String f(String file) {
        if (file == null) return null;
        return fi(file);
    }

    static {
        System.loadLibrary("file");
    }

    public static String ROOT;

    public static boolean init(Context c, String ROOT) {
        FileOp.ROOT = ROOT;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            BaseApplication.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BaseApplication.getContext(), BaseApplication.getContext().getResources().getIdentifier("sdcard_not_prepared", "string", BaseApplication.getContext().getPackageName()), Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
        i(ROOT);
        String[] p = p();
        for (int i = 0; i < p.length; ++i) {
            File f = new File(p[i]);
            if (!f.mkdirs()) {
                Utils.LOGE("create dir fails " + f);
            }
        }
        return true;
    }
}
