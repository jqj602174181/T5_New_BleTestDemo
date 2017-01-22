package com.centerm.device;


import com.centerm.blecentral.blelibrary.bluetooth.BLEClient;
import com.centerm.device.bluetooth.Device_BT;
import com.centerm.device.usbhid.Device_HID;
import com.centerm.t5.util.dev.DeviceOperatorData;

import android.content.Context;
import android.util.Log;


public class CommService {
	private Device_BT  device_BT;
	private Device_HID device_HID;
	private BLEClient device_BLE;
	private DeviceIntf device;
	public  static  int type = 1;


	private static CommService commService;

	public static CommService getInstance(){

		synchronized (CommService.class) {
			if(commService==null){
				commService = new CommService();
			}
		}
		return commService;
	}
	private CommService() {
		// TODO Auto-generated constructor stub
		/*
		if(type==BLUETOOTH){//����
			device_BT = new Device_BT();
			device = device_BT;
		}else if(type==HID){//hid
			device_HID = new Device_HID();
			device = device_HID;
		}
		 */

	}


	public void switchDevice(int style)
	{
		if(type==DeviceOperatorData.BLUETOOTH){//����
			if(device_BT==null){
				device_BT = new Device_BT();
			}

			//Log.e("Dev","switchDevice : device_BT");
			device = device_BT;
		}else if(type==DeviceOperatorData.HID){//hid
			if(device_HID==null){
				device_HID = new Device_HID();
			}

			//Log.e("Dev","switchDevice : Device_HID");
			device = device_HID;
		}else if(type==DeviceOperatorData.BLE){
			if(device_BLE==null){
				device_BLE = BLEClient.getInstance();
			}

			//Log.e("Dev","switchDevice : Device_HID");
			device = device_BLE;
		}
	}
	/*
	 * �������豸
	 */
	public boolean openDevice(Context context,String addr)
	{
		switchDevice(type);
		return device.openDevice(context,addr);

	}

	/*
	 * �ж��Ƿ�������
	 */
	public boolean isConnect()
	{

		if( device !=null )
		{
			return device.isConnect();
		}
		else {
			return false;
		}
	}

	public int getState()
	{
		return device.getState();
	}
	/*
	 * ���������豸
	 */
	public boolean connectDevice()
	{

		return device.connectDevice();
	}

	/*
	 * �Ͽ����Ӳ��ر�
	 */
	public void closeDevice()
	{
		device.closeDevice();

	}

	/*
	 * д������
	 */
	public int writeData(byte[] data,int len)
	{
		return device.writeData(data,len);
	}
	/*
	 * д������
	 */
	public int writeData(byte data)
	{
		return device.writeData(data);
	}
	/*
	 * ��ȡ����
	 */
	public int readData(byte[] buffer,int timeOut)
	{

		return device.readData(buffer, timeOut);
	}

	public static  void destroyCommServer()
	{
		commService = null;
	}

	public void setObject(Object object)
	{
		device.setObject(object);
	}

	public void quitRead()
	{
		device.quitRead();
	}
}
