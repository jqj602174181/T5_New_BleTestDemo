package com.centerm.financial;

import android.R.integer;
import android.util.Log;

import com.centerm.device.CommService;
import com.centerm.device.ResultCode;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;


public abstract class FinancialBase {


	protected final static int SUCCESS 	 = 0;
	protected final static String sRight ="0";
	protected final static String sError = "1";
	protected ReadThread readThread;
	protected SendThread sendThread;

	protected final int packLength = 1024*50;
	public abstract Object getTestData(Object object);
	protected abstract int readData(byte [] data,int len,int timeOut);
	protected abstract int writeData(byte[] data,int len);
	protected final static String split = "@";

	protected  boolean isSendThreadStop = false;
	protected  boolean isReadThreadStop = false;
	protected boolean isSendLock = false;
	protected final static int timeDelay= 5;

	protected boolean isSendQuit = false;//�����жϷ����Ƿ����
	protected boolean isReadQuit = false;//�����ж϶�ȡ�Ƿ����

	protected static final  int T5_STARTPLAYDEMO_ERROR  = -302; //������������ʧ��
	protected static final  int T5_CLOSEPLAYDEMO_ERROR =  -303; //�رտ�������ʧ��

	public FinancialBase()
	{

	}

	protected int getRealReadedLength(byte[] byRes,int byResLength){
		if(CommService.type==DeviceOperatorData.BLUETOOTH){//����ͨ��
			return byResLength;
		}else if(CommService.type==DeviceOperatorData.HID){
			//hid,ÿ�λ�ȡ�����ݶ���1024���ֽ�,Ҫ�����һ���ֽ���ǰ�㣬ֱ�����ǿ��ַ���ʱ��ʹ�����Ч���ݵĳ���
			int realReadedLength = byRes.length;
			for(int i = byRes.length - 1;i >= 0;i --){
				if(byRes[i] == (byte)0x00){
					realReadedLength --;
				} else {
					return realReadedLength;
				}
			}
			return realReadedLength;
		}else if(CommService.type==DeviceOperatorData.BLE){
			return byResLength;
		}

		return byResLength;

	}

	protected  int  readCommData(byte [] data,int time)
	{
		int length = CommService.getInstance().readData2(data,time);
		return getRealReadedLength(data, length);
	}

	protected  void sendCommData(byte [] data,int len)
	{

		byte data1[] =  new byte[len];
		System.arraycopy(data, 0, data1, 0, len);
		CommService.getInstance().writeData(data1, len);
	}

	protected static  int WriteDataToTransPort( byte[] szData, int nDataLen )
	{
		byte data1[] =  new byte[nDataLen];
		System.arraycopy(szData, 0, data1, 0, nDataLen);
		//Log.e("WriteDataToTransPort", "writeData: "+ StringUtil.bytesToHexString(data1) );
		return CommService.getInstance().writeData(data1, nDataLen);
	}


	protected int ReadDataFromTransPort( byte[] readData, int time)
	{

		//Log.e("ReadDataFromTransPort", "time = "+ time);
		int timeout = time;

		if(CommService.type==DeviceOperatorData.BLUETOOTH){//����ͨ��
			timeout = time * 1000;

		}
		else if(CommService.type==DeviceOperatorData.HID)
		{
			timeout = time + 2;
		}
		else if(CommService.type==DeviceOperatorData.BLE){
			timeout = time * 1000;
		}
		int length= CommService.getInstance().readData(readData,timeout );
		//		Log.e("ReadDataFromTransPort", "length = "+ length);
		if(length> 0)
		{
			//			Log.e("ReadDataFromTransPort", "readData: "+ new String(readData) );
			//			Log.e("ReadDataFromTransPort", "readData: "+ StringUtil.bytesToHexString(readData) );
		}
		return getRealReadedLength(readData, length);
	}



	protected int transProc( byte[] szReq, int nReqLen, byte[] szRes, int[] pnResLen, int time)
	{
		int nRet = ResultCode.DEVICE_SUCCESS;
		//�·�ָ��
		if(WriteDataToTransPort( szReq, nReqLen ) < nReqLen )
		{
			return ResultCode.DEVICE_WRITEDEVICE_FAILED;
		}
		// Log.e("transProc", "WriteDataToTransPort: success!");
		//����ָ��
		
		while(CommService.getInstance().isWork()){ //���ble���
			
		}
		
		if( szRes != null )
		{
			nRet = ReadDataFromTransPort( szRes, time);
			//Log.e( "transProc", "ReadDataFromTransPort nRet=" + nRet);
			if(nRet > 0)
			{

				pnResLen[0] = nRet;
				nRet = ResultCode.DEVICE_SUCCESS;
			}
			else 
			{
				nRet = ResultCode.DEVICE_READDEVICE_FAILED;
			}
		}

		return nRet;
	}

	public abstract byte[] getFinancialOpenCommad();

	public abstract byte[] getFinancialCloseCommad();

	protected boolean isSuccess(int len,byte[] data)
	{
		//Log.e( "isSuccess","len= "+ len + " ,data:"+StringUtil.bytesToHexString(data ) );

		if(len==3&&data[0]==0x02&&data[1]==(byte)0xAA&&data[2]==0x03)
		{
			return true;
		}

		return false;
	}

