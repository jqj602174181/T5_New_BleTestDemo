package com.centerm.rdcard;

import android.util.Log;

import com.centerm.financial.FinancialBase;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.util.Formater;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.MsgCardData;


/**
 * ����������
 */
public class Rdcard extends FinancialBase{
	private MsgCardData msgCardData;
    private  final  boolean T5_device = true;//T5 �豸
	private static boolean s_bCancel = false;
	private static boolean  USE_CENT_CMD = false; //ʹ������ָ�����ʹ����Ĭ��������ָ�
	private static int  READ_TIME  = 2;
    private static String TAG = "MsgCard";
    private static final int PACKET_LEN = 1024;
	//��������
	public static final int   MAXLEN_TRCKDATA         = 1024;



	//�ŵ�λ�ö���
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
   		// 41h֮ǰ�Ĵŵ�����
   		inums = pos -3;
   		if( inums > 0)
   		{
   			System.arraycopy(data, 2, szTrackBegin , 0, inums);
   		}
   		
   	//	Log.e( TAG, "szTrackBegin="+ StringUtil.bytesToHexString(szTrackBegin));
   		//41h֮��Ĵŵ�����
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
	/***************************����ָ��********************************/
	/*****************************************************************/
    /** 
	  * ����ָ�� ����ͷ + ���ĳ��� + ���� + У��ֵ + ����β
	  * */
	 private   int MakeRequestPacket(byte head, byte end,  byte[] szData, int nDataLen, byte[] szPacket )
	 {
	 	int  nLen = 0;
	 	byte[] ucTemp = new byte[2]; 
	 	byte lrc = 0;

	 	
	 	szPacket[ nLen++ ] = head;

	 	
	 	//���Ȳ��
	 	ucTemp[0] = (byte)(( nDataLen >>8 )&0xFF);
	 	lrc = ucTemp[0];
	 	Formater.SplitData( ucTemp, 1, szPacket, nLen );
	 	nLen += 2;

	 	ucTemp[0] = (byte)( nDataLen&0xFF );
	 	Formater.SplitData( ucTemp, 1, szPacket, nLen );
	 	nLen += 2;

	 	//���ݲ��
	 	Formater.SplitData( szData, nDataLen, szPacket,  nLen );
	 	nLen += 2*nDataLen;

	 	//��ȡУ��ֵ
	 	ucTemp[0]  = Formater.GetDataLRC( szData, nDataLen );


	    Formater.SplitData( ucTemp, 1, szPacket , nLen );
	 	nLen += 2;

	 	szPacket[ nLen++ ] = end;   

	 	return nLen;
	 }

 
	 
	 /**
	  * ��������
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
	

	 
	 //�շ�ָ��
	private  int SendAndRecvForCentCmd( int timeOut,  byte[] pszCmd,int nCmdSize, byte[] pszRetData , int[] pDataLen  )
	 {
	 	byte[] szSendData = new byte[ PACKET_LEN ];
	 	byte[] szRecvData = new byte[ PACKET_LEN ];;
	 	int  nSendLen = 0;
	 	int  nRecvLen = 0;
	 	int  nRetVal = MsgCardDef.ERR_OTHER;
       
		//Log.e( TAG, " callSendAndRecvForCentCmd" );
	 	//����ָ��
	 	nSendLen = MakeRequestPacket(MsgCardDef.SOH, MsgCardDef.EOT, pszCmd, nCmdSize, szSendData);
	  
	 
	 	//Log.e( TAG, " MakeRequestPacket: szSendData=  " + StringUtil.bytesToHexString(szSendData));
	 	//Log.e( TAG, " MakeRequestPacket: nSendLen =" + nSendLen );
	 	//����
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
	/***************************����ָ��********************************/
	/*****************************************************************/
	
	
	private static byte[] satusStart = {  0x1B, 0x73 };
	private static byte[] satusEnd = {  0x3F , 0x1C };
	
	

