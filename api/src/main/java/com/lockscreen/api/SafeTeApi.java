package com.lockscreen.api;

import android.content.Context;
import android.util.SparseArray;

import com.lockscreen.api.liberal.yibuas.LoadingAsync;
import com.lockscreen.api.module.PaperEntry;
import com.lockscreen.api.module.WenjianType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by song on 15/8/5.
 */
public class SafeTeApi {
    static SafeTeApi api;

    Context context;

    public static final Object lock = new Object();

    SparseArray<HashMap<String, PaperEntry>> folderEntriesMap = new SparseArray<>();
    SparseArray<ArrayList<PaperEntry>> folderEntriesWithType = new SparseArray<>();

    private SafeTeApi(Context context) {
        this.context = context;
        for (int i = WenjianType.TYPE_PIC; i < WenjianType.TYPE_LAST; ++i) {
            folderEntriesWithType.put(i, new ArrayList<PaperEntry>());
            folderEntriesMap.put(i, new HashMap<String, PaperEntry>());
        }
    }

    public static synchronized SafeTeApi instance(Context context) {
        if (api == null) {
            api = new SafeTeApi(context.getApplicationContext());
        }
        return api;
    }

    public PaperEntry getFolder(int fileType, String bucket) {
        PaperEntry entry = folderEntriesMap.get(fileType).get(bucket);
        if (entry == null) {
            entry = new PaperEntry();
            entry.bucketId = bucket;
            entry.bucketName = bucket.substring(bucket.lastIndexOf('/') + 1);
            entry.fileType = fileType;
            folderEntriesMap.get(fileType).put(bucket, entry);
            folderEntriesWithType.get(fileType).add(entry);
        }
        return entry;
    }

    public ArrayList<PaperEntry> getFolders(int fileType) {
        return folderEntriesWithType.get(fileType);
    }

    public PaperEntry getFolder(int fileType, int which) {
        ArrayList<PaperEntry> folderEntries = folderEntriesWithType.get(fileType);
        return folderEntries.size() <= which ? null : folderEntries.get(which);
    }

    public void waiting(Runnable callback) {
        task.waiting(callback);
    }

    final LoadingAsync task = new LoadingAsync() {
        @Override
        protected void doInBackground() {
            synchronized (lock) {
                HiLishi.WalkHistory walker = new HiLishi.WalkHistory() {
                    @Override
                    public void onHistory(int fileType, String file) {
                        HashMap<String, PaperEntry> map = folderEntriesMap.get(fileType);
                        String bucket = file.substring(0, file.lastIndexOf('/'));
                        PaperEntry entry = map.get(bucket);
                        if (entry == null) {
                            entry = new PaperEntry();
                            entry.bucketId = bucket;
                            entry.bucketName = bucket.substring(bucket.lastIndexOf('/') + 1);
                            entry.fileType = fileType;
                            map.put(bucket, entry);
                        }
                        entry.addFile(file);
                    }
                };

                if (HiLishi.isHistoryValid() && HiLishi.iterateHistory(walker)) {

                } else {
                    String f = FileOp.p(FileOp.INFO_PATH);
                    File root = new File(f);
                    File[] files = root.listFiles();
                    if (files != null) {
                        HiLishi.dropHistory();
                        HiLishi.begin();
                        for (File ff : files) {
                            try {
                                String content = FileOp.f(ff.getAbsolutePath());
                                int type = content.charAt(0);
                                if (type == WenjianType.TYPE_PIC_NATIVE) type = WenjianType.TYPE_PIC;
                                String path = content.substring(1);
                                HiLishi.addHistory(type, path);
                                walker.onHistory(type, path);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        HiLishi.end();
                    }
                }
                for (int i = WenjianType.TYPE_PIC; i < WenjianType.TYPE_LAST; ++i) {
                    folderEntriesWithType.get(i).addAll(folderEntriesMap.get(i).values());
                }
            }
        }
    };

    public void notifyDataSetChanged() {
        HiLishi.dropHistory();
        HiLishi.begin();
        for (int i = WenjianType.TYPE_PIC; i < WenjianType.TYPE_LAST; ++i) {
            ArrayList<PaperEntry> folders = folderEntriesWithType.get(i);
            HashMap<String, PaperEntry> maps = folderEntriesMap.get(i);
            for (int j = folders.size() - 1; j >= 0; --j) {
                PaperEntry entry = folders.get(j);
                if (entry.count() == 0) {
                    folders.remove(j);
                    maps.remove(entry.bucketId);
                    continue;
                }
                HiLishi.addHistories(i, entry.getFiles());
            }
        }
        HiLishi.end();
    }
}
