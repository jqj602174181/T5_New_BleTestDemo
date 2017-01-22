package com.centerm.finger.wellcom;

import android.util.Log;

import com.centerm.financial.FinancialBase;
import com.centerm.idcard.IDCardDef;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.util.Formater;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.FingerData;



/**
 * 建行维尔指纹仪
 */
public class IFpDevDriver extends FinancialBase{

 
	private static final String TAG = "finger_wellcom";	
	private static final  String companyCode = "WELLCOM_WEL-401AFE-V30_V1.0.3.2_A_R_For-CCB[20150501]";
    private static final int PACKET_LEN = 1024;
    private  static  boolean T5_device = true;//T5 设备
  //使用升腾指令集
  	public static final  byte  SOH				= (0x02);
  	public static final  byte  EOT			    = (0x03);
	 /** 
	  * 构造指令 报文头 + 报文长度 + 报文 + 校验值 + 报文尾
	  * */
	 private   int MakeRequestPacket(byte head, byte end,  byte[] szData, int nDataLen, byte[] szPacket )
	 {
	 	int  nLen = 0;
	 	byte[] ucTemp = new byte[2]; 
	 	byte lrc = 0;

	 	
	 	szPacket[ nLen++ ] = head;

	 	
	 	//长度拆分
	 	ucTemp[0] = (byte)(( nDataLen >>8 )&0xFF);
	 	lrc = ucTemp[0];
	 	Formater.SplitData( ucTemp, 1, szPacket, nLen );
	 	nLen += 2;

	 	ucTemp[0] = (byte)( nDataLen&0xFF );
	 	lrc ^= ucTemp[0];
	 	Formater.SplitData( ucTemp, 1, szPacket, nLen );
	 	nLen += 2;

	 	//内容拆分
	 	Formater.SplitData( szData, nDataLen, szPacket,  nLen );
	 	nLen += 2*nDataLen;

	 	//获取校验值
	 	ucTemp[0]  = Formater.GetDataLRC( szData, nDataLen );
	    lrc ^= ucTemp[0];
	    
	    ucTemp[0] = lrc;
	    Formater.SplitData( ucTemp, 1, szPacket , nLen );
	 	nLen += 2;

	 	szPacket[ nLen++ ] = end;   

	 	return nLen;
	 }


	 
	 /**
	  * 解析报文
	  * */
	 private  int ParseResponseData( byte[] szPacket, int nPacketLen, byte[] szData , int[] pnDataLen )
	 {
	 	byte[]  ucPacketBody = new byte[ PACKET_LEN ];
	 	byte[]  ucBody = new byte[ PACKET_LEN ];
	 	int nBodyLen = 0; 
	 	int nStatus = 0;

	 	nBodyLen = Formater.GetPacketBody( SOH, EOT, szPacket, nPacketLen, ucPacketBody );
	 	if( (nBodyLen < 2)|| ( nBodyLen%2 !=0) )
	 	{
	 		return FingerPrintDef.ERR_PACKAGE_FORMAT;
	 	}

	 	
	 	
	 	Formater.MergeData( ucPacketBody, nBodyLen ,ucBody, 0 );
	 	nBodyLen = nBodyLen/2; 
	     
	    //Log.e(TAG , "ParseResponseData ucBody = " + nBodyLen) ;

		 	if( Formater.GetDataLRC( ucBody, nBodyLen - 1 ) == ucBody[ nBodyLen - 1 ] )
	 	{

	 		nStatus = ucBody[2] & 0xff;
	       //Log.e(TAG,  "ParseResponseData: nStatus = " + nStatus );
	 		//Log.e( TAG, "ParseResponseData: ucBody[2] =  " + StringUtil.bytesToHexString(ucBody) ); 
	 		nBodyLen = ucBody[0]*256+ucBody[1];   
	 		
	 		pnDataLen[0] = nBodyLen-2 ;

	 		System.arraycopy(ucBody, 4, szData, 0, nBodyLen -2);
	 	   
	 		
	 		return nStatus;
	 	}    

	 	return FingerPrintDef.ERR_PACKAGE_FORMAT;
	 }
	

	 
	 //收发指令
	private  int SendAndRecvForCentCmd( int timeOut,  byte[] pszCmd,int nCmdSize, byte[] pszRetData , int[] pDataLen  )
	 {
	 	byte[] szSendData = new byte[ PACKET_LEN ];
	 	byte[] szRecvData = new byte[ PACKET_LEN ];;
	 	int  nSendLen = 0;
	 	int  nRecvLen = 0;
	 	int  nRetVal = FingerPrintDef.ERR_OTHER;
        
		//Log.e( TAG, " callSendAndRecvForCentCmd" );
	 	//构造指令
	 	nSendLen = MakeRequestPacket(SOH, EOT, pszCmd, nCmdSize, szSendData);
	  
	 
	 	//Log.e( TAG, " MakeRequestPacket: szSendData=  " + StringUtil.bytesToHexString(szSendData));
	 	//Log.e( TAG, " MakeRequestPacket: nSendLen =" + nSendLen );
	 	//发送
	 	nRetVal = WriteDataToTransPort(szSendData, nSendLen);
	 	//Log.e(TAG, "WriteDataToTransPort nRetVal =" + nRetVal);
	 	
	 	if(  nRetVal == nSendLen )
	 	{
	 	
	 		nRecvLen = ReadDataFromTransPort( szRecvData, timeOut );
	 		
	 		//Log.e(TAG,  "ReadDataFromTransPort: nRecvLen=" + nRecvLen ) ;
	 		
	 		if( nRecvLen > 0 )
	 		{
	 			nRetVal = ParseResponseData(szRecvData,nRecvLen, pszRetData, pDataLen  );
	 			//Log.e( TAG , "ParseResponseData: nRetVal  " +nRetVal);
	 			if( nRetVal == 0xFF ) 
	 			{
	 				nRetVal = FingerPrintDef.ERR_TIMEOUT;
	 			}
	 					
	 		}	
	 		else
	 		{
	 			nRetVal = FingerPrintDef.ERR_READ;
	 		}
	 	}
	 	else
	 	{
	 		 nRetVal = FingerPrintDef.ERR_WRITE;
	 	}

	 	return nRetVal;
	 }

