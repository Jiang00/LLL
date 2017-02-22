package com.suo.applock;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.dev.SafeDB;
import com.suo.applock.track.ConfigDa;

import java.util.Calendar;

/**
 * Created by song on 15/10/21.
 */
public class Preference {
    private static SharedPreferences sharedPreferences;

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }


    public static boolean getSet() {
        return sharedPreferences.getBoolean("_s_s_s_s_", false);
    }

    public static void setSet(boolean b) {
        sharedPreferences.edit().putBoolean("_s_s_s_s_", b).apply();
    }

    public static boolean getBattery() {
        return sharedPreferences.getBoolean("_b_a_t_t_", false);
    }

    public static void setBattery(boolean b) {
        sharedPreferences.edit().putBoolean("_b_a_t_t_", b).apply();
    }

    public static void setScreenLockisLock(boolean b){
        sharedPreferences.edit().putBoolean("_isLoc_k", b).apply();
    }
    public static boolean getScreenLockisLock(){
        return sharedPreferences.getBoolean("_isLoc_k",false );
    }
    public static boolean isFirstLunch(){
        return sharedPreferences.getBoolean("_first_Lunch",true );
    }
    public static void setFisetLunch(boolean b){
        sharedPreferences.edit().putBoolean("_first_Lunch", b).apply();
    }


    public static void initialize(Context context) {
        sharedPreferences = context.getSharedPreferences("_c_f_", Context.MODE_MULTI_PROCESS);
    }

    public static boolean snackBarNewDay() {
        boolean newDay = System.currentTimeMillis() / 1000L - sharedPreferences.getInt("_new_day_snack_bar", 0) >= 86400;
        if (newDay) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MILLISECOND, 0);
            int time = (int) (cal.getTimeInMillis()/1000);
            sharedPreferences.edit().putInt("_new_day_snack_bar", time).apply();
        }
        return newDay;
    }

    public static int getPasswordMode() {
        return SafeDB.defaultDB().getInt(ConfigDa.PASSWORD_KEYS[0], ConfigDa.PASSWORD_TYPE_DEFAULT);
    }
    public static boolean isVibrateForPasswordInputEnabled() {
        return sharedPreferences.getBoolean(
                ConfigDa.VIBRATE_FOR_PASSWORD_INPUT_KEY,
                ConfigDa.VIBRATE_FOR_PASSWORD_INPUT_DEFAULT);
    }
public static boolean isTagScreenlock(){
    return sharedPreferences.getBoolean("_tag_srceen_lock_", false);
}
public static void putTagScreenlock(boolean value){
    sharedPreferences.edit().putBoolean("_tag_srceen_lock_", value).apply();
}




//入侵者
    private static final String INTRUDER_SLOT_KEY = "_intruder_sl_";

    public static void setIntruderSlot(int intruderSlot) {
        sharedPreferences.edit().putInt(INTRUDER_SLOT_KEY, intruderSlot).apply();
    }

    public static int getIntruderSlot() {
        return sharedPreferences.getInt(INTRUDER_SLOT_KEY, 0);
    }

    public static boolean isShutterSoundEnabled() {
        return sharedPreferences.getBoolean(ConfigDa.INTRUDER_SHUTTER_SOUND, ConfigDa.INTRUDER_SHUTTER_SOUND_DEFAULT);
    }

    public static void setShutterSoundEnabled(boolean enable) {
        sharedPreferences.edit().putBoolean(ConfigDa.INTRUDER_SHUTTER_SOUND, enable).apply();
    }

















    public static boolean hasDaily() {
        return sharedPreferences.getBoolean("_DAILY_", false);
    }

    public static void setHasDaily(boolean hasDaily) {
        sharedPreferences.edit().putBoolean("_DAILY_", hasDaily).apply();
    }


    public static void setHasNewUpgrade(boolean hasNewUpgrade) {
        sharedPreferences.edit().putBoolean("_UPGRADE_", hasNewUpgrade).apply();
    }

    public static String getDailyUrl() {
        return sharedPreferences.getString("_DAILY_URL_", "");
    }

    public static void setDailyUrl(String url) {
        sharedPreferences.edit().putString("_DAILY_URL_", url).apply();
    }
}
