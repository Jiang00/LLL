package com.suo.applock.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.suo.theme.SuoPatternView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuoPatternUtils {

    //private static final String TAG = "SuoPatternUtils";
    private final static String KEY_LOCK_PWD = "lock_pwd";


    private static Context mContext;

    private static SharedPreferences preference;

    //private final ContentResolver mContentResolver;

    public SuoPatternUtils(Context context) {
        mContext = context;
        preference = PreferenceManager.getDefaultSharedPreferences(mContext);
        // mContentResolver = context.getContentResolver();
    }

    /**
     * Deserialize a pattern.
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<SuoPatternView.Cell> stringToPattern(String string) {
        List<SuoPatternView.Cell> result = new ArrayList<SuoPatternView.Cell>();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(SuoPatternView.Cell.of(b / 3, b % 3));
        }
        return result;
    }

    /**
     * Serialize a pattern.
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<SuoPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            SuoPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return Arrays.toString(res);
    }

    public void saveLockPattern(List<SuoPatternView.Cell> pattern){
        Editor editor = preference.edit();
        editor.putString(KEY_LOCK_PWD, patternToString(pattern));
        editor.commit();
    }

    public String getLockPaternString(){
        return preference.getString(KEY_LOCK_PWD, "");
    }

    public boolean checkPattern(List<SuoPatternView.Cell> pattern) {
        String stored = getLockPaternString();
        if(stored.length() > 0){
            return stored.equals(patternToString(pattern));
        } else return false;
    }

    public static boolean checkPattern(List<SuoPatternView.Cell> p1, List<SuoPatternView.Cell> p2)
    {
        return patternToString(p1).equals(patternToString(p2));
    }


    public void clearLock() {
        saveLockPattern(null);
    }


}