	//读取设备号
	private  int getFingerDeviceInfo( int  nTimeout, int nDeviceLen, byte[] pszFingerDevice )
	{
		
		Log.e(TAG, "----------------getFingerDeviceInfo------------------------");
		int nRetVal = 0;
		byte[] szRecvBuf= new byte[ PACKET_LEN ];
        int[] nRecvLen = new int[2];
        
        
		nRetVal = SendAndRecvForCentCmd( nTimeout, FingerPrintDef.COMMAND_GETDEVINFO, FingerPrintDef.COMMAND_GETDEVINFO.length, szRecvBuf , nRecvLen );
	    Log.e(TAG, "getFingerDeviceInfo : SendAndRecvForCentCmd-nRetVal = " + nRetVal);
		if(nRetVal == FingerPrintDef.ERR_WRITE)
		{
			return nRetVal;
		}
		else if(nRetVal == FingerPrintDef.ERR_TIMEOUT)
		{
			return nRetVal;
		}
		else if( (0 != nRetVal)&&(0x09 != nRetVal) )
		{
			return FingerPrintDef.ERR_READ;
		}

		nRecvLen[0] = nRecvLen[0] > nDeviceLen ? nDeviceLen : nRecvLen[0];
		
		System.arraycopy(szRecvBuf, 0, pszFingerDevice, 0, nRecvLen[0]);
		
		return nRecvLen[0];
	}
    
