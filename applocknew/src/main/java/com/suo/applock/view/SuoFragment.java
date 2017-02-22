package com.suo.applock.view;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.client.AndroidSdk;
import com.suo.applock.Application;
import com.suo.libra.view.LoaddImageView;
import com.suo.applock.InvadePre;
import com.suo.applock.Toolcls;
import com.suo.applock.menu.MyYingys;
import com.suo.libra.view.BaseFragment;
import com.suo.applock.SuoMain;
import com.suo.applock.R;
import com.suo.applock.db.MyfileDBHelper;
import com.suo.applock.menu.MyProfiles;
import com.lockscreen.api.liberal.Utils;
import com.lockscreen.api.liberal.clis.CCViewAdaptor;
import com.lockscreen.api.liberal.clis.CCViewScroller;
import com.suo.applock.track.TrackString;
import com.suo.applock.MyTrack;
import com.suo.applock.SearchRun;
import com.privacy.lock.aidl.IWorker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

/**
 * Created by SongHualin on 6/24/2015.
 */
public class SuoFragment extends BaseFragment implements SearchRun.OnSearchResult {
    public static final String PROFILE_ID_KEY = "profile_id";
    public static final String PROFILE_NAME_KEY = "profile_name";
    public static final String PROFILE_HIDE = "lockscreen_hide";
    static View headerView;


    @InjectView(R.id.suo_sre_swip)
    SwipeRefreshLayout refreshLayout;

    @InjectView(R.id.suo_list_v)
    public ListView listView;

    CCViewScroller scroller;
    CCViewAdaptor adaptor;

    MyfileDBHelper.ProfileEntry profileEntry;
    SQLiteDatabase db;


    public static final Object searchLock = new Object();

    private static List<SearchRun.SearchData> apps;
    private List<SearchRun.SearchData> searchResult;
    int count = 0;
    boolean hide;
    private SharPFive shareFive;

