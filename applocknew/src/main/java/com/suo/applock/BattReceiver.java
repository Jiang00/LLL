package com.suo.applock;

import android.content.*;

import com.lockscreen.api.liberal.Utils;
import com.suo.applock.menu.MyYingys;
import com.suo.applock.menu.SharPre;

/**
 * Created by superjoy on 2014/8/25.
 */
public class BattReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String act = intent.getAction();
        Utils.LOGER("on receive " + intent.toString());
        switch (act) {
            case Intent.ACTION_BOOT_COMPLETED:
                context.startService(new Intent(context, WorkService.class));
                break;
            case Intent.ACTION_PACKAGE_REMOVED:{
                if (intent.getDataString() == null || intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return;
                final String pkg = intent.getDataString().substring("package:".length());
                if (pkg.startsWith(context.getPackageName())) {
                    //do nothing for our skins and ourselves
                    return;
                }
                MyYingys.removed(pkg);
            }
                break;
            case Intent.ACTION_PACKAGE_ADDED:{
                if (intent.getDataString() == null || intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return;
                final String pkg = intent.getDataString().substring("package:".length());
                if (pkg.startsWith(context.getPackageName())) {
                    //do nothing for our skins and ourselves
                    return;
                }
                MyYingys.add(context, pkg);
                SharedPreferences sp = Application.getSharedPreferences();
                if (sp.getBoolean(SharPre.LOCK_NEW, SharPre.LOCK_DEFAULT)){
                    context.startService(new Intent(context, WorkService.class).putExtra(WorkService.WORK_EXTRA_KEY, WorkService.WORK_LOCK_NEW).putExtra("pkg", pkg));
                }
            }
                break;
            case Intent.ACTION_USER_PRESENT:
                context.startService(new Intent(context, WorkService.class).putExtra("on", true));
                break;
            case Intent.ACTION_MEDIA_MOUNTED:
                context.startService(new Intent(context, WorkService.class));
                break;

        }
    }
}
