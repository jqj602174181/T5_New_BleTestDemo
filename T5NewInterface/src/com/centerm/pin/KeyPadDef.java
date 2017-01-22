package com.centerm.pin;

public class KeyPadDef {

	
	//读写操作结果
	//错误返回值
		public static final  int   ERR_SUCCESS        =  (0);//成功
		public static final  int  ERR_OPENFAILED      =  (-1);//打开设备失败
		public static final  int  ERR_SEND            =  (-2);//发送指令失败----与设备通讯失败
		public static final  int   ERR_READ			 =	(-3);//接收数据失败----与设备通讯失败
		public static final  int  ERR_PACKAGE_FORMAT	 =	(-4);//接收到的报文格式错误
		public static final  int  ERR_READ_CARD		 =	(-5);//读操作失败
		public static final  int  ERR_WRITE_CARD		 =	(-6);//写操作失败
		public static final  int  ERR_CANCELED        =   (-7);//客户取消输入密码
		public static final  int  ERR_TIMEOUT         =   (-8);//超时
		public static final  int  ERR_PARAM			 =	 (-9); //参数错误
		public static final  int  ERR_DIFFERENT       =   (-10);//两次输入密码不一致
		public static final  int  ERR_OTHER			 =	(-100);//其他错误
}