    public SuoFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PROFILE_NAME_KEY, profileEntry.name);
        outState.putLong(PROFILE_ID_KEY, profileEntry.id);
        outState.putBoolean(PROFILE_HIDE, hide);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        onArguments(savedInstanceState);
    }

    @Override
    protected void onArguments(Bundle arguments) {
        profileEntry = new MyfileDBHelper.ProfileEntry();
        profileEntry.id = arguments.getLong(PROFILE_ID_KEY);
        profileEntry.name = arguments.getString(PROFILE_NAME_KEY);
        hide = arguments.getBoolean(PROFILE_HIDE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        shareFive = new SharPFive(getActivity());
        updateLocks();
        showDialogFive();

        if (shareFive.getFirstEnter()) {
            int times = (int) (System.currentTimeMillis() / 1000);
            shareFive.setFirstcometime(times);

        } else {
            int putTime = shareFive.getFirstcometime();
            int nowTime = (int) (System.currentTimeMillis() / 1000);
            if ((nowTime - putTime) > 43200) {
                try {
                    new Thread().sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //启动评价dialog
//                if (!shareFive.getFiveRate()) {
//                    ShowDialogview.showDialog(getActivity(), MyTrack.CATEGORY_APPS,listView,headerView);
//                }
            }
            shareFive.setFirstEnter(false);
        }
    }

    private void updateLocks() {
        if (!hide) {
            db = MyfileDBHelper.singleton(getActivity()).getWritableDatabase();
            if (profileEntry.name != null) {
                try {
                    locks = MyfileDBHelper.ProfileEntry.getLockedApps(db, profileEntry.id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.suo_list_lay, container, false);
        ButterKnife.inject(this, v);

        headerView = inflater.inflate(R.layout.suo_main_title_rate, null);

        refreshLayout.setColorSchemeResources(R.color.suo_acc_2, R.color.suo_acc_1);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MyYingys.setWaiting(action);
            }
        });
        scroller = new CCViewScroller(listView);

        if (!shareFive.getFiveRate()) {
            listView.addHeaderView(headerView);
            headerClick(headerView);
        }
        setAdaptor();


        return v;
    }


    @Override
    public void onResume() {
        super.onResume();

        MyYingys.setWaiting(action);
        updateLocks();
    }

    public void switchProfile(MyfileDBHelper.ProfileEntry entry, IWorker server) {
        if (dirty) {
            saveOrCreateProfile(profileEntry.name, server);
        }
        refreshLayout.setRefreshing(true);
        profileEntry = entry;
        locks = MyfileDBHelper.ProfileEntry.getLockedApps(db, entry.id);
        MyProfiles.switchProfile(entry, server);
        MyYingys.setWaiting(action);
    }

    public void saveOrCreateProfile(String profileName, IWorker server) {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(locks.keySet());
        try {
            if (profileEntry.name == null) {
                profileEntry.id = MyfileDBHelper.ProfileEntry.createProfile(db, profileName, list);
                profileEntry.name = profileName;
                MyProfiles.addProfile(profileEntry);
            } else {
                MyfileDBHelper.ProfileEntry.updateProfile(db, profileEntry.id, list);
                if (server != null) {
                    server.notifyApplockUpdate();
                }
            }
            dirty = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        db = null;
        super.onDestroy();
    }

    private void setAdaptor() {
        adaptor = new CCViewAdaptor(scroller, R.layout.suo_main_list_item) {

            private void updateUI(int position, ViewHolder h, boolean forceLoading) {
                if (requireCheckHeader()) {
                    if (position == 0) {
                        h.icon.setImageResource(R.drawable.suo_intrude);
                        h.name.setText(R.string.suo_ruqinzhe);
                        h.lock.setImageResource(R.drawable.suo_ne);
                        return;
                    } else {
                        --position;
                    }
                }
                List<SearchRun.SearchData> list = searchResult == null ? apps : searchResult;

                if (position >= list.size()) return;
                SearchRun.SearchData data = list.get(position);

                String pkgName = data.pkg;
                h.icon.setImageIcon(pkgName, forceLoading);
                h.name.setText(data.label);

                Bundle b = getArguments();
                boolean ishideApp = b.getBoolean("ishide_app");
                if (ishideApp) {
//                    h.lock.setImageResource(R.drawable.lockscreen_hidelock_bg);

                } else {
                    h.lock.setImageResource(R.drawable.suo_list_item_bt);
                }
                h.lock.setEnabled(locks.containsKey(pkgName));
            }

            @Override
            protected void onUpdate(int position, Object holder, boolean scrolling) {
                ViewHolder h = (ViewHolder) holder;
                updateUI(position, h, !scrolling);
            }

            @Override
            protected Object getHolder(View root) {
                return new ViewHolder(root);
            }

            @Override
            public int getCount() {
                if (searchResult == null) {
                    return hide ? count : (count + 1);
                } else {
                    return searchResult.size();
                }
            }
        };
        listView.setAdapter(adaptor);

    }

    Runnable refreshSearchResult = new Runnable() {
        @Override
        public void run() {
            Utils.notifyDataSetChanged(listView);
        }
    };
    boolean searching = false;

    public List<SearchRun.SearchData> getSearchData() {
        searching = true;
        return apps;
    }

    @Override
    public void onResult(ArrayList<SearchRun.SearchData> list) {
        synchronized (searchLock) {
            if (searching) {
                searchResult = list;
                if (list == null) {
                    searching = false;
                    MyYingys.setWaiting(action);
                } else {
                    refreshUI(refreshSearchResult);
                }
            }
        }
    }

    public void refreshUI(Runnable action) {
        Application.runOnUiThread(action);
    }

    class ViewHolder {
        @InjectView(R.id.suo_ima)
        public LoaddImageView icon;

        @InjectView(R.id.suo_appna)
        public TextView name;

        @InjectView(R.id.suo_ima_loc)
        public ImageView lock;

        public ViewHolder(View root) {
            ButterKnife.inject(this, root);
        }
    }

    Map<String, Boolean> locks = new HashMap<>();
    boolean dirty = false;

    Toast toast;

    private boolean requireCheckHeader() {
        return !hide && searchResult == null;
    }

    @OnItemClick(R.id.suo_list_v)
    public void onItemClick(View view, int which) {
        if (requireCheckHeader()) {

            Log.e("boolean", Application.getSharedPreferences().getBoolean("five_r", false) + "----");

            if (!shareFive.getFiveRate()) {
                if (which == 1) {
                    InvadePre.show();
                    return;
                } else if (which == 0) {

                } else {
                    --which;
                }
            } else {
                if (which == 0) {
                    InvadePre.show();
                    return;
                } else {
                    --which;
                }
            }


        }
        List<SearchRun.SearchData> list = searchResult == null ? apps : searchResult;

//        if (which >= list.size()) return;
        SearchRun.SearchData data;
        if (!shareFive.getFiveRate()) {
            data = list.get(which - 1);
        } else {
            data = list.get(which);

        }


        dirty = true;
        if (toast != null) {
            toast.cancel();
        }
        final String pkgName = data.pkg;
        Context context = view.getContext().getApplicationContext();
        if (locks.containsKey(pkgName)) {
            if (hide) {
                if (!Toolcls.showApp(pkgName)) {
                    toast = Toast.makeText(context, getString(R.string.suo_notlock_app_f, data.label), Toast.LENGTH_SHORT);
                } else {
                    toast = Toast.makeText(context, getString(R.string.suo_show_success, data.label), Toast.LENGTH_SHORT);
                    locks.remove(pkgName);
                    MyYingys.show(data);
                }
                toast.show();
            } else {
                locks.remove(pkgName);
                toast = Toast.makeText(getActivity().getApplicationContext(), getString(R.string.suo_unlock_chengg, data.label), Toast.LENGTH_SHORT);
            }
        } else {
            if (hide) {
                if (!Toolcls.hideApp(pkgName)) {
                    toast = Toast.makeText(getActivity().getApplicationContext(), getString(R.string.suo_lock_app_f, data.label), Toast.LENGTH_SHORT);

                } else {
                    toast = Toast.makeText(context, getString(R.string.suo_lock_success, data.label), Toast.LENGTH_SHORT);
                    locks.put(pkgName, true);
                    MyYingys.hide(data);
                }
            } else {
                toast = Toast.makeText(getActivity().getApplicationContext(), getString(R.string.suo_lock_chengg, data.label), Toast.LENGTH_SHORT);
                locks.put(pkgName, true);
            }
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.lock.setEnabled(locks.containsKey(pkgName));

        toast.show();
    }

    private Runnable action = new Runnable() {
        @Override
        public void run() {
            apps = hide ? MyYingys.getHiddenApps(locks) : MyYingys.getApps(locks);
            count = apps.size();
            listView.setVisibility(View.VISIBLE);
            refreshLayout.setRefreshing(false);
            Utils.notifyDataSetChanged(listView);
        }
    };


    public void showDialogFive() {
        if (!hide) {

            int time = shareFive.getEnterAppsTimes();
            time++;
            shareFive.setEnterAppsTimes(time);

            if (time == 2) {
                SharPFive sh = new SharPFive(getActivity());
                if (!sh.getFiveRate()) {
                    ShowDialogview.showDialog(getActivity(), MyTrack.CATEGORY_APPS,listView);
                }

            }

        }
    }


    private void headerClick(final View headerView) {

        headerView.findViewById(R.id.suo_bad_tit).setOnClickListener(new View.OnClickListener(

        ) {
            @Override
            public void onClick(final View v) {

                AndroidSdk.track(TrackString.CATEGORY_APP,
                        TrackString.CATEGORY_RATE_BAD, "", 1);

                final View alertDialogView = View.inflate(v.getContext(), R.layout.suo_main_emale, null);
                final android.support.v7.app.AlertDialog d = new android.support.v7.app.AlertDialog.Builder(v.getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert).create();

                Utils.addAlertAttribute(d.getWindow());
                d.setView(alertDialogView);
                d.show();

                TextView text = (TextView) alertDialogView.findViewById(R.id.suo_ema_sub);
                text.setOnClickListener(new View.OnClickListener(

                ) {
                    @Override
                    public void onClick(View v) {
                        shareFive.setFiveRate(true);
                        EditText content = (EditText) alertDialogView.findViewById(R.id.suo_text);
                        EditText email = (EditText) alertDialogView.findViewById(R.id.suo_emal);

                        if (content.getText().length() != 0 && email.getText().length() != 0) {

                            AndroidSdk.track(TrackString.CATEGORY_RATE_BAD_CONTENT,
                                    email.getText().toString() + "  " + content.getText().toString(), "", 1);

                        } else if (content.getText().length() == 0 && email.getText().length() != 0) {
                            AndroidSdk.track(TrackString.CATEGORY_RATE_BAD_CONTENT,
                                    email.getText().toString() + "  ", "", 1);


                        } else if (content.getText().length() != 0 && email.getText().length() == 0) {
                            AndroidSdk.track(TrackString.CATEGORY_RATE_BAD_CONTENT,
                                    content.getText().toString() + "  ", "", 1);


                        }

                        if (content.getText().length() != 0 || email.getText().length() != 0) {
                            Application.getSharedPreferences().edit().putBoolean("five_r", true).apply();
                            getActivity().finish();
                            Intent intent = new Intent(getActivity(), SuoMain.class);
                            startActivity(intent);
//                            listView.removeHeaderView(headerView);
                        }
                        d.cancel();
                    }
                });

            }
        });


        headerView.findViewById(R.id.suo_good_tit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                shareFive.setFiveRate(true);
                AndroidSdk.track(TrackString.CATEGORY_APP,
                        TrackString.CATEGORY_RATE_GOOD, "", 1);
                if (Utils.hasPlayStore(getActivity())) {
                    Utils.rate(v.getContext());
                }

                View alertDialogView = View.inflate(v.getContext(), R.layout.suo_rate_result, null);

                final RateWidget w = new RateWidget(getActivity(), RateWidget.MATCH_PARENT, RateWidget.MATCH_PARENT, RateWidget.PORTRAIT);
                w.addView(alertDialogView);
                w.addToWindow();

                w.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        w.removeAllViews();
                        w.removeFromWindow();

                    }
                });

                listView.removeHeaderView(headerView);

            }
        });


        headerView.findViewById(R.id.suo_rat_cha).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
//                Preference.setRated(true);
                Application.getSharedPreferences().edit().putBoolean("five_r", true).apply();
                getActivity().finish();
                Intent intent = new Intent(getActivity(), SuoMain.class);
                startActivity(intent);
//                listView.removeHeaderView(headerView);


            }
        });
    }


}
