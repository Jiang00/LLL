package com.suo.applock;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import com.android.client.AndroidSdk;
import com.suo.applock.menu.AppBridge;
import com.suo.applock.menu.SharPre;
import com.suo.applock.view.PattFragment;
import com.suo.applock.db.MyfileDBHelper;
import com.suo.applock.menu.MyProfiles;
import com.suo.applock.view.PassFragment;
import com.lockscreen.api.liberal.liu.TuMaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@SuppressWarnings("unchecked")
/**
 * Created by superjoy on 2014/8/25.
 */
public class MainActivity extends SetupActivity {
    public static final int ACTION_UNLOCK_SELF = 0;
    public static final int ACTION_UNLOCK_OTHER = 1;
    public static final int ACTION_SWITCH_PROFILE = 2;
    public static final int ACTION_TOGGLE_PROTECT = 3;
    int action = ACTION_UNLOCK_SELF;

    PattFragment patternFrag;
    PassFragment passFrag;
    boolean normal = false;

    public void toggle(boolean normal) {
        if (normal) {
            if (passFrag == null) {
                passFrag = new PassFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.suo_frag_co, passFrag).commitAllowingStateLoss();
        } else {
            if (patternFrag == null) {
                patternFrag = new PattFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.suo_frag_co, patternFrag).commitAllowingStateLoss();
        }
        this.normal = normal;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
    }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }


    @Override
    protected void onStart() {
        resetThemeBridgeImpl();
        super.onStart();
        if (!toggled && (passFrag != null || patternFrag != null)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.suo_frag_co, normal ? passFrag : patternFrag).commitAllowingStateLoss();

        }
    }

    protected void resetThemeBridgeImpl() {
//        MyAppBridgeImpl.reset(this, true, false, "");
        MyAppBridgeImpl.reset(this, action == ACTION_UNLOCK_SELF, false, pkg);
    }

    @Override
    public void onResume() {
        switchTheme();
        super.onResume();
        AndroidSdk.onResume(this);
    }

    @Override
    protected void onStop() {
//        if (passFrag != null || patternFrag != null){
//            getSupportFragmentManager().beginTransaction().remove(normal ? passFrag : patternFrag).commitAllowingStateLoss();
//        }
//        toggled = false;
        this.finish();
        super.onStop();
    }


    public void unlockSuccess(boolean unlockMe) {
        switch (action) {
            case ACTION_SWITCH_PROFILE:
                for (MyfileDBHelper.ProfileEntry entry : MyProfiles.getEntries()) {
                    if (entry.name.equals(profileName)) {
                        MyProfiles.switchProfile(entry, server);
                        break;
                    }
                }
                finish();
                break;

            case ACTION_TOGGLE_PROTECT:
                try {
                    server.toggleProtectStatus();
                    finish();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

            case ACTION_UNLOCK_OTHER:
                try {
                    server.unlockLastApp(unlockMe);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
                break;

            case ACTION_UNLOCK_SELF:
                startListApp();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (unlockApp || setting == SET_EMPTY) {
            backHome();
        }
        finish();
    }

    boolean firstLaunchShowResult = false;

    @Override
    public void startListApp() {
        if (firstLaunchShowResult) {
            firstTimeLaunch();
            firstLaunchShowResult = false;
            return;
        }

        if (Preference.getSet()) {
            Intent intent = new Intent();
            intent.setClassName(getPackageName(), SettingActivity.class.getName());
            startActivity(intent);
            Preference.setSet(false);
            Log.d("aaa", "----applock1");
        } else {
            Intent intent = new Intent();
            intent.setClassName(getPackageName(), SuoMain.class.getName());
            intent.putExtra("lockscreen_hide", false);
            intent.putExtra("lockscreen_launch", true);
            startActivity(intent);
            Log.d("aaa", "----applock2");
        }
        Log.d("aaa", "----applock3");
        finish();
    }

    public void switchTheme() {
        if (AppBridge.requestTheme && AppBridge.needUpdate) {
            AppBridge.needUpdate = false;
            AppBridge.requestTheme = false;
            destroyThemeContext();
            selectOperation();
        }
    }

    public void destroyThemeContext() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (passFrag != null) {
            fragmentTransaction.remove(passFrag);
        }
        if (patternFrag != null) {
            fragmentTransaction.remove(patternFrag);
        }
        fragmentTransaction.commit();
        passFrag = null;
        patternFrag = null;
        AppBridge.themeContext = null;
        System.gc();
    }

    public boolean unlockApp = false;
    public byte setting;

    @Override
    public void setupView() {
        if (SharPre.isANewDay()) {
            MyTrack.sendEvent(MyTrack.CATE_ACTION, MyTrack.ACT_DAILY_USE, MyTrack.ACT_DAILY_USE, 1L);
        }
        SharPre.upgrade();
        Intent intent = getIntent();
        if (intent.hasExtra("lockscreen_theme")) {
            String theme = intent.getStringExtra("lockscreen_theme");
            Application.getSharedPreferences().edit().putString("lockscreen_theme", theme).putBoolean("lockscreen_theme-switched", true).apply();
            AppBridge.needUpdate = true;
            AppBridge.requestTheme = true;
            switchTheme();
        } else {
            selectOperation();
        }
    }

    ArrayList<String> firstLaunchList;
    HashMap<String, String> firstLaunchLabels;
    HashMap<String, Boolean> firstLaunchLocked;
    HashMap<String, Boolean> firstLaunchFilter;

    void loadPackages() {
        if (firstLaunchList != null && firstLaunchList.size() > 0) return;
        PackageManager packageManager = getPackageManager();
        String[] predefinedpkgs = new String[]{

                "com.facebook.katana",
                "com.facebook.orca",
                "com.whatsapp",
                "com.android.chrome",
                "com.google.android.googlequicksearchbox",
                "com.android.phone",
                "com.android.mms",
                "com.android.vending",
                "com.sec.android.gallery3d",
                "com.vkontakte.android",
                "com.android.gallery3d",
                "com.android.gallery",
                "com.android.email",
                "jp.naver.suo_invade_li.android",
                "com.kakao.talk",
                "com.instagram.android",
                "com.twitter.android",
                "com.android.contacts",
                "com.google.android.gm",
                "com.google.android.youtube",
                "com.tencent.mm",
                "com.google.android.talk",
                "com.skype.raider",
        };
        ArrayList<String> commons = new ArrayList<>();
        HashMap<String, Boolean> filter = new HashMap<>();
        HashMap<String, String> labels = new HashMap<>();
        for (String pkg : predefinedpkgs) {
            try {
                PackageInfo pi = packageManager.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
                labels.put(pkg, pi.applicationInfo.loadLabel(packageManager).toString());
                commons.add(pkg);
            } catch (Exception ignore) {
            }
            filter.put(pkg, true);
        }

        firstLaunchList = commons;
        firstLaunchLabels = labels;
        firstLaunchLocked = new HashMap<>();
        for (int i = 0; i < commons.size(); ++i) {
            if (i > 5) break;
            firstLaunchLocked.put(commons.get(i), true);
        }
        firstLaunchFilter = filter;
    }

    public void firstTimeLaunch() {
        setContentView(R.layout.suo_first_ftl);

        loadPackages();

        final Button next = (Button) findViewById(R.id.suo_main_next);
        final ListView lv = (ListView) findViewById(R.id.suo_list_v);

        View header = getLayoutInflater().inflate(R.layout.suo_first_han, null, false);
        lv.addHeaderView(header, null, false);

        final boolean clickable = !firstLaunchShowResult;

        lv.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return firstLaunchList.size();
            }

            @Override
            public boolean isEnabled(int position) {
                return clickable;
            }

            @Override
            public boolean areAllItemsEnabled() {
                return clickable;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewPho holder;

                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.suo_first_select_check, parent, false);
                    holder = new ViewPho();
                    holder.icon = (android.widget.ImageView) convertView.findViewById(R.id.suo_ima);
                    holder.appName = (TextView) convertView.findViewById(R.id.suo_appna);
                    holder.encrypted = convertView.findViewById(R.id.suo_backg_s);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewPho) convertView.getTag();
                }

                String pkg = firstLaunchList.get(position);
                Bitmap icon = TuMaster.getImage(pkg);
                if (icon == null) {
                    try {
                        BitmapDrawable bd = (BitmapDrawable) getPackageManager().getPackageInfo(pkg, 0).applicationInfo.loadIcon(getPackageManager());
                        icon = bd.getBitmap();
                        TuMaster.addImage(pkg, icon);
                    } catch (OutOfMemoryError | Exception error) {
                        error.printStackTrace();
                    }
                }
                holder.icon.setImageBitmap(icon);
                holder.appName.setText(firstLaunchLabels.get(pkg));
                holder.encrypted.setEnabled(firstLaunchLocked.containsKey(pkg));
                if (!clickable) {
                    holder.encrypted.setSelected(false);
                } else {
                    holder.encrypted.setSelected(true);
                }

                return convertView;
            }
        });

        if (!firstLaunchShowResult) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) return;
                    --position;
                    String pkg = firstLaunchList.get(position);
                    boolean locked = firstLaunchLocked.containsKey(pkg);
                    if (locked) {
                        firstLaunchLocked.remove(pkg);
                    } else {
                        firstLaunchLocked.put(pkg, true);
                    }
                    if (firstLaunchLocked.size() > 0) {
                        next.setEnabled(true);
                    } else {
                        next.setEnabled(false);
                    }
                    ((BaseAdapter) ((WrapperListAdapter) lv.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
                }
            });
            new Thread() {
                @Override
                public void run() {
                    PackageManager packageManager = getPackageManager();
                    final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> pkgs = packageManager.queryIntentActivities(mainIntent, 0);

                    String pkgname = getPackageName();

                    HashMap<String, String> labels = new HashMap<>();
                    ArrayList<String> apps = new ArrayList<>();
                    for (int i = 0; i < pkgs.size(); ++i) {
                        ResolveInfo pkg = pkgs.get(i);
                        String pkgName = pkg.activityInfo.packageName;
                        if (pkgName.equals(pkgname)) {
                            pkgs.remove(i);
                            --i;
                            continue;
                        }
                        String pn = pkgName.toLowerCase();
                        if ((pkg.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                            pkgs.remove(i);
                            --i;
                        } else if (firstLaunchFilter.containsKey(pn)) {
                            pkgs.remove(i);
                            --i;
                        } else {
                            labels.put(pkgName, pkg.loadLabel(packageManager).toString());
                            apps.add(pkgName);
                            if (labels.size() == 10 || i == pkgs.size() - 1) {
                                final HashMap<String, String> labels_ = (HashMap<String, String>) labels.clone();
                                final ArrayList<String> apps_ = (ArrayList<String>) apps.clone();
                                labels.clear();
                                apps.clear();
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        firstLaunchList.addAll(apps_);
                                        apps_.clear();
                                        firstLaunchLabels.putAll(labels_);
                                        labels_.clear();
                                        ((BaseAdapter) ((WrapperListAdapter) lv.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                }
            }.start();
        } else {
            next.setEnabled(true);
            firstLaunchList.clear();
            firstLaunchList.addAll(firstLaunchLocked.keySet());
        }

        if (firstLaunchShowResult) {
            TextView title = (TextView) header.findViewById(R.id.suo_title_bar_te);
            TextView desc = (TextView) header.findViewById(R.id.suo_text_des);
            title.setText(R.string.suo_protect_succ);
            desc.setText(R.string.suo_select_a_lo);
            next.setText(R.string.suo_done);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startListApp();
                }
            });
        } else {
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstSetup = true;
                    firstLaunchShowResult = true;
                    setPasswdView();
                }
            });
        }
    }

    @Override
    public void setEmail() {
        if (firstSetup) {
            if (firstLaunchLocked != null && firstLaunchLocked.size() > 0) {
                try {
                    SQLiteDatabase db = MyfileDBHelper.singleton(getApplicationContext()).getWritableDatabase();
                    long profileId = MyfileDBHelper.ProfileEntry.createProfile(db, SharPre.PREF_DEFAULT_LOCK, new ArrayList<>(firstLaunchLocked.keySet()));
                    MyfileDBHelper.ProfileEntry entry = new MyfileDBHelper.ProfileEntry();
                    entry.id = profileId;
                    entry.name = SharPre.PREF_DEFAULT_LOCK;
                    MyProfiles.addProfile(entry);
                    MyProfiles.switchProfile(entry, server);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.setEmail();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("action", action);
        outState.putString("pkg", pkg);
        outState.putInt("lockscreen_setting_bat", setting);
    }

    @Override
    protected void onRestoreInstanceStateOnCreate(Bundle savedInstanceState) {
        action = savedInstanceState.getInt("action");
        pkg = savedInstanceState.getString("pkg");
        setting = (byte) savedInstanceState.getInt("lockscreen_setting_bat");
    }

    String pkg;

    @Override
    protected void onIntent(Intent intent) {
        action = intent.getIntExtra("action", intent.hasExtra("pkg") ? ACTION_UNLOCK_OTHER : ACTION_UNLOCK_SELF);
        pkg = intent.getStringExtra("pkg");
        setting = intent.getByteExtra("set", SET_EMPTY);
        profileName = intent.getStringExtra("profileName");
    }

    boolean toggled = false;
    String profileName = null;

    public void selectOperation() {
        try {
            switch (setting) {
                case SET_EMPTY:
                    MyAppBridgeImpl.reset(this, action == ACTION_UNLOCK_SELF, false, pkg);
                    if (SharPre.isPasswdSet(true) || SharPre.isPasswdSet(false)) {
                        unlockApp = true;
                        setContentView(R.layout.suo_passwd_con);
                        toggle(SharPre.isUseNormalPasswd());
                        toggled = true;
                    } else {
                        firstTimeLaunch();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
//            TCAgent.onError(getApplicationContext(), e);
            recreate();
        }
    }

    @Override
    protected void onDestroy() {

        if (passFrag != null || patternFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(normal ? passFrag : patternFrag).commitAllowingStateLoss();
        }
        toggled = false;

        AppBridge.themeContext = null;
//        MyAppBridgeImpl.lockscreen_clear();
        super.onDestroy();
    }


}