package com.suo.applock.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;

import com.suo.applock.Application;
import com.suo.applock.db.MyAppBridge;
import com.suo.applock.R;
import com.suo.applock.menu.AppBridge;
import com.suo.theme.SuoPatternView;
import com.suo.theme.MyFrameLayout;

import java.util.List;

/**
 * Created by huale on 2014/11/21.
 */
public class PattFragment extends AppFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return pattern = getView(inflater, container, ctrl, new ICheckResult() {
            @Override
            public void onSuccess() {
                getActivity().finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pattern != null) {
            ((SuoPatternView) pattern.findViewWithTag("app_pattern")).resetPattern();
        }
    }

    @Override
    public void onDestroyView() {
        if (pattern != null) {
            ViewGroup group = (ViewGroup) getView();
            if (group != null) {
                group.removeView(pattern);
            }
            pattern = null;
        }
        super.onDestroyView();
    }

    View pattern;

    public static View getView(LayoutInflater inflater, final ViewGroup container, Flow ctrl, final ICheckResult callback) {

        inflater = AppBridge.themeContext == null ? inflater : LayoutInflater.from(AppBridge.themeContext);
        View v = inflate("app_pattern_view", container,inflater.getContext());

        ((MyFrameLayout) v).setOverflowCtrl(ctrl);

        final MyAppBridge bridge = AppBridge.bridge;
        final SuoPatternView lock = (SuoPatternView) v.findViewWithTag("app_pattern");
        LinearLayout parent = (LinearLayout) lock.getParent();
        ((LinearLayout.LayoutParams) parent.getLayoutParams()).weight = 1.5f;
        parent.requestLayout();

        v.findViewWithTag("app_can_pp").setVisibility(View.GONE);

        final ViewStub forbidden = new ViewStub(Application.getContext(), R.layout.suo_error);
        ((MyFrameLayout) v).addView(forbidden);
        final ErrorView forbiddenView = new ErrorView(forbidden);
        forbiddenView.init();

        lock.setOnPatternListener(new SuoPatternView.OnPatternListener() {

            public void onPatternStart() {

            }

            public void onPatternDetected(List<SuoPatternView.Cell> pattern) {
                if (!bridge.check(SuoPatternUtils.patternToString(pattern), false)) {
                    forbiddenView.wrong();
                    lock.setDisplayMode(SuoPatternView.DisplayMode.Wrong);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            lock.clearPattern();
                        }
                    }, 500);
                } else {
                    forbiddenView.right();
                    lock.clearPattern();
                    callback.onSuccess();
                }
            }

            public void onPatternCleared() {
            }

            public void onPatternCellAdded(List<SuoPatternView.Cell> pattern) {

            }
        });
        lock.clearPattern();
        v.setOnClickListener(ctrl.hideOverflow);

        return v;
    }
}
