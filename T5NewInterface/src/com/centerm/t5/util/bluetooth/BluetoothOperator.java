package com.centerm.t5.util.bluetooth;


import com.centerm.device.CommService;
import com.centerm.t5.util.dev.DeviceOperatorData;

import android.content.Context;
import android.os.Handler;
import android.util.Log;


public class BluetoothOperator {

	private Handler operatorHandler;
	private OperatorDataThread operatorDataThread ;
	private CheckBluetoothConnectThread checkBluetoothConnectThread;
	private String remoteMac;
	private boolean isConnect;

	private Context context;

	private static BluetoothOperator instance;

	/*
	 * 用于连接蓝牙与判断蓝牙通信是否连接着
	 */

	private BluetoothOperator(){

	}

	public static BluetoothOperator getInstance(){
		if(instance == null){
			instance = new BluetoothOperator();
		}
		return instance;
	}

	public void setHandler(Handler handler){
		this.operatorHandler = handler;
		if(operatorDataThread != null){
			operatorDataThread.setHandler(operatorHandler);
		}
	}

	public void setContext(Context context){
		this.context = context;
	}

	public boolean getIsConnect()
	{
		return isConnect;
	}
	private class CheckBluetoothConnectThread extends Thread{
		private boolean isStop = false;
		public void run()
		{
			super.run();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//	CommService.getInstance().switchDevice(CommService.BLUETOOTH);
			if(CommService.getInstance().isConnect())
			{
				Log.e( "Dev","close last connect");
				//sendHandlerMsg(DeviceOperatorData.CONNECTSUCCESS);
				CommService.getInstance().closeDevice();
			}
			boolean isOpen = CommService.getInstance().openDevice(context,remoteMac);
			if(!isOpen){
				sendHandlerMsg(DeviceOperatorData.CONNECTFAIL);
				return;
			}

			isConnect = CommService.getInstance().connectDevice();
			if(!isConnect){
				/*
				 * 连接失败
				 */

				Log.e("Dev","CheckBluetoothConnectThread: connect fail" );
				sendHandlerMsg(DeviceOperatorData.CONNECTFAIL);
				return ;

			}else{
				/*
				 * 连接成功
				 */
				Log.e("Dev","CheckBluetoothConnectThread: connect success" );
				sendHandlerMsg(DeviceOperatorData.CONNECTSUCCESS);
			}
			/*
			 * 连着成功后开启读写线程
			 */
			operatorDataThread = new OperatorDataThread(operatorHandler);
			operatorDataThread.start();


			checkConnect();
			CommService.getInstance().closeDevice();

		}



		/*
		 * 检测 是否有连接着
		 */
		private void checkConnect()
		{

			while (!isStop) {
				isConnect = CommService.getInstance().isConnect(); 
				int status = CommService.getInstance().getState();

				if(!isConnect||status != 0 ){
					quitBluetooth();
					Log.e( "Dev", "checkConnect: isConnect =" +isConnect + ",status" + status );
					if(!isStop){
						operatorHandler.sendEmptyMessage(DeviceOperatorData.CONNECTCANCEL);
					}

					break;
				}

				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		@Override
		public void interrupt() {
			// TODO Auto-generated method stub
			isStop = true;
			super.interrupt();
			CommService.getInstance().setObject(true);

			isConnect = false;
		}


		public void quitCheckThread()
		{
			interrupt();
			if(operatorDataThread!=null)
				operatorDataThread.quitThread();

		}

	}
	/*
	 * 
	 */
	public void startBluetooth()
	{
		quitBluetooth();
		checkBluetoothConnectThread = new CheckBluetoothConnectThread();
		checkBluetoothConnectThread.start();

	}

	public void quitBluetooth()
	{
		if(checkBluetoothConnectThread!=null){
			checkBluetoothConnectThread.quitCheckThread();
			checkBluetoothConnectThread = null;
		}
	}


	/*
	 * 用于向主线程发送传输结果 
	 */
	private void sendHandlerMsg(int msg)
	{
		if(operatorHandler!=null){
			operatorHandler.sendEmptyMessage(msg);
		}
	}
	/*
	 * 
	 */
	public void sendMessage(int msg,int style)
	{
		if(!isConnect)return;
		operatorDataThread.sendMessage(msg,style);
	}
	/*
	 * 
	 */
	public void sendMessage(int msg,int style,Object object)
	{
		if(!isConnect)return;
		operatorDataThread.sendMessage(msg,style,object);
	}
	/*
	 * 
	 */
	public void sendMessage(int msg)
	{
		if(!isConnect)return;
		operatorDataThread.sendMessage(msg);
	}

	public void setBluetoothMac(String mac)
	{
		this.remoteMac = mac;
	}

	/****************************************************************************/
	public CheckBleConnectThread mThread = new CheckBleConnectThread();

	public void checkBleConnect(){
		mThread.start();
	}

	public class CheckBleConnectThread extends Thread{

		private boolean isStop = false;
		
		public CheckBleConnectThread(){
			CommService.getInstance().openDevice(context, null);
		}
		
		@Override
		public void run() {
			while(!isStop){
				isConnect = CommService.getInstance().isConnect();

				if(isConnect){
					/*
					 * 连着成功后开启读写线程
					 */
					operatorDataThread = new OperatorDataThread(operatorHandler);
					operatorDataThread.start();
					
					break;
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void interrupt() {
			isStop = true;
			super.interrupt();
		}
	}
}
