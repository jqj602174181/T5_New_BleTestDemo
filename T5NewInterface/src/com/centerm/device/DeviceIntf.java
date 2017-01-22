package com.centerm.device;

import android.content.Context;

public interface DeviceIntf {

	/*
	 * ���豸
	 */
	public boolean openDevice(Context context,String addr);

	/*
	 * �ж��Ƿ�������
	 */
	public boolean isConnect();
	
	/*
	 * ���������豸
	 */
	public boolean connectDevice();

	/*
	 * �Ͽ����Ӳ��ر�
	 */
	public void closeDevice();
	
	/*
	 * д������
	 */
	public int  writeData(byte[] data,int len);
	/*
	 * д������
	 */
	public int writeData(byte data);
	/*
	 * ��ȡ����
	 */
	public int readData(byte[] buffer,int timeOut);
	
	
	public int getState();
	
	public void quitRead();
	public void setObject(Object object);
}
