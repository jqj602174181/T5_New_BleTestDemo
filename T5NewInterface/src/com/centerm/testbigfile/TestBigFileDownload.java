package com.centerm.testbigfile;

import com.centerm.device.CommService;
import com.centerm.financial.FinancialBase;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.TestCommandData;

public class TestBigFileDownload extends FinancialBase{

	private String command = null;
	private int waitNumber = 5;
	private final int MAX_LEN = 1024;
	private BFileSave picSignFile = null;

	private final static String SING_PNG_PATH = "/mnt/sdcard/PsamPaySystem.rar";
	private final static String CONFIG_FILE_PATH = "/mnt/sdcard/New_PsamPaySystem.rar";//������ܺ������

	public TestBigFileDownload(){
		picSignFile = new BFileSave(CONFIG_FILE_PATH, true);
	}

	@Override
	public Object getTestData(Object object) {
		TestCommandData data = (TestCommandData)object;
		command = data.COMMAND;
		picSignFile.init();
		return getTestCommand(data.timeOut);
	}

	public Object getTestCommand(String strTimeout){
		int timeOut = getTime(strTimeout);
		if(timeOut==-1){
			return getParamErr();
		}
		Object object = getBigFileDownload(timeOut);
		return object;
	}

	@SuppressWarnings("null")
	private Object getBigFileDownload(int timeOut)
	{
		String[] result = getFinancialData(timeOut);
		return result;
	}

	private String[] getFinancialData(int timeOut) {
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
		int ilenth  = 512; //ÿ�ζ�ȡ�����ݳ���
		//hid �봫ͳ����
		if(CommService.type==DeviceOperatorData.HID || CommService.type==DeviceOperatorData.BLUETOOTH){
			ilenth = 512;
		}else if(CommService.type==DeviceOperatorData.BLE){ //bleһ�ν����ֽ�
			ilenth = 1024*50;
		}else{
			ilenth = 512;
		}

		byte[] szCmd = new byte[ MAX_LEN ];
		fileSize = picSignFile.fileLen;
		while(true)
		{
			//��2����ȡ���ݱ߻�ȡ���߱���
			//Log.e ( TAG, "picSignFile: offset="+ offset);
			szCmdLen = getSignDataCmd(png_path, offset, ilenth, fileSize, szCmd );//����ָ��
			sendCommData(szCmd, szCmdLen);
			//	//Log.e( TAG, "getpicSignFile: in while szCmd"+ StringUtil.bytesToHexString(szCmd));
			length =ReadDataFromTransPort(readData, timeOut);
			//Log.e( TAG, "getpicSignFile: in while: realen=" + length);

			if(length>0)
			{
				time = waitNumber;
				//Log.e( TAG, "getpicSignFile: realen=" + length);
				//Log.e( TAG,  "readCommData: readData: =" + StringUtil.bytesToHexString(readData));
				if(picSignFile.writeData(readData, length)){
					//Log.e(TAG, "picSignFile: isover");
					break;
				}
				offset += length;//ƫ����
			}
			else
			{
				if(time==0){
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				time--;
				//break;
			}
		}

		StringBuilder builder = new StringBuilder();
		builder.append(sRight);
		builder.append(split);
		builder.append(CONFIG_FILE_PATH);
		return builder.toString().split(split);
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
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		nRet = ReadDataFromTransPort( byCmdRes, timeout);

		//Log.e(TAG,"findSignData: byReqData"+ StringUtil.bytesToHexString(byReqData)) ;
		//Log.e(TAG,"findSignData: nRet"+nRet);
		if ( nRet > 0 )
		{
			//Log.e(TAG,"findSignData: byCmdRes"+ StringUtil.bytesToHexString(byCmdRes)  );
		}// ����ͨ�Ŵ�����
		if (nRet <= 0)
		{  
			nRet = -4;

			return nRet;
		}

		if ( byCmdRes[0]==0x02&&byCmdRes[1] == 0x55&& byCmdRes[2]==0x03)
		{

			nRet = -4;
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

	@Override
	protected int readData(byte[] data, int len, int timeOut) {
		return 0;
	}

	@Override
	protected int writeData(byte[] data, int len) {
		return 0;
	}

	@Override
	public byte[] getFinancialOpenCommad() {
		return null;
	}

	@Override
	public byte[] getFinancialCloseCommad() {
		return null;
	}

}
