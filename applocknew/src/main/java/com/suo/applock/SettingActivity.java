package com.suo.applock;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.suo.applock.menu.SharPre;

/**
 * Created by superjoy on 2014/9/4.
 */
public class SettingActivity extends FirBActivity {
    public static byte idx = 0;
    public static final byte SETTING_SLOT = idx++;
    public static final byte SETTING_MODE = idx++;
    public static final byte SETTING_HIDE_GRAPH_PATH = idx++;
    public static final byte SETTING_RANDOM = idx++;
//    public static final byte SETTING_ADVANCED = idx++;
    //    public static final int SETTING_FAKE_SELECTOR = idx++;
    public static final byte SETTING_LOCK_NEW = idx++;
    public static final byte SETTING_RATE = idx++;
//    public static final byte SETTING_SHARE = idx++;
    public static final int SETTING_INTRUDER = idx++;

    public static final byte REQ_CODE_PASS = 2;
    public static final byte REQ_CODE_PATTERN = 4;
    static final int[] items = new int[]{
            R.string.suo_br_setting,
            0,
            R.string.suo_draw_path,
            R.string.suo_random,
//            R.string.lockscreen_advanced_security,
//            R.string.lockscreen_fake_selector,
            R.string.suo_lock_newyingyong,
            R.string.suo_rate,
//            R.string.lockscreen_help_share,
    };

    ListView lv;

    @Override
    protected boolean hasHelp() {
        return false;
    }

    @Override
    public void setupView() {
        setContentView(R.layout.suo_set_layout);
        setup(R.string.suo_setting);
        final TextView title = (TextView) findViewById(R.id.suo_title_bar_te);
        title.setText("    "+getString(R.string.suo_setting));
        title.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.suo_back), null, null, null);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setViewVisible(View.GONE, R.id.suo_title_bt,R.id.suo_set_bt, R.id.suo_bt_tit_ba, R.id.suo_jindu);
        findViewById(R.id.suo_list_v).setVisibility(View.VISIBLE);

        lv = (ListView) findViewById(R.id.suo_list_v);
        lv.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return items.length;
            }

            @Override
            public Object getItem(int i) {
                return i;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {

                if (i == SETTING_MODE) {
                    view = getLayoutInflater().inflate(R.layout.suo_invade_line, viewGroup, false);
                    TextView title = (TextView) view.findViewById(R.id.suo_title_bar_te);
                    TextView desc = (TextView) view.findViewById(R.id.suo_text_des);
                    title.setText(R.string.suo_again_reset);
                    desc.setText(SharPre.isUseNormalPasswd() ? R.string.suo_password_lock : R.string.suo_pattern_lock);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setPasswd(true, !SharPre.isUseNormalPasswd());
                        }
                    });
                } else if (i == SETTING_SLOT) {
                    view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.suo_invade_line, null, false);
                    LinearLayout it = (LinearLayout) view.findViewById(R.id.suo_linera);
                    int slot = Application.getSharedPreferences().getInt(SharPre.PREF_BRIEF_SLOT, SharPre.PREF_DEFAULT);
                    ((TextView) it.findViewById(R.id.suo_text_des)).setText(getResources().getStringArray(R.array.suo_setting_brief)[slot]);
                    ((TextView) it.findViewById(R.id.suo_title_bar_te)).setText(items[i]);
                    it.setOnClickListener(onClickListener);
                    it.setId(i);
                }
                else if (i == SETTING_RANDOM) {
                    view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.suo_notica_it, null, false);
                    ((TextView) view.findViewById(R.id.suo_title_bar_te)).setText(items[i]);
