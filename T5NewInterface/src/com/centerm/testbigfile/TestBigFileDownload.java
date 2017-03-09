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
	private final static String CONFIG_FILE_PATH = "/mnt/sdcard/New_PsamPaySystem.rar";//保存解密后的数据

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
		//获取png数据
		int length = 0;
		byte[] readData = new byte[packLength]; 
		int fileSize = 0;
		int time = waitNumber;
		byte[] png_path = StringUtil.StringToHexAscii(SING_PNG_PATH);

		//(1)查询数据
		length = findSignData(png_path, timeOut, readData);
		if(length < 0)//查询失败
		{
			return picSignFile.getError(length);
		}
		byte[] temp1 = new byte[length];
		System.arraycopy(readData, 0, temp1 , 0, length);
		//Log.e( TAG, "getFinancialData: temp1" + StringUtil.bytesToHexString(temp1));
		picSignFile.writeData(temp1, length);

		int offset = 0;
		int szCmdLen = 0;
		int ilenth  = 512; //每次读取的数据长度
		//hid 与传统蓝牙
		if(CommService.type==DeviceOperatorData.HID || CommService.type==DeviceOperatorData.BLUETOOTH){
			ilenth = 512;
		}else if(CommService.type==DeviceOperatorData.BLE){ //ble一次接收字节
			ilenth = 1024*50;
		}else{
			ilenth = 512;
		}

		byte[] szCmd = new byte[ MAX_LEN ];
		fileSize = picSignFile.fileLen;
		while(true)
		{
			//（2）获取数据边获取，边保存
			//Log.e ( TAG, "picSignFile: offset="+ offset);
			szCmdLen = getSignDataCmd(png_path, offset, ilenth, fileSize, szCmd );//构造指令
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
				offset += length;//偏移量
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
	 * 获取签名数据命令
	 * @param path[in]
	 * @param ioffset[in]--偏移位置
	 * @param  ilength[in]---偏移量
	 * @param  allLength[in]---文件总长度
	 * @param szCmd[out]---构造的指令
	 * @return 指令长度
	 */
	private int getSignDataCmd(byte[] path, int ioffset, int ilength, int allLength, byte[] szCmd)
	{
		int nReqLen = 0;

		//组合指令
		byte[] offset = StringUtil.StringToHexAscii(String.valueOf(ioffset));//起始位置
		if ((ioffset + ilength) > allLength)
		{
			ilength = allLength - ioffset;
		}
		byte[] length = StringUtil.StringToHexAscii(String.valueOf(ilength));//要读取的长度

		//Log.e( TAG, "getSignDataCmd: ilength="+ ilength + " ,ioffset = "+ ioffset + ",allLength = "+allLength );
		//byte[] szCmd = new byte[MAX_LEN];
		byte[] byCmdReq = { 0x1B, 0x5B, 0x30, 0x55, 0x50, 0x4C, 0x4F, 0x41,
				0x44, 0x02 };
		System.arraycopy(byCmdReq, 0, szCmd, 0, byCmdReq.length);
		nReqLen += byCmdReq.length;

		// p1
		System.arraycopy(path, 0, szCmd, nReqLen, path.length);
		nReqLen += path.length;

		// 分隔符
		szCmd[nReqLen] = (byte)0x7C;
		nReqLen += 1;

		// P2
		System.arraycopy(offset, 0, szCmd, nReqLen, offset.length);
		nReqLen += offset.length;

		// 分隔符
		szCmd[nReqLen] = (byte)0x7C;
		nReqLen += 1;

		// P3
		System.arraycopy(length, 0, szCmd, nReqLen, length.length);
		nReqLen += length.length;

		// 结尾
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
		}// 设置通信错误码
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

	/**截取数据
	 * 
	 * @param res[in]
	 * @param resLen[in]
	 * @param begin[in]
	 * @param end[in]
	 * @param outData[out]获得截取的数据长度
	 * @return 截取到的数据长度
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
