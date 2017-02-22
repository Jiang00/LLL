package com.suo.applock.menu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.suo.applock.Application;
import com.suo.applock.R;
import com.lockscreen.api.liberal.yibuas.LoadingAsync;
import com.suo.applock.SearchRun;

import java.util.*;

/**
 * Created by SongHualin on 6/26/2015.
 */
public class MyYingys {
    public static void init() {
        loadingTask.start();
    }

    public static void setWaiting(Runnable waiting) {
        loadingTask.waiting(waiting);
    }

    public static void add(Context context, String pkg) {
        if (loadingTask.isFinished()) {
            SearchRun.SearchData data = new SearchRun.SearchData();
            try {
                data.pkg = pkg;
                ApplicationInfo applicationInfo = context.getPackageManager().getPackageInfo(pkg, 0).applicationInfo;
                data.label = applicationInfo.loadLabel(context.getPackageManager()).toString();
                data.system = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (data.system) {
                    systems.add(data);
                    Collections.sort(systems, comparator);
                } else {
                    thirdparties.add(data);
                    Collections.sort(thirdparties, comparator);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            init();
        }
    }

    private static boolean remove(List<SearchRun.SearchData> pool, String pkg) {
        for (int i = pool.size() - 1; i >= 0; --i) {
            if (pool.get(i).pkg.equals(pkg)) {
                pool.remove(i);
                return true;
            }
        }
        return false;
    }

    public static void removed(String pkg) {
        if (loadingTask.isFinished()) {
            if (remove(thirdparties, pkg)) return;
            if (remove(predefined, pkg)) return;
            remove(systems, pkg);
        } else {
            init();
        }
    }

    public static void show(SearchRun.SearchData data) {
        hiddens.remove(data);
        if (data.predefined) {
            predefined.add(data);
            Collections.sort(predefined, comparator);
        } else if (data.system) {
                systems.add(data);
                Collections.sort(systems, comparator);

        } else {
            thirdparties.add(data);
            Collections.sort(thirdparties, comparator);
        }
    }

    public static void hide(SearchRun.SearchData data) {
        hiddens.add(data);
        Collections.sort(hiddens, comparator);
        if (data.predefined) {
            predefined.remove(data);
        } else if (data.system) {
            systems.remove(data);
        } else {
            thirdparties.remove(data);
        }
    }

    public static List<SearchRun.SearchData> getHiddenApps(Map<String, Boolean> outHiddens) {
        List<SearchRun.SearchData> apps = new ArrayList<>();
        outHiddens.clear();
        for (SearchRun.SearchData data : hiddens) {
            outHiddens.put(data.pkg, true);
        }
        apps.addAll(hiddens);
        apps.addAll(predefined);
        apps.addAll(thirdparties);
        apps.addAll(systems);
        return apps;
    }

    /**
     * @param filter suo_main_check apps
     * @return
     */
    public static List<SearchRun.SearchData> getApps(Map<String, Boolean> filter) {
        List<SearchRun.SearchData> apps = new ArrayList<>();
        List<SearchRun.SearchData> left = new ArrayList<>();
        apps.addAll(top);
        filter(filter, predefined, apps, left);
        filter(filter, thirdparties, apps, left);
        filter(filter, systems, apps, left);
        apps.addAll(left);
        return apps;
    }

    private static void filter(Map<String, Boolean> filter, List<SearchRun.SearchData> pool, List<SearchRun.SearchData> filtered, List<SearchRun.SearchData> left) {
        if (filter.size() == 0) {
            left.addAll(pool);
        } else {
            for (SearchRun.SearchData data : pool) {
                if (filter.containsKey(data.pkg)) {
                    filtered.add(data);
                } else {
                    left.add(data);
                }
            }
        }
    }

    public static final ArrayList<SearchRun.SearchData> top = new ArrayList<>(4);
    private static final ArrayList<SearchRun.SearchData> hiddens = new ArrayList<>();
    private static final ArrayList<SearchRun.SearchData> predefined = new ArrayList<>();
    private static final ArrayList<SearchRun.SearchData> thirdparties = new ArrayList<>();
    private static final ArrayList<SearchRun.SearchData> systems = new ArrayList<>();
    static final Comparator<SearchRun.SearchData> comparator = new Comparator<SearchRun.SearchData>() {
        @Override
        public int compare(SearchRun.SearchData lhs, SearchRun.SearchData rhs) {
            return lhs.label.compareTo(rhs.label);
        }
    };
    static LoadingAsync loadingTask = new LoadingAsync() {
        private List<ResolveInfo> getDesktopApps() {
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            return Application.getContext().getPackageManager().queryIntentActivities(mainIntent, 0);
        }
        @Override
        protected void doInBackground() {
            SearchRun.SearchData incoming = new SearchRun.SearchData();
            incoming.label = Application.getContext().getString(R.string.suo_call);
            incoming.pkg = "com.android.phone";
            top.add(incoming);

            SearchRun.SearchData recents = new SearchRun.SearchData();
            recents.pkg = "com.android.systemui";
            recents.label = Application.getContext().getString(R.string.suo_hide);
            top.add(recents);
            ArrayList<SearchRun.SearchData> hiddens_ = new ArrayList<>();
            PackageManager packageManager = Application.getContext().getPackageManager();
            List<PackageInfo> ps = packageManager.getInstalledPackages(0);
            for (PackageInfo p : ps) {
                if (!p.applicationInfo.enabled) {
                    SearchRun.SearchData data = new SearchRun.SearchData();
                    data.label = p.applicationInfo.loadLabel(packageManager).toString();
                    data.pkg = p.packageName;
                    data.system = (p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    hiddens_.add(data);
                }
            }

            HashMap<String, Boolean> filters = new HashMap<>();
            String[] predefineds = {
                    "com.sec.android.gallery3d",
                    "com.android.gallery3d",
                    "com.android.gallery",
                    "com.android.contacts",
                    "com.android.mms",
                    "com.android.phone",
//                    "com.android.packageinstaller",
                    "com.facebook.katana",
                    "com.google.android.gm",
                    "com.android.email",
                    "com.android.vending",
                    "com.twitter.android",
                    "com.instagram.android",
                    "com.google.android.youtube",
                    "jp.naver.suo_invade_li.android",
                    "com.whatsapp",
                    "com.facebook.orca",
                    "com.tencent.mm",
                    "com.google.android.talk",
                    "com.skype.raider",
                    "com.kakao.talk"
            };

            Map<String, Boolean> excludes = new HashMap<>();
            excludes.put(Application.getContext().getPackageName(), true);

            List<ResolveInfo> apps_ = getDesktopApps();
            ArrayList<SearchRun.SearchData> systems_ = new ArrayList<>();
            ArrayList<SearchRun.SearchData> thirdParties_ = new ArrayList<>();
            ArrayList<SearchRun.SearchData> predefinedData_ = new ArrayList<>();

            for (String pkg : predefineds) {
                try {
                    PackageInfo pi = packageManager.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
                    SearchRun.SearchData data = new SearchRun.SearchData();
                    data.pkg = pkg;
                    data.label = pi.applicationInfo.loadLabel(packageManager).toString();
                    data.predefined = true;
                    filters.put(pkg, true);
                    if (!pkg.equals("com.android.phone")) {
                        predefinedData_.add(data);
                    }
                } catch (Exception ignore) {
                }
            }

            for (ResolveInfo app : apps_) {
                String pkg = app.activityInfo.packageName;
                if (excludes.containsKey(pkg) || filters.containsKey(pkg)) continue;
                String label = app.loadLabel(packageManager).toString();
                SearchRun.SearchData data = new SearchRun.SearchData();
                data.label = label;
                data.pkg = pkg;
                if(!data.pkg.equals("com.android.settings")) {
                    if ((app.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        data.system = true;
                        systems_.add(data);
                    } else {
                        data.system = false;
                        thirdParties_.add(data);
                    }
                }
            }

            Collections.sort(hiddens_, comparator);
            Collections.sort(thirdParties_, comparator);
            Collections.sort(systems_, comparator);

            synchronized (this) {
                hiddens.clear();
                hiddens.addAll(hiddens_);
                systems.clear();
                systems.addAll(systems_);
                thirdparties.clear();
                thirdparties.addAll(thirdParties_);
                predefined.clear();
                predefined.addAll(predefinedData_);
            }
        }
    };
}
