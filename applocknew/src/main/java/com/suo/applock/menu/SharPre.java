package com.suo.applock.menu;

import android.content.SharedPreferences;

import com.suo.applock.Application;
import com.lockscreen.api.liberal.Utils;
import com.lockscreen.api.liberal.data.IntType;
import com.lockscreen.api.liberal.liu.SafeIDB;

/**
 * Created by huale on 2014/11/28.
 */
public class SharPre {
    SharedPreferences.Editor editor;

    static SharPre pref = new SharPre();

    public static void upgrade() {
        SafeIDB safeDB = SafeIDB.defaultDB();
        int version = safeDB.getInt("version", 0);
        if (version == 0) {
            boolean useNormal = Application.getSharedPreferences().getBoolean("nor", false);
            safeDB.putBool("nor", useNormal);
            String numpass = Application.getSharedPreferences().getString("pp", "");
            safeDB.putString("pp", numpass);
            String patternPass = Application.getSharedPreferences().getString("pg", "");
            safeDB.putString("pg", patternPass);
            safeDB.putInt("version", 1);
            safeDB.commit();
        }
    }

    public static SharPre begin(){
        if (pref.editor != null)
            throw new RuntimeException("SharPre.begin() is called but haven't called SharPre.commit()");
        pref.editor = Application.getSharedPreferences().edit();
        return pref;
    }

    public void commit(){
        if (editor == null)
            throw new RuntimeException("SharPre.commit() is called but haven't called SharPre.Begin()");
        editor.commit();
        editor = null;
    }

    public SharPre useNormalPasswd(boolean normal){
        SafeIDB.defaultDB().putBool("nor", normal);
        SafeIDB.defaultDB().commit();
//        editor.putBoolean("nor", normal);
        return this;
    }

    public static boolean isUseNormalPasswd() {
        return SafeIDB.defaultDB().getBool("nor", false);
//        return Application.getSharedPreferences().getBoolean("nor", false);
    }

    public SharPre setPasswd(String pass, boolean normal){
//        editor.putString(normal ? "pp" : "pg", pass);
        SafeIDB.defaultDB().putString(normal ? "pp" : "pg", pass);
        SafeIDB.defaultDB().commit();
        return this;
    }

    public static boolean checkPasswd(String pass, boolean normal) {
        String passed = SafeIDB.defaultDB().getString(normal ? "pp" : "pg", "");
        Utils.LOGER("app_password_ " + passed);
        return passed.equals(pass);
//        return Application.getSharedPreferences().getString(normal ? "pp" : "pg", "").equals(pass);
    }

    public static String getPasswd() {
        return SafeIDB.defaultDB().getString("pp", "");
//        return Application.getSharedPreferences().getString("pp", "");
    }

    public static String getPattern() {
        return SafeIDB.defaultDB().getString("pg", "");
    }

    public static boolean isPasswdSet(boolean normal){
        String string = SafeIDB.defaultDB().getString(normal ? "pp" : "pg", "");
//        String string = Application.getSharedPreferences().getString(normal ? "pp" : "pg", null);
        return string != null && string.length() > 0;
    }


    public SharPre putBoolean(String tag, boolean value){
        editor.putBoolean(tag, value);
        return this;
    }

    public SharPre remove(String tag){
        editor.remove(tag);
        return this;
    }

    public SharPre putString(String tag, String value){
        editor.putString(tag, value);
        return this;
    }

    public SharPre putInt(String tag, int value){
        editor.putInt(tag, value);
        return this;
    }

    public static boolean isANewDay(){
        long yest = Application.getSharedPreferences().getLong("yesterday", 0L);
        boolean newday = (System.currentTimeMillis() - yest) / 1000 >= 86400;
        if (newday){
            Application.getSharedPreferences().edit().putLong("yesterday", System.currentTimeMillis()).apply();
        }
        return newday;
    }

    public static boolean hasReddot(){
        return Application.getSharedPreferences().getBoolean("suo_red", true);
    }


    public static boolean tip4Rate(){
        return Application.getSharedPreferences().getInt("rate", 0) >= 12 && !Application.getSharedPreferences().contains("rate_showed");
    }




    public static final int OPT_RATE_REDDOT = 0;
    public static final int OPT_ADVANCE_REDDOT = 1;
    static final String[] setting_reddot_key = {
            "rate_red", "advance_red", "toggle_red"
    };

