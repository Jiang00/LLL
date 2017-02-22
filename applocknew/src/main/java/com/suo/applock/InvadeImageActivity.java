package com.suo.applock;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lockscreen.api.InvadeApi;
import com.lockscreen.api.liberal.BaseActivity;
import com.lockscreen.api.module.WenjianType;
import com.lockscreen.api.module.InvadeEntry;
import com.suo.applock.track.Filety;
import com.suo.libra.view.LoaddImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by song on 16/1/4.
 */
public class InvadeImageActivity extends BaseActivity{
    @InjectView(R.id.suo_invade_shang_ic)
    ImageView blockIcon;

    @InjectView(R.id.suo_invade_peple)
    LoaddImageView blockImage;


    @InjectView(R.id.suo_xia_ic)
    ImageView dateIcon;
    @InjectView(R.id.suo_invade_data)
    TextView dateView;

    @InjectView (R.id.suo_invade_tishi)
    TextView messageView;
    @InjectView(R.id.suo_title_bar_te)
    TextView title;
    @InjectView(R.id.suo_et_m)
    ImageButton edit_mode;
    @InjectView(R.id.suo_set_bt)
    ImageButton delete;


//    LoadImagePresenter presenter;

    private static final String EXTRA_KEY_URL = "url";
    private static final String EXTRA_KEY_DATE = "date";
    private static final String EXTRA_KEY_PKG = "pkg";
    private static final String EXTRA_KEY_POSITION= "position";

    private String url;
    private String date;
    private String pkg;
    private int position;

    @Override
    protected void onIntent(Intent intent) {
        url = intent.getStringExtra(EXTRA_KEY_URL);
        date = intent.getStringExtra(EXTRA_KEY_DATE);
        pkg = intent.getStringExtra(EXTRA_KEY_PKG);
        position = intent.getIntExtra(EXTRA_KEY_POSITION,-1);
    }

    public static void launch(Context context, InvadeEntry entity) {
        Intent i = new Intent(context, InvadeImageActivity.class);
        i.putExtra(EXTRA_KEY_URL, entity.url);
        i.putExtra(EXTRA_KEY_DATE, entity.date);
        i.putExtra(EXTRA_KEY_PKG, entity.pkg);
        context.startActivity(i);
    }

    @Override
    protected void onRestoreInstanceStateOnCreate(Bundle savedInstanceState) {
        url = savedInstanceState.getString(EXTRA_KEY_URL);
        date = savedInstanceState.getString(EXTRA_KEY_DATE);
        pkg = savedInstanceState.getString(EXTRA_KEY_PKG);
        position = savedInstanceState.getInt(EXTRA_KEY_POSITION,-1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.suo_invade_view);
        ButterKnife.inject(this);
//       设置 actionbar
//        setSupportActionBar(lockscreen_toolbar);
//        ActionBar bar = getSupportActionBar();
//        if (bar != null) {
//            bar.setDisplayHomeAsUpEnabled(true);
//            bar.setTitle(R.string.intruder_detail);
//        }
//
//        presenter = LoadImagePresenter.getPresenter();
        //设置自定义标题
        title.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.suo_back), null, null, null);
        title.setText("      "+getString(R.string.suo_intru_detail));
        edit_mode.setVisibility(View.GONE);
        delete.setImageResource(R.drawable.suo_incade_de);
        title.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent in=new Intent();
                in.putExtra("position",position);
//                deleteIntruder();
                    setResult(1,in);
                finish();
            }
        });
        Drawable icon=null;
        CharSequence label=null;

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(pkg, 0);
             icon = packageInfo.applicationInfo.loadIcon(getPackageManager());

             label = packageInfo.applicationInfo.loadLabel(getPackageManager());
            messageView.setText(getResources().getString(R.string.suo_intru_f_app, label==null?getResources().getString(R.string.app_name):label));
        } catch (PackageManager.NameNotFoundException e) {
            messageView.setText(getResources().getString(R.string.suo_intru_f_app, label==null?getResources().getString(R.string.app_name):label));

        }
        blockIcon.setBackgroundDrawable(icon==null?getResources().getDrawable(R.drawable.suo_ic):icon);
        dateIcon.setBackgroundDrawable(icon==null?getResources().getDrawable(R.drawable.suo_ic):icon);
        blockImage.setImage(url,0L, WenjianType.TYPE_PIC,true);
//        messageView.setText(getResources().getString(R.string.block_intruder_for_app, label));
        dateView.setText(date);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //本类将不会调用 presenter.stop
//        presenter.start(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();



        Filety file = new Filety();
        file.filePath = url;
        file.fileType = Filety.TYPE_PIC;

//        ImageTManager.setImageView(blockImage,url,true);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.suo_invade_view, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
//        else if (item.getItemId() == R.id.lockscreendelete) {
//            deleteIntruder();
//            return true;
//        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void deleteIntruder() {
        InvadeApi.deleteIntruder(url);
        finish();
    }

}
