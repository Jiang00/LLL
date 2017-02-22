package com.suo.applock.view;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.client.AndroidSdk;
import com.android.dev.Utils;
import com.suo.applock.MyTrack;
import com.suo.applock.R;


/**
 * Created by wangqi on 16/4/11.
 */
public class ShowDialogview {
    public static final String FIVE_STARED = "five_sta_ed";


    public static void showDialog(final Context context, final String style, final ListView listview) {
        final View alertDialogView = View.inflate(context, R.layout.suo_main_five_rate, null);
        final AlertDialog d = new AlertDialog.Builder(context,R.style.show_dia).create();
//        Utils.addAlertAttribute(d.getWindow());
        d.setView(alertDialogView);
        d.show();

        alertDialogView.findViewById(R.id.suo_ne_dia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                if (style != null) {


                    switch (style) {

                        case MyTrack.CATEGORY_INTRUDE:
                            AndroidSdk.track(MyTrack.CATEGORY_INTRUDE,
                                    MyTrack.INTRUDE_NEXT, "", 1);

                            break;

                        case MyTrack.CATEGORY_VIDEO:
                            AndroidSdk.track(MyTrack.CATEGORY_VIDEO,
                                    MyTrack.VIDEO_NEXT, "", 1);
                            break;

                        case MyTrack.CATEGORY_FAKECOVER:
                            AndroidSdk.track(MyTrack.CATEGORY_FAKECOVER,
                                    MyTrack.FAKECOVER_NEXT, "", 1);
                            break;
                        case MyTrack.CATEGORY_PHOTOS:
                            AndroidSdk.track(MyTrack.CATEGORY_PHOTOS,
                                    MyTrack.PHOTO_NEXT, "", 1);
                            break;
                    }
                } else {

                }

            }
        });
        alertDialogView.findViewById(R.id.suo_good_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharPFive sh = new SharPFive(context);
                sh.setFiveRate(true);
                if(listview!=null){
                    if(SuoFragment.headerView!=null){
                        listview.removeHeaderView(SuoFragment.headerView);
                    }
                }



                if (style != null) {


                    switch (style) {

                        case MyTrack.CATEGORY_INTRUDE:
                            AndroidSdk.track(MyTrack.CATEGORY_INTRUDE,
                                    MyTrack.INTRUDE_FIVE_RATE, "", 1);

                            break;

                        case MyTrack.CATEGORY_VIDEO:
                            AndroidSdk.track(MyTrack.CATEGORY_VIDEO,
                                    MyTrack.VIDEO_FIVE_RATE, "", 1);
                            break;

                        case MyTrack.CATEGORY_FAKECOVER:
                            AndroidSdk.track(MyTrack.CATEGORY_FAKECOVER,
                                    MyTrack.FAKECOVR_FIVE_RATE, "", 1);
                            break;
                        case MyTrack.CATEGORY_PHOTOS:
                            AndroidSdk.track(MyTrack.CATEGORY_PHOTOS,
                                    MyTrack.PHOTO_FIVE_RATE, "", 1);
                            break;
                    }
                }
                d.dismiss();
                Utils.rateUs(v.getContext());
            }
        });


        alertDialogView.findViewById(R.id.suo_bad_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.cancel();
                showComplainDialog(context);
            }
        });


    }


    public static void showComplainDialog(final Context context) {
        final View alertDialogView = View.inflate(context, R.layout.suo_main_emale, null);
        final AlertDialog d = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert).create();



        Utils.addAlertAttribute(d.getWindow());
        d.setView(alertDialogView);
        d.show();

        TextView submitText = (TextView) alertDialogView.findViewById(R.id.suo_ema_sub);
        submitText.setOnClickListener(new View.OnClickListener(

        ) {
            @Override
            public void onClick(View v) {
                SharPFive sh = new SharPFive(context);

                sh.setFiveRate(true);
                Log.i("five2",sh.getFiveRate()+"-----");
                EditText content = (EditText) alertDialogView.findViewById(R.id.suo_text);
                EditText email = (EditText) alertDialogView.findViewById(R.id.suo_emal);

                if (content.getText().length() != 0 && email.getText().length() != 0) {

                    AndroidSdk.track(MyTrack.CATEGORY_RATE_BAD_CONTENT,
                            email.getText().toString() + "  " + content.getText().toString(), "", 1);

                } else if (content.getText().length() == 0 && email.getText().length() != 0) {
                    AndroidSdk.track(MyTrack.CATEGORY_RATE_BAD_CONTENT,
                            email.getText().toString() + "  ", "", 1);


                } else if (content.getText().length() != 0 && email.getText().length() == 0) {
                    AndroidSdk.track(MyTrack.CATEGORY_RATE_BAD_CONTENT,
                            content.getText().toString() + "  ", "", 1);

                }
                d.cancel();


            }
        });

    }




}