    public static boolean hasOption(int idx){
        return Application.getSharedPreferences().contains(setting_reddot_key[idx]);
    }

    public static void pressOption(int idx){
        Application.getSharedPreferences().edit().putBoolean(setting_reddot_key[idx], true).apply();
    }

    public static boolean isOptionPressed(int idx){
        return Application.getSharedPreferences().getBoolean(setting_reddot_key[idx], false);
    }

    public static boolean isAdvanceEnabled(){
        return Application.getSharedPreferences().getBoolean("advanced", false);
    }

    public static void enableAdvance(boolean yes){
        Application.getSharedPreferences().edit().putBoolean("advanced", yes).apply();
    }

    public static boolean requireFetchAgain(){
        SharedPreferences sp = Application.getSharedPreferences();
        long l = sp.getLong("__last_fetch", 0L);
        long current = System.currentTimeMillis();
        if (current - l > 10000) {
            sp.edit().putLong("__last_fetch", current).apply();
            long last = sp.getLong("help_fetch_time", 0L);
            return current - last > 3600000;
        } else {
            return false;
        }
    }

    public static void fetchAgainSuccess(){
        Application.getSharedPreferences().edit().putLong("help_fetch_time", System.currentTimeMillis()).apply();
    }

    public static boolean isProtectStopped(){
        return Application.getSharedPreferences().getBoolean("stop_service", false);
    }

    public static void stopProtect(boolean yes){
        Application.getSharedPreferences().edit().putBoolean("stop_service", yes).apply();
    }

    public static void selectLanguage(boolean english){
        Application.getSharedPreferences().edit().putBoolean(PREF_LANG, english).apply();
    }

    public static boolean isEnglish(){
        return Application.getSharedPreferences().getBoolean(PREF_LANG, false);
    }

    public static final int PREF_BRIEF_EVERY_TIME = 0;
    public static final int PREF_BRIEF_5_MIN = 1;
    public static final int PREF_BRIEF_AFTER_SCREEN_OFF = 2;
    public static final int PREF_DEFAULT = PREF_BRIEF_EVERY_TIME;
    public static final String PREF_BRIEF_SLOT = "brief_slot";

    public static final String PREF_DEFAULT_LOCK = "Default";
    public static final String PREF_TMP_UNLOCK = "tmp-suo_main_not_check";
    public static final String PREF_ACTIVE_PROFILE = "active_profile";
    public static final String PREF_ACTIVE_PROFILE_ID = "active_profile_id";
    public static final String PREF_PROFILES = "lock_profiles";

    public static final String LOCK_NEW = "lock_new";
    public static final boolean LOCK_DEFAULT = true;

    public static final String PREF_SHOW_WIDGET = "lockscreen_widget";

    public static final String PREF_LANG = "lang";

    public static boolean requireAsk() {
        long last = Application.getSharedPreferences().getLong("last-ask-time", 0L);
        if (System.currentTimeMillis() - last > 86400000) {
            Application.getSharedPreferences().edit().putLong("last-ask-time", System.currentTimeMillis()).apply();
            return true;
        }
        return false;
    }

    public static boolean hasNewVersion() {
        boolean has = Application.getSharedPreferences().getBoolean(BackgData.KEY_NEW_VERSION, false);
        if (has) {
//            Application.getSharedPreferences().edit().remove(BackgData.KEY_NEW_VERSION).apply();
        }
        return has;
    }

    public static String getNewVersionDesc() {
        return Application.getSharedPreferences().getString(BackgData.KEY_NEW_VERSION_DESC, "");
    }

    public static boolean hasIntruder() {
        return Application.getSharedPreferences().getBoolean("suo_invade_ac", false);
    }

    public static void setHasIntruder(boolean yes) {
        Application.getSharedPreferences().edit().putBoolean("suo_invade_ac", yes).apply();
    }

    public static boolean fetchIntruder() {
        return Application.getSharedPreferences().getBoolean("fetch_intruder", true);
    }
//入侵者拍照开关
    public static void setFetchIntruder(boolean yes) {
        Application.getSharedPreferences().edit().putBoolean("fetch_intruder", yes).apply();
    }

    public static final IntType blockAdsTime = new IntType("tf_block_ads", 0);

    public static boolean isAdsBlocked() {
        int time = blockAdsTime.getValue();
        if ((System.currentTimeMillis() / 1000L - time) < 86400) {
            return true;
        } else {
            return false;
        }
    }

}
