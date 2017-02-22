package com.lockscreen.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.lockscreen.api.liberal.yibuas.LoadingAsync;
import com.lockscreen.api.liberal.liu.Baseline;
import com.lockscreen.api.module.PaperEntry;
import com.lockscreen.api.module.WenjianType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by song on 15/8/7.
 */
public class NorApi {
    static NorApi api;

    public static synchronized NorApi instance(Context context) {
        if (api == null) {
            api = new NorApi(context.getApplicationContext());
        }
        return api;
    }

    Context context;
    int fileType;

    public NorApi(Context context) {
        this.context = context;
    }

    public void waiting(int fileType, Runnable callback) {
        if (this.fileType == fileType) {
            loadingTask.waiting(callback);
        } else {
            this.fileType = fileType;
            loadingTask.restart(callback);
        }
    }

    ArrayList<PaperEntry> folderEntries = new ArrayList<>();

    public ArrayList<PaperEntry> getFolders() {
        return folderEntries;
    }

    public PaperEntry getFolder(int which) {
        return folderEntries.get(which);
    }

    final LoadingAsync loadingTask = new LoadingAsync() {
        @Override
        protected void doInBackground() {
            String[] projection = {
                    Baseline._ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_MODIFIED
            };
            folderEntries.clear();
            Cursor cursor = context.getContentResolver().query(getUri(fileType), projection, null, null, null);
            try {
                cursor.moveToFirst();
                Map<String, PaperEntry> entryMap = new HashMap<>();
                if (isCanceled()) return;
                do {
                    String file = cursor.getString(2);
                    String bucketId = file.substring(0, file.lastIndexOf('/'));
                    PaperEntry entry = entryMap.get(bucketId);
                    if (entry == null) {
                        entry = new PaperEntry();
                        entry.bucketId = bucketId;
                        entry.bucketName = cursor.getString(1);
                        entry.fileType = fileType;
                        entryMap.put(bucketId, entry);
                    }
                    entry.addFile(file);
                    entry.addFileId(cursor.getLong(0));

                    if (isCanceled()) return;
                } while (cursor.moveToNext());
                folderEntries.addAll(entryMap.values());
            } finally {
                cursor.close();
            }
        }
    };

    public static Uri getUri(int fileType) {
        switch (fileType) {
            case WenjianType.TYPE_PIC:
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            case WenjianType.TYPE_VIDEO:
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            case WenjianType.TYPE_AUDIO:
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            default:
                return null;
        }
    }

    public void notifyDataSetChanged() {
        this.fileType = WenjianType.TYPE_LAST;
    }
}
