package com.suo.applock;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.lockscreen.api.FileOp;
import com.lockscreen.api.liberal.liu.TuMaster;
import com.lockscreen.api.module.PaperEntry;
import com.lockscreen.api.module.WenjianType;
import com.suo.applock.menu.FileTy;
import com.suo.applock.menu.MyCorporFile;
import com.lockscreen.api.HiLishi;
import com.lockscreen.api.NorApi;
import com.lockscreen.api.SafeTeApi;
import com.lockscreen.api.liberal.Utils;
import com.lockscreen.api.liberal.liu.Baseline;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by SongHualin on 7/2/2015.
 */
public class HandlewenjianService extends IntentService {
    public static final int MSG_HAND_SHAKE = 1;
    public static final int MSG_CANCEL = 2;
    public static final int MSG_CANCELED = 3;
    public static final int MSG_FINISHED = 4;
    public static final int MSG_UPDATE_PROGRESS = 5;
    public static final int MSG_REFRESHING = 6;

    Messenger client;

    public HandlewenjianService() {
        super("worker");
    }

    public static void startService(int fileType, int total, boolean[] selects, boolean normal, boolean folder, ArrayList<Integer> entryIdx) {
        Application.getContext().startService(
                new Intent(Application.getContext(), HandlewenjianService.class)
                        .putExtra("fileType", fileType)
                        .putExtra("total", total)
                        .putExtra("selects", selects)
                        .putExtra("normal", normal)
                        .putExtra("folder", folder)
                        .putExtra("entries", entryIdx)
        );
    }

    public static void startService(int total, boolean[] selects, File root) {
        Application.getContext().startService(new Intent(Application.getContext(), HandlewenjianService.class)
                .putExtra("total", total)
                .putExtra("selects", selects)
                .putExtra("file", root));
    }

    boolean cancel = false;
    boolean handshake = false;

    static class HandleHandler extends Handler {
        WeakReference<HandlewenjianService> service;

        public HandleHandler(Looper looper, HandlewenjianService service) {
            super(looper);
            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            HandlewenjianService s = service.get();
            if (s == null) return;
            switch (msg.what) {
                case MSG_HAND_SHAKE:
                    s.client = msg.replyTo;
                    s.handshake = true;
                    break;

                case MSG_CANCEL:
                    s.cancel = true;
                    break;
            }
        }
    }

    HandleHandler handleHandler;
    Messenger messenger;

