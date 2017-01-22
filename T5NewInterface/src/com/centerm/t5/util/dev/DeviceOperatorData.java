package com.centerm.t5.util.dev;

import android.R.integer;

public class DeviceOperatorData {

	public final static int CONNECTSUCCESS 	= 0;//连接成功
	public final static int CONNECTFAIL    	= -1;//连接失败
	public final static int CONNECTCANCEL  	= -2;//断开连接
	public final static int NODEVICE    	= -3;//没有蓝牙
	public final static int MESSAGE 		= 1000;
	public final static int READ_FAIL 	 	= 1001;

	public final static int OPEN_FINANCIAL 	 = 0;
	public final static int CLOSE_FINANCIAL  = 1;
	public final static int READ_FINANCIAL 	 = 2;
	public final static int SWITCH			 = 3;

	public static final int BLUETOOTH 		= 1;
	public static final int HID		  		= 2;
	public static final int BLE				= 3;

	public final static int IDCARD 			= 5;//二代证
	public final static int ICCARD1         = 1;//接触式
	public final static int ICCARD2         = 2;//射频卡
	public final static int MAGCARD1        = 3;//磁卡
	public final static int FINGER         	= 4;//指纹
	public final static int KEYPAD			= 6;//键盘
	public final static int SIGN			= 7;//手写签名
	public final static int MAGCARD2        = 11;//磁卡
	public final static int MAGCARD3        = 12;//磁卡

	public final static int JRZIDCARD 		= 13;//T5金融展 二代证
	public final static int JRZICCARD 		= 14;//T5金融展 IC卡
	public final static int JRZMAGCARD 		= 16;//T5金融展 磁卡
	
	/*****************************ble*******************************/
	public final static int IDCARD_BLE 		= 20;//身份证 ble
}
