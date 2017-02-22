package com.suo.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Environment;

public class StoreUtil {
	public static boolean isExternalStorageAvaliable() {
		boolean avaliable = false;
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			if (Environment.getExternalStorageDirectory().canWrite()) {
				avaliable = true;
			}
		}
		return avaliable;
	}
	
	// ****************** SharedPreference Start ******************//
	private static int _putCount = 0;
	private static SharedPreferences db = null;
	private static Context _context = null;

	private static SharedPreferences getDB() {
		if (db == null && _context != null)
			db = _context.getSharedPreferences("_DB_", 0);
		return db;
	}
	
	public static void initDB(Context context) {
		if(context != null)
			_context = context.getApplicationContext();
	}

	public static void saveDB() {
		SharedPreferences db = getDB();
		if (db != null) {
			saveDB(db.edit(), true);
		}
	}
	
	private static void saveDB(Editor e, boolean force) {
		if (e != null) {
			if (force) {
				e.commit();
				_putCount = 0;
			} else {
				if (_putCount++ >= 9) {
					e.commit();
					_putCount = 0;
				} else {
					if (Build.VERSION.SDK_INT >= 9) {
						e.apply();
					} else {
						e.commit();
						_putCount = 0;
					}
				}
			}
		}
	}

	public static <T> void putDB(String key, T value) {
		try {
			SharedPreferences db = getDB();
			if (db != null) {
				Editor e = db.edit();
				if (value instanceof String) {
					e.putString(key, (String) value);
				} else if (value instanceof Integer) {
					e.putInt(key, (Integer) value);
				} else if (value instanceof Boolean) {
					e.putBoolean(key, (Boolean) value);
				} else if (value instanceof Long) {
					e.putLong(key, (Long) value);
				} else if (value instanceof Float) {
					e.putFloat(key, (Float) value);
				}
				saveDB(e, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean removeDB(String key) {
		try {
			SharedPreferences db = getDB();
			if (db != null) {
				Editor e = db.edit();
				e.remove(key);
				saveDB(e, false);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean hasDB(String key) {
		SharedPreferences db = getDB();
		if (db != null) {
			return db.contains(key);
		}
		return false;
	}

	public static int getDB(String key, int defValue) {
		SharedPreferences db = getDB();
		if (db != null) {
			if (db.contains(key)) {
				return db.getInt(key, defValue);
			} else {
				db.edit().putInt(key, defValue).commit();
			}
		}
		return defValue;
	}

	public static long getDB(String key, long defValue) {
		SharedPreferences db = getDB();
		if (db != null) {
			if (db.contains(key)) {
				return db.getLong(key, defValue);
			} else {
				db.edit().putLong(key, defValue).commit();
			}
		}
		return defValue;
	}

	public static String getDB(String key, String defValue) {
		SharedPreferences db = getDB();
		if (db != null) {
			if (db.contains(key)) {
				return db.getString(key, defValue);
			} else {
				db.edit().putString(key, defValue).commit();
			}
		}
		return defValue;
	}

	public static boolean getDB(String key, boolean defValue) {
		SharedPreferences db = getDB();
		if (db != null) {
			if (db.contains(key)) {
				return db.getBoolean(key, defValue);
			} else {
				db.edit().putBoolean(key, defValue).commit();
			}
		}
		return defValue;
	}

	// ******************* SharedPreference End *******************//
}
