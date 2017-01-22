package com.centerm.t5.util.bluetooth;

import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.FinancialServer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

class OperatorDataThread extends Thread{

	private Handler handler;
	private Handler operatorHandler;
	private FinancialServer financialServer;
	public OperatorDataThread(Handler handler)
	{
		operatorHandler = handler;
		financialServer = new FinancialServer();
	}

	public void setHandler(Handler handler){
		operatorHandler = handler;
	}

	@Override
	public void run() {
		super.run();
		Looper.prepare();
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case DeviceOperatorData.OPEN_FINANCIAL://读取
					//Log.e("Dev", "OperatorDataThread: DeviceOperatorData.OPEN_FINANCIAL");
					financialServer.setStyle(msg.arg1);//设置当前模块
					Object object = financialServer.getFinalcialData(msg.obj);	//获取读到模块的数据
					if(object!=null){//如果可以读取到数据
						Message message = operatorHandler.obtainMessage(DeviceOperatorData.MESSAGE);
						message.obj = object;
						operatorHandler.sendMessage(message);
					}else{//如果读取的数据为空，则读取失败
						operatorHandler.sendEmptyMessage(DeviceOperatorData.READ_FAIL);
					}
					break;
				case DeviceOperatorData.SWITCH:

					break;
				case DeviceOperatorData.CLOSE_FINANCIAL://关闭

					break;
				default:
					break;
				}

			}

		};
		Looper.loop();
	}

	/*
	 * msg用于判断是否是打开0，关闭1，读取2
	 * style用于判断是哪种金融模块操作
	 */
	public void sendMessage(int msg,int style,Object object)
	{
		if(handler!=null){
			Message message = handler.obtainMessage(msg);
			message.arg1 = style;
			message.obj = object;
			handler.sendMessage(message);
		}
	}

	/*
	 * msg用于判断是否是打开0，关闭1，读取2
	 * style用于判断是哪种金融模块操作
	 */
	public void sendMessage(int msg,int style)
	{
		if(handler!=null){
			Message message = handler.obtainMessage(msg);
			message.arg1 = style;
			handler.sendMessage(message);
		}

	}
	/*
	 * msg用于判断是否是打开0，关闭1，读取2
	 *
	 */
	public void sendMessage(int msg)
	{
		if(handler!=null){
			Message message = handler.obtainMessage(msg);
			handler.sendMessage(message);
		}

	}
	/*
	 * 退出消息循环
	 */
	public void quitThread()
	{
		if(handler!=null)
			handler.getLooper().quit();
		//interrupt();

	}
}