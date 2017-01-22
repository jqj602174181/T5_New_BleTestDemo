package com.centerm.jrz;

import com.centerm.financial.FinancialBase;
import com.centerm.util.financial.MsgCardData;

public class JRZMagcard extends FinancialBase{

	@Override
	public Object getTestData(Object object) {
		MsgCardData data = (MsgCardData)object;
		return getMagcardFullInfo(data.timeOut);
	}

	public Object getMagcardFullInfo(String strTimeout){
		int timeOut = getTime(strTimeout);
		if(timeOut==-1){
			return getParamErr();
		}
		Object object = getFinancialData(timeOut);
		return object;
	}

	private Object getFinancialData(int timeOut)
	{
		byte szCommand[] = {0x1b, 0x5b, 0x38, 0x4d, 0x61, 0x67, 0x63, 0x61, 0x72, 0x64, 0x52, 0x65, 0x61, 0x64, 0x02, 0x31, 0x35, 0x03};
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
