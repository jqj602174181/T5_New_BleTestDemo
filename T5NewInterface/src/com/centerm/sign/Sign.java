package com.centerm.sign;

import android.R.bool;
import android.util.Log;

import com.centerm.device.CommService;
import com.centerm.financial.FinancialBase;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.SignData;

public class Sign extends FinancialBase {
	//�ɵ�ָ��
	//private final String pic = "[0GETSIGNPIC";
	//private final String xml = "[0GETSIGNDATA";
	private final String key = "[2DOWNLOADSIGNKEY";
	private byte[] picData;

	private Integer signStyle = 0;
    private final static String CONFIG_FILE_PATH = "/mnt/sdcard/Sign_Dncrypt.png";//������ܺ������
    
    private final static String SING_DATA_FILE = "/mnt/sdcard/Sign_Dncrypt.xml";
    
    private final static String SING_XML_PATH = "/mnt/sdcard/hw.xml";//��ȡǩ�������ļ�
    private final static String SING_PNG_PATH = "/mnt/sdcard/hw.png";
    		
   
    private final static String TAG ="Sign";
	boolean isOver = false;

	private boolean isSureTimeout = false;
	
	private String sTimeOut = "[2SETTIMEOUT";
	private SignFile picSignFile;
	private SignFile xmlSignFile;
	private int timeOut = 20;
	private final int MAX_LEN = 1024;
	private int waitNumber = 5;
	
	private byte[] picSize;//ǩ�����ĵ�pngʵ�ʳ���
	private byte[] xmlSize;//ǩ�����ĵ�xmlʵ�ʳ���
	
	public Sign()
	{
		
		//���������������ڱ���xml ,�Լ�pngͼƬ
		picSignFile = new SignFile(CONFIG_FILE_PATH,true);
		xmlSignFile = new SignFile(SING_DATA_FILE,true);
	}
	
	
	//��ȡ����ֵ
	private void readMoreData()
	{
		int waitTimeOut = 2;
		if(CommService.type==DeviceOperatorData.HID){
			waitTimeOut = 2;
		}else if(CommService.type==DeviceOperatorData.BLUETOOTH){
			waitTimeOut = 1000*(2);
		}
		byte[] readData = new byte[packLength];
		while (true) {
			int len = readCommData(readData,waitTimeOut);
			int realLen = getRealReadedLength(readData, len);
			if(isSuccess(realLen, readData)){
				
				break;
			}else{
			
				break;
			}
			
		}
	}
	
	
	
