package com.centerm.testcommand;

import com.centerm.financial.FinancialBase;
import com.centerm.util.financial.ICCardData;
import com.centerm.util.financial.TestCommandData;

public class TestCommand extends FinancialBase{

	private String command = null;

	@Override
	public Object getTestData(Object object) {
		TestCommandData data = (TestCommandData)object;
		command = data.COMMAND;
		return getTestCommand(data.timeOut);
	}

	public Object getTestCommand(String strTimeout){
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
		byte[] szOutBuf = new byte[packLength];
		//		byte[] szCommand = command.getBytes();
		byte[] szCommand = {0x43, 0x54};
		//		byte[] szCommand = {0x1b, 0x5b, 0x30, 0x47, 0x45, 0x54, 0x55, 0x50, 0x44, 0x41, 0x54, 0x45, 0x53,
		//				0x54, 0x41, 0x54, 0x55, 0x53};
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