	//读取指纹特征
	private  int getFingerPrintCharacter( int  nTimeout, int nCharacterLen, byte[] pszCharacter)
	{
		
		Log.e(TAG, "----------------getFingerPrintCharacter------------------------");
		int nRetVal = 0;
		byte[] szRecvBuf= new byte[ PACKET_LEN ];
        int[] nRecvLen = new int[2];
        byte[] szCmd = new byte[4];
        
        System.arraycopy(  FingerPrintDef.COMMAND_GETFINGRCH,  0 , szCmd, 0,  FingerPrintDef.COMMAND_GETFINGRCH.length );
        szCmd[ 1 ] = (byte)(nTimeout);
        
		nRetVal = SendAndRecvForCentCmd( nTimeout, szCmd, 4 , szRecvBuf , nRecvLen );
		Log.e(TAG, "getFingerPrintCharacter : SendAndRecvForCentCmd-nRetVal = " + nRetVal);
		if(nRetVal == FingerPrintDef.ERR_WRITE)
		{
			return nRetVal;
		}
		else if(nRetVal == FingerPrintDef.ERR_TIMEOUT)
		{
			return nRetVal;
		}
		else if( (0 != nRetVal)&&(0x09 != nRetVal) )
		{
			return FingerPrintDef.ERR_READ;
		}

		nRecvLen[0] = nRecvLen[0] > nCharacterLen ? nCharacterLen : nRecvLen[0];
		
		System.arraycopy(szRecvBuf, 0, pszCharacter, 0, nRecvLen[0]);
		
		return nRecvLen[0];
	}
	
	//登记指纹
	private  int getFingerPrintEnroll ( int  nTimeout, int nFingerTemplateLen, byte[]  pszFingerTemplate)
	{
		Log.e(TAG, "----------------getFingerPrintEnroll------------------------");
		
		int nRetVal = 0;
		byte[] szRecvBuf= new byte[ PACKET_LEN ];
        int[] nRecvLen = new int[2];
        byte[] szCmd = new byte[4];
        
        System.arraycopy(  FingerPrintDef.COMMAND_GETENROLL,  0 , szCmd, 0,  FingerPrintDef.COMMAND_GETENROLL.length );
        szCmd[ 1 ] = (byte)(nTimeout);
        
		nRetVal = SendAndRecvForCentCmd( nTimeout, szCmd, 4, szRecvBuf , nRecvLen );
		Log.e(TAG, "getFingerPrintEnroll : SendAndRecvForCentCmd-nRetVal = " + nRetVal);
		if( nRetVal != 0)
		{
			return nRetVal;
		}
		

		nRecvLen[0] = nRecvLen[0] > nFingerTemplateLen ? nFingerTemplateLen : nRecvLen[0];
		
		System.arraycopy(szRecvBuf, 0, pszFingerTemplate, 0, nRecvLen[0]);
		
		return nRecvLen[0];
	}
	
	
	
	
	/**
	* 读取指纹模板
	* 
	* @param
	* strCompanyCode : 指纹仪厂商代码
	* strTimeout     : 超时间隔
	*/
	public String[] registerFinger(String strCompanyCode, String strTimeout){
	
		int time = getTime(strTimeout);
        int style = 0;
		if(time==-1){
			
			return getParamErr();
		}

		
		//开机动画指令
		if(T5_device)
		{
			Log.e( TAG, "-----------------T5_StartPlayDemo-------------" );
			 style = T5_StartPlayDemo( 2 );
			 Log.e(TAG,"T5_StartPlayDemo: nRet is = " + style);
			 if(style != FingerPrintDef.ERR_SUCCESS)
			 {
				 T5_ClosePlayDemo( );
				 style = FingerPrintDef.ERR_OPENFAILED;
				return getFailOpenErr();
			 }
			
		}
		
		StringBuffer buffer = new StringBuffer();
		byte [] info = new byte[packLength];
		int l = getFingerDeviceInfo( time,info.length,info);
		
		if(l>0){
			String str = new String(info,0,info.length).trim();
			if(str.equals(strCompanyCode)){
				byte[] data = new byte[1024];
			    style = getFingerPrintEnroll( time,data.length,data);
				if(style>0){
					str = new String(data,0,data.length).trim();
					buffer.append(sRight);
					buffer.append(split);
					buffer.append(strCompanyCode);
					buffer.append(split);
					buffer.append(StringUtil.HexToStringA(str.getBytes()));
				}else{
					buffer.append(sError);
					buffer.append(split);
					buffer.append(getError(style));
					
				}
				
			}else{
				buffer.append(sError);
				buffer.append(split);
				buffer.append(RetUtil.Unknown_Err);
			}
			
			
		}
		
		//关闭动画指令
		if(T5_device)
		{
			style = T5_ClosePlayDemo( );
			 Log.e(TAG,"T5_ClosePlayDemo: nRet is = " + style);
			 if(style != FingerPrintDef.ERR_SUCCESS)
			 {
				 return getUnknownErr();
			 }
		}
	
		return buffer.toString().split(split);
	}
	
