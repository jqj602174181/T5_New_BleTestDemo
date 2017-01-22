package com.centerm.pin;

import android.util.Log;

import com.centerm.device.CommService;
import com.centerm.financial.FinancialBase;
import com.centerm.idcard.IDCardDef;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.PinData;



/**
 * 密码键盘
 */
public class Pin extends FinancialBase{
	
	

	public static final int NO_PASSWORD	= 1;
	public static final int DES 		= 2;
	public static final int DES3 		= 3;
	public static final String tag  ="pin";
	public static final int keyAffusePin = 4;
	
	private boolean isTimeOut = false;
	private int encryType = 1;
	private static boolean s_bCancel = false;
	private static  int MAX_LEN = 512;
	private static int MAX_PIN_LEN  = 1024;
	private StringBuilder byteBuilder;
	private int style = 1;//用于判断是密钥灌注还是读取密码
	
	public Pin()
	{
		byteBuilder = new StringBuilder();
	}
	
	/**
	 * 获取密码键盘密码长度以及超时时间设置
	 * @param type[in]: 1--时间 2--长度
	 * @param data[in]： 传入参数
	 * 
	 * @return 命令
	 * */
	private byte[] getSetCmd( int type, int data)
	{   
		//时间：0x1B  0x5B 0x31 0x3B <p1> 0x54
		//长度： 0x1B  0x5B 0x31 0x3B <p1> 0x4C
		String str = String.valueOf(data);
		byte[]  tmp= {0x1B, 0x5B,0x31,0x3B};
		byte[] cmdData = new byte[ 5 + str.length()];
		int len = tmp.length;
		
		
		System.arraycopy(tmp, 0 , cmdData, 0 , len);
		System.arraycopy(str.getBytes(), 0, cmdData, len, str.getBytes().length );
		
		if(type == 1)
		{
			cmdData[cmdData.length -1 ] = 0x54;
		}
		if(type == 2)
		{
			cmdData[cmdData.length -1 ] = 0x4C;
		}
		
		return cmdData;
	}
	
	
	/**
	 * 接收以及发送消息
	 * @param timeout[in]---超时时间
	 * @param sendData[in] ----发送的消息
	 * @param sendDataLen[in]----发送消息的长度
	 * @param recvData[out] ---接收到的消息
	 * @param  isCommPin[in] ---判断是否非加密
	 * 
	 * @return --- >0 获取密码长度 ，<0错误
	 * 
	 * */
	private int sendAndRecvCmd( int timeout, byte[] sendData, int sendDataLen, byte[] recvData, int isCommPin )
	{
		
		int iLoop = 0;//循环次数
		int nRet = 0;
		int nRecvLen = 0;//获取的密码长度
		byte pinData = 0x00; 
		byte[] recvTmp1 = new byte[ MAX_PIN_LEN ];
		byte[] recvTmp2 = new byte[ MAX_PIN_LEN ];
		long dwStart   = System.currentTimeMillis();
		int readLen = 1;
		if(CommService.type==DeviceOperatorData.HID)
		{
			readLen = MAX_PIN_LEN;
		}
		
		//Log.e( tag, "sendAndRecvCmd: isCommPin=" + isCommPin );
		nRet = WriteDataToTransPort( sendData, sendDataLen );
		//Log.e(tag, "sendAndRecvCmd:  WriteDataToTransPort: nRet=" + nRet );
		if(nRet < 0)
		{
			return KeyPadDef.ERR_SEND;
		}
		else
		{
			//不加密
			if(isCommPin  == 1 )
			{
				byte[] recvData1 = new byte[readLen];
				while( pinData != 0x0D )
				{
					nRecvLen = ReadDataFromTransPort( recvData1, 1 );
					//Log.e(tag, "sendAndRecvCmd: in while nRecvLen ="+ nRecvLen);
					if( nRecvLen <= 0 )
					{
						nRet = KeyPadDef.ERR_READ;
					}
					else 
					{
						pinData = ( byte )recvData1[0];
						if(iLoop < MAX_PIN_LEN )
						{
							if( pinData == 0x08 && iLoop > 0)
							{
								iLoop--;
							}
							else
							{
							  recvTmp2[ iLoop ] = pinData;
							  iLoop++;
							}
						}
					}
					
					
				 if( System.currentTimeMillis()-dwStart > timeout*1000 ) //读不到信息，且超时
				  {
							return KeyPadDef.ERR_TIMEOUT;
				  }
						
					try {
						Thread.sleep( 200 );
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				nRecvLen = iLoop;
				
				if( nRecvLen > 0  && recvTmp2[nRecvLen -1]  == 0x0D)
				{
					System.arraycopy( recvTmp2, 0,  recvData , 0 , nRecvLen -1 );
					nRet = nRecvLen;
				}
				else
				{
					nRet= KeyPadDef.ERR_PACKAGE_FORMAT;
				}
			}
			
			//加密类型
			else
			{
				
				nRecvLen = ReadDataFromTransPort(recvTmp1, timeout);
				if( nRecvLen <= 0 )
				{
					nRet = KeyPadDef.ERR_READ;
				}
				else
				{
					System.arraycopy(recvTmp1, 0, recvData, 0, nRecvLen);
					
					nRet = nRecvLen;
			
				}
			}
				
		}
		
		return nRet;
	}
	
	
	
	//不加密
   private int readCommonPin(int iTimes, int iLength,int timeout,byte[] pchPassword1,byte[] pchPassword2 )
   {
	   int nRet = 0;
	   int i = 0; 
	   int nRecvLen = 0;
	   //时间设置
	   byte[] timeoutCmd = getSetCmd( 1, timeout );
	   nRet = WriteDataToTransPort(timeoutCmd, timeoutCmd.length );
	   //Log.e(tag, "timeset : nRet= " + nRet );
	   if(nRet < 0)
	   {
		   return KeyPadDef.ERR_SEND;
	   }
	   
	   // 长度设置
	   byte[] iLengthCmd = getSetCmd(2 ,iLength );
	   nRet = WriteDataToTransPort(iLengthCmd, iLengthCmd.length );
	   //Log.e(tag, "lenthset: nRet="+ nRet );
	   if(nRet < 0)
	   {
		   return KeyPadDef.ERR_SEND;
	   }
	   
	   //开启密码
	   
	   for( i = 0 ; i < iTimes; i++ )
	   {
		  byte[] cmdData = new byte[MAX_LEN];
		  if( i == 0)
		  {
			  cmdData[0] = (byte)0x82;
		  }
		  else
		  {
			  cmdData[0] = (byte)0x81;
		  }
		  
		  byte[] szRetData = new byte[MAX_PIN_LEN];
		  nRecvLen = sendAndRecvCmd( timeout, cmdData, 1, szRetData , 1  );
		  //Log.e(tag, "readCommonPin:sendAndRecvCmd nRet=  " + nRecvLen );
		  if(nRecvLen <= 0 )
		  {
			  return nRecvLen;
		  }
		  else 
		  { 
			 nRet = KeyPadDef.ERR_SUCCESS;
		  }
		  
		  if( i == 0)
		  {
			  System.arraycopy(szRetData, 0, pchPassword1  , 0 , nRecvLen );
		  }
		  else
		  { 
			  System.arraycopy(szRetData, 0, pchPassword2  , 0 , nRecvLen );
		  }
		  
	   }
	   
	   return nRet;
   }
	
   
   //加密
   private int readEncPin(int nEncType ,int iTimes, int iLength,int timeout,byte[] pchPassword1,byte[] pchPassword2 )
   {
	   return  0;
   }
   
   
	/**
	* 读取密码
	* 
	* @param iEncryType     : 加密方式 1--不加密， 2--Des， 3-3Des 加密
	* @param iTimes         : 输入密码次数
	* @param iLength        : 密码长度
	* @param strTimeout     : 输入密码超时间隔
	*/
	private int CT_readPin( int nEncType,int iTimes, int iLength,int timeout, byte[] pchPassword1, byte[] pchPassword2)
	{
		
		int  nRet = 0;
		byte[] szPin = new byte[MAX_PIN_LEN];
		int nRetSeparate = -1 ;
		if (nEncType <= 0 || nEncType > 3 || iTimes <= 0 || iTimes > 2 || iLength < 0 || iLength >32 || timeout < 0)
		{
			return KeyPadDef.ERR_PARAM;
		}
		
		if(timeout == 0)
		{
			timeout = 30;
		}
		
		//判断通信是否连接
		if (CommService.getInstance().isConnect())
		{
			if (nEncType == 1)
			{
				nRet = readCommonPin(iTimes, iLength,timeout, pchPassword1, pchPassword2 );
				
				//Log.e(tag, "readCommonPin: nRet" + nRet );
			}
			else
			{
				//加密
				nRet = readEncPin(nEncType,iTimes, iLength, timeout, pchPassword1, pchPassword2 );
			}
		} 
		return nRet;
	}
	
	
	
	
	private int CT_keyAffusePin(String[] keys)
	{
		int nRet = 0;
		
		return nRet;
	}
	
	
	/**
	* 读取密码
	* 
	* @param iEncryType     : 加密方式
	* @param iTimes         : 输入密码次数
	* @param iLength        : 密码长度
	* @param strTimeout     : 输入密码超时间隔
	*/
	public String[] readPin(int iEncryType, int iTimes, int iLength, String strTimeout)
	{
			style = 1;
			encryType = iEncryType;
			byteBuilder.delete(0,byteBuilder.length());
			int timeout = getTime(strTimeout);
			if(timeout==-1){
				return getParamErr();
			}
			isTimeOut = false;
			
			byte[] pchPassword1 = new byte[256];
			byte[] pchPassword2 = new byte[256];
			int s = CT_readPin(iEncryType, iTimes, iLength, timeout, pchPassword1, pchPassword2);
			//Log.e(tag, "CT_readPin: s = " + s);
			String [] list = null;
		
			if(s==0){
				
				StringBuilder builder = new StringBuilder();
				builder.append(sRight);
				builder.append(split);
				String password1 = new String(pchPassword1, 0,pchPassword1.length).trim();
				//Log.e(tag, "readPin: password1" + password1 );
				if(encryType==1){
					builder.append(password1);
				}else{
					builder.append(password1);
					//builder.append(StringUtil.HexToStringA(password1.getBytes()));
				}
			
				if(iTimes==2){
					String password2 = new String(pchPassword2, 0,pchPassword2.length).trim();
					//Log.e(tag, "readPin: password2"+password2);
					builder.append(split);
					if(encryType==1){
						builder.append(password2);
					}else{
						builder.append(password2);
						//builder.append(StringUtil.HexToStringA(password2.getBytes()));
					}
					
				}else if(iTimes<2){
					
				}
				
				list =builder.toString().split(split);
			}
			else{
				list = getError(s);
			}


		return list;
	}
	
	
	private String [] getError(int style)
	{
		
		String[] errList = new String[2];
		errList[0]=sError;
		isTimeOut = true;
		switch (style) {
			case -1:
			case -9:
				errList[1]=RetUtil.Param_Err;
				break;
			case -8:
				errList[1]=RetUtil.Timeout_Err;
			
				break;
			case -4:
				errList[1]=RetUtil.Recv_Error_Mess;
				break;
			default:
				errList[1]=RetUtil.Unknown_Err;
			break;
		}
		return errList;
	}
	
	/**
	* 对密码键盘进行密钥灌注
	* 
	* @param keys     : 主密钥组
	*/
	public String[] keyAffusePin (String[] keys)
	{
		style = 2;
		
		int s = CT_keyAffusePin(keys);	
		//Log.e(tag, "keyAffusePin: s= "+ s);
		String[] list = null;
		if(s==0){
			list = new String[1];
			list[0]="0";
		}else{
			list = getError(s);
		}
		
		return list;
	}
	
	


	@Override
	public Object getTestData(Object object) {
		// TODO Auto-generated method stub
		PinData data = (PinData)object;

		String[] list = null;
		if(data.style==1){
			list = readPin(data.iEncryType,data.iTimes,data.iLength,data.timeOut);
		}else if(data.style==2){
			list = keyAffusePin(data.keys);
		}
		return list;
	}
	
	@Override
	public byte[] getFinancialOpenCommad() {
		// TODO Auto-generated method stub
		byte[] cmd = {(byte)0x83}; 
			return null;
	}
	
	
	@Override
	public byte[] getFinancialCloseCommad() {
		// TODO Auto-generated method stub
		byte[] cmd = null;
		switch (encryType) {
			case NO_PASSWORD:
			
				break;	
			case DES:
				break;
			case DES3:
				break;

		default:
			break;
		}
		return null;
	}
	
	
	@Override
	protected int readData(byte[] data, int len, int timeOut) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int writeData(byte[] data, int len) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
