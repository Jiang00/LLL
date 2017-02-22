package com.privacy.api;

import android.test.ApplicationTestCase;

import com.lockscreen.api.HiLishi;
import com.lockscreen.api.SafeTeApi;
import com.lockscreen.api.liberal.BaseApplication;
import com.lockscreen.api.liberal.Utils;
import com.privacy.model.FileType;
import com.privacy.model.FolderEntry;

import junit.framework.Assert;

import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<BaseApplication> {
    public ApplicationTest() {
        super(BaseApplication.class);
    }

    public void testHistory() throws InterruptedException {
        try {
            testApplicationTestCaseSetUpProperly();
            getApplication().onCreate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals("begin fails", true, HiLishi.begin());
        HiLishi.addHistory(FileType.TYPE_AUDIO, "/sdcard/abc/abc.wav");
        HiLishi.addHistory(FileType.TYPE_AUDIO, "/sdcard/ac/def.wav");
        HiLishi.addHistory(FileType.TYPE_AUDIO, "/sdcard/abc/ghi.wav");
        HiLishi.addHistory(FileType.TYPE_AUDIO, "/sdcard/bc/jkl.wav");
        HiLishi.addHistory(FileType.TYPE_AUDIO, "/sdcard/abc/mno.wav");
        HiLishi.addHistory(FileType.TYPE_AUDIO, "/sdcard/abc/pqr.wav");
        HiLishi.addHistories(FileType.TYPE_DOC, "/sdcard/def/def.abc", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab", "/sdcard/hij/def.ab");
        HiLishi.end();

        SafeTeApi.instance(getContext()).waiting(new Runnable() {
            @Override
            public void run() {
                ArrayList<FolderEntry> folderEntries = SafeTeApi.instance(getContext()).getFolders(FileType.TYPE_DOC);
                for (FolderEntry entry : folderEntries) {
                    Utils.LOGE("--->>>", entry.bucketId + " name " + entry.bucketName);
                    for (String file : entry.getFiles()) {
                        Utils.LOGE("------", file);
                    }
                }
            }
        });

        Assert.assertEquals("not valid", true, HiLishi.isHistoryValid());
        Assert.assertEquals("drop fails", true, HiLishi.dropHistory());
    }
}