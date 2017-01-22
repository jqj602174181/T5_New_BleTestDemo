package com.centerm.finger.wellcom;

public class FingerPrintDef {

	
	public static final  int  ERR_SUCCESS			 =  0; //成功
	public static final  int   ERR_OPENFAILED     = -1;//打开设备失败
	public static final  int   ERR_WRITE			 = -2;//发送指令失败----与设备通讯失败
	public static final  int   ERR_READ			 = -3;//接收数据失败----与设备通讯失败
	public static final  int   ERR_PACKAGE_FORMAT	 = -4;//接收到的报文格式错误
	public static final  int   ERR_READ_CARD	     = -5;//读卡操作失败
	public static final  int   ERR_WRITE_CARD	     = -6;//写卡操作失败
	public static final  int   ERR_CANCELED       = -7;//用户取消
	public static final  int   ERR_TIMEOUT        = -8;//超时
	public static final  int   ERR_OTHER		     = -100;//其他错误


	//使用升腾指令集
	public static final  byte  SOH				= (0x02);
	public static final  byte  EOT			    = (0x03);
	
	
	
	public static final  byte[] COMMAND_GETDEVINFO = { 0x09, 0x00, 0x00, 0x00 }; //获取设备信息
	public static final byte[]  COMMAND_GETENROLL =  {  0x0B, 0x00 ,0x00 , 0x00 } ;//登记指纹信息
	
	public static final byte[] COMMAND_GETFINGRCH =  {  0x0C, 0x00, 0x00, 0x00};
}