	private String[] getFailOpenErr()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(sError);
		buffer.append(split);
		buffer.append(RetUtil.Open_Serial_Err);
		
		return  buffer.toString().split(split);
		
	}
	
	private String getError(int style){
		

		String str =RetUtil.Unknown_Err;
		switch(style){
			case -1:
				str =RetUtil.Open_Serial_Err;
				break;
			case -2:
			case -3:
				str = RetUtil.Send_Mess_Err;
				break;
			case -8://超时
				str  = RetUtil.Timeout_Err;
				break;
			default:
				break;
					
		}
		return str;
	}
	
	/**
	* 读取指纹特征
	* 
	* @param
	* strCompanyCode : 指纹仪厂商代码
	* strTimeout     : 超时间隔
	*/
	
	
public String[] readFinger(String strCompanyCode, String strTimeout){
		
		int time = getTime(strTimeout);
		int style = 0;
		if(time==-1){
		
			return getParamErr();
		}
		
		StringBuffer buffer = new StringBuffer();
		//开机动画指令
		if(T5_device)
		{
			 style = T5_StartPlayDemo( 2 );
			 Log.e(TAG,"T5_StartPlayDemo: nRet is = " + style);
			 if(style != FingerPrintDef.ERR_SUCCESS)
			 {
				 T5_ClosePlayDemo( );
				 style = FingerPrintDef.ERR_OPENFAILED;
				return getFailOpenErr();
			 }
			
		}
	
		byte [] info = new byte[packLength];
		int l = getFingerDeviceInfo( time,info.length,info);
		//Log.e(TAG, "readFinger: getFingerDeviceInfo: nRet =" + l );

		
		if(l>0){
			String str = new String(info,0,info.length).trim();
	
			if(str.equals(strCompanyCode)){
				byte[] data = new byte[1024];
			     style = getFingerPrintCharacter( time,data.length,data);
				if(style>0){
					str = new String(data,0,data.length).trim();
					buffer.append(sRight);
					buffer.append(split);
					buffer.append(strCompanyCode);
					buffer.append(split);
					buffer.append(StringUtil.HexToStringA(str.getBytes()));
				}else{
					buffer.append(sError);
					buffer.append(split);
					buffer.append(getError(style));
					
				}
				
			}else{
				buffer.append(sError);
				buffer.append(split);
				buffer.append(RetUtil.Unknown_Err);
			}
			
			
		}
		
		//关闭动画指令
		if(T5_device)
		{
			style = T5_ClosePlayDemo( );
			 Log.e(TAG,"T5_ClosePlayDemo: nRet is = " + style);
			 if(style != FingerPrintDef.ERR_SUCCESS)
			 {
				 return getUnknownErr();
			 }
		}
	
		return buffer.toString().split(split);
	}
	
	@Override
	public Object getTestData(Object object) {
		// TODO Auto-generated method stub
		
		FingerData data = (FingerData)object;
		String[] strList = null;
		if(data.style==2){
			strList = registerFinger(data.companyCode,data.timeOut);
		}else if(data.style==1){
			strList = readFinger(data.companyCode,data.timeOut);
		}
	
		return strList;
	}

	@Override
	public byte[] getFinancialOpenCommad() {
		// TODO Auto-generated method stub
		return BusinessInstruct.OpenBusiness((byte)0x34);
	}

	@Override
	public byte[] getFinancialCloseCommad() {
		// TODO Auto-generated method stub
		//return null;
		return BusinessInstruct.CloseBusiness((byte)0x34);
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
