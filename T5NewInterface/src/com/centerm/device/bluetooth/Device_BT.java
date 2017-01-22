package com.centerm.device.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.centerm.device.DeviceIntf;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

public class Device_BT implements DeviceIntf
{

	private BluetoothDevice remotedev = null;//远程蓝牙设备
	private BluetoothSocket sock = null;
	private OutputStream out = null;
	private InputStream inputStream;
	private String remoteMac;
	private  boolean isConnect = false;
	protected boolean isQuitRead = false;
	private boolean isExit = false;


	public Device_BT()
	{

	}

	/*
	 * 打开蓝牙设备
	 */
	public boolean openDevice(Context context,String mac)
	{
		Log.e("Dev", "openDevice:Device_BT" );
		this.remoteMac = mac;
		remotedev = getBluetoothDevice(remoteMac);
		if(remotedev!=null){
			return  checkPairing();
		}
		return false;

	}

	/*
	 * 判断是否连接着
	 */
	public boolean isConnect()
	{
		if(sock==null){
			isConnect = false;
			return false;
		}else{
			isConnect = sock.isConnected();
		}

		return isConnect;
	}
	/*
	 * 检测配对
	 */
	private boolean  checkPairing()
	{
		int checkTime = 60;
		isExit = false;
		while(!isExit)
		{

			int status =getState();
			if( status ==0  ){
				//已经配对了
				//if(operatorHandler!=null)
				return true;
			}else{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			checkTime--;
			if(checkTime==0){
				return false;
			}
		}
		return false;
	}
	/*
	 * 在配对和连接时有循环，当响应中断是要退出，在这里设置标志位
	 */
	public void setObject(Object object)
	{
		isExit = (Boolean)object;
	}
	/*
	 * 0代表配对成功
	 */
	public int getState()
	{
		if(remotedev!=null){
			int status = remotedev.getBondState();
			if(status == BluetoothDevice.BOND_BONDED ){
				return 0;
			}
			return status;
		}else{
			return -1;
		}
	}
	/*
	 * 连接蓝牙设备
	 */
	public boolean connectDevice()
	{
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		try {
			sock = remotedev.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(sock==null)return false;

		int time = 10;
		while (!isExit) {
			if(isExit){
				break;
			}
			try {
				sock.connect();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isConnect = sock.isConnected();

			if(isConnect){
				break;
			}else{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			time--;
			if(time==0){
				isConnect = false;
				break;
			}
		}

		if(isConnect){
			try {
				inputStream = sock.getInputStream();
				inputStream.available();

				out = sock.getOutputStream();
				isConnect = true; 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{
			//isConnect = false;
		}
		return isConnect;
	}

	/*
	 * 断开连接并关闭
	 */
	public void closeDevice()
	{

		closeBluetoothDevice();

	}
	public void closeBluetoothDevice()
	{


		if(!isConnect)return;
		if(sock!=null){
			try {
				sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		if(inputStream!=null){
			try {
				synchronized (inputStream) {
					inputStream.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if(out!=null){
			try {
				synchronized (out) {
					out.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		/*
		if(remotedev!=null){
			try {
				Bluetooth.removeBond( remotedev );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		 */
		inputStream = null;
		out= null;
		sock = null;
		isConnect = false;
	}
	/*
	 * 写入数据
	 */
	public int writeData(byte[] data,int len)
	{
		if(isConnect&&out!=null){
			try {
				synchronized (out) {
					out.write(data);

					return len;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (NullPointerException e) {
				// TODO: handle exception
			}

		}

		return -1;
	}
	/*
	 * 写入数据
	 */
	public int writeData(byte data)
	{
		if(isConnect&&out!=null){
			try {
				synchronized (out) {
					out.write(data);
					return 1;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch ( NullPointerException e) {
				// TODO: handle exception
			}
		}

		return -1;
	}
	/*
	 * 读取数据
	 */
	/*public int readData(byte[] buffer,int timeOut)
	{
		isQuitRead = false;
		int len = -1;
		int time = timeOut;
		Log.e("BT", "readData:time" +time);
		if(isConnect){
			try {
				synchronized (inputStream) {


				while (inputStream.available()<=0){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if(isExit){
						return -1;
					}
					time--;
					Log.e("BT", "time = in while");
					if(time==0||isQuitRead){
						Log.e("BT", "isQuitRead" + isQuitRead);
						Log.e("BT", "time" +time);

						return -2;
					}
				}
				len = inputStream.read(buffer);

				return len;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		//		 closeBluetoothDevice();
			}catch (NullPointerException e) {
				// TODO: handle exception
			}
		}


		return len;
	}
	 */


	public int readData(byte[] buffer,int timeOut)
	{
		isQuitRead = false;
		int len = -1;
		int time = timeOut;
		//Log.e("BT", "readData:time" +time);
		if(isConnect){
			try {
				synchronized (inputStream) {


					while (inputStream.available()<=0){
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if(isExit){
							return -1;
						}
						time -=10;
						//Log.e("BT", "time = in while"+ time);
						if(time==0||isQuitRead){
							//Log.e("BT", "isQuitRead" + isQuitRead);
							//Log.e("BT", "time" +time);

							return -2;
						}
					}
					len = inputStream.read(buffer);

					return len;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//		 closeBluetoothDevice();
			}catch (NullPointerException e) {
				// TODO: handle exception
			}
		}


		return len;
	}

	//获取蓝牙设备
	private static  BluetoothDevice  getBluetoothDevice( String remoteMac )
	{
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();	
		BluetoothDevice dev = null;

		if( adapter != null )//硬件支持蓝牙功能
		{
			if( !adapter.isEnabled() )//开启蓝牙
			{
				adapter.enable();
				while(  adapter.getState() == BluetoothAdapter.STATE_TURNING_ON
						|| adapter.getState() != BluetoothAdapter.STATE_ON )
				{
					try {
						Thread.sleep( 100 );
					} catch (Exception e) {
						e.printStackTrace();
					}	    			
				}
			}



			//获取蓝牙地址
			Set<BluetoothDevice> devices = adapter.getBondedDevices(); //获取已经配对的蓝牙设备的集合, 如果蓝牙未被打开, 则返回null;

			if(devices.size()>0){

				Iterator<BluetoothDevice> it = devices.iterator();//从已配对的蓝牙设备集合中选出T5设备
				BluetoothDevice device = (BluetoothDevice)it.next();
				remoteMac = device.getAddress();//获取蓝牙地址
				Log.i("MAC" ,device.getAddress());

			} 

			//获取蓝牙地址失败
			if(remoteMac == null || remoteMac == "")
			{
				return dev;
			}

			dev = adapter.getRemoteDevice(remoteMac);

			if( adapter.isDiscovering() )//取消扫描，否则匹配会不稳定
			{
				adapter.cancelDiscovery();
			}

			//如果已配对，先删除
			int status = dev.getBondState();
			if( status == BluetoothDevice.BOND_BONDED  )//已经配对了
			{
				/*
						try {
							Bluetooth.removeBond( dev );
						} catch (Exception e) {
							e.printStackTrace();
						}*/
				return dev;
			}
			else if( status == BluetoothDevice.BOND_BONDING )//正在配对
			{
				try {
					Bluetooth.cancelBondProcess( dev );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if( dev.getBondState() == BluetoothDevice.BOND_NONE )
			{
				try {
					Bluetooth.createBond( dev );
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}

			//mhandler.sendEmptyMessageDelayed( MSG_DETECT_AGAIN, 200 );//200毫秒后检测是否绑定完成
		}

		return dev;
	}

	@Override
	public void quitRead() {
		// TODO Auto-generated method stub
		isQuitRead = true;
	}

}
