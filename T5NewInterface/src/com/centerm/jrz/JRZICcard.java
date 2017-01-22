package com.centerm.jrz;

import com.centerm.financial.FinancialBase;
import com.centerm.util.financial.ICCardData;

public class JRZICcard extends FinancialBase{

	private int icFlag = 1;

	@Override
	public Object getTestData(Object object) {
		ICCardData data = (ICCardData)object;
		icFlag = data.cardStyle;
		return getICFullInfo(data.timeOut);
	}

	public Object getICFullInfo(String strTimeout){
		int timeOut = getTime(strTimeout);
		if(timeOut==-1){
			return getParamErr();
		}
		Object object = getFinancialData(timeOut);
		return object;
	}

	@SuppressWarnings("null")
	private Object getFinancialData(int timeOut)
	{
		byte[] szCommand = null;
		if(icFlag == 1){
			szCommand = new byte[]{0x1b, 0x5b, 0x38, 0x49, 0x43, 0x63, 0x61, 0x72, 0x64, 0x52, 0x65, 0x61, 0x64, 0x02, 0x31, 0x35, 0x7c, 0x31, 0x03};
		}else if(icFlag == 2){
			szCommand = new byte[]{0x1b, 0x5b, 0x38, 0x49, 0x43, 0x63, 0x61, 0x72, 0x64, 0x52, 0x65, 0x61, 0x64, 0x02, 0x31, 0x35, 0x7c, 0x32, 0x03};
		}
		byte[] szOutBuf = new byte[packLength];
		if(szCommand!=null)
		{
			int[] resLen = new int[2];
			resLen[0] = szOutBuf.length;
			int nRet = transProc( szCommand, szCommand.length, szOutBuf, resLen, timeOut );
		}

		return szOutBuf;
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
