package com.example.commenlibary;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ShareprefrencesUitls {

	
	public static void savedata(Context context, String key, String values) {
		SharedPreferences share = context.getSharedPreferences("config",
				Context.MODE_WORLD_WRITEABLE);
		Editor editor = share.edit();
		editor.putString(key, values);
		editor.commit();
	}

	public static String getShareData(Context context, String key) {
		SharedPreferences share = context.getSharedPreferences("config",
				Context.MODE_WORLD_WRITEABLE);
		String values = share.getString(key, "");
		return values;
	}

	public static void delShareData(Context context, String key) {
		SharedPreferences share = context.getSharedPreferences("config",
				Context.MODE_WORLD_WRITEABLE);
		Editor editor = share.edit();
		editor.remove("key");
		editor.commit();
	}
	
	public static void saveShareDataBoolean(Context context, String key, boolean values) {
		SharedPreferences share = context.getSharedPreferences("config",
				Context.MODE_WORLD_WRITEABLE);
		Editor editor = share.edit();
		editor.putBoolean(key, values);
		editor.commit();
	}
	
	public static boolean getShareDataBoolean(Context context, String key) {
		SharedPreferences share = context.getSharedPreferences("config",
				Context.MODE_WORLD_WRITEABLE);
		boolean values = share.getBoolean(key, false);
		return values;
	}
	
	public static void saveShareDataInt(Context context, String key, int values) {
		SharedPreferences share = context.getSharedPreferences("config",
				Context.MODE_WORLD_WRITEABLE);
		Editor editor = share.edit();
		editor.putInt(key, values);
		editor.commit();
	}
	
	public static int getShareDataInt(Context context, String key) {
		SharedPreferences share = context.getSharedPreferences("config",
				Context.MODE_WORLD_WRITEABLE);
		int values = share.getInt(key, 1);
		return values;
	}
}
