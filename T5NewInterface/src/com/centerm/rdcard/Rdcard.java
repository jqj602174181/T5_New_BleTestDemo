package com.centerm.rdcard;

import android.util.Log;

import com.centerm.financial.FinancialBase;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.util.Formater;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.MsgCardData;


/**
 * 磁条读卡器
 */
public class Rdcard extends FinancialBase{
	private MsgCardData msgCardData;
    private  final  boolean T5_device = true;//T5 设备
	private static boolean s_bCancel = false;
	private static boolean  USE_CENT_CMD = false; //使用升腾指令集，不使用则默认用南天指令集
	private static int  READ_TIME  = 2;
    private static String TAG = "MsgCard";
    private static final int PACKET_LEN = 1024;
	//常量定义
	public static final int   MAXLEN_TRCKDATA         = 1024;



	//磁道位置定义
	public static final   int  MODE_TRACK1            =  1;
	public static final   int  MODE_TRACK2            =  2;
	public static final 	int  MODE_TRACK3            =  3;
	public static final 	int  MODE_TRACK12           = 12;
	public static final 	int  MODE_TRACK23           = 23;

	
	public Rdcard()
	{
		msgCardData = new MsgCardData();
	}
	
   
   int SetFormat( int nType )
   {
	   int nRet = 0;

	   WriteDataToTransPort(MsgCardDef.COMMAND_RESET, 2 );
	   switch( nType)
	   {
		   	case 0:
		   	{
		   		
		   		nRet = WriteDataToTransPort( MsgCardDef.COMMAND_ISO, MsgCardDef.COMMAND_ISO.length );
		   		break;
		   	}
		   	
		   	case 1:
		   	{
		   		nRet = WriteDataToTransPort( MsgCardDef.COMMAND_IBM, MsgCardDef.COMMAND_IBM.length );
		   		break;
		   	}
			 
		   	case 2:
		   	{
		   		nRet = WriteDataToTransPort( MsgCardDef.COMMAND_ISO_D, MsgCardDef.COMMAND_ISO_D.length );
		   		break;
		   	}
		   	
		   	 case 3:
		    {
		    	nRet = WriteDataToTransPort( MsgCardDef.COMMAND_IBM_D, MsgCardDef.COMMAND_IBM_D.length );
		   		break;

		    }
		    
		    default:
		    {
		    	nRet = WriteDataToTransPort( MsgCardDef.COMMAND_ISO, MsgCardDef.COMMAND_ISO.length );
		   		break;

		    }
	   }
	   //Log.e( TAG, "SetFormat: nRet = " + nRet );
	   return nRet;
   }
	
	
	
   private void SeperateByTrays(  final byte[] data, final int dataLen,  final byte[] szTrackBegin, final byte[] szTrackEnd )
   {
   	int pos = -1;
   	int i = 0;
   	int inums = 0;
   	int inumz = 0;
   	for( i = 0; i < dataLen-1 ; i++ )
   	{
   		if( data[i] == 0x41 )
   		{   
   			pos = i;
   			break;
   		}
   	}
   	
   	//Log.e( TAG, "SeperateByTrays, data = "+ StringUtil.bytesToHexString(data));
   	if(pos > 0)
   	{
   		pos++;
   		// 41h之前的磁道数据
   		inums = pos -3;
   		if( inums > 0)
   		{
   			System.arraycopy(data, 2, szTrackBegin , 0, inums);
   		}
   		
   	//	Log.e( TAG, "szTrackBegin="+ StringUtil.bytesToHexString(szTrackBegin));
   		//41h之后的磁道数据
   		pos = 2 + inums + 1 ;
   		inumz = dataLen - 2 -pos;
   		
   		if( inumz > 0 )
   		{
   			System.arraycopy(data, pos, szTrackEnd , 0, inumz);
   		//	Log.e(TAG, "szTrackEnd=" + StringUtil.bytesToHexString(szTrackEnd));
   		}
   		
   	}
   	
   	
   }
    
    
    
	/*****************************************************************/
	/***************************升腾指令********************************/
	/*****************************************************************/
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
	 	Formater.SplitData( ucTemp, 1, szPacket, nLen );
	 	nLen += 2;

