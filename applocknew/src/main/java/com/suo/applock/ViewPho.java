package com.suo.applock;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by superjoy on 2014/8/29.
 */
public class ViewPho {
    int idx = -1;

    @Optional @InjectView(R.id.suo_ima)
    ImageView icon;

    @Optional @InjectView(R.id.suo_appna)
    TextView appName;

    @Optional @InjectView(R.id.suo_backg_s)
    View encrypted;



    @Optional @InjectView(R.id.suo_invade_ic)
    ImageView blockIcon;
    @Optional @InjectView(R.id.suo_invade_data)
    TextView simName;

    @Optional @InjectView(R.id.suo_invade_cia_ic)
    LinearLayout title_date;
    long id = 0;

    @Optional @InjectView(R.id.suo_set_checked)
    CheckBox box;

    public ViewPho() {
    }

    public ViewPho(View root) {
        ButterKnife.inject(this, root);
        root.setTag(this);
    }
}
