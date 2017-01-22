package com.centerm.device.usbhid;

import java.util.HashMap;

import com.centerm.device.DeviceIntf;
import com.centerm.device.ResultCode;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class Device_HID implements DeviceIntf
{
	private String TAG = "hid";
	
	private static final String tg="dv";
	public static final int VendorID = 0x2B46; //T5 �򿪵���18D1 �رյ���2B46
//	public static final int ProductID = 0xBB01;//T5 �򿪵���D003 �رյ���BC01 //CR200��BE01
	public static final int ProductID = 0xBE01;//T5 �򿪵���D003 �رյ���BC01 //CR200��BE01
//	public static final int VendorID = 0x18D1; //T5 �򿪵���18D1 �رյ���2B46
//	public static final int VendorID = 0x18D1; //T5 �򿪵���18D1 �رյ���2B46
//	public static final int ProductID = 0xD003;//T5 �򿪵���D003 �رյ���BC01 //CR200��BE01
	private static final String ACTION_USB_PERMISSION ="com.android.example.USB_PERMISSION";
	private UsbManager usbManager;
	private UsbDevice usbDevice;
	private UsbInterface usbInf;
	private UsbEndpoint epOut, epIn;
	private UsbDeviceConnection deviceConnection;
	private boolean isConnected = false;
	private Context context;
	protected boolean isQuitRead;
	private int timeOut = 2;
	
	private UsbReceiver usbReceiver;
	public Device_HID() {
	//	this.context = context;
		usbReceiver = null;
		
	}

	
	
	private class UsbReceiver extends BroadcastReceiver{

		private Context context;
		private boolean isRegist = false;
		public UsbReceiver(Context context)
		{
			this.context = context;
			//Log.e("br","recvicer");
		}
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			 String action = intent.getAction();
		       //Log.e(TAG,"action is "+action);
		       if (ACTION_USB_PERMISSION.equals(action)) {
		           synchronized (this) {
		        	   UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		               if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
		            	   usbDevice = device;
		               } 
		               else {
		                  // Log.d(TAG, "permission denied for device " + usbDevice);
		               }
		           }
		       }
		}
		
		public void registBroadcast()
		{
			//Log.e("br","recvicer is regiths is "+isRegist);
			if(!isRegist){
				IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
				context.registerReceiver(this, filter);
				isRegist  = true;
			}
		}
		
		public void unregistBroadcast()
		{
			if(isRegist){
				isRegist  = false;
				context.unregisterReceiver(this);
			}
		}
		
	}
	
	@Override
	public boolean openDevice(Context context,String addr) {
		// TODO Auto-generated method stub
		
		Log.e("Dev", "openDevice:Device_HID");
		this.context = context;
		if(usbReceiver==null){
		//
		//	usbReceiver = null;
			usbReceiver = new UsbReceiver(context);
			
		}else{
			usbReceiver.unregistBroadcast();
		}
		
		usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
		int ret = 0;
		ret = findUsbDevice();
		if (ret != 0)
		{
			return false;
		}

		ret = findInterface();
		Log.i(TAG, "2. findInterface �� " + ret);
		if (ret != 0)
		{
			return false;
		}

		
		
		isConnected = true;
		return true;
	}

	@Override
	public boolean isConnect() {
		// TODO Auto-generated method stub
		return isConnected;
	}
	private int connectHidDevice()
	{
		int ret = 0;
		UsbDeviceConnection connection = null;
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		usbReceiver.registBroadcast();
		//����requestPermission()��������ʾ��������豸��Ȩ�޵ĶԻ���
		usbManager.requestPermission(usbDevice, mPermissionIntent);

		// ��ʱ��ûȨ�޾ͷ���
		long startTime = System.currentTimeMillis();
		while(true){
			if ((System.currentTimeMillis() - startTime) > 20 * 1000)
			{
				//Log.e("Dev","connectHidDevice: 1" );
				return ResultCode.USB_NO_PERMISSION;
			}
			if (usbManager.hasPermission(usbDevice)){
				break;
			}
		}

		// ���豸����ȡ UsbDeviceConnection���������豸�����ں����ͨѶ
		connection = usbManager.openDevice(usbDevice);
		if (connection == null)
		{
			//Log.e("Dev","connectHidDevice: 2" );
			//Log.e(TAG, "�豸����ʧ��");
			return ResultCode.USB_CONNECT_FAIL;
		}

		if (connection.claimInterface(usbInf, true))
		{
			//Log.e("Dev","connectHidDevice: 3" );
			//Log.e(TAG, "�����豸�ɹ�");
			deviceConnection = connection;//����android�豸�Ѿ�����HID�豸
			ret = getEndpoint(deviceConnection, usbInf);
		}
		else
		{
			//Log.e("Dev","connectHidDevice: 4" );
			connection.close();
			ret = ResultCode.USB_CLAIMED_FAILED;
		}

		return ret;
	}

	@Override
	public boolean connectDevice() {
		// TODO Auto-generated method stub
		int ret = connectHidDevice();
	    Log.i(TAG, "3. connectDevice �� " + ret);
		if (ret != 0)
		{
			return false;
		}
		
		return true;
	}

	@Override
	public void closeDevice() {
		// TODO Auto-generated method stub
		usbReceiver.unregistBroadcast();
		//Log.i(TAG,"�ر�HID����");
		if (deviceConnection != null)
		{
			deviceConnection.releaseInterface(usbInf);
			deviceConnection.close();
			isConnected = false;
		}
	
	}
	/* ����USB�豸 */
	private int findUsbDevice()
	{
		int ret = 0;

		if (usbManager == null)
		{
			//Log.e(TAG, "û���豸������");
			return ResultCode.USBMANAGER_IS_NULL;
		}

		HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
		if (deviceList.isEmpty())
		{
			//Log.e(TAG, "�豸��Ϊ��");
			return ResultCode.DEVICE_LIST_NULL;
		}

		usbDevice = null;
		for (UsbDevice device : deviceList.values())
		{
			//Log.e(TAG,"VendorID is "+Integer.toHexString(device.getVendorId())+" ProductID is "+Integer.toHexString(device.getProductId()));
			if (device.getVendorId() == VendorID
					&& device.getProductId() == ProductID)
			{
				usbDevice = device;
				if (usbDevice == null)
				{
					Log.i(TAG, "û�ҵ��豸");
					return ResultCode.USB_FIND_DEVICE_FAIL;
				}
			}
		}

		return ret;
	}

	/* ��ȡ�ӿ� */
	private int findInterface()
	{
		int ret = 0;
		if (usbDevice == null)
		{
			return ResultCode.USB_FIND_DEVICE_FAIL;
		}
		
		for (int i = 0; i < usbDevice.getInterfaceCount(); i++)
		{
			// ��ȡ�豸�ӿڣ�һ�㶼��һ���ӿڣ�����Դ�ӡgetInterfaceCount()�����鿴��
			// �ڵĸ�����������ӿ����������˵㣬OUT �� IN
			Log.i(TAG,"�ӿ�������" + usbDevice.getInterfaceCount());
			UsbInterface intf = usbDevice.getInterface(i);
			if (intf != null){
				Log.i(TAG,"intf.getInterfaceClass():" + intf.getInterfaceClass() + " intf.getInterfaceSubclass():"+intf.getInterfaceSubclass()  + " intf.getInterfaceProtocol():"+ intf.getInterfaceProtocol());
				usbInf = intf;
				return ret;
			}
			if (intf == null)
			{
				Log.i(TAG, "û�ҵ��豸�ӿ�");
				return ResultCode.USB_FIND_INTERFACE_FAIL;
			}
		}
		return ret;
	}


	/**
	 * ��ȡ�˵�
	 * 
	 * @param connection
	 * @param intf
	 * @return
	 */
	private int getEndpoint(UsbDeviceConnection connection, UsbInterface intf)
	{
		int ret = 0;

		if (intf.getEndpoint(1) != null) {
			epOut = intf.getEndpoint(1);
		} else {
			//Log.e(TAG, "��ȡ�豸����˵�ʧ��");
			return ResultCode.USB_GET_EPIN_FAIL;
		}

		if (intf.getEndpoint(0) != null) {
			epIn = intf.getEndpoint(0);
		} else {
			//Log.e(TAG, "��ȡ�豸����˵�ʧ��");
			return ResultCode.USB_GET_EPOUT_FAIL;
		}
		
		return ret;
	}
	@Override
	public int writeData(byte[] data, int len) {
		// TODO Auto-generated method stub
		// ���������ж��Ƿ�ɹ�
				int ret = deviceConnection.bulkTransfer(epOut, data,len, timeOut* 1000);
				Log.e(tg,"write ret is "+ret);
				if (data.length != ret)
				{
					Log.e("transfer", "��������ʧ�� :" + ret + " " + data.length + " " + (deviceConnection == null));
					return ResultCode.USB_SEND_MESSSAGE_FAIL;
				} else {
					Log.e("transfer", "�������ݳɹ� :" + ret + " " + data.length + " " + (deviceConnection == null));
				}
				return ret;
	}

	@Override
	public int writeData(byte data) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public int readData(byte[] buffer, int timeOut) {
		// TODO Auto-generated method stub
		//��������ǰ�����ͨ���ŵ�
		
				int dataClearLen = -1;
				int time = timeOut;
			     isQuitRead = false;
				while(true){
					dataClearLen = deviceConnection.bulkTransfer(epIn, buffer, buffer.length, 1000);
					if(dataClearLen>=0){
						Log.e(tg,"readData len is "+dataClearLen);
						break;
					}else{
						//Log.e(tg,"readData timeOut "+dataClearLen);
						
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						time--;
						//break;
					}
					
					if(time==0||isQuitRead){
						break;
					}
					
				}
				
		
				if(dataClearLen<0){
					dataClearLen = 0;
				}else{
					
				}
				return dataClearLen;
				
	}

	@Override
	public void setObject(Object object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void quitRead() {
		// TODO Auto-generated method stub
		isQuitRead = true;
	}
	
}