//                    ((TextView) view.findViewById(R.id.desc)).setText(R.string.random_keyboard_desc);
                    view.findViewById(R.id.suo_text_des).setVisibility(View.GONE);
                    final ImageView checkbox = (ImageView) view.findViewById(R.id.suo_set_checked);
                    if (Application.getSharedPreferences().getBoolean("random", false)) {
                        checkbox.setImageResource(R.drawable.suo_setting_check);
                    } else {
                        checkbox.setImageResource(R.drawable.suo_setting_not_check);
                    }

                    checkbox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Application.getSharedPreferences().getBoolean("random", false)) {
                                checkbox.setImageResource(R.drawable.suo_setting_not_check);
                                MyTrack.sendEvent(MyTrack.CATE_SETTING, MyTrack.ACT_RANDOM, MyTrack.ACT_RANDOM, 1L);
                                Application.getSharedPreferences().edit().putBoolean("random", false).apply();
                            } else {
                                checkbox.setImageResource(R.drawable.suo_setting_check);
                                MyTrack.sendEvent(MyTrack.CATE_SETTING, MyTrack.ACT_RANDOM, MyTrack.ACT_RANDOM, 1L);
                                Application.getSharedPreferences().edit().putBoolean("random", true).apply();

                            }
                        }
                    });

                }
                 else if (i == SETTING_INTRUDER) {
                    view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.suo_notica_it, null, false);
                    ((TextView) view.findViewById(R.id.suo_title_bar_te)).setText(items[i]);
                    ((TextView) view.findViewById(R.id.suo_text_des)).setText(R.string.suo_ruqinzhe_de);
                    final ImageView checkBox = (ImageView) view.findViewById(R.id.suo_set_checked);

                    if (SharPre.fetchIntruder()) {
                        checkBox.setImageResource(R.drawable.suo_setting_check);
                    } else {
                        checkBox.setImageResource(R.drawable.suo_setting_not_check);
                    }

                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (SharPre.fetchIntruder()) {
                                checkBox.setImageResource(R.drawable.suo_setting_not_check);
                                Toast.makeText(getApplicationContext(), R.string.suo_ruqinzhe_off, Toast.LENGTH_SHORT).show();
                                SharPre.setFetchIntruder(false);
                            } else {
                                checkBox.setImageResource(R.drawable.suo_setting_check);
                                Toast.makeText(getApplicationContext(), R.string.suo_ruqinzhe_on, Toast.LENGTH_SHORT).show();

                                SharPre.setFetchIntruder(true);

                            }
                        }
                    });


                } else if (i == SETTING_LOCK_NEW) {
                    view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.suo_notica, null, false);
                    ((TextView) view.findViewById(R.id.suo_title_bar_te)).setText(items[i]);
                    ((TextView) view.findViewById(R.id.suo_text_des)).setText(R.string.suo_insta);
                    final ImageView checkbox = (ImageView) view.findViewById(R.id.suo_set_checked);
                    if (Application.getSharedPreferences().getBoolean(SharPre.LOCK_NEW, SharPre.LOCK_DEFAULT)) {
                        checkbox.setImageResource(R.drawable.suo_setting_check);
                    } else {
                        checkbox.setImageResource(R.drawable.suo_setting_not_check);
                    }
                    checkbox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Application.getSharedPreferences().getBoolean(SharPre.LOCK_NEW, SharPre.LOCK_DEFAULT)) {
                                checkbox.setImageResource(R.drawable.suo_setting_not_check);
                                MyTrack.sendEvent(MyTrack.CATE_SETTING, MyTrack.ACT_NEW_APP, MyTrack.ACT_NEW_APP, 1L);
                                Application.getSharedPreferences().edit().putBoolean(SharPre.LOCK_NEW, false).apply();

                            } else {
                                checkbox.setImageResource(R.drawable.suo_setting_check);
                                MyTrack.sendEvent(MyTrack.CATE_SETTING, MyTrack.ACT_NEW_APP, MyTrack.ACT_NEW_APP, 1L);
                                Application.getSharedPreferences().edit().putBoolean(SharPre.LOCK_NEW, true).apply();
                            }

                        }
                    });


                } else if (i == SETTING_RATE) {
                    if (SharPre.hasOption(SharPre.OPT_RATE_REDDOT) && !SharPre.isOptionPressed(SharPre.OPT_RATE_REDDOT)) {
                        view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.suo_new_it, null, false);//lockscreen_red
                    } else {
                        view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.suo_new_it, null, false);
                    }
                    Button it = (Button) view.findViewById(R.id.suo_abuout_bt);
                    it.setText(items[i]);
                    it.setOnClickListener(onClickListener);
                    it.setId(i);
                } else if (i == SETTING_HIDE_GRAPH_PATH) {
                    view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.suo_notica_it, null, false);
                    ((TextView) view.findViewById(R.id.suo_title_bar_te)).setText(items[i]);
                    ((TextView) view.findViewById(R.id.suo_text_des)).setVisibility(View.GONE);
                    final ImageView b = (ImageView) view.findViewById(R.id.suo_set_checked);
                    if (Application.getSharedPreferences().getBoolean("hide_path", false)) {
                        b.setImageResource(R.drawable.suo_setting_check);
                    } else {
                        b.setImageResource(R.drawable.suo_setting_not_check);
                    }
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Application.getSharedPreferences().getBoolean("hide_path", false)) {
                                MyTrack.sendEvent(MyTrack.CATE_SETTING, MyTrack.ACT_HIDE_PATH, MyTrack.ACT_HIDE_PATH, 1L);
                                Application.getSharedPreferences().edit().putBoolean("hide_path", false).apply();
                                b.setImageResource(R.drawable.suo_setting_not_check);


                            } else {
                                MyTrack.sendEvent(MyTrack.CATE_SETTING, MyTrack.ACT_HIDE_PATH, MyTrack.ACT_HIDE_PATH, 1L);
                                Application.getSharedPreferences().edit().putBoolean("hide_path", true).apply();
                                b.setImageResource(R.drawable.suo_setting_check);

                            }
                        }
                    });

                } else {
                    view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.suo_new_it, null, false);
                    Button it = (Button) view.findViewById(R.id.suo_abuout_bt);
                    it.setText(items[i]);
                    it.setOnClickListener(onClickListener);
                    it.setId(i);
                }

                return view;
            }
        });
    }

    public View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == SETTING_SLOT) {
                SharedPreferences sp = Application.getSharedPreferences();
                int idx = sp.getInt(SharPre.PREF_BRIEF_SLOT, SharPre.PREF_DEFAULT);
                new AlertDialog.Builder(context).setTitle(R.string.suo_be_slot).setSingleChoiceItems(R.array.suo_setting_brief, idx, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Application.getSharedPreferences().edit().putInt(SharPre.PREF_BRIEF_SLOT, i).apply();
                        notifyDatasetChanged();
                        dialogInterface.dismiss();
                    }
                }).create().show();
            } else if (id == SETTING_RATE) {
                if (!SharPre.isOptionPressed(SharPre.OPT_RATE_REDDOT)) {
                    SharPre.pressOption(SharPre.OPT_RATE_REDDOT);
                }
                Fenxiang.rate(context);
                MyTrack.sendEvent(MyTrack.CATE_SETTING, MyTrack.ACT_RATE, MyTrack.ACT_RATE, 1L);
                notifyDatasetChanged();
            }
        }
    };

    public void setPasswd(boolean forResult, boolean pattern) {
        if (forResult)
            startActivityForResult(new Intent(context, SetupActivity.class).putExtra("set", pattern ? SetupActivity.SET_GRAPH_PASSWD : SetupActivity.SET_NORMAL_PASSWD), pattern ? REQ_CODE_PATTERN : REQ_CODE_PASS);
        else
            startActivity(new Intent(context, SetupActivity.class).putExtra("set", pattern ? SetupActivity.SET_GRAPH_PASSWD : SetupActivity.SET_NORMAL_PASSWD));
        overridePendingTransition(R.anim.huadong_left_in, R.anim.huadong_right_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQ_CODE_PATTERN:
                if (resultCode == 1) {
//                    SharPre.begin().useNormalPasswd(false).commit();
                    notifyDatasetChanged();
                }
                break;
            case REQ_CODE_PASS:
                if (resultCode == 1) {
//                    SharPre.begin().useNormalPasswd(true).commit();
                    notifyDatasetChanged();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        notifyDatasetChanged();
    }

    public void notifyDatasetChanged() {
        if (lv != null) {
            ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
        }
    }
}