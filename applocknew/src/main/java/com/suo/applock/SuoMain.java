package com.suo.applock;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

import com.suo.applock.menu.SharPre;
import com.suo.applock.view.MsgLinbo;
import com.suo.applock.view.ShowDialogview;
import com.suo.applock.view.SuoFragment;
import com.suo.applock.menu.MyProfiles;
import com.lockscreen.api.liberal.Utils;
import com.lockscreen.api.liberal.liu.SafeIDB;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by SongHualin on 6/12/2015.
 */
public class SuoMain extends FirBActivity {
//    public static void switchFakeIcon() {
//        Class[] classes = new Class[2];
//                classes[0] = Main1Activity.class;
//                classes[1] = MainActivity.class;
//        switchLauncher(classes);
//    }
    private static void switchLauncher(Class... classes) {
        if (classes.length > 0) {
            Application.getContext().getPackageManager()
                    .setComponentEnabledSetting(new ComponentName(Application.getContext(), classes[0])
                            , PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            for (int i = 1; i < classes.length; ++i) {
                Application.getContext().getPackageManager()
                        .setComponentEnabledSetting(new ComponentName(Application.getContext(), classes[i])
                                , PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        switchFakeIcon();
    }

    public void samsungShortCut(Context context, String num) {
//        int numInt = Integer.valueOf(num);
//        if (numInt < 1)
//        {
//            num = "0";
//        }else if (numInt > 99){
//            num = "99";
//        }
//        String activityName = this.getLaunchActivityName(context);
//        Intent localIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
//        localIntent.putExtra("badge_count", Integer.parseInt(num));
//        localIntent.putExtra("badge_count_package_name", context.getPackageName());
//        localIntent.putExtra("badge_count_class_name", activityName);
//        context.sendBroadcast(localIntent);






    }








    @Override
    public void onResult(ArrayList<SearchRun.SearchData> list) {
        if (fragment != null) {
            fragment.onResult(list);
        }
    }

    @Override
    protected void onSearchExit() {
        if (fragment != null) {
            fragment.onResult(null);
        }
    }

    @Override
    public List<SearchRun.SearchData> getSearchList() {
        return fragment == null ? super.getSearchList() : fragment.getSearchData();
    }

    @InjectView(R.id.suo_main_me)
    public ImageButton float_action_menu;

    SuoFragment fragment;

    private String profileName;

    boolean hide;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void requirePermission() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (Utils.requireCheckAccessPermission(this)) {
                final Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                if (getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                    new android.app.AlertDialog.Builder(this).setTitle(R.string.suo_permiss)
                            .setMessage(R.string.suo_permiss_msg)
                            .setPositiveButton(R.string.suo_permiss_grant, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(intent);
                                }
                            }).setNegativeButton(android.R.string.cancel, null).create().show();
                }
            }
        }
    }

    @Override
    protected void onIntent(Intent intent) {
        hide = intent.getBooleanExtra("lockscreen_hide", false);
        Bundle bundle = new Bundle();
        bundle.putBoolean("ishide_app", hide);

    }

    @Override
    protected void onRestoreInstanceStateOnCreate(Bundle savedInstanceState) {
        hide = savedInstanceState.getBoolean("lockscreen_hide");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("lockscreen_hide", hide);
    }

    @Override
    protected boolean hasHelp() {
        return true;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        super.onServiceConnected(name, service);
        if (MyProfiles.requireUpdateServerStatus()) {
            try {
                server.notifyApplockUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setupView() {
        setContentView(R.layout.suo_main_layout);
        ButterKnife.inject(this);


        setup(R.string.suo_app_title);
        if (!hide) {
            help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
                }
            });
        }
        profileName = SafeIDB.defaultDB().getString(SharPre.PREF_ACTIVE_PROFILE, SharPre.PREF_DEFAULT_LOCK);
        long profileId = SafeIDB.defaultDB().getLong(SharPre.PREF_ACTIVE_PROFILE_ID, 1);

        fragment = (SuoFragment) getFragmentManager().findFragmentByTag("fragment");
        if (fragment == null) {
            fragment = new SuoFragment();
            Bundle args = new Bundle();
            args.putLong(SuoFragment.PROFILE_ID_KEY, profileId);
            args.putString(SuoFragment.PROFILE_NAME_KEY, profileName);
            args.putBoolean(SuoFragment.PROFILE_HIDE, hide);
            args.putBoolean("ishide_app", hide);
            fragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.suo_frag_co, fragment, "fragment").commit();
        }
            setupFloatingActionButtons();
        if (SharPre.hasNewVersion()) {
            MsgLinbo.Data data = new MsgLinbo.Data();
            data.button = MsgLinbo.BUTTON_YES_NO;
            data.style = R.style.Msg;
            data.title = R.string.suo_update_title;
            data.yes = R.string.suo_undate;
            data.no = R.string.suo_later;
            data.messages = Html.fromHtml(SharPre.getNewVersionDesc());
            data.onyes = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.openPlayStore(Application.getContext(), getPackageName());
                }
            };
            MsgLinbo.show_(this, data);
        } else if (SharPre.tip4Rate()) {
//评价


        } else if (!SharPre.isAdvanceEnabled()) {

        }
//        ShowDialogview.showDialog(this, MyTrack.CATEGORY_APPS);
        requirePermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.suo_me_m, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @InjectView(R.id.suo_back_sl)
    View black_bg;

    public static final int REQ_ADD_PROFILE = 2;

    //主页面情景模式
    private void setupFloatingActionButtons() {
        black_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        float_action_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InvadePre.show();
            }
        });

    }

    @Override
    protected void onPause() {
        if (!hide) {
            fragment.saveOrCreateProfile(profileName, server);
        }
        super.onPause();
    }


    protected void onPostResume() {
        super.onPostResume();
        String pn = SafeIDB.defaultDB().getString(SharPre.PREF_ACTIVE_PROFILE, SharPre.PREF_DEFAULT_LOCK);
        if (!pn.equals(profileName)) {
            fragment.switchProfile(MyProfiles.getEntries().get(MyProfiles.getActiveProfileIdx(pn)), server);
            profileName = pn;
        }
    }

    SharedPreferences getSharedPreferences() {
        return getSharedPreferences("cf", MODE_MULTI_PROCESS);
    }


}

