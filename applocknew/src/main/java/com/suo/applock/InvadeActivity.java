package com.suo.applock;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.client.AndroidSdk;
import com.lockscreen.api.liberal.Utils;
import com.lockscreen.api.module.WenjianType;
import com.lockscreen.api.module.InvadeEntry;
import com.suo.applock.menu.SharPre;
import com.suo.applock.view.SharPFive;
import com.suo.libra.view.LoaddImageView;
import com.suo.applock.view.RateWidget;
import com.lockscreen.api.InvadeApi;
import com.lockscreen.api.liberal.BaseActivity;
import com.lockscreen.api.liberal.clis.CCViewAdaptor;
import com.lockscreen.api.liberal.clis.CCViewScroller;
import com.suo.applock.track.TrackString;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by song on 15/10/8.
 */
public class InvadeActivity extends BaseActivity {
    @InjectView(R.id.suo_list_v)
    ListView listView;

    @InjectView(R.id.suo_backg)
    LinearLayout tip;


    @InjectView(R.id.suo_title_bar_te)
    TextView title;
    @InjectView(R.id.suo_set_bt)
    ImageButton help;
    int intrudeTime;

    Context context;

    static View headerView;
    CCViewAdaptor adapter;
    ArrayList<InvadeEntry>  intruderEntries;
    SharPFive fiveRate;

    protected void onIntent(Intent intent) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.suo_invade_con);
        ButterKnife.inject(this);
        fiveRate=new SharPFive(this);
        context=this;

        title.setText("    "+getString(R.string.suo_ruqinzhe));
        title.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.suo_back), null, null, null);

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        help.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i=new Intent(InvadeActivity.this,InvadeSetActivity.class);
                startActivity(i);
            }
        });
//        findViewById(R.id.lockscreen_help).setVisibility(View.GONE);
        findViewById(R.id.suo_title_bt).setVisibility(View.GONE);

        CheckBox checkBox = (CheckBox) findViewById(R.id.suo_invade_ss);
        checkBox.setChecked(SharPre.fetchIntruder());

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, boolean b) {
                SharPre.setFetchIntruder(b);
                if (b) {
                    Toast.makeText(Application.getContext(), R.string.suo_ruqinzhe_on, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Application.getContext(), R.string.suo_ruqinzhe_off, Toast.LENGTH_SHORT).show();
                }
            }
        });

         intruderEntries = InvadeApi.getIntruders();
        for (int i=0;i<intruderEntries.size();i++){
            Log.i("intruderEntries",intruderEntries.get(i).date+"22222222222");
        }

        if (intruderEntries.size() == 0) {
            listView.setVisibility(View.GONE);
        } else {
            tip.setVisibility(View.GONE);
            ListView lv = listView;

            if (!fiveRate.getFiveRate()) {
                headerView = LayoutInflater.from(this).inflate(R.layout.suo_main_title_rate, null);
                lv.addHeaderView(headerView);
                headerClick(headerView);
            }

            adapter=new CCViewAdaptor(new CCViewScroller(lv), R.layout.suo_invade_ac) {

                @Override
                protected void onUpdate(final int position, Object holderObject, boolean scroll) {
                    ViewPho holder = (ViewPho) holderObject;
                    if (position >= intruderEntries.size()) return;
                    final InvadeEntry entry = intruderEntries.get(position);
                    holder.idx = position;
                    holder.appName.setText(entry.date);
                    holder.simName.setText(entry.simdate);
                    if (position>0) {
                        InvadeEntry entry2 = intruderEntries.get(position - 1);
                        if (!entry.simdate.equals(entry2.simdate)){
                            titleDate=entry.simdate;
                            holder.title_date.setVisibility(View.GONE);
                        }else{
                            holder.title_date.setVisibility(View.GONE);
                        }
                    }else{
                        holder.title_date.setVisibility(View.GONE);
                    }

                    ((LoaddImageView) holder.icon).setImage(entry.url, 0L, WenjianType.TYPE_PIC, !scroll);
                    holder.icon.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v) {
                            Intent intent=new Intent(InvadeActivity.this,InvadeImageActivity.class);
                            intent.putExtra("url", entry.url);
                            intent.putExtra("date", entry.date);
                            intent.putExtra("pkg", entry.pkg);
                            intent.putExtra("position", position);
                            startActivityForResult(intent,1);
//                            InvadeImageActivity.lockscreen_launch(v.getContext(), entry);

                        }
                    });
                    try {
                        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(entry.pkg, 0);
                        Drawable icon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
                        holder.blockIcon.setBackgroundDrawable(icon);

                        CharSequence label = packageInfo.applicationInfo.loadLabel(context.getPackageManager());
//                        holder.blockmessage.setText(getResources().getString(R.string.block_intruder_for_app, label));
                    } catch (PackageManager.NameNotFoundException e) {
                        holder.blockIcon.setBackgroundResource(R.drawable.suo_ic);

                    }
                }

                @Override
                protected Object getHolder(View root) {
                    final ViewPho viewHolder = new ViewPho(root);
                    //删除照片入侵者照片
                    viewHolder.encrypted.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            InvadeEntry intruder = intruderEntries.remove(viewHolder.idx);
                            InvadeApi.deleteIntruder(intruder);
                            notifyDataSetChanged();
                            if (intruderEntries.size() == 0) {
                                listView.setVisibility(View.GONE);
                                tip.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    return viewHolder;
                }

                @Override
                public int getCount() {
                    return intruderEntries.size();
                }
            };
            lv.setAdapter(adapter);

        }

//        if (!sh.getFiveRate()) {
//            intrudeTime = sh.getIntrude();
//            intrudeTime++;
//            sh.setIntrude(intrudeTime);
//            if (intrudeTime == 1 || intrudeTime == 4 || intrudeTime == 7) {
//
//                new CountDownTimer(3000, 1000) {
//
//                    @Override
//                    public void onFinish() {
//                        ShowDialogview.showDialog(context, MyTrack.CATEGORY_INTRUDE);
//
//                    }
//
//                    @Override
//                    public void onTick(long millisUntilFinished) {
//
//                    }
//                }.start();
//
//            }
//        }
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
                            Application.getSharedPreferences().edit().putBoolean("five_r_intrude", true).apply();
                            finish();
                            Intent intent = new Intent(getApplication(), InvadeActivity.class);
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
                AndroidSdk.track(TrackString.CATEGORY_APP,
                        TrackString.CATEGORY_RATE_GOOD, "", 1);
                Application.getSharedPreferences().edit().putBoolean("five_r_intrude", true).apply();
                if (Utils.hasPlayStore(getApplication())) {
                    Utils.rate(v.getContext());
                }
//                getActivity().finish();
//                Intent intent = new Intent(getActivity(), SuoMain.class);
//                startActivity(intent);

                View alertDialogView = View.inflate(v.getContext(), R.layout.suo_rate_result, null);

                final RateWidget w = new RateWidget(getApplication(), RateWidget.MATCH_PARENT, RateWidget.MATCH_PARENT, RateWidget.PORTRAIT);
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


        headerView.findViewById(R.id.suo_rat_cha).setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1&&resultCode==1){
            int position=data.getIntExtra("position",-1);
            if(position>=0){
                InvadeEntry intruder = intruderEntries.remove(position);
                InvadeApi.deleteIntruder(intruder);
               adapter.notifyDataSetChanged();
                if (intruderEntries.size() == 0) {
                    listView.setVisibility(View.GONE);
                    tip.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}





