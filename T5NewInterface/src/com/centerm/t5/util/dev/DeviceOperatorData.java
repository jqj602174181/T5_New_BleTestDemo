package com.centerm.t5.util.dev;

import android.R.integer;

public class DeviceOperatorData {

	public final static int CONNECTSUCCESS 	= 0;//���ӳɹ�
	public final static int CONNECTFAIL    	= -1;//����ʧ��
	public final static int CONNECTCANCEL  	= -2;//�Ͽ�����
	public final static int NODEVICE    	= -3;//û������
	public final static int MESSAGE 		= 1000;
	public final static int READ_FAIL 	 	= 1001;

	public final static int OPEN_FINANCIAL 	 = 0;
	public final static int CLOSE_FINANCIAL  = 1;
	public final static int READ_FINANCIAL 	 = 2;
	public final static int SWITCH			 = 3;

	public static final int BLUETOOTH 		= 1;
	public static final int HID		  		= 2;
	public static final int BLE				= 3;

	public final static int IDCARD 			= 5;//����֤
	public final static int ICCARD1         = 1;//�Ӵ�ʽ
	public final static int ICCARD2         = 2;//��Ƶ��
	public final static int MAGCARD1        = 3;//�ſ�
	public final static int FINGER         	= 4;//ָ��
	public final static int KEYPAD			= 6;//����
	public final static int SIGN			= 7;//��дǩ��
	public final static int MAGCARD2        = 11;//�ſ�
	public final static int MAGCARD3        = 12;//�ſ�

	public final static int JRZIDCARD 		= 13;//T5����չ ����֤
	public final static int JRZICCARD 		= 14;//T5����չ IC��
	public final static int JRZMAGCARD 		= 16;//T5����չ �ſ�
	
	/*****************************ble*******************************/
	public final static int IDCARD_BLE 		= 20;//���֤ ble
}