    @Override
    public void onCreate() {
        super.onCreate();
        handleHandler = new HandleHandler(getMainLooper(), this);
        messenger = new Messenger(handleHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    public void stopMe() {
        int count = 0;
        while (!handshake) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (++count > 10) break;
        }
        manager.cancel(1);
        stopSelf();
    }

    boolean scanning = false;
    public void waiting() {
        while (scanning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    int fileType;
    Callback scannerCallback;

    @Override
    protected void onHandleIntent(Intent intent) {
        selects = intent.getBooleanArrayExtra("selects");
        total = intent.getIntExtra("total", 0);
        ArrayList<Integer> entries = intent.getIntegerArrayListExtra("entries");
        File root = (File) intent.getSerializableExtra("file");
        fileType = intent.getIntExtra("fileType", WenjianType.TYPE_PIC);

        if (root != null) {
            normal = true;
            handleNormalFiles(root, selects);
        } else if (intent.getBooleanExtra("normal", false)) {
            normal = true;
            if (intent.getBooleanExtra("folder", false)) {
                handleNormalFolder(entries);
            } else {
                currentFolderIdx = 1;
                PaperEntry entry = NorApi.instance(this).getFolder(entries.get(0));
                handleNormalFile(entry, true);
            }
        } else {
            normal = false;
            scanning = true;
            scannerCallback = new Callback(new Callback.ScannerListener() {
                @Override
                public void updateProgress() {
                    try {
                        if (client != null) {
                            Message msg = Message.obtain();
                            msg.what = MSG_REFRESHING;
                            msg.arg1 = scannerCallback.currentCount;
                            msg.arg2 = scannerCallback.totalCount;
                            client.send(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onComplete() {
                    scanning = false;
                    onFinished();
                    stopMe();
                }
            });
            if (intent.getBooleanExtra("folder", false)) {
                handleSafeFolder(entries);
            } else {
                currentFolderIdx = 1;
                PaperEntry entry = SafeTeApi.instance(this).getFolder(fileType, entries.get(0));
                handleSafeFile(entry, true);
            }
        }
    }

    private void handleNormalFolder(ArrayList<Integer> entries) {
        ArrayList<PaperEntry> entries1 = new ArrayList<>();
        for (int entryIdx : entries) {
            PaperEntry entry = NorApi.instance(this).getFolder(entryIdx);
            entries1.add(entry);
        }
        for (PaperEntry entry : entries1) {
            ++currentFolderIdx;
            int count = entry.getFiles().size();
            selects = new boolean[count];
            Arrays.fill(selects, true);
            total = count;
            handleNormalFile(entry, false);
            if (cancel) break;
        }
        onFinished();
        stopMe();
    }

    private void onFinished() {
        if (client != null) {
            try {
                Message msg = Message.obtain();
                if (cancel) {
                    msg.what = MSG_CANCELED;
                } else {
                    msg.what = MSG_FINISHED;
                }
                if (failFiles.size() > 0) {
                    Bundle b = new Bundle();
                    b.putStringArrayList("fails", failFiles);
                    msg.setData(b);
                }
                client.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    int currentFolderIdx = 0;

    private void handleSafeFolder(ArrayList<Integer> entries) {
        ArrayList<PaperEntry> entries1 = new ArrayList<>();
        for (int entryIdx : entries) {
            PaperEntry entry = SafeTeApi.instance(this).getFolder(fileType, entryIdx);
            entries1.add(entry);
        }
        for (PaperEntry entry : entries1) {
            ++currentFolderIdx;
            int count = entry.getFiles().size();
            selects = new boolean[count];
            total = count;
            Arrays.fill(selects, true);
            handleSafeFile(entry, false);
            if (cancel) break;
        }
        scannerCallback.requireStop();
        waiting();
//        onFinished();
//        stopMe();
    }

    private void handleSafeFile(PaperEntry entry, boolean stop) {
        try {
            current = 0;
            startForeGround();
            ContentResolver resolver = getContentResolver();
            ArrayList<String> fileNames = new ArrayList<>();
            ArrayList<String> files = entry.getFiles();
            new File(entry.bucketId).mkdirs();
            HiLishi.begin();
            for (int i = selects.length - 1; i >= 0; --i) {
                if (selects[i]) {
                    String fileName = files.get(i);
                    if (FileOp.d(fileName)) {
                        files.remove(i);
                        deleteThumbnail(resolver, fileName, 0);
                        fileNames.add(fileName);
                        if (fileNames.size() == 4) {
                            scannerCallback.addCount(4);
                            MediaScannerConnection.scanFile(Application.getContext(), fileNames.toArray(new String[fileNames.size()]), null, scannerCallback);
                            fileNames.clear();
                        }
                    } else {
                        Utils.LOGER(fileName + " error: " + FileOp.e());
                        failFiles.add(fileName);
                    }
                    ++current;
                    updateNotification();
                }
                if (cancel) break;
            }
            HiLishi.end();

            SafeTeApi.instance(this).notifyDataSetChanged();
            NorApi.instance(this).notifyDataSetChanged();

            if (fileNames.size() > 0) {
                scannerCallback.addCount(fileNames.size());
                MediaScannerConnection.scanFile(Application.getContext(), fileNames.toArray(new String[fileNames.size()]), null, scannerCallback);
                fileNames.clear();
            }

            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stop) {
                scannerCallback.requireStop();
                waiting();
            }
        }
    }

    private void deleteThumbnail(ContentResolver resolver, String fileName, long aLong) {
        File f = new File(FileOp.s(fileName, true));
        f.delete();
    }

    void startForeGround() {
        Bitmap largeIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.suo_ic)).getBitmap();
        notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(largeIcon)
                .setOngoing(true)
                .setContentTitle(getString(getTitle()))
                .setTicker(getString(getTitle()));
        manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
    }

    boolean normal;

    private int getTitle() {
        return normal ? R.string.suo_en_move_in : R.string.suo_dec_title;
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager manager;

    void updateNotification() {
        notificationBuilder.setContentText(current + "/" + total).setProgress(total, current, false);
        manager.notify(1, notificationBuilder.build());
        try {
            if (client != null) {
                Message msg = Message.obtain();
                msg.what = MSG_UPDATE_PROGRESS;
                msg.arg1 = current;
                msg.arg2 = (currentFolderIdx << 24) | total;
                client.send(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        client = null;
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public int total;
    public int current;
    boolean[] selects;
    ArrayList<String> failFiles = new ArrayList<>();

    void handleNormalFile(PaperEntry entry, boolean stop) {
        try {
            current = 0;
            startForeGround();
            ContentResolver resolver = getContentResolver();
            ArrayList<Long> deleteIds = new ArrayList<>();
            ArrayList<String> files = entry.getFiles();
            PaperEntry safeEntry = SafeTeApi.instance(this).getFolder(fileType, entry.bucketId);
            HiLishi.begin();
            for (int i = selects.length - 1; i >= 0; --i) {
                if (selects[i]) {
                    String fileName = files.get(i);
                    long id = entry.getFileId(i);
                    saveThumbnail(resolver, entry.fileType, fileName, id);
                    if (FileOp.e(fileName, entry.fileType)) {
                        deleteIds.add(id);
                        safeEntry.addFile(fileName);
                        files.remove(i);
                        entry.getFileIds().remove(i);
                    } else {
                        //文件不存在，则删除记录
                        int e = FileOp.e();
                        if (e == FileOp.ERROR_NOT_FOUND) {
                            deleteIds.add(id);
                            files.remove(i);
                            entry.getFileIds().remove(i);
                        } else if (e == FileOp.ERROR_RENAME_FAILS) {
                            failFiles.add(fileName);
                        }
                    }
                    ++current;
                    updateNotification();
                }
                if (cancel) break;
            }
            HiLishi.end();
            SafeTeApi.instance(this).notifyDataSetChanged();
            if (entry.count() == 0) {
                NorApi.instance(this).getFolders().remove(entry);
            }

            final int length = deleteIds.size();
            final int segmentLength = 20;
            int segments = (int) Math.ceil(deleteIds.size() / 20.0);
            Uri uri = FileTy.getUri(entry.fileType);
            if (uri != null) {
                for (int i = 0; i < segments; ++i) {
                    int end = (i + 1) * segmentLength;
                    if (end > length) end = length;
                    end -= 1;
                    StringBuilder sb = new StringBuilder(Baseline.LEFT_PARENTHESIS);
                    for (int j = i * segmentLength; j < end; ++j) {
                        sb.append(deleteIds.get(j)).append(Baseline.DOT);
                    }
                    sb.append(deleteIds.get(end)).append(Baseline.RIGHT_PARENTHESIS);
                    resolver.delete(uri, Baseline._ID + Baseline.IN + sb.toString(), null);
                }
            }
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stop) {
                onFinished();
                stopMe();
            }
        }
    }

    public static void saveImageToSDCard(String fileName, Bitmap bmp) {
        try {
            File f = new File(fileName);
            FileOutputStream stream = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 50, stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveThumbnail(ContentResolver resolver, int fileType, String fileName, long id) {
        Bitmap bmp = TuMaster.getImage(fileName);
        if (bmp != null && !bmp.isRecycled()) {
            saveImageToSDCard(FileOp.s(fileName, true), bmp);
        } else {
            switch (fileType) {
                case FileTy.TYPE_PIC:
                    bmp = getImageThumbnail(resolver, id);
                    break;

                case FileTy.TYPE_VIDEO:
                    bmp = getVideoThumbnail(resolver, id);
                    break;
            }
            if (bmp != null) {
                saveImageToSDCard(FileOp.s(fileName, true), bmp);
                bmp.recycle();
            } else {
                Utils.LOGE("fails save thumbnail " + fileName);
            }
        }
    }

    /**
     * <pre>
     * <b>Design</b>
     * 1，隐藏文件
     *      生成缩略图
     *      正式隐藏
     * 2，更新目录信息
     *      更新保护目录
     *          添加到保护目录
     *          更新保护目录的预览图
     *      更新当前目录
     *          从当前目录删除
     *          更新当前目录的预览图
     * 3，更新文件信息
     *      添加保护文件的信息
     *      删除普通文件的信息
     * </pre>
     */
    static boolean handleSingleNormalFile(PaperEntry currentFolder, String currentFile, long currentFileId) {
        ContentResolver resolver = Application.getContext().getContentResolver();
        saveThumbnail(resolver, currentFolder.fileType, currentFile, currentFileId);
        if (FileOp.e(currentFile, currentFolder.fileType)) {
            PaperEntry safeEntry = SafeTeApi.instance(Application.getContext()).getFolder(currentFolder.fileType, currentFolder.bucketId);
            safeEntry.addFile(currentFile);
            HiLishi.begin();
            HiLishi.addHistory(currentFolder.fileType, currentFile);
            HiLishi.end();
            currentFolder.removeFile(currentFile);
            if (currentFolder.count() == 0) {
                NorApi.instance(Application.getContext()).getFolders().remove(currentFolder);
            }
            resolver.delete(FileTy.getUri(currentFolder.fileType), Baseline._ID + Baseline.EQU + currentFileId, null);
            return true;
        } else {
            Toast.makeText(Application.getContext(), R.string.suo_lock_b, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * <pre>
     * <b>Design</b>
     *  1，显示文件
     *      正式显示文件
     *      删除缩略图
     *  2，更新目录信息
     *      更新当前目录信息
     *      更新普通目录信息
     *  3，更新文件信息
     *      删除保护文件的信息
     *      添加普通文件信息
     * </pre>
     */
    static boolean handleSingleSafeFile(PaperEntry currentFolder, String currentFile, long currentFileId) {
        if (FileOp.d(currentFile)) {
            new File(FileOp.s(currentFile, true)).delete();
            MediaScannerConnection.scanFile(Application.getContext(), new String[]{currentFile}, null, null);
            NorApi.instance(Application.getContext()).notifyDataSetChanged();
            currentFolder.removeFile(currentFile);
            SafeTeApi.instance(Application.getContext()).notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    /**
     * <pre>
     * <b>Design</b>
     *  1，隐藏文件
     *  2，更新保护的目录信息
     *      更新该目录下的保护文件数量
     *  3，更新保护的文件信息
     *      添加到保护数据表中
     * </pre>
     *
     * @param currentFile
     */
    static void handleSingleNormalFile(File currentFile) {
//        if (FileOp.e(currentFile.getAbsolutePath(), FileTy.TYPE_COMMON)) {
//            PaperEntry folder = new PaperEntry();
//            Filety file = currentFile.getParentFile();
//            folder.fileType = FileTy.TYPE_COMMON;
//            folder.bucketName = file.getName();
//            folder.bucketUrl = file.getAbsolutePath() + "/";
//            folder.bucketId = file.toString().toLowerCase().hashCode() + "";
//            folder.count = 1;
//
//            SQLiteDatabase db = singleton(Application.getContext()).getWritableDatabase();
//            String tableName = PaperEntry.bucketToTableName(folder.bucketId, FileTy.TYPE_COMMON);
//            FileEntry.createTable(db, tableName);
//            FileEntry.addFile(db, tableName, currentFile.getName());
//
//            MSafeFolder.addFolder(folder);
//        }
    }

    void handleNormalFiles(File root, boolean[] selects) {
        try {
            current = 0;
            startForeGround();

            handleNormalRoot(root, selects);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            onFinished();
            stopMe();
        }
    }

    private void handleNormalRoot(File root, boolean[] selects) {
        List<File> files = MyCorporFile.getFiles(root);
        List<String> fileNames = new ArrayList<>();
        for (int i = 0; i < selects.length; ++i) {
            if (selects[i]) {
                File file = files.get(i);
                if (file.isDirectory()) {
                    ++currentFolderIdx;
                    try {
                        handleNormalRoot(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String fileName = file.getAbsolutePath();
                    if (FileOp.e(fileName, FileTy.TYPE_COMMON)) {
                        fileNames.add(fileName);
                    }
                    ++current;
                    updateNotification();
                }
            }
        }
        try {
            updateSafeFile(root, fileNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNormalRoot(File root) throws Exception {
        List<File> files = MyCorporFile.getFiles(root);
        List<String> fileNames = new ArrayList<>();
        for (int i = files.size() - 1; i >= 0; --i) {
            File f = files.get(i);
            if (f.isDirectory()) {
                break;
            } else {
                String fileName = f.getAbsolutePath();
                if (FileOp.e(fileName, FileTy.TYPE_COMMON)) {
                    fileNames.add(fileName);
                }
                ++current;
                updateNotification();
            }
        }
        updateSafeFile(root, fileNames);
    }

    private void updateSafeFile(File root, List<String> fileNames) throws Exception {
        if (fileNames.size() > 0) {
            PaperEntry folder = SafeTeApi.instance(this).getFolder(WenjianType.TYPE_COMMON, root.getAbsolutePath());
            folder.getFiles().addAll(fileNames);
            SafeTeApi.instance(this).notifyDataSetChanged();
        }
    }

    public static Bitmap getVideoThumbnail(ContentResolver r, long id) {
        return MediaStore.Video.Thumbnails.getThumbnail(r, id, MediaStore.Video.Thumbnails.MINI_KIND, null);
    }

    public static Bitmap getImageThumbnail(ContentResolver r, long id) {
        return MediaStore.Images.Thumbnails.getThumbnail(r, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
    }
}