	 	//内容拆分
	 	Formater.SplitData( szData, nDataLen, szPacket,  nLen );
	 	nLen += 2*nDataLen;

	 	//获取校验值
	 	ucTemp[0]  = Formater.GetDataLRC( szData, nDataLen );


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

	 	nBodyLen = Formater.GetPacketBody( MsgCardDef.SOH, MsgCardDef.EOT, szPacket, nPacketLen, ucPacketBody );
	 	if( (nBodyLen < 2)|| ( nBodyLen%2 !=0) )
	 	{
	 		return MsgCardDef.ERR_PACKAGE_FORMAT;
	 	}

	 	
	 	
	 	Formater.MergeData( ucPacketBody, nBodyLen ,ucBody, 0 );
	 	nBodyLen = nBodyLen/2; 
	     
	   //Log.e(TAG , "ParseResponseData ucBody = " + nBodyLen) ;

		 	if( Formater.GetDataLRC( ucBody, nBodyLen - 1 ) == ucBody[ nBodyLen - 1 ] )
	 	{

	 		nStatus = ucBody[2] & 0xff;
	       // Log.e(TAG,  "ParseResponseData: nStatus = " + nStatus );
	 		//Log.e( TAG, "ParseResponseData: ucBody[2] =  " + StringUtil.bytesToHexString(ucBody) ); 
	 		nBodyLen = ucBody[0]*256+ucBody[1];   
	 		
	 		pnDataLen[0] = nBodyLen-2 ;

	 		System.arraycopy(ucBody, 4, szData, 0, nBodyLen -2);
	 	   
	 		
	 		return nStatus;
	 	}    

