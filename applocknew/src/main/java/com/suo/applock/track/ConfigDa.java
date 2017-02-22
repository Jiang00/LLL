package com.suo.applock.track;

import android.os.Environment;

/**
 * Created by song on 15/9/23.
 */
public class ConfigDa {
    public static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String ROOT = SDCARD_ROOT + "/.android/";
    public static final String CACHE = ROOT + "/.themeeeee/";
    public static final boolean INTRUDER_SHUTTER_SOUND_DEFAULT = true;
    public static final String INTRUDER_SHUTTER_SOUND = "_is_s_";
    public static String LOCK_MODE_PATH;



    public static final int PASSWORD_TYPE_NUMBER = 1;
    public static final int PASSWORD_TYPE_PATTERN = 2;
    public static final int PASSWORD_TYPE_DEFAULT = PASSWORD_TYPE_PATTERN;
    public static final String[] PASSWORD_KEYS = {
            "_k_p_",
            "_n_m_",
            "_p_t_",
            "_f_p_",
            "_f_r_",
            "_s_q_"
    };


    public static final String VIBRATE_FOR_PASSWORD_INPUT_KEY = "vb_i";
    public static final boolean VIBRATE_FOR_PASSWORD_INPUT_DEFAULT = true;

}
