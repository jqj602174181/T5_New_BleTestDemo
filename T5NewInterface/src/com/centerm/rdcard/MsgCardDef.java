package com.centerm.rdcard;

public class MsgCardDef {
	
	//错误返回值
		public static final  int ERR_SUCCESS         = 0 ;//成功
		public static final  int  ERR_OPENFAILED     = -1;//打开设备失败
		public static final  int  ERR_WRITE          = -2;//发送指令失败----与设备通讯失败
		public static final  int  ERR_READ           = -3;//接收数据失败----与设备通讯失败
		public static final  int  ERR_PACKAGE_FORMAT = -4;//接收到的报文格式错误
		public static final  int  ERR_READ_CARD		= -5;//读卡操作失败
		public static final  int  ERR_WRITE_CARD		= -6;//写卡操作失败
		public static final  int  ERR_CANCELED       =  -7;//用户取消
		public static final  int  ERR_TIMEOUT        =  -8;//超时
		public static final  int  ERR_OTHER			= -100;//其他错误
		
		
		
		
		//使用升腾指令集
		public static final byte SOH				          =(0x01);
		public static final byte EOT			              = (0x04);
		public static final byte[] CENT_COMMAND_SEPARATOR  = {0x20,0x01,0x7C };  //设置分割符,'|'
		public static final byte[] CENT_COMMAND_RD123      = { 0x20,0x02 };  //读磁道123
		public static final byte[] CENT_COMMAND_WT123      ={ 0x20,0x03 };  //写磁道123
		public static final byte[] CENT_COMMAND_TIMEOUT	  ={ 0x20, 0x04 };  //设置超时

		//南天磁卡操作命令
		public static final byte[] COMMAND_RESET        = { 0x1B, 0x30 } ;//复位
		public static final byte[] COMMAND_STATUS       = { 0x1B , 0x6A};//状态查询

		public static final byte[] COMMAND_ISO          = { 0x1B,0x3D };//设置磁道为ISO
		public static final byte[] COMMAND_IBM          = { 0x1B,0x27 }; //设置磁道格式为IBM
		public static final byte[] COMMAND_ISO_D        = { 0x1B,0x4F };//设置磁道格式为ISO(德卡)
		public static final byte[] COMMAND_IBM_D        = { 0x1B, 0x41 };//设置磁道格式为IBM(德卡)

		//南天读卡命令
		public static final byte[] COMMAND_RD1          = { 0x1B, 0x72}; 
		public static final byte[] COMMAND_RD2          = { 0x1B, 0x5D };
		public static final byte[] COMMAND_RD3          = { 0x1B,0x54,0x5D };
		public static final byte[] COMMAND_RD12         = { 0x1B,0x44, 0x5D };
		public static final byte[] COMMAND_RD23         = { 0x1B, 0x42,0x5D }; 

	



}
