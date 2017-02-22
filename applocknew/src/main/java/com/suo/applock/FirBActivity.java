package com.suo.applock;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.widget.Toast;

import com.privacy.lock.aidl.IWorker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SongHualin on 6/26/2015.
 */
public abstract class FirBActivity extends FiraActivity implements ServiceConnection {
    public IWorker server;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        server = IWorker.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        server = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (!bindService(new Intent(this, WorkService.class), this, 0)){
                Toast.makeText(this, "服务没有启动", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        try {
            unbindService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }
    /**
     * 判断当前界面是否是桌面
     */
    public  boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }
}