	 	return MsgCardDef.ERR_PACKAGE_FORMAT;
	 }
	

	 
	 //收发指令
	private  int SendAndRecvForCentCmd( int timeOut,  byte[] pszCmd,int nCmdSize, byte[] pszRetData , int[] pDataLen  )
	 {
	 	byte[] szSendData = new byte[ PACKET_LEN ];
	 	byte[] szRecvData = new byte[ PACKET_LEN ];;
	 	int  nSendLen = 0;
	 	int  nRecvLen = 0;
	 	int  nRetVal = MsgCardDef.ERR_OTHER;
       
		//Log.e( TAG, " callSendAndRecvForCentCmd" );
	 	//构造指令
	 	nSendLen = MakeRequestPacket(MsgCardDef.SOH, MsgCardDef.EOT, pszCmd, nCmdSize, szSendData);
	  
	 
	 	//Log.e( TAG, " MakeRequestPacket: szSendData=  " + StringUtil.bytesToHexString(szSendData));
	 	//Log.e( TAG, " MakeRequestPacket: nSendLen =" + nSendLen );
	 	//发送
	 	nRetVal = WriteDataToTransPort(szSendData, nSendLen);
	 	Log.e(TAG, "WriteDataToTransPort nRetVal =" + nRetVal);
	 	
	 	if(  nRetVal == nSendLen )
	 	{
	 	
	 		//Log.e(TAG,  "ReadDataFromTransPort: timeOut=" + timeOut ) ;
	 		nRecvLen = ReadDataFromTransPort( szRecvData, timeOut );
	 		//Log.e(TAG,  "ReadDataFromTransPort: nRecvLen=" + nRecvLen ) ;
	 		
	 		if( nRecvLen > 0 )
	 		{
	 			nRetVal = ParseResponseData(szRecvData,nRecvLen, pszRetData, pDataLen  );
	 			//Log.e( TAG , "ParseResponseData: nRetVal  " +nRetVal);
	 			if( nRetVal != 0 ) 
	 			{
	 				nRetVal = MsgCardDef.ERR_OTHER;
	 			}
	 					
	 		}	
	 		
	 	}
	 	
	 	
	 	return nRetVal;
	 }

	private void SetReadMsgTimeOut(int nTimeOut)
	{
		byte[] szCmd = new byte[PACKET_LEN];
		byte[] szRetData = new byte[PACKET_LEN];
		int[] nDataLen = new int[2];
		System.arraycopy(MsgCardDef.CENT_COMMAND_TIMEOUT, 0 , szCmd, 0, MsgCardDef.CENT_COMMAND_TIMEOUT.length );
        szCmd[2] = (byte)nTimeOut;
       
        //Log.e(TAG, "SetReadMsgTimeOut"+ StringUtil.bytesToHexString(szCmd).trim());
        SendAndRecvForCentCmd(  READ_TIME,  szCmd, 3, szRetData , nDataLen );

	}
	
	/*****************************************************************/
	/***************************南天指令********************************/
	/*****************************************************************/
	
	
	private static byte[] satusStart = {  0x1B, 0x73 };
	private static byte[] satusEnd = {  0x3F , 0x1C };
	
	

	/**
	*
	* 函数名称: ReadTrack
	* 函数功能: 读磁道
	*  
	*@param     szCommand	[in]    -读磁道命令
	*@param     szBuff		[out]	-用来保存读出来的数据
	*@param     szBuffsize [out ]  -用来保存实际读取的数据
	*@param     timeout  [in]      -超时时间
	*@return
	*            0            -读成功
	*            非0          -读失败
	*/
	private int ReadTrack( byte[] szCommand, byte[] szBuff, int[] szBuffszie, int timeout )
	{
		int   nRet = MsgCardDef.ERR_READ_CARD;
		byte[]  szStatus = new byte[PACKET_LEN];
		int nWriteLen = szCommand.length;
       
		//Log.e( TAG, "---------ReadTrack-----------");
		//发送命令
		if ( WriteDataToTransPort( szCommand, nWriteLen ) < nWriteLen )
		{
			return MsgCardDef.ERR_WRITE;
		}
		
		
		nRet = ReadDataFromTransPort( szBuff, timeout);
		//Log.e( TAG, "ReadDataFromTransPort----2");
		if ( nRet < 0 )
		{
			return MsgCardDef.ERR_TIMEOUT;
		}
		szBuffszie[ 0 ] = nRet; 
		//Log.e(TAG, "ReadTrack: readLen=" + nRet);
		//Log.e(TAG, "ReadTrack: readdata" + StringUtil.bytesToHexString( szBuff ).trim() );

		//检测包格式是否正确
		if ( StringUtil.isEquals( szBuff, 2, satusStart ) != true ||
		     StringUtil.isSubByte( szBuff, satusEnd) == false)
		{
			return MsgCardDef.ERR_PACKAGE_FORMAT;
		}

		//获取状态
		nWriteLen = MsgCardDef.COMMAND_STATUS.length;
		if( WriteDataToTransPort( MsgCardDef.COMMAND_STATUS, nWriteLen ) < nWriteLen )
		{
			return MsgCardDef.ERR_WRITE;
		}

	
		nRet = ReadDataFromTransPort( szStatus, READ_TIME);
		if ( nRet < 0 )
		{
			return MsgCardDef.ERR_READ;
		}

		//Log.e(TAG, "ReadTrack: getstatus = " + StringUtil.bytesToHexString(szStatus ).trim() );
	
		int szStatusLen = nRet;
		//Log.e(TAG, "ReadTrack: getstatus: szStatusLen= " +szStatusLen );
		if ( (szStatus[ szStatusLen -1 ] != 0x70 ) && (szStatus[ szStatusLen -1 ] != 0x71 ) )
		{
			return MsgCardDef.ERR_PACKAGE_FORMAT;
		}
		
		if ( szStatus[ szStatusLen -1 ] == 0x70 )
		{
			return MsgCardDef.ERR_SUCCESS;
		}
		
		
		return MsgCardDef.ERR_READ_CARD;
	}

	
	
	
	/**
	* 函数名称: ReadTrack1
	* 函数功能: 读磁道1
	*@param  szBuff		[out]	-用来保存读出来的磁道1数据
	*@param  timeout  [in]    -超时时间
	*@return 
	*            0            -读成功
	*            非0          -读失败
	**/
	private int  ReadTrack1( byte[] szBuff,  int  timeout )
	{
		byte[] szTmpData = new byte[ MAXLEN_TRCKDATA ];
		int[] readDataLen = new int[2];
		
		int nRet = ReadTrack( MsgCardDef.COMMAND_RD1, szTmpData, readDataLen,  timeout );
		if ( nRet != MsgCardDef.ERR_SUCCESS )
		{
			return nRet;
		}
		
		
		System.arraycopy(szTmpData , 2, szBuff, 0, readDataLen[0] - 4 );
		return nRet;
	}

	
	/**
	* 函数名称: ReadTrack2
	* 函数功能: 读磁道2
	*@param  szBuff		[out]	-用来保存读出来的磁道1数据
	*@param  timeout  [in]    -超时时间
	*@return 
	*            0            -读成功
	*            非0          -读失败
	**/
	private int  ReadTrack2( byte[] szBuff,  int  timeout )
	{
		byte[] szTmpData = new byte[ MAXLEN_TRCKDATA ];
		int[] readDataLen = new int[2];
		
		int nRet = ReadTrack( MsgCardDef.COMMAND_RD2, szTmpData, readDataLen,  timeout );
		if ( nRet != MsgCardDef.ERR_SUCCESS )
		{
			return nRet;
		}
		
		
		System.arraycopy(szTmpData , 2, szBuff, 0, readDataLen[0] - 4 );
		return nRet;
	}
	
	/**
	* 函数名称: ReadTrack3
	* 函数功能: 读磁道3
	*@param  szBuff		[out]	-用来保存读出来的磁道1数据
	*@param  timeout  [in]    -超时时间
	*@return 
	*            0            -读成功
	*            非0          -读失败
	**/
	
	private int  ReadTrack3( byte[] szBuff,  int  timeout )
	{
		byte[] szTmpData = new byte[ MAXLEN_TRCKDATA ];
		int[] readDataLen = new int[2];
		
		int nRet = ReadTrack( MsgCardDef.COMMAND_RD3, szTmpData, readDataLen,  timeout );
		if ( nRet != MsgCardDef.ERR_SUCCESS )
		{
			return nRet;
		}
		
		
		System.arraycopy(szTmpData , 3, szBuff, 0, readDataLen[0] - 5 );
		return nRet;
	}
	
	
	/**
	 ** 函数名称: ReadTrack12
	 * 函数功能: 读磁道1与磁道2 
	 *@param            szTrack1		  [out]	-用来保存读出来的磁道1数据
	 *@param            szTrack2		  [out]	-用来保存读出来的磁道2数据
 	 *@param            timeout [int] -超时时间
	 **@return 
	*            0            -读成功
	*            非0          -读失败
	 */
	private int ReadTrack12 ( byte[] szTrack1, byte[] szTrack2 , int timeout)
	{

		byte[] szTmpData = new byte[ MAXLEN_TRCKDATA  ];
		int[] readDataLen = new int[2];
		
		int nRet = ReadTrack( MsgCardDef.COMMAND_RD12, szTmpData, readDataLen,  timeout );
		if ( nRet != MsgCardDef.ERR_SUCCESS )
		{
			return nRet;
		}
		 
		SeperateByTrays( szTmpData , readDataLen[0], szTrack1, szTrack2 );
		return nRet;
		
	}
	
	/**
	 ** 函数名称: ReadTrack23
	 * 函数功能: 读磁道2与磁道3 
	 *@param            szTrack2		  [out]	-用来保存读出来的磁道1数据
	 *@param            szTrack3		  [out]	-用来保存读出来的磁道2数据
	 *@param          timeout [int] -超时时间
	 **@return 
	*            0            -读成功
	*            非0          -读失败
	 */
	
	private int ReadTrack23 ( byte[] szTrack2, byte[] szTrack3, int timeout )
	{

		byte[] szTmpData = new byte[ MAXLEN_TRCKDATA ];
		int[] readDataLen = new int[2];
		
		int nRet = ReadTrack( MsgCardDef.COMMAND_RD23, szTmpData, readDataLen,  timeout );
		Log.e( TAG , "ReadTrack23: ReadTrack: nRet =" + nRet );
		if ( nRet != MsgCardDef.ERR_SUCCESS )
		{
			return nRet;
		}
		 
		SeperateByTrays( szTmpData , readDataLen[0], szTrack2, szTrack3 );
		return nRet;
		
	}
	
	public  int ReadMsgCard( int nCharset,int nMode , int timeOut, 
			byte[] szBuff1,  int nBuff1Len, 
			byte[] szBuff2,   int nBuff2Len,
			byte[] szBuff3,   int nBuff3Len)
			
	{
		int nRet = MsgCardDef.ERR_READ_CARD;
		byte[]  szMsgData = new byte[ MAXLEN_TRCKDATA ];
		byte[] szTmp1  = new byte[ MAXLEN_TRCKDATA ];
		byte[] szTmp2  = new byte[ MAXLEN_TRCKDATA ];
		byte[] szTmp3  = new byte[ MAXLEN_TRCKDATA ];
		
		s_bCancel = false;
		long dwStart   = System.currentTimeMillis();
		
		
	
		//开机动画
		if(T5_device)
		{
			Log.e( TAG, "-----------T5_StartPlayDemo---------------");
			 nRet = T5_StartPlayDemo( READ_TIME );
			 Log.e(TAG,"T5_StartPlayDemo: nRet is = " + nRet);
			 if(nRet != 0)
			 {
				 T5_ClosePlayDemo( );
				 nRet = MsgCardDef.ERR_OPENFAILED;
				 return nRet;
			 }
			
		}
		
		//设置磁道格式
		SetFormat( nCharset);
		
		
		//设置超时时间
		SetReadMsgTimeOut( timeOut );
		
		//Log.e(TAG, "after TimeOutSet");
		if(USE_CENT_CMD ) //升腾指令
		{
			
		}
		
		else //南天指令
		{
			switch ( nMode )
			{
			case MODE_TRACK1:
				nRet = ReadTrack1( szTmp1, timeOut);
				break;
			case MODE_TRACK2:
				nRet = ReadTrack2( szTmp2,timeOut );
				break;
			case MODE_TRACK3:
				nRet = ReadTrack3( szTmp3, timeOut );
				break;
			case MODE_TRACK12:
				nRet = ReadTrack12( szTmp1,  szTmp2, timeOut);
				break;
			case MODE_TRACK23:
				nRet = ReadTrack23( szTmp2, szTmp3, timeOut );
				break;
			}
			Log.e(TAG, "ReadMsgCard:  ReadTrack: nRet = " + nRet );
	
			if (nRet == MsgCardDef.ERR_SUCCESS )
			{
				if ( null != szBuff1 )
				{
					System.arraycopy(szTmp1, 0, szBuff1, 0, nBuff1Len);
				}
				if ( null != szBuff2 )
				{
					System.arraycopy(szTmp2, 0, szBuff2, 0, nBuff2Len);
				}
				if ( null != szBuff3 )
				{
					System.arraycopy(szTmp2, 0, szBuff2, 0, nBuff2Len);
				}
			
			}
	
		}
	
		
		WriteDataToTransPort( MsgCardDef.COMMAND_RESET, 2 );//复位
	
		//关闭动画指令
		if(T5_device)
		{
			int retval = T5_ClosePlayDemo( );
			 Log.e(TAG,"T5_ClosePlayDemo: nRet is = " + retval);
			 if(retval != MsgCardDef.ERR_SUCCESS)
			 {
				 nRet = MsgCardDef.ERR_OTHER;
				 return nRet;
			 }
		}
	
		if(s_bCancel)
		{
			nRet = MsgCardDef.ERR_CANCELED;
		}
		else if ( System.currentTimeMillis() - dwStart > timeOut*1000)
		{
			nRet = MsgCardDef.ERR_TIMEOUT;
		}
		
	
		
		return nRet;
		
	}
	
	
	/**
	*
	* 函数名称: CancelReadMsgCard
	* 函数功能: 取消读磁条卡
	*/
	public  int CancelReadMsgCard( )
	{
		if( !s_bCancel )
		{
			s_bCancel = true;
			
			WriteDataToTransPort(MsgCardDef.COMMAND_RESET, 2);
			return MsgCardDef.ERR_CANCELED;
		}
		return MsgCardDef.ERR_OPENFAILED;  
	}
	
	
	
	public  int CT_ReadMsgCard( int nCharset,int nTimeout,byte[] szTrack1,int nTrack1Len,byte[] szTrack2,int nTrack2Len,byte[] szTrack3,int nTrack3Len )
	{
		Log.e(TAG, "CT_ReadMsgCard: nTimeout = "+ nTimeout );
		int nRet = MsgCardDef.ERR_READ_CARD;
		
		if ( null != szTrack1 && null == szTrack2 && null == szTrack3 )   //读1磁道
		{;
			nRet = ReadMsgCard( nCharset,MODE_TRACK1, nTimeout , szTrack1, nTrack1Len,  null, 0, null, 0);
			
		}
		else if ( null == szTrack1 && null != szTrack2 && null == szTrack3 ) //读2磁道
		{
			nRet = ReadMsgCard(nCharset,  MODE_TRACK2, nTimeout, null, 0, szTrack2, nTrack2Len,  null, 0 );
		}
		else if ( null == szTrack1 && null == szTrack2 && null != szTrack3 ) //读3磁道
		{
			nRet = ReadMsgCard( nCharset, MODE_TRACK3, nTimeout, null, 0, null, 0, szTrack3, nTrack3Len );
		}
		else if ( null != szTrack1 && null != szTrack2 && null == szTrack3 ) //读12磁道
		{
			
			nRet = ReadMsgCard(  nCharset, MODE_TRACK12, nTimeout, szTrack1, nTrack1Len, szTrack2, nTrack2Len,  null, 0 );
		}
		else if ( null == szTrack1 && null != szTrack2 && null != szTrack3 )  //读23磁道
		{

			nRet = ReadMsgCard(  nCharset, MODE_TRACK23, nTimeout, null, 0, szTrack2, nTrack2Len,  szTrack3, nTrack3Len );
		}
		
		return nRet;
		
	}
	
	
	
	/**
	* 读存折磁道
	* 
	* @param strTimeout 读存折超时间隔 默认20秒
	*/
	public String[] getBookAcct(String strTimeout)
	{
		
			int time = getTime(strTimeout);
			
			if(time==-1){
				return getParamErr();
			}

			byte[] szMsgData2 = new byte[512];
			szMsgData2[0] = '1';
			byte[] szMsgData3 = new byte[512];
			szMsgData3[0] = '1';
			int retval =0;
		
			StringBuilder builder = new StringBuilder();
			String msgCardNo2 = null;
			String msgCardNo3 = null;
			retval = CT_ReadMsgCard( 0,time,null,0,szMsgData2,szMsgData2.length,szMsgData3,szMsgData3.length);
			Log.e( TAG, "getBookAcct: CT_ReadMsgCard: retval =" + retval );
			if(retval==0){
				msgCardNo2 = new String(szMsgData2,0,64).trim();
				msgCardNo3 = new String(szMsgData3,0,64).trim();
				builder.append(sRight);
				builder.append(split);
				builder.append(msgCardNo2+"#"+msgCardNo3);
		
			}else{
			
			
				builder.append(sError);
				builder.append(split);
				
				switch (retval) {
					case -1:
						builder.append(RetUtil.Unknown_Err);
						break;
					case -2:
					case -3:
						builder.append(RetUtil.Send_Mess_Err);
						break;
					case -8:
						builder.append(RetUtil.Timeout_Err);
						break;

				default:
					builder.append(RetUtil.Unknown_Err);
					break;
				}
				
	
			}
			return builder.toString().split(split);
	}

	

	
	
	@Override
	public byte[] getFinancialOpenCommad() {
		// TODO Auto-generated method stub
		return BusinessInstruct.OpenBusiness((byte)0x33);
	}

	@Override
	public byte[] getFinancialCloseCommad() {
		// TODO Auto-generated method stub
		return BusinessInstruct.CloseBusiness((byte)0x33);
	}
	@Override
	public Object getTestData(Object object) {
		// TODO Auto-generated method stub
		MsgCardData data = (MsgCardData)object;
		return getBookAcct(data.timeOut);
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
