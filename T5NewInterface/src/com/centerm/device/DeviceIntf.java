package com.centerm.device;

import android.content.Context;

public interface DeviceIntf {

	/*
	 * 打开设备
	 */
	public boolean openDevice(Context context,String addr);

	/*
	 * 判断是否连接着
	 */
	public boolean isConnect();
	
	/*
	 * 连接蓝牙设备
	 */
	public boolean connectDevice();

	/*
	 * 断开连接并关闭
	 */
	public void closeDevice();
	
	/*
	 * 写入数据
	 */
	public int  writeData(byte[] data,int len);
	/*
	 * 写入数据
	 */
	public int writeData(byte data);
	/*
	 * 读取数据
	 */
	public int readData(byte[] buffer,int timeOut);
	
	
	public int getState();
	
	public void quitRead();
	public void setObject(Object object);
}
