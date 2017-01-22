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
				case DeviceOperatorData.OPEN_FINANCIAL://��ȡ
					//Log.e("Dev", "OperatorDataThread: DeviceOperatorData.OPEN_FINANCIAL");
					financialServer.setStyle(msg.arg1);//���õ�ǰģ��
					Object object = financialServer.getFinalcialData(msg.obj);	//��ȡ����ģ�������
					if(object!=null){//������Զ�ȡ������
						Message message = operatorHandler.obtainMessage(DeviceOperatorData.MESSAGE);
						message.obj = object;
						operatorHandler.sendMessage(message);
					}else{//�����ȡ������Ϊ�գ����ȡʧ��
						operatorHandler.sendEmptyMessage(DeviceOperatorData.READ_FAIL);
					}
					break;
				case DeviceOperatorData.SWITCH:

					break;
				case DeviceOperatorData.CLOSE_FINANCIAL://�ر�

					break;
				default:
					break;
				}

			}

		};
		Looper.loop();
	}

	/*
	 * msg�����ж��Ƿ��Ǵ�0���ر�1����ȡ2
	 * style�����ж������ֽ���ģ�����
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
	 * msg�����ж��Ƿ��Ǵ�0���ر�1����ȡ2
	 * style�����ж������ֽ���ģ�����
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
	 * msg�����ж��Ƿ��Ǵ�0���ر�1����ȡ2
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
	 * �˳���Ϣѭ��
	 */
	public void quitThread()
	{
		if(handler!=null)
			handler.getLooper().quit();
		//interrupt();

	}
}