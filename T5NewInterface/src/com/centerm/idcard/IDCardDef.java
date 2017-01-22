package com.centerm.idcard;

//身份证常量
public class IDCardDef {
	//错误返回值
	public static final  int  ERR_SUCCESS = 0;   //成功
	public static final  int  ERR_DEVICE  = -1;    //设备错误
	public static final  int  ERR_RECV    = -2;   //接收错误
	public static final  int  ERR_SEND    = -3;    //发送错误
	public static final  int  ERR_CHECK   = -4;    //校验错误
	public static final  int  ERR_TIMEOUT = -5;    //超时
	public static final  int  ERR_READ    = -6;    //读卡错误
	public static final  int  ERR_IMAGE   = -7;    //解析头像错误
	public static final  int  ERR_SAVEIMG = -8;    //保存图像错误
	public static final  int  ERR_CANCEL  = -9;    //取消操作
	public static final  int  ERR_OTHER   = -100;  //其他错误
		
	//常量——要获取的身份证图片类型定义
	public static final  int  IMG_HEAD     = 0;
	public static final  int  IMG_FRONT	  = 1;
	public static final  int  IMG_BACK	  = 2;
	public static final  int  IMG_IDCARD   = 3;
}
