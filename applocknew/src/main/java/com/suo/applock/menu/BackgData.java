package com.suo.applock.menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.client.AndroidSdk;
import com.android.dev.ICacheHandler;
import com.android.dev.queue.QueueEvent;
import com.android.dev.queue.QueuedExecutor;
import com.suo.applock.Application;
import com.suo.applock.Configda;
import com.suo.applock.yibuasync.ImageTManager;
import com.suo.applock.Preference;
import com.suo.applock.Toolcls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by huale on 2015/2/6.
 */
public class BackgData implements ICacheHandler {
    public static final String KEY_DAILY_MENU = "daily";
    public static final String KEY_DAILY_UNLOCK = "unlock";
    public static final String KEY_DAILY_UNLOCK_URL = "unlock_url";
    public static final String KEY_DAILY_UNLOCK_NEW = "unlock_new";
    public static final String KEY_DAILY_ICON = "icon";
    public static final String KEY_DAILY_ICON_PREF_KEY = "icon_url";
    public static final String KEY_DAILY_ICON_PERSISTENT = "icon_persistent";

    public static final String KEY_PLUGINS = "plugins";
    public static final String KEY_PLUGIN_NAME = "name";
    public static final String KEY_PLUGIN_DESC = "desc";
    public static final String KEY_PLUGIN_URL = "url";
    public static final String KEY_PLUGIN_ICON = "icon";
    public static final String KEY_PLUGIN_PKG = "pkg";
    public static final String KEY_PLUGIN_HIDE_DESKTOP_ICON = "hide";


    public static final String KEY_NEW_VERSION = "version";
    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_VERSION_EDITION = "versionEdition";
    public static final String KEY_NEW_VERSION_DESC = "desc";

    public static final String KEY_DAYLY_SILENCE = "silence";
    public static final String KEY_DAYLY_SILENCE_TIME = "silenceTime";
    public static final String KEY_DAYLY_NOTIFICATION = "notification";

    public static final String KEY_HAS_DAILY = "hasUpgrade";

    public static final String KEY_COMMON_URL = "url";


    private static final BackgData data = new BackgData();

