package com.suo.applock;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.suo.applock.menu.SharPre;
import com.suo.applock.view.SuoPatternUtils;
import com.suo.theme.SuoPatternView;
import com.suo.theme.Dot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by superjoy on 2014/10/24.
 */
public class SetupActivity extends FirBActivity implements View.OnClickListener {
    public EditText email_address;
    CharSequence lastPasswd;
    public static final byte SET_EMPTY = 0;
    public static final byte SET_NORMAL_PASSWD = 1;
    public static final byte SET_GRAPH_PASSWD = 2;
    public static final byte SET_EMAIL = 3;

    @Override
    protected boolean hasHelp() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.RGBA_8888);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void tips() {

    }

    @Override
    public void setupView() {
        byte setting = getIntent().getByteExtra("set", SET_EMPTY);
        switch (setting) {
            case SET_EMAIL:
                setEmail();
                break;

            case SET_NORMAL_PASSWD:
                setPasswdView();
                break;

            case SET_GRAPH_PASSWD:
                setGraphView();
                break;
        }
    }

    public void randomNumpadIfPossible() {
        if (!Application.getSharedPreferences().getBoolean("random", false)) {
            return;
        }

        int[] buttons = new int[]{
                R.id.suo_bt_n_0, R.id.suo_bt_n_1, R.id.suo_bt_n_2, R.id.suo_bt_n_3,
                R.id.suo_bt_n_4, R.id.suo_bt_n_5, R.id.suo_bt_n_6, R.id.suo_bt_n_7,
                R.id.suo_bt_n_8, R.id.suo_bt_n_9
        };
        ArrayList<Integer> idx = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
            idx.add(i);

        for (int button : buttons) {
            int i = getRandomInt(0, idx.size() - 1);
            Integer v = idx.remove(i);
            ((Button) findViewById(button)).setText(v.toString());
        }
    }

    // 返回a到b之間(包括a,b)的任意一個自然数,如果a > b || a < 0，返回-1
    public static int getRandomInt(int a, int b) {
        if (a > b || a < 0)
            return -1;
        // 下面两种形式等价
        // return a + (int) (new Random().nextDouble() * (b - a + 1));
        return a + (int) (Math.random() * (b - a + 1));
    }

    public void setEmail() {
        startListApp();
    }

    Dot passdot;

    @Override
    public void onClick(View view) {
        Button v = (Button) view;
        passdot.setNumber(v.getText().charAt(0));
        if (togglePattern){
            togglePattern = false;
            Button ok = (Button) findViewById(R.id.suo_ok);
            ok.setText(R.string.suo_next);
            ok.setTextColor(getResources().getColor(R.color.suo_inva));
//            ok.setBackgroundResource(R.drawable.suo_bt_backg_);
        }
    }

    public void passwdIsEmpty() {
        Toast t = Toast.makeText(context, R.string.suo_password_tishi, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        t.show();
    }

    public void setPasswd() {
        String pwd = lastPasswd.toString();
        String email = email_address.getText().toString();
        Application.getSharedPreferences().edit().putString("email", email).apply();
        if (pwd.length() == 0) {
            passwdIsEmpty();
            return;
        }
        SharPre p = SharPre.begin();
        p.setPasswd(pwd, true).useNormalPasswd(true).commit();
        MyTrack.sendEvent(MyTrack.CATE_DEFAULT, MyTrack.ACT_APPLOCK, MyTrack.ACT_APPLOCK, 1L);
        startListApp();
    }

    public Drawable getIcon() {
        String packageName = getIntent().getStringExtra("pkg");
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return pi.applicationInfo.loadIcon(getPackageManager());
        } catch (Exception e) {
            return super.getResources().getDrawable(R.drawable.suo_ic);
        }
    }

    public void setupTitle(ImageView tv) {
        String packageName = getIntent().getStringExtra("pkg");
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            Drawable ic = pi.applicationInfo.loadIcon(getPackageManager());
            tv.setImageDrawable(ic);
        } catch (Exception e) {
            tv.setImageResource(R.drawable.suo_ic);
        }
    }

    public SuoPatternView lockPatternView;
    public boolean confirmMode = false;

    @InjectView(R.id.suo_password_cn)
    Button cancel;

    @InjectView(R.id.suo_backg)
    TextView tip;

    public void enterGraphNormal() {
        confirmMode = false;
        tip.setTextColor(getResources().getColor(R.color.suo_inva));
        tip.setText(R.string.suo_draw_tu);
        tip.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        cancel.setText(R.string.suo_password_lock);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPasswdView();
            }
        });
    }

    public void enterConfirmMode() {
        confirmMode = true;
        tip.setText(R.string.suo_again_draw_tu);
        cancel.setVisibility(View.VISIBLE);
        cancel.setText(R.string.suo_again_reset);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterGraphNormal();
            }
        });
    }

    void setGraphView() {
        setContentView(R.layout.suo_set_pattern);
        ButterKnife.inject(this);
        View back = findViewById(R.id.suo_fanh);
        if (firstSetup){
            back.setVisibility(View.GONE);
        }
        lockPatternView = (SuoPatternView) findViewById(R.id.suo_lp_ll);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        lockPatternView.setOnPatternListener(new SuoPatternView.OnPatternListener() {
            @Override
            public void onPatternStart() {

            }

            @Override
            public void onPatternCleared() {
            }

            @Override
            public void onPatternCellAdded(List<SuoPatternView.Cell> pattern) {

            }

            @Override
            public void onPatternDetected(List<SuoPatternView.Cell> pattern) {
                if (confirmMode) {
                    if (!SuoPatternUtils.checkPattern(pattern, pattern1)) {
                        lockPatternView.setDisplayMode(SuoPatternView.DisplayMode.Wrong);
                        tip.setTextColor(0xffcc0000);
                        tip.setText(R.string.suo_mima_not_);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lockPatternView.clearPattern();
                                tip.setTextColor(getResources().getColor(R.color.suo_inva));
                                tip.setText(R.string.suo_again_draw_tu);
                            }
                        }, 700);
                    } else {
                        try {
                            SharPre.begin().setPasswd(SuoPatternUtils.patternToString(pattern1), false).useNormalPasswd(false).commit();

                            Toast.makeText(context, R.string.suo_set_password, Toast.LENGTH_SHORT).show();
                            setResult(1);
                            if (firstSetup) {
                                setEmail();
                                firstSetup = false;
                            } else {
                                startListApp();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, R.string.suo_password_f, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (pattern.size() < 3) {
                        lockPatternView.setDisplayMode(SuoPatternView.DisplayMode.Wrong);
                        tip.setTextColor(0xffcc0000);
                        tip.setText(R.string.suo_mima_small);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lockPatternView.clearPattern();
                                tip.setTextColor(getResources().getColor(R.color.suo_inva));
                                tip.setText(R.string.suo_again_draw_tu);
                            }
                        }, 700);
                    } else {
                        if (pattern1 == null)
                            pattern1 = new ArrayList<>(pattern);
                        else {
                            pattern1.clear();
                            pattern1.addAll(pattern);
                        }
                        lockPatternView.clearPattern();
                        enterConfirmMode();
                    }
                }
            }
        });
        enterGraphNormal();
    }

    public boolean firstSetup = false;
    public boolean togglePattern = true;

    public void setPasswdView() {
        setContentView(R.layout.suo_set_password);
        View back = findViewById(R.id.suo_fanh);
        if (firstSetup){
            back.setVisibility(View.GONE);
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ((ImageView) findViewById(R.id.suo_cha)).setColorFilter(getResources().getColor(R.color.suo_np_c));
        passdot = (Dot) findViewById(R.id.suo_r_dot);
        passdot.setFlag(true);
        passdot.init(new Dot.ICheckListener() {
            @Override
            public void match(String pass) {
                SharPre.begin().setPasswd(pass, true).useNormalPasswd(true).commit();

                setResult(1);
                Toast.makeText(context, R.string.suo_set_password, Toast.LENGTH_SHORT).show();
                if (firstSetup) {
                    setEmail();
                    firstSetup = false;
                } else {
                    startListApp();
                }
            }
        });
        final Button okBtn = (Button) findViewById(R.id.suo_ok);
        okBtn.setText(R.string.suo_pattern_lock);
        okBtn.setVisibility(View.VISIBLE);
        okBtn.setBackgroundDrawable(null);
        okBtn.setTextColor(getResources().getColor(R.color.suo_inva));
        okBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (togglePattern){
                    setGraphView();
                    return;
                }
                if (setProgress == 0) {
                    if (passdot.empty()) {
                        passwdIsEmpty();
                        return;
                    }
                    ++setProgress;
                    passdot.setFlag(false);
                    passdot.clear();
//                    okBtn.setVisibility(View.INVISIBLE);
                    okBtn.setText(R.string.suo_again_reset);
                    okBtn.setTextColor(getResources().getColor(R.color.suo_inva));
                    okBtn.setBackgroundDrawable(null);
                    ((TextView) findViewById(R.id.suo_title_bar_te)).setText(R.string.suo_con_password);
                    ((TextView) findViewById(R.id.suo_backg)).setText(R.string.suo_confirm_tip);
                } else if (setProgress == 1){
                    setProgress = 0;
                    passdot.setFlag(false);
                    passdot.clear();
                    togglePattern = true;
                    okBtn.setText(R.string.suo_pattern_lock);
                    ((TextView) findViewById(R.id.suo_title_bar_te)).setText(R.string.suo_set_pass_title);
                    ((TextView) findViewById(R.id.suo_backg)).setText(R.string.suo_set_tip_password);
                }
            }
        });
        findViewById(R.id.suo_cha).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passdot.backSpace();
                if (passdot.empty() && !togglePattern){
                    togglePattern = true;
                    okBtn.setText(R.string.suo_pattern_lock);
                    okBtn.setBackgroundDrawable(null);
                    okBtn.setTextColor(getResources().getColor(R.color.suo_inva));
                }
            }
        });

        //ignore
        findViewById(R.id.suo_password_cn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setProgress == 0) {
                    onBackPressed();
                } else {
                    --setProgress;
                    passdot.clear();
                    ((TextView) findViewById(R.id.suo_title_bar_te)).setText(R.string.suo_set_pass_title);
                    okBtn.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void startListApp() {
        finish();
    }

    public List<SuoPatternView.Cell> pattern1, pattern2;
    public byte setProgress = 0;
}
