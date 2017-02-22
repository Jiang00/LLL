package com.suo.applock.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

//import com.privacy.ad.FullAdMaster;
//import com.privacy.ad.INativeAd;
//import com.privacy.ad.NativeAdsScrollView;
import com.android.client.AndroidSdk;
import com.android.client.ClientNativeAd;
import com.suo.applock.Application;
import com.suo.applock.db.MyAppBridge;
import com.suo.applock.menu.AppBridge;
import com.suo.applock.R;
import com.suo.applock.menu.SharPre;
import com.lockscreen.api.liberal.Utils;
import com.suo.theme.MyFrameLayout;

/**
 * Created by huale on 2014/11/20.
 */
public class AppFragment extends Fragment {
    public static final String TAG_UNLOCK="unlock";
    public interface ICheckResult {
        void onSuccess();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctrl = new Flow();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        afterViewCreated(view, ctrl);
    }

    public static void afterViewCreated(View view, Flow ctrl) {
        setupTitle(view);
            createAdView((ViewGroup) view);
//        setupOverflow(view, ctrl);
//        setupDaily(view);
    }

    public static void setupTitle(View v) {
        MyAppBridge bridge = AppBridge.bridge;
        TextView appName = new TextView(v.getContext());
        appName.setTag("realAppName");
        appName.setText(bridge.appName());
        appName.setTextSize(21);
        appName.setTextColor(0xffffffff);
        appName.setGravity(Gravity.CENTER);
        appName.setVisibility(View.GONE);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Utils.getDimens(v.getContext(), 48));
        lp.leftMargin = Utils.getDimens(v.getContext(), 8);
        ((ViewGroup) v).addView(appName, lp);
        appName.setAlpha(0);
        TextView viewById = (TextView) v.findViewWithTag("suo_lable");
        viewById.setVisibility(View.GONE);
        ImageView icon = (ImageView) v.findViewWithTag("app_ico");
        icon.setBackgroundDrawable(bridge.icon());

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ViewGroup group = (ViewGroup) getView();
        if (group != null) {
            group.removeAllViewsInLayout();
            View v = onCreateView(LayoutInflater.from(getActivity()), group, null);
            group.addView(v);
            onViewCreated(v, null);
        }
    }

    protected static Animation out, in;
    protected Flow ctrl;



    @Override
    public void onDestroyView() {
        ctrl.hideOverflow = null;
        if (ctrl.overflowStub != null) {
            ctrl.overflowStub.removeAllViews();
        }
        ctrl.overflowStub = null;
        ctrl.ovf = null;
        ctrl = null;
        ViewGroup group = (ViewGroup) getView();
        if (group != null) {
            group.removeAllViews();
        }
        super.onDestroyView();
    }

    protected static void createAdView(ViewGroup view) {
       if ( AndroidSdk.hasNativeAd(TAG_UNLOCK,AndroidSdk.NATIVE_AD_TYPE_ALL)) {
           Log.d("aaa","----sdk2");
           FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
           Point size = Utils.getScreenSize(view.getContext());
           if (size.y < 854) {
               layoutParams.topMargin = Utils.getDimens(view.getContext(), 32);
           } else {
               layoutParams.topMargin = Utils.getDimens(view.getContext(), 48);
           }
           final TextView realAppName = (TextView) view.findViewWithTag("realAppName");
           final TextView appName = ((TextView) view.findViewWithTag("suo_lable"));
           final ImageView icon = ((ImageView) view.findViewWithTag("app_ico"));
           realAppName.setAlpha(1.0f);
           appName.setAlpha(0.0f);
              View scrollView=AndroidSdk.peekNativeAdScrollViewWithLayout(TAG_UNLOCK, AndroidSdk.NATIVE_AD_TYPE_ALL, AndroidSdk.HIDE_BEHAVIOR_AUTO_HIDE, R.layout.suo_native_layout, new ClientNativeAd.NativeAdClickListener() {
                  @Override
                  public void onNativeAdClicked(ClientNativeAd clientNativeAd) {

                  }
              }, new ClientNativeAd.NativeAdScrollListener() {
                  @Override
                  public void onNativeAdScrolled(float v) {
                      icon.setAlpha(1 - v);
                      appName.setAlpha(1 - v);
                      realAppName.setAlpha(v);
                  }
              });
           if (scrollView != null) {
               Application.getWatcher().watch(scrollView);
                Log.d("aaa","----risvscroll_true");
               view.addView(scrollView, layoutParams);
           }
           Log.d("aaa","----risvscroll_false");
       }
        else{
           Log.d("aaa","----risvscroll_3");
       }
    }

    public static void checkMenu(Button button, boolean check) {
        if (check)
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.suo_check_on_, 0);
        else
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.suo_check_off_, 0);
    }

    public static MyFrameLayout inflate(String layoutId, ViewGroup container, Context c) {
        Context themeContext = c;
        LayoutInflater inflater = LayoutInflater.from(themeContext);
        int layout = themeContext.getResources().getIdentifier(layoutId, "layout", themeContext.getPackageName());
        MyFrameLayout v = (MyFrameLayout) inflater.inflate(layout, container, false);

        return v;
    }

}
