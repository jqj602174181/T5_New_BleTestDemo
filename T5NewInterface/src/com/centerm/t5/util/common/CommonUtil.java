package com.centerm.t5.util.common;

import android.R.integer;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class CommonUtil {
	
	public static void showTip(Context context,String text)
	{
		Toast.makeText(context, text,Gravity.CENTER).show();
	}
	
	public static void showTip1(Context context,String text)
	{
		Toast.makeText(context, text,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL).show();
	}
}