	/**
	*
	* ��������: ReadTrack
	* ��������: ���ŵ�
	*  
	*@param     szCommand	[in]    -���ŵ�����
	*@param     szBuff		[out]	-�������������������
	*@param     szBuffsize [out ]  -��������ʵ�ʶ�ȡ������
	*@param     timeout  [in]      -��ʱʱ��
	*@return
	*            0            -���ɹ�
	*            ��0          -��ʧ��
	*/
	private int ReadTrack( byte[] szCommand, byte[] szBuff, int[] szBuffszie, int timeout )
	{
		int   nRet = MsgCardDef.ERR_READ_CARD;
		byte[]  szStatus = new byte[PACKET_LEN];
		int nWriteLen = szCommand.length;
       
		//Log.e( TAG, "---------ReadTrack-----------");
		//��������
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

		//������ʽ�Ƿ���ȷ
		if ( StringUtil.isEquals( szBuff, 2, satusStart ) != true ||
		     StringUtil.isSubByte( szBuff, satusEnd) == false)
		{
			return MsgCardDef.ERR_PACKAGE_FORMAT;
		}

		//��ȡ״̬
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
	* ��������: ReadTrack1
	* ��������: ���ŵ�1
	*@param  szBuff		[out]	-��������������Ĵŵ�1����
	*@param  timeout  [in]    -��ʱʱ��
	*@return 
	*            0            -���ɹ�
	*            ��0          -��ʧ��
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
	* ��������: ReadTrack2
	* ��������: ���ŵ�2
	*@param  szBuff		[out]	-��������������Ĵŵ�1����
	*@param  timeout  [in]    -��ʱʱ��
	*@return 
	*            0            -���ɹ�
	*            ��0          -��ʧ��
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
	* ��������: ReadTrack3
	* ��������: ���ŵ�3
	*@param  szBuff		[out]	-��������������Ĵŵ�1����
	*@param  timeout  [in]    -��ʱʱ��
	*@return 
	*            0            -���ɹ�
	*            ��0          -��ʧ��
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
	 ** ��������: ReadTrack12
	 * ��������: ���ŵ�1��ŵ�2 
	 *@param            szTrack1		  [out]	-��������������Ĵŵ�1����
	 *@param            szTrack2		  [out]	-��������������Ĵŵ�2����
 	 *@param            timeout [int] -��ʱʱ��
	 **@return 
	*            0            -���ɹ�
	*            ��0          -��ʧ��
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
	 ** ��������: ReadTrack23
	 * ��������: ���ŵ�2��ŵ�3 
	 *@param            szTrack2		  [out]	-��������������Ĵŵ�1����
	 *@param            szTrack3		  [out]	-��������������Ĵŵ�2����
	 *@param          timeout [int] -��ʱʱ��
	 **@return 
	*            0            -���ɹ�
	*            ��0          -��ʧ��
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
		
		
	
		//��������
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
		
		//���ôŵ���ʽ
		SetFormat( nCharset);
		
		
		//���ó�ʱʱ��
		SetReadMsgTimeOut( timeOut );
		
		//Log.e(TAG, "after TimeOutSet");
		if(USE_CENT_CMD ) //����ָ��
		{
			
		}
		
		else //����ָ��
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
	
		
		WriteDataToTransPort( MsgCardDef.COMMAND_RESET, 2 );//��λ
	
		//�رն���ָ��
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
	* ��������: CancelReadMsgCard
	* ��������: ȡ����������
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
		
		if ( null != szTrack1 && null == szTrack2 && null == szTrack3 )   //��1�ŵ�
		{;
			nRet = ReadMsgCard( nCharset,MODE_TRACK1, nTimeout , szTrack1, nTrack1Len,  null, 0, null, 0);
			
		}
		else if ( null == szTrack1 && null != szTrack2 && null == szTrack3 ) //��2�ŵ�
		{
			nRet = ReadMsgCard(nCharset,  MODE_TRACK2, nTimeout, null, 0, szTrack2, nTrack2Len,  null, 0 );
		}
		else if ( null == szTrack1 && null == szTrack2 && null != szTrack3 ) //��3�ŵ�
		{
			nRet = ReadMsgCard( nCharset, MODE_TRACK3, nTimeout, null, 0, null, 0, szTrack3, nTrack3Len );
		}
		else if ( null != szTrack1 && null != szTrack2 && null == szTrack3 ) //��12�ŵ�
		{
			
			nRet = ReadMsgCard(  nCharset, MODE_TRACK12, nTimeout, szTrack1, nTrack1Len, szTrack2, nTrack2Len,  null, 0 );
		}
		else if ( null == szTrack1 && null != szTrack2 && null != szTrack3 )  //��23�ŵ�
		{

			nRet = ReadMsgCard(  nCharset, MODE_TRACK23, nTimeout, null, 0, szTrack2, nTrack2Len,  szTrack3, nTrack3Len );
		}
		
		return nRet;
		
	}
	
	
	
	/**
	* �����۴ŵ�
	* 
	* @param strTimeout �����۳�ʱ��� Ĭ��20��
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
