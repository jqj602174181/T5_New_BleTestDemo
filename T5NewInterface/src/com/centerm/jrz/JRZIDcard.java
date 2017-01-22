package com.centerm.jrz;

import com.centerm.financial.FinancialBase;
import com.centerm.idcard.IDCardDef;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.IDCardData;

public class JRZIDcard extends FinancialBase{

	private final int TRANSPROC_DEFAULT_TIMEOUT = 1;
	private final int MAX_LEN = 1024;

	@Override
	public Object getTestData(Object object) {
		IDCardData data = (IDCardData)object;
		return getIDFullInfo(data.timeOut);
	}

	public Object getIDFullInfo( String strTimeout){
		int timeOut = getTime(strTimeout);
		if(timeOut==-1){
			return getParamErr();
		}

		Object object = getFinancialData(timeOut);

		return object;
	}

	private Object getFinancialData(int timeOut)
	{
		byte szCommand[] = {0x1b, 0x5b, 0x38, 0x49, 0x44, 0x63, 0x61, 0x72, 0x52, 0x65, 0x61, 0x64, 0x02, 0x31, 0x35, 0x03};
		byte[] szOutBuf = new byte[1024*10];
		if(szCommand!=null)
		{
			int nRet = readIDCard(szOutBuf, szCommand, timeOut);
			byte[] realOutBuf = new byte[nRet];
			System.arraycopy(szOutBuf, 4, realOutBuf, 0, nRet); //从第四位开始拷贝 前四位是长度
			return realOutBuf;
		}
		return null;
	}

	private int readIDCard( byte[] szOutBuf, byte[] szCommand, int timeOut ) 
	{
		byte[] szRes = new byte[ MAX_LEN ] ;
		long dwStart   = System.currentTimeMillis();
		int nRet = 0;
		int pos = 0;
		int recvlen = 0;

		//下发指令
		if(WriteDataToTransPort( szCommand, szCommand.length ) < szCommand.length )
		{
			return IDCardDef.ERR_SEND;
		}

		while( true)
		{
			recvlen = ReadDataFromTransPort(szRes, TRANSPROC_DEFAULT_TIMEOUT);	

			if( recvlen > 0  )
			{   
				System.arraycopy(szRes, 0, szOutBuf, pos, recvlen);
				if( endofRead( szOutBuf, pos+recvlen ) == 0 )
				{  
					nRet = IDCardDef.ERR_SUCCESS;
					break;
				}
				pos += recvlen;
			}
			else
			{
				nRet = IDCardDef.ERR_READ;
			}

			if( System.currentTimeMillis()-dwStart > timeOut*1000 ) //读不到信息，且超时
			{
				return IDCardDef.ERR_TIMEOUT;
			}

			try {
				Thread.sleep( 200 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return  pos+recvlen;
	}

	public  int endofRead( byte[] szBuff, int nRdLen)
	{
		byte[] dataByte = new byte[4];
		dataByte[0] = szBuff[0];
		dataByte[1] = szBuff[1];
		dataByte[2] = szBuff[2];
		dataByte[3] = szBuff[3];
		int len = bytesToIntByBigEndian(dataByte, 0);
		if(nRdLen >= len+4){
			return 0;
		}
		return 1;
	}

	private int bytesToIntByBigEndian(byte[] src, int offset) {  
		int value;    
		value = (int) ((src[offset+3] & 0xFF)   
				| ((src[offset+2] & 0xFF)<<8)   
				| ((src[offset+1] & 0xFF)<<16)   
				| ((src[offset] & 0xFF)<<24));  
		return value;  
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