	private boolean isSendValueSuccess(byte[] value,int index)
	{
		String sec = Integer.toHexString(index);
		//Log.e("sec"," sec is "+sec);
		byte[] sendData= new byte[value.length+4+sec.length()+key.length()];
		sendData[0]= (byte)0x1B;
		System.arraycopy(key.getBytes(), 0, sendData, 1, key.length());
		sendData[sendData.length-1] = (byte)0x03;
		sendData[key.length()+1] =(byte)0x02; 
		System.arraycopy(sec.getBytes(), 0, sendData, key.length()+2, sec.length());
		sendData[key.length()+2+sec.length()] =(byte)('|'); 
		System.arraycopy(value, 0, sendData, key.length()+3+sec.length(), value.length);
		
		String str = new String(sendData,0,sendData.length);
		//Log.e("sec", " str is "+str);
		sendCommData(sendData, sendData.length);
		byte[] readData = new byte[packLength];
		int waitTimeOut = 10;
		if(CommService.type==DeviceOperatorData.HID){
			waitTimeOut = 10;
		}else if(CommService.type==DeviceOperatorData.BLUETOOTH){
			waitTimeOut = 100*(10);
		}
		int time = waitNumber;
		while (true) {
			int len = readCommData(readData,waitTimeOut);
			int realLen = getRealReadedLength(readData, len);
			
			if(isSuccess(realLen, readData)){
			
				return true;
			}else{
				if(time==0){
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				time--;
				continue;
			}
			
		}
		return false;
	}

	

   public int readData(byte[] data, int len, int timeOut) {

		synchronized(signStyle){
			if(signStyle==1){
				
				System.arraycopy(picData,0, data, 0,picData.length);
				signStyle=-1;
				return picData.length;
			}

		
			return -1;
		}
	}

   
   /**��ȡ����
    * 
    * @param res[in]
    * @param resLen[in]
    * @param begin[in]
    * @param end[in]
    * @param outData[out]��ý�ȡ�����ݳ���
    * @return ��ȡ�������ݳ���
    */
   private int separateSignData(byte[] res, int resLen, byte begin, byte end, byte[] outData)
	{
		boolean mark = false;
		int index = 0;

		for (int i = 0; i < resLen; i++)
		{
			if (res[i] == begin)
			{
				mark = true;
				continue;
			}
			if (res[i] == end)
			{
				break;
			}
			if (mark)
			{
				outData[index++] = res[i];
			}
		}

		return index;

	}
   
   
   /***
    * �����ѯָ��
    * */
   

	/**��ѯǩ������
	 * 
	 * @param path[in]
	 * @param timeout[in]
	 * @param signSize[out]
	 * @return ǩ��������ռ�õĳ���
	 */
	public int findSignData(byte[] path,int timeout, byte[] signSize)
	{

		int nRet = 0;
		int nReqLen = 0;
		byte[] byReqData = new byte[MAX_LEN];
		byte[] byCmdReq = { 0x1B, 0x5B, 0x30, 0x53, 0x50, 0x45, 0x43, 0x51,
				0x55, 0x45, 0x52, 0x59, 0x46, 0x49, 0x4C, 0x45, 0x02 };
		System.arraycopy(byCmdReq, 0, byReqData, 0, byCmdReq.length);
		nReqLen += byCmdReq.length;

		System.arraycopy(path, 0, byReqData, nReqLen, path.length);
		nReqLen += path.length;

		byReqData[nReqLen] = 0x03;
		nReqLen += 1;

		byte[] byCmdRes = new byte[MAX_LEN];
		sendCommData( byReqData, nReqLen);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nRet = readCommData( byCmdRes, timeout);
		
		//Log.e(TAG,"findSignData: byReqData"+ StringUtil.bytesToHexString(byReqData)) ;
		//Log.e(TAG,"findSignData: nRet"+nRet);
		if ( nRet > 0 )
		{
			//Log.e(TAG,"findSignData: byCmdRes"+ StringUtil.bytesToHexString(byCmdRes)  );
		
		}// ����ͨ�Ŵ�����
		if (nRet <= 0)
		{  
			nRet = SignFile.COMMERROR;

			return nRet;
		}

		if ( byCmdRes[0]==0x02&&byCmdRes[1] == 0x55&& byCmdRes[2]==0x03)
		{
			
			nRet = SignFile.COMMERROR;
			return nRet;
		}
		else
		{
		    nRet = separateSignData(byCmdRes, byCmdRes.length, (byte) 0x02,
				(byte) 0x7C, signSize);
		    byte[] MD5 = new byte[MAX_LEN];
		    separateSignData(byCmdRes, byCmdRes.length, (byte) 0x7C,
					(byte) 0x03, MD5);
            //Log.e(TAG, "findSignData: MD5=" + StringUtil.AsciiToString(MD5) );
		}
		

		return nRet;
	}

	

	/**
	 * ��ȡǩ����������
	 * @param path[in]
	 * @param ioffset[in]--ƫ��λ��
	 * @param  ilength[in]---ƫ����
	 * @param  allLength[in]---�ļ��ܳ���
	 * @param szCmd[out]---�����ָ��
	 * @return ָ���
	 */
	private int getSignDataCmd(byte[] path, int ioffset, int ilength, int allLength, byte[] szCmd)
	{
		int nReqLen = 0;
		
		//���ָ��
		byte[] offset = StringUtil.StringToHexAscii(String.valueOf(ioffset));//��ʼλ��
		if ((ioffset + ilength) > allLength)
		{
            ilength = allLength - ioffset;
		}
		byte[] length = StringUtil.StringToHexAscii(String.valueOf(ilength));//Ҫ��ȡ�ĳ���

		//Log.e( TAG, "getSignDataCmd: ilength="+ ilength + " ,ioffset = "+ ioffset + ",allLength = "+allLength );
		//byte[] szCmd = new byte[MAX_LEN];
		byte[] byCmdReq = { 0x1B, 0x5B, 0x30, 0x55, 0x50, 0x4C, 0x4F, 0x41,
				0x44, 0x02 };
		System.arraycopy(byCmdReq, 0, szCmd, 0, byCmdReq.length);
		nReqLen += byCmdReq.length;

		// p1
		System.arraycopy(path, 0, szCmd, nReqLen, path.length);
		nReqLen += path.length;

		// �ָ���
		szCmd[nReqLen] = (byte)0x7C;
		nReqLen += 1;

		// P2
		System.arraycopy(offset, 0, szCmd, nReqLen, offset.length);
		nReqLen += offset.length;

		// �ָ���
		szCmd[nReqLen] = (byte)0x7C;
		nReqLen += 1;

		// P3
		System.arraycopy(length, 0, szCmd, nReqLen, length.length);
		nReqLen += length.length;

		// ��β
		szCmd[nReqLen] = 0x03;
		nReqLen += 1;
	   
		return nReqLen;
	}
   
  
	
	
   /**
    * ��ѯ�Լ���ȡ����
    * 
    * */  
   private String[]  getFinancialData(int timeOut) {

		
	   //��ȡpng����
		int length = 0;
		byte[] readData = new byte[packLength]; 
		int fileSize = 0;
		int time = waitNumber;
		byte[] png_path = StringUtil.StringToHexAscii(SING_PNG_PATH);
    
		
		//(1)��ѯ����
		length = findSignData(png_path, timeOut, readData);
		if(length < 0)//��ѯʧ��
		{
			 return picSignFile.getError(length);
		}
		byte[] temp1 = new byte[length];
		System.arraycopy(readData, 0, temp1 , 0, length);
		//Log.e( TAG, "getFinancialData: temp1" + StringUtil.bytesToHexString(temp1));
		picSignFile.writeData(temp1, length);
		
		
		int offset = 0;
		int szCmdLen = 0;
		int ilenth  = 512;//ÿ�ζ�ȡ�����ݳ���
		byte[] szCmd = new byte[ MAX_LEN ];
		fileSize = picSignFile.fileLen;
		//Log.e(TAG, "picSignFile: fileLen= "+ fileSize);
		while(true)
		{
			//��2����ȡ���ݱ߻�ȡ���߱���
			//Log.e ( TAG, "picSignFile: offset="+ offset);
			szCmdLen = getSignDataCmd(png_path, offset,ilenth, fileSize, szCmd );//����ָ��
			sendCommData(szCmd, szCmdLen);
		//	//Log.e( TAG, "getpicSignFile: in while szCmd"+ StringUtil.bytesToHexString(szCmd));
			length =readCommData(readData, timeOut);
			//Log.e( TAG, "getpicSignFile: in while: realen=" + length);
		
			
			if(length>0)
			{
				time = waitNumber;
				//Log.e( TAG, "getpicSignFile: realen=" + length);
				//Log.e( TAG,  "readCommData: readData: =" + StringUtil.bytesToHexString(readData));
				if(picSignFile.writeData(readData, length)){
					isOver = true;
					//Log.e(TAG, "picSignFile: isover");
					break;
				}
				offset += length;//ƫ����
			
			}
			else
			{
				if(time==0){
					isOver = false;
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
				
			time--;
			
				//break;
			}
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		
		
		
		//��ȡxml����
		if(isOver){	
			png_path = StringUtil.StringToHexAscii(SING_XML_PATH);
			//��ѯ����
		
			length = findSignData(png_path, timeOut, readData);
			if(length < 0)//��ѯʧ��
			{
				 return xmlSignFile.getError(length);
			}
			byte[] temp2 = new byte[length];
			System.arraycopy( readData, 0, temp2, 0, length);
			xmlSignFile.writeData(temp2, length);
			
			
			offset = 0;
			fileSize = xmlSignFile.fileLen;
			//Log.e(TAG, "xmlSignFile: fileLen= "+ fileSize);
			
			while(true)
			{
				
				szCmdLen = getSignDataCmd(png_path, offset,ilenth, fileSize, szCmd );//����ָ��
				sendCommData(szCmd, szCmdLen);
			//	Log.e( TAG, "xmlSignFile: in while szCmd"+ StringUtil.bytesToHexString(szCmd));
				length =readCommData(readData, timeOut);
				//Log.e( TAG, "xmlSignFile: in while: realen=" + length);
				if(length>0){
					time = waitNumber;
					//Log.e( TAG,  "readCommData: readData: =" + StringUtil.bytesToHexString(readData));
					if(xmlSignFile.writeData(readData, length)){
						isOver = true;
						break;
					}
					
					offset += length;
				}
				else{
					if(time==0){
						isOver = false;
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					time--;
				}
			}
		}
		
		
		
		else{
			return picSignFile.getError();
		}
		StringBuilder builder = new StringBuilder();
		if(isOver){//�ɹ�
			builder.append(sRight);
			builder.append(split);
			builder.append(CONFIG_FILE_PATH);
			builder.append(split);
			builder.append(SING_DATA_FILE);
			return builder.toString().split(split);
		}else{//ʧ��
			return xmlSignFile.getError();
		}

	}
	
	

	//	��ȡ��ʱָ��
	private byte[] getTimeOut(String time)
	{
		//<1B>[2SETTIMEOUT<02><P1><03>
		byte[] timeOutData = new byte[15+time.length()];
		timeOutData[0] = (byte)0x1B;
		int timeLen = time.length();
		System.arraycopy(sTimeOut.getBytes(), 0, timeOutData, 1,sTimeOut.getBytes().length);
		timeOutData[13]=0x02;
		System.arraycopy(time.getBytes(), 0, timeOutData, 14,timeLen);
		timeOutData[timeOutData.length-1]=0x03;
		return timeOutData;
	}
	
	

	
	/**
	* Ҫ��ͻ�����ǩ��������ȡǩ������
	* 
	* @param strTimeout ��ʱ�����Ĭ��Ϊ30��
	*/
	public String[] getSignature(String strTimeout)
	{
		
//		readMoreData();
		picSignFile.init();
		xmlSignFile.init();
		isSureTimeout = false;
		isOver = false;
		
		//ʱ����������򷵻�ʧ��
		int time = getTime(strTimeout);
		if(time==-1){
			return getParamErr();
		}
		
		
		
		int readtime = 2;
		int waitTimeOut = time;
		//���ݲ�ͬ��ͨѶ��ʽ���ó�ʱʱ��
		if(CommService.type==DeviceOperatorData.HID){
			waitTimeOut = time+2;
			readtime = readtime+2 ;
		}else if(CommService.type==DeviceOperatorData.BLUETOOTH){
			waitTimeOut = 1000*(time+2);
			readtime = 1000*(readtime+2) ;
			
		}
		timeOut = time;
		
	
		String[] strList = null;
		
		//���ʹ���дǩ����ʱ�Լ�����ָ��
		byte openData[] = getFinancialOpenCommad();
		sendCommData(openData, openData.length);
		byte[] readData = new byte[packLength];
		int length= readCommData(readData,waitTimeOut);
		
		byte[] tempDate = new byte[length];
		System.arraycopy(readData, 0, tempDate,0, length);
		int len = getRealReadedLength(readData, length);
		
	     //�ж�������дǩ���Ƿ������ɹ�����ʧ���򷵻ش�������
		if(!isSuccess(len, readData)){
			//Log.e(TAG, "getSignature: start signfail");
			picSignFile.setFileStyle(SignFile.TIMEOUT);
			return picSignFile.getError();
		}
	    
		//time = waitTimeOut;
		strList = getFinancialData(readtime);	
	
		
		return strList;
	}
	
	
	
	
	public String[] keyAffuse (String[] keys)
	{
		if(keys==null){
			return SignFile.getError(SignFile.ParamErr);
		}
	
		readMoreData();
		boolean is = false;
		for(int i=0;i<keys.length;i++){
			byte[] value =keys[i].getBytes();
			if(isSendValueSuccess(value,i)){
				is = true;
			}else{
				is = false;
				break;
			}
			
		}
		
	
		if(is){//�ɹ�
			String [] str = new String[1];
			str[0]=sRight;
			return str;
		}else{//ʧ��
			return SignFile.getError(SignFile.ParamErr);
		}
	}
	

	
	@Override
	public Object getTestData(Object object) {
		// TODO Auto-generated method stub
		SignData signData = (SignData)object;
		
		if(signData.style==1){
			return getSignature(signData.timeOut);
		}else if(signData.style==2){
			return keyAffuse(signData.keyAffuseList);
		}
		return SignFile.getError(SignFile.ParamErr);
		
	}
	
	@Override
	protected int writeData(byte[] data, int len) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

	@Override
	public byte[] getFinancialOpenCommad() {
		byte[]timeOutData = getTimeOut(""+timeOut); 
		sendCommData(timeOutData,timeOutData.length );
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return BusinessInstruct.OpenHandwrite();
		//return BusinessInstruct.getHandwrite();
	}

	@Override
	public byte[] getFinancialCloseCommad() {
		//return super.getFinancialCloseCommad();
		return null;
	}
	
	
	/**
	 * ��ѯ�Լ���ȡǩ������ :��ָ��
	 * */
/*	private String[]  getFinancialData(int timeOut) {

	
		
		////��ȡpng �ļ�����
		byte [] picData = new byte[pic.length()+1];
		picData[0]=(byte)0x1B;
		System.arraycopy(pic.getBytes(),0, picData, 1, pic.length());
		sendCommData(picData, picData.length);
		int length = 0;
		byte[] readData = new byte[packLength]; 
		
		int time = waitNumber;
		
		
		while(true)
		{
			length =readCommData(readData, timeOut);
			if(length>0)
			{
				time = waitNumber;
				if(picSignFile.writeData(readData, length)){
					isOver = true;
					break;
				}
			}
			
			else
			{
				if(time==0){
					isOver = false;
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
				
			time--;
			
				//break;
			}
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//��ȡxml����
		if(isOver){
			byte [] xmlData = new byte[xml.length()+1];
			xmlData[0]=(byte)0x1B;
			System.arraycopy(xml.getBytes(),0, xmlData, 1, xml.length());
			sendCommData(xmlData, xmlData.length);
			
			
			while(true)
			{
				length =readCommData(readData, timeOut);
				if(length>0){
					time = waitNumber;
					if(xmlSignFile.writeData(readData, length)){
						isOver = true;
						break;
					}
				}else{
					if(time==0){
						isOver = false;
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					time--;
				}
			}
		}
		
		
		
		else{
			return picSignFile.getError();
		}
		StringBuilder builder = new StringBuilder();
		if(isOver){//�ɹ�
			builder.append(sRight);
			builder.append(split);
			builder.append(CONFIG_FILE_PATH);
			builder.append(split);
			builder.append(SING_DATA_FILE);
			return builder.toString().split(split);
		}else{//ʧ��
			return xmlSignFile.getError();
		}

	}*/
   
	
	
}
