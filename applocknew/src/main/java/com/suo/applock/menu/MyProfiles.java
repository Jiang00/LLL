package com.suo.applock.menu;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.RemoteException;

import com.suo.applock.Application;
import com.suo.applock.db.MyfileDBHelper;
import com.lockscreen.api.liberal.yibuas.LoadingAsync;
import com.lockscreen.api.liberal.liu.SafeIDB;
import com.privacy.lock.aidl.IWorker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by SongHualin on 6/26/2015.
 */
public class MyProfiles {
    private static List<MyfileDBHelper.ProfileEntry> profileList;
    private static String[] profiles;
    public static final String KEY_UPGRADED = "upgrade_profile";

    static SQLiteDatabase db;
    static boolean updateServerStatus = false;

    static final LoadingAsync loadingTask = new LoadingAsync() {
        @Override
        protected void doInBackground() {
            db = MyfileDBHelper.singleton(Application.getContext()).getWritableDatabase();
            upgrade();
            profileList = MyfileDBHelper.ProfileEntry.getProfiles(db);
            updateProfiles();
        }

        private void upgrade(){
            SharedPreferences sp = Application.getSharedPreferences();
            if (sp.contains(SharPre.PREF_PROFILES) && !SafeIDB.defaultDB().getBool(KEY_UPGRADED, false)){
                Set<String> profiles_key = sp.getStringSet(SharPre.PREF_PROFILES, null);
                if (profiles_key != null){
                    String currentProfile = sp.getString(SharPre.PREF_ACTIVE_PROFILE, SharPre.PREF_DEFAULT_LOCK);
                    long currentProfileId = 1L;
                    List<String> apps = new ArrayList<>();
                    try {
                        currentProfileId = MyfileDBHelper.ProfileEntry.createProfile(db, currentProfile, apps);
                        SafeIDB.defaultDB().putLong(SharPre.PREF_ACTIVE_PROFILE_ID, currentProfileId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    for (String profile : profiles_key) {
                        String[] appPkgNames = sp.getString(profile, "").split(";");
                        Collections.addAll(apps, appPkgNames);
                        try {
                            if (profile.equals(currentProfile)){
                                MyfileDBHelper.ProfileEntry.updateProfile(db, currentProfileId, apps);
                                updateServerStatus = true;
                            } else {
                                MyfileDBHelper.ProfileEntry.createProfile(db, profile, apps);
                            }
                            sp.edit().remove(profile).apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        apps.clear();
                    }
                    SafeIDB.defaultDB().putBool(KEY_UPGRADED, true).commit();
                    sp.edit().remove(SharPre.PREF_PROFILES).apply();
                }
            } else if (SafeIDB.defaultDB().getLong(SharPre.PREF_ACTIVE_PROFILE_ID, 0L) == 0L) {
                SafeIDB.defaultDB().putLong(SharPre.PREF_ACTIVE_PROFILE_ID, sp.getLong(SharPre.PREF_ACTIVE_PROFILE_ID, 1L)).commit();
            }
        }
    };

    public static SQLiteDatabase getDB(){
        return db;
    }

    public static void updateProfiles() {
        profiles = new String[profileList.size()];
        for (int i = 0; i < profiles.length; ++i) {
            profiles[i] = profileList.get(i).name;
        }
    }

    public static boolean requireUpdateServerStatus() {
        return updateServerStatus;
    }

    public static void init() {
        loadingTask.start();
    }

    public static List<MyfileDBHelper.ProfileEntry> getEntries() {
        return profileList;
    }

    public static String[] getProfiles() {
        return profiles;
    }

    public static int getActiveProfileIdx(String activeProfile) {
        for (int i = 0; i < profiles.length; ++i) {
            if (profiles[i].equals(activeProfile)) return i;
        }
        return 0;
    }

    public static void waiting(Runnable waiting) {
        loadingTask.waiting(waiting);
    }

    public static void addProfile(MyfileDBHelper.ProfileEntry entry){
        profileList.add(entry);
        updateProfiles();
    }

    public static void removeProfile(MyfileDBHelper.ProfileEntry entry){
        profileList.remove(entry);
        updateProfiles();
    }

    public static void switchProfile(MyfileDBHelper.ProfileEntry entry, IWorker server){
        SafeIDB.defaultDB().putLong(SharPre.PREF_ACTIVE_PROFILE_ID, entry.id).putString(SharPre.PREF_ACTIVE_PROFILE, entry.name).commit();
        if (server != null){
            try {
                server.notifyApplockUpdate();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isLoading() {
        return !loadingTask.isFinished();
    }
}
