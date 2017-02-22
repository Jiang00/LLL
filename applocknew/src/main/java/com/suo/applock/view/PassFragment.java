package com.suo.applock.view;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;

import com.suo.applock.Application;
import com.suo.applock.Toolcls;
import com.suo.applock.db.MyAppBridge;
import com.suo.applock.menu.AppBridge;
import com.suo.applock.R;
import com.suo.applock.Preference;
import com.suo.theme.MyFrameLayout;
import com.suo.theme.Dot;

/**
 * Created by huale on 2014/11/19.
 */
public class PassFragment extends AppFragment {
    private static Dot passwordDot;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return passwd = getView(inflater, container, ctrl, new ICheckResult() {
            @Override
            public void onSuccess() {
                getActivity().finish();
            }
        });
    }

    public View passwd;
    public  static View getView(LayoutInflater inflater, ViewGroup container, Flow ctrl, final ICheckResult callback) {
        final MyAppBridge bridge = AppBridge.bridge;
        inflater = AppBridge.themeContext == null ? inflater : LayoutInflater.from(AppBridge.themeContext);
        View v = inflate("app_password_", container, inflater.getContext());
        ((MyFrameLayout) v).setOverflowCtrl(ctrl);
        passwordDot = (Dot) v.findViewWithTag("app_dot_dian");
        final Dot dot = passwordDot;
        final ViewStub forbidden = new ViewStub(Application.getContext(), R.layout.suo_error);
        ((MyFrameLayout) v).addView(forbidden);
         dot.forbiddenView = new ErrorView(forbidden);
        dot.forbiddenView.init();
        v.setTag(null);
        dot.init(new Dot.ICheckListener() {
            @Override
            public void match(String passwd) {
               if (bridge.check(passwd, true)) {
                   if (callback != null) {
                       callback.onSuccess();
                   }

               }
            }
        });
        dot.clear();


//        v.findViewWithTag("theme_passwd_cancel").setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bridge.back();
//            }
//        });

        v.findViewWithTag("aoo_qie").setVisibility(View.INVISIBLE);

        v.findViewWithTag("app_huitui").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Preference.isVibrateForPasswordInputEnabled()) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                    | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                }
                dot.backSpace();
            }
        });
        String [] buttons = {
                "suo_bt_n_0", "suo_bt_n_1", "suo_bt_n_2", "suo_bt_n_3", "suo_bt_n_4",
                "suo_bt_n_5", "suo_bt_n_6", "suo_bt_n_7", "suo_bt_n_8", "suo_bt_n_9",
        };
        Toolcls.RandomNumpad(bridge, v, buttons);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dot.setNumber(((Button) v).getText().charAt(0));
            }
        };
        for(String btn : buttons){
            v.findViewWithTag(btn).setOnClickListener(clickListener);
        }

//        v.setOnClickListener(ctrl.hideOverflow);

        return v;
    }

    @Override
    public void onDestroyView() {
        if (passwd != null){
            ViewGroup group = (ViewGroup) getView();
            if (group != null) {
                group.removeView(passwd);
            }
            passwd = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (passwordDot != null) {
            passwordDot.clear();
        }
    }
}
