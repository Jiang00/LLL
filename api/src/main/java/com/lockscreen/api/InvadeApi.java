package com.lockscreen.api;

import android.graphics.Bitmap;
import android.util.Log;


import com.lockscreen.api.module.InvadeEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by song on 15/8/18.
 */
public class InvadeApi {

    public static ArrayList<InvadeEntry> getIntruders() {
        File intruderDir = makeDirValid();

        ArrayList<InvadeEntry> intruders = new ArrayList<>();
        String[] files = intruderDir.list();
        if (files != null) {
//            DateFormat df = DateFormat.getDateTimeInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat cf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            long now = System.currentTimeMillis();
            int count = files.length;
            for (String file : files) {

                InvadeEntry entry = new InvadeEntry();
                String date = file.substring(file.lastIndexOf('_') + 1);

                String pkg = file.substring(file.indexOf('_') + 1, file.lastIndexOf('_'));
                file = intruderDir.getAbsolutePath() + "/" + file;
                long time = Long.parseLong(date);
                //如果数量超过12个，并且时间已经超过10天，则直接删除
                if (count > 12) {
                    if (now - time > 864000000L) {
                        --count;
                        deleteIntruder(file);
                        continue;
                    }
                }
                entry.date = df.format(new Date(time));
//                Log.i("aaa",entry.date+"--2222222");
                Log.i("name",file+"--22222");
                entry.simdate=cf.format(new Date(time));
                entry.url = file;
                entry.pkg = pkg;
                entry.lastModified= Long.valueOf(date);
                intruders.add(entry);
            }
            Collections.sort(intruders,new FileCop());
//            Collections.reverse(intruders);
        }

        return intruders;
    }

    private static File makeDirValid() {
        File intruderDir = new File(FileOp.ROOT + "ic/");
        if (!(intruderDir.exists() && intruderDir.isDirectory())) {
            intruderDir.delete();
            intruderDir.mkdirs();
        }
        return intruderDir;
    }

    public static boolean deleteIntruder(String url) {
        return new File(url).delete();
    }

    public static boolean deleteIntruder(InvadeEntry intruder) {
        return new File(intruder.url).delete();
    }

    public static boolean addIntruder(Bitmap bmp, String pkg) {
        try {
            makeDirValid();
            FileOutputStream fos = new FileOutputStream(new File(FileOp.ROOT + "ic/intruder_" + pkg + "_" + System.currentTimeMillis()));
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            bmp.recycle();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


    class FileCop implements Comparator<InvadeEntry> {
    public int compare(InvadeEntry file1, InvadeEntry file2) {
        if(file1.lastModified > file2.lastModified)
        {
            return -1;
        }else
        {
            return 1;
        }
    }
    }