    public static void onReceiveData(Context context, String extraJson) {
        try {

            JSONObject extraData = new JSONObject(extraJson);
            data.onReceive(context, extraData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void fetchIfNecessary(final Context context) {
        if (SharPre.requireFetchAgain() && Toolcls.isWifi(context)) {
//            Http.executeGetResult(new Http.OnHttpResult() {
//                @Override
//                public void onSuccess(String result) {
//                    try {
//                        data.onReceive(context, new JSONObject(result));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(int code, String exception) {
//                    Utils.LOGER("fail: " + code + " exception" + exception);
//                }
//            },getUrl());//BuildConfig.SERVER_URL
            String extraJson = AndroidSdk.getExtraData();
            Log.e("ha", "extra " + extraJson);
            JSONObject extraData = null;
            try {
                extraData = new JSONObject(extraJson);
                if (extraData != null) {
                    data.onReceive(context, extraData);
                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
    }

    private JSONObject newObj;

    public void onReceive(Context context, JSONObject obj) throws JSONException {
        SharedPreferences sp = Application.getSharedPreferences();
        int ourVersionCode = 0;
        try {
            newObj = obj;//obj.getJSONObject(NEW_THEME_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (newObj.has(KEY_NEW_VERSION)) {
                int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                ourVersionCode = versionCode;

                Log.e("myversioncode", ourVersionCode + "----");
                JSONObject version = newObj.getJSONObject(KEY_NEW_VERSION);
                if (version.getInt(KEY_VERSION_CODE) > versionCode) {
                    sp.edit().putBoolean(KEY_NEW_VERSION, true).putString(KEY_VERSION_EDITION, version.getString(KEY_VERSION_EDITION))
                            .putString(KEY_NEW_VERSION_DESC, version.getString(KEY_NEW_VERSION_DESC)).apply();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            if (newObj.has(KEY_DAILY_UNLOCK)) {
                String s = newObj.getString(KEY_DAILY_UNLOCK);
                /**
                 * @design
                 * @see KEY_DAILY_MENU's design
                 */
                if (s.length() < 3) {
                    sp.edit().putBoolean(KEY_DAILY_UNLOCK, false).apply();
                } else {
                    sp.edit().putBoolean(KEY_DAILY_UNLOCK, true).putString(KEY_DAILY_UNLOCK_URL, s).putBoolean(KEY_DAILY_UNLOCK_NEW, true).apply();
                }
            } else
                sp.edit().putBoolean(KEY_DAILY_UNLOCK, false).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }




        try {
            if (newObj.has(KEY_PLUGINS)) {
                JSONArray jsonArray = newObj.getJSONArray(KEY_PLUGINS);
                int size = jsonArray.length();
                SharedPreferences.Editor editor = sp.edit();
                Set<String> pluginNames = new HashSet<>(size);
                for (int i = 0; i < size; ++i) {
                    JSONObject ob = jsonArray.getJSONObject(i);
                    String name = ob.getString(KEY_PLUGIN_NAME);
                    String desc = ob.getString(KEY_PLUGIN_DESC);
                    String url = ob.getString(KEY_PLUGIN_URL);
                    String icon = ob.getString(KEY_PLUGIN_ICON);
                    String pkg = ob.getString(KEY_PLUGIN_PKG);
                    boolean hide = ob.getBoolean(KEY_PLUGIN_HIDE_DESKTOP_ICON);

                    String pluginIconKeyName = DefuMeta.PLUGIN_ICON_PREFIX + pkg;
//                    addCache(pluginIconKeyName, icon);

                    DefuMeta pd = new DefuMeta(name, desc, url, pkg, pluginIconKeyName, hide);
                    String pluginKeyName = DefuMeta.PLUGIN_PREFIX + pkg;
                    editor.putString(pluginKeyName, pd.toString());
                    pluginNames.add(pluginKeyName);
                }
                editor.putStringSet(KEY_PLUGINS, pluginNames).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (newObj.has(KEY_DAYLY_SILENCE)) {
                if (Preference.isFirstLunch()) {
                    Preference.setFisetLunch(false);
                    JSONObject json = newObj.getJSONObject(KEY_DAYLY_SILENCE);
                    int notifi = (int) json.get(KEY_DAYLY_NOTIFICATION);
                    Log.i("bbb", notifi + "------------------");
                    Application.getSharedPreferences().edit().putInt(Configda.SILENCETIME_NOTIFI, notifi).apply();
                    int time = (int) json.get(KEY_DAYLY_SILENCE_TIME);
                    Log.e("silencetime", time + "");
                    Application.getSharedPreferences().edit().putInt(Configda.SILENCETIME, time).apply();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (newObj.has("ads")) {
                JSONObject o = newObj.getJSONObject("ads");
                int versionCode = o.getInt(KEY_VERSION_CODE);
                Log.e("ourCode", ourVersionCode + "");
                Log.e("versionCode", versionCode + "----");

                if (ourVersionCode <= versionCode) {
                    Preference.setHasDaily(o.getBoolean(KEY_HAS_DAILY));
                    String url = o.getString(KEY_COMMON_URL);
                    Preference.setHasNewUpgrade(url.equals(Preference.getDailyUrl()));
                    Preference.setDailyUrl(url);
//                    addCache(FiraActivity.DAILY_ICON_CACHE_KEY, o.getString(KEY_DAILY_ICON));
//                    addCache(FiraActivity.DAILY_IMAGE_CACHE_KEY, o.getString(KEY_DAILY_IMAGE));
                } else {
                    Preference.setHasDaily(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        SharPre.fetchAgainSuccess();
    }

    /**
     * @param prefKey
     * @param url
     * @design check if target is cached, if cached, check it is valid or not
     * if valid then ignore this target
     * <p/>
     * download target and compress to sdcard
     */
    public void addCache(final String prefKey, final String url) {
        if (Application.getSharedPreferences().contains(prefKey)) {
            String oldUrl = Application.getSharedPreferences().getString(prefKey, "");
            File f = new File(ImageTManager.CACHE_ROOT + prefKey);
            if (url.equals(oldUrl)) {
                if (f.exists() && f.length() > 0)
                    return;
            } else {
                f.delete();
            }
        }
    }

    public BackgData() {
        queueEventQueuedExecutor.start();
    }

    final QueuedExecutor<QueueEvent> queueEventQueuedExecutor = new QueuedExecutor<QueueEvent>() {
        @Override
        public void onNext(QueueEvent queueEvent) {
            queueEvent.runner.run();
        }

        @Override
        public boolean onDone() {
            return false;
        }
    };
}