	/**
	 *������������ 
	 */
	protected int T5_StartPlayDemo( int nTimeout)
	{
		byte szCommand[] = getFinancialOpenCommad();
		int nRet = T5_STARTPLAYDEMO_ERROR;
		if(szCommand!=null)
		{
			byte[] szOutBuf = new byte[packLength];
			int[] resLen = new int[2];
			resLen[0] = szOutBuf.length;
			nRet = transProc( szCommand, szCommand.length,  szOutBuf, resLen, nTimeout );

			if(isSuccess(resLen[0], szOutBuf))
			{
				nRet = 0;
			}
			else
			{
				nRet = T5_STARTPLAYDEMO_ERROR;
			}
		}

		return nRet;
	}

	/**
	 * �رտ�������
	 * */
	protected int T5_ClosePlayDemo( )
	{
		byte szCommand[] = getFinancialCloseCommad();
		int nRet = T5_CLOSEPLAYDEMO_ERROR;
		if(szCommand!=null)
		{
			byte[] szOutBuf = new byte[packLength];
			int[] resLen = new int[2];
			resLen[0] = szOutBuf.length;
			nRet = transProc( szCommand, szCommand.length,  null, null , 0 );
		}
		return nRet;
	}




	protected int getTime(String sTime)
	{
		int time = -1;
		try{
			time = Integer.parseInt(sTime);
		}catch(NumberFormatException e){

		}

		return time;

	}

	protected String[] getParamErr()
	{

		String[] errList = new String[2];
		errList[0] = sError;
		errList[1] = RetUtil.Param_Err;

		return errList;
	}

	protected String[] getUnknownErr()
	{
		//return sError+split+RetUtil.Unknown_Err;

		String str = sError+split+RetUtil.Unknown_Err;
		return str.split(split);
	}

	/*
	 * 
	 */
	protected void readEndData()
	{

	}








	/*
	 * ��ȡ���߳�
	 */
	protected class  ReadThread extends Thread{

		public ReadThread()
		{
			isReadThreadStop = false;
		}

		public void run()
		{
			super.run();
			isReadThreadStop = false;
			isReadQuit = false;
			ReadThreadRun();
			readEndData();
			isReadQuit= true;

		}

		@Override
		public void interrupt() {
			// TODO Auto-generated method stub

			isReadThreadStop = true;
			//	writeUnlock();
			CommService.getInstance().quitRead();
			super.interrupt();
		}

		public void quitThread()
		{
			interrupt();
		}
	}


	protected void lock()
	{

	}

	protected void unlock()
	{

	}
	protected class SendThread extends Thread{



		public SendThread()
		{
			isSendLock = false;
			isSendThreadStop = false;
		}
		public void run()
		{
			super.run();
			isSendThreadStop = false;
			isSendQuit = false;
			isSendLock= false;
			byte cmdData[] = getFinancialOpenCommad();
			if(cmdData!=null){	
				sendCommData(cmdData,cmdData.length);
				isSendLock = true;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				isSendLock = false;
			}

			SendThreadRun();

			if(!isSendQuit){
				sendEnd();
			}

			cmdData= getFinancialCloseCommad();
			if(cmdData!=null){
				sendCommData(cmdData, cmdData.length);

			}
			isSendQuit = true;
			//Log.e("quit","send quit");
		}

		@Override
		public void interrupt() {
			isSendThreadStop = true;
			super.interrupt();
		}

		public void quitThread()
		{
			interrupt();
		}
	}

	/**
	 * д�̵߳�ִ�к���������������������д
	 * 
	 */
	protected void SendThreadRun()
	{
		byte data[] = new byte[packLength];
		while (!isSendThreadStop) {
			if(isSendLock){
				//	writeLock();
			}else{

			}

			int len =readData(data, data.length, 10);

			if(len==0){
				//break;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if(!isSendLock){
				//		readUnlock();
				isSendLock = true;
			}

			if(len>0){	
				if(len==3&&data[0]==0x02&&data[1]==(byte)0x0f&&data[2]==0x03){	//�д�jni�ж������ݣ�Ҫ��ͨ�Žӿڷ���

					isSendThreadStop = true;
					CommService.getInstance().quitRead();
					isReadThreadStop = true;
					isSendQuit = true;
					break;
				}else{
					sendCommData(data, len);
				}
			}
		}
	}

	protected void sendEnd()
	{

	}
	/*
	 * ���̵߳�ִ�к���������������������д
	 * 
	 */
	protected void ReadThreadRun()
	{
		byte[] data = new byte[packLength];
		while(!isReadThreadStop)
		{
			int len = readCommData(data,10);
			if(len==-1){
				return;
			}

			if(len>0){
				int l = writeData(data, len);//��ͨ�Žӿڶ�������,Ҫдjni��
			}
		}
		//Log.e("quit","read quit");
	}

	protected void start()

	{
		//Log.e("Dev","start in FinancialBase" );
		if(sendThread!=null){
			sendThread.quitThread();
			sendThread = null;
		}

		if(readThread!=null){
			readThread.quitThread();
			readThread = null;
		}

		readThread = new ReadThread();
		sendThread = new SendThread();

		readThread.start();
		sendThread.start();
	}

	protected void quit()
	{
		readThread.quitThread();
		sendThread.quitThread();
		readThread = null;
		sendThread = null;

		quitWait();
	}





	/*
	 * �˳�ʱҪ����ʱ5��
	 */
	protected void quitWait()
	{
		int time = timeDelay;
		while (true) {
			if(isSendQuit&&isReadQuit){//���д�̺߳Ͷ��̶߳��˳����Ͳ�Ҫ�ٵȴ���
				break;
			}
			time--;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(time==0){
				break;
			}
		}
	}

}
