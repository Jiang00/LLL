package com.suo.applock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.client.AndroidSdk;
import com.android.client.SdkResultListener;
import com.android.dev.data.SBoolean;
import com.suo.applock.menu.BackgData;
import com.suo.applock.menu.SharPre;
import com.lockscreen.api.liberal.BaseActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by superjoy on 2014/9/11.
 */
public abstract class FiraActivity extends BaseActivity implements SearchRun.OnSearchResult {

    private static final SBoolean firstOpen = new SBoolean("firs_o_pen", true);
    public static final String DAILY_ICON_CACHE_KEY = "DAILY_ICON_CACHE_KEY";
    public static final String DAILY_IMAGE_CACHE_KEY = "DAILY_IMAGE_CACHE_KEY";


    public FiraActivity context;
    boolean invisible;

    @Override
    protected void onPause() {
        super.onPause();
//        TCAgent.onPause(this);
        invisible = true;
    }

    protected void onIntent(Intent intent) {

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        TCAgent.onResume(this);
        invisible = false;
        tips();
    }

    protected void tips() {
        if (SharPre.hasIntruder()) {
            InvadePre.show();
            SharPre.setHasIntruder(false);
        }
    }

    static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        boolean crashing = false;

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            try {
                if (crashing) return;
                crashing = true;
                StringWriter sw = new StringWriter();
                sw.append(thread.toString());
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
//                TCAgent.onError(Application.getContext(), ex);
                ex.printStackTrace();
                MyTrack.sendEvent(MyTrack.CATE_EXCEPTION, MyTrack.ACT_CRASH, sw.toString(), 0L);
                defaultHandler.uncaughtException(thread, ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Thread.UncaughtExceptionHandler exceptionHandler = new MyUncaughtExceptionHandler();

    public static void showSoftKeyboard(Activity activity, View view, boolean show) {
        if (show) {
            if (view != null && view.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) activity.
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            InputMethodManager imm = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View viewById = activity.findViewById(android.R.id.content);
            if (viewById != null)
                imm.hideSoftInputFromWindow(viewById.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected abstract boolean hasHelp();

    public static final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        initNow();
    }

    @Override
    protected void onDestroy() {
        context = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceStateOnCreate(Bundle savedInstanceState) {
    }

    protected void initNow() {
        AndroidSdk.Builder builder = new AndroidSdk.Builder();
        builder.setSdkResultListener(new SdkResultListener() {
            @Override
            public void onInitialized() {

            }

            @Override
            public void onReceiveServerExtra(String s) {
                Log.e("ha", "extra " + s);
                BackgData.onReceiveData(getApplicationContext(), s);
            }

            @Override
            public void onReceiveNotificationData(String s) {

            }
        });
        AndroidSdk.onCreate(this, builder);

        if (firstOpen.yes()) {
            Application.getSharedPreferences().edit().putInt(Configda.FIRSTOPENTIME, (int) (System.currentTimeMillis() / 1000)).apply();
            firstOpen.setValue(false);
        }

        setupView();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    //    Locks menu;
    ImageButton help;

    protected void setup(int titleId) {
        ButterKnife.inject(this);
        final TextView title = (TextView) findViewById(R.id.suo_title_bar_te);
        title.setText(titleId);

        EditText searchEditor = (EditText) findViewById(R.id.suo_title_bar_et);
        if (searchEditor != null) {
            searchEditor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (search)
                        searchThread.waittingForSearch(editable.toString(), getSearchList(), FiraActivity.this);
                }
            });
            findViewById(R.id.suo_title_bt).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            findViewById(R.id.suo_bc).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        findViewById(R.id.suo_et_m).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterEditMode();
            }
        });
        setMenu();
    }

    protected void setMenu() {
        View menu = findViewById(R.id.suo_app_m);
        if (menu != null) {
            help = (ImageButton) findViewById(R.id.suo_set_bt);
            help.setOnClickListener(helpOnLisitener);
        } else {
            findViewById(R.id.suo_set_bt).setVisibility(View.GONE);
        }
    }

    View.OnClickListener helpOnLisitener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            context.startActivity(new Intent(context, SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
        }
    };

    public List<SearchRun.SearchData> getSearchList() {
        return new ArrayList<>();
    }

    @Override
    public void onResult(ArrayList<SearchRun.SearchData> list) {

    }

    public void enterEditMode() {
        View view = findViewById(R.id.suo_et_m);
        if (!edit) {
            edit = true;
            findViewById(R.id.suo_bt_tit_ba).setVisibility(View.VISIBLE);
            view.setSelected(true);
            onEditMode(true);
        } else
            onBackPressed();
    }

    public void backHome() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(setIntent);
    }

    protected void askForExit() {
        showAd();
        super.onBackPressed();
    }

    private void showAd() {
//        AndroidSdk.showFullAd(AndroidSdk.FULL_TAG_PAUSE);
    }

    boolean search = false;
    public boolean edit = false;
    public static SearchRun searchThread = new SearchRun();

    static {
        searchThread.start();
    }

    protected void onEditMode(boolean show) {
    }

    protected void onSearchExit() {
    }

    static final Interpolator acc = new AccelerateInterpolator();
    static final Interpolator dec = new DecelerateInterpolator();


    @Override
    public void onBackPressed() {
        if (search) {
        } else if (edit) {
            exitEditMode();
        } else
            super.onBackPressed();
    }

    protected void exitEditMode() {
        findViewById(R.id.suo_et_m).setSelected(false);
        findViewById(R.id.suo_bt_tit_ba).setVisibility(View.GONE);
        edit = false;
        onEditMode(false);
    }

    protected void setViewVisible(int type, int... ids) {
        for (int id : ids) {
            findViewById(id).setVisibility(type);
        }
    }

    public abstract void setupView();
}