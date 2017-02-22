package com.suo.applock.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suo.applock.menu.AppBridge;
import com.suo.libra.view.MyothView;
import com.suo.applock.R;
import com.suo.applock.track.ConfigDa;
import com.suo.applock.Preference;

/**
 * Created by SongHualin on 4/2/2015.
 */
public class ErrorView {
    private int wrong_time;
    private ViewStub forbidden;
    private View wrongView;
    private ValueAnimator colorAnim;
    private boolean catchIntruder = true;
    private int wrongSum = 0;
    private int countdown = 0;
    private LinearLayout.LayoutParams layoutParams;

    public ErrorView(ViewStub forbidden) {
        this.forbidden = forbidden;

    }

    public void right() {
        wrong_time = 0;
        wrongSum=0;
        countdown = 0;
        Log.i("sdsd","----right");
        catchIntruder = true;
    }

    public void wrong() {
        if (wrongView == null) {
            return;
        }
        wrong_time += 1;
        Log.i("time","----"+wrong_time);
        if (wrong_time == Preference.getIntruderSlot()+1) {
            if (catchIntruder) {
                String currentApp = AppBridge.bridge.currentPkg();
                ((MyothView) wrongView.findViewById(R.id.suo_fa_ss)).catchIntruder(currentApp);
                catchIntruder = false;
            }
        }
        if (wrong_time > 4) {
            wrong_time = 0;
            wrongSum++;

            switch (wrongSum) {

                case 1:
                    countdown = 5000;
                    break;
                case 2:
                    countdown = 10000;
                    break;
                case 3:
                    countdown = 15000;
                    break;
                case 4:
                    countdown = 20000;
                    break;
                case 5:
                    countdown = 25000;
                    break;
                default:
                    countdown = 25000;
                    break;
            }
            layoutParams = (LinearLayout.LayoutParams) ((LinearLayout) wrongView.findViewById(R.id.suo_lin_er)).getLayoutParams();
            if(Preference.getPasswordMode()== ConfigDa.PASSWORD_TYPE_NUMBER){
                layoutParams.setMargins(0,0,0,200);//4个参数按顺序分别是左上右下
            }else{
                layoutParams.setMargins(0,0,0,230);
            }

            new CountDownTimer(countdown, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    ((LinearLayout) wrongView.findViewById(R.id.suo_lin_er)).setLayoutParams(layoutParams); //mView是控件

                    ((TextView) wrongView.findViewById(R.id.suo_te_in)).setVisibility(View.VISIBLE);
                    ((TextView) wrongView.findViewById(R.id.suo_te_time)).setText("(" + millisUntilFinished / 1000 + ")");

                }

                @Override
                public void onFinish() {
                    hideTips();
                }
            }.start();
            colorAnim.start();
            showTips();
        }
        // }
    }

    public void init() {
        if (forbidden.getParent() == null) {
            return;
        }
        wrongView = forbidden.inflate();
        colorAnim = ObjectAnimator.ofInt(wrongView, "backgroundColor", 0x00000000, 0x00000000);
//        colorAnim.setDuration(5000);
//        colorAnim.setEvaluator(new ArgbEvaluator());
        hideTips();
    }

    public void hideTips() {
        ((TextView) wrongView.findViewById(R.id.suo_te_in)).setVisibility(View.GONE);
        wrongView.findViewById(R.id.suo_te_time).setVisibility(View.GONE);
        wrongView.setClickable(false);
        wrongView.setBackgroundDrawable(null);

    }

    public void showTips() {
        wrongView.findViewById(R.id.suo_te_time).setVisibility(View.VISIBLE);
        wrongView.setClickable(true);
    }
}
