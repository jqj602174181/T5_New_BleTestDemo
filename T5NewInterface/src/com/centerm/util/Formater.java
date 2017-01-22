package com.centerm.util;

/**
 * �ַ���ʽ������
 */
public class Formater {	
	/**
     * 
     * ���ֽ�����ָ���ķ�Χ�ڵ��ֽ�ת��Ϊ������ת������ȡ�����4λ
     * 
     * @param bytes  ��ת�����ֽ�����
     * @param start  ��ת���Ŀ�ʼλ��
     * @param length ���ת�����ֽ�����ĳ���
     * @return
     */
    public static int byteArrayToInteger(byte[] bytes, int start, int length) {
        int num = bytes[start];
        for (int i = start + 1; i < start + 4 && i < start + length
                && i < bytes.length; i++) {
            num = (num << 8) | (bytes[i] & 255);
        }
        return num;
    }

    /**
     * ����������ת��Ϊ����Ϊ4���ֽ�����
     * 
     * @param num ��ת��������
     * @return ��õĽ��
     */
    public static byte[] IntegerToByteArray(int num) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) (num >>> ((3 - i) * 8));
        }
        return bytes;
    }
    
    
    /**
	 * �ֽڲ��
	 * 0x30---> 0x33 0x30
	 * */
	 public static void SplitData(byte[] ucTemp, int indatalen, byte[] outData, int OutDataPos )
	{
		
		 int  i = 0;
		 int  j = 0;
		 int  len = indatalen; 
	    if( ( null == ucTemp )||( len <= 0 ))
	    {
	       return;    
	    }
	    byte[] temp = new byte[ len*2 ];
	    for( i = 0; i < len; i++, j+=2 )
	    {
	    	temp[ j ] = (byte) (((ucTemp[i] >> 4)&0xF)+0x30);
	    	temp[ j + 1 ] = (byte) ((ucTemp[i]&0xF)+0x30);
	    }
		
	    System.arraycopy(temp, 0, outData, OutDataPos, len*2 ) ;
	 
	}
	
	 /**
	  * �ֽںϲ�
	  * ox33 0x30 -> 0x30
	  **/
	 public static void MergeData(byte[] inData, int inDataLen, byte[] outData, int outDataPos)
	 {
		   int i = 0;
		   int j = 0;
		   int len = inDataLen; 
		  
		   if( ( inData == null )||( (len % 2) != 0 ) )
		   {
		       return;    
		   }  
		    
		    byte[] temp = new byte[ len/2 ]; 
		    for( i = 0;  i < (len/2); i++, j+=2 )
		    {
		    	temp[i] = (byte) (( (inData[j]<<4)&0xF0 ) | (  inData[j+1]&0xF  ));
		    }
		  
		    
		    System.arraycopy(temp, 0, outData , outDataPos , len/2 );
	 }
	 
	 
	 
	public static byte GetDataLRC( byte[] pData, int iLen )
	 {
	       int  i = 0;
	      byte Temp = 0;
	       
	       for( i = 0; i < iLen; i++ )
	       {
	          Temp ^= pData[ i ];
	       }
	       return Temp;
	 }
	
	
	
	 /**
	   * ��ȡ������
	  **/
	 public static  int GetPacketBody( byte head, byte end, byte[] szInData, int nInDataLen,byte[] szOutData )
	 {   
	 	int nLen = -1;
	 	int nBeginPos = 0;
	 	int nEndPos = 0;

	 	
	 	while( nInDataLen > nBeginPos )
	 	{
	 		if( head == szInData[ nBeginPos++ ] )
	 		{

	 			break;
	 		}   
	 	}

	 	if( nBeginPos >= nInDataLen-1 )
	 	{        
	 		return -1;   
	 	} 

	 	nEndPos = nBeginPos;
	 	while(  nInDataLen >  nEndPos )
	 	{

	 		if(  end == szInData[ nEndPos ] )
	 		{
	 			break;
	 		}
	 		nEndPos ++;
	 	}

	 	nLen = nEndPos - nBeginPos;
	 	if( ( nLen > 0 )&&( nEndPos <= (nInDataLen-1) ) )
	 	{
	 		System.arraycopy(szInData, nBeginPos, szOutData, 0, nLen ) ;
	 		return nLen;   
	 	}

	 	return -1;
	 }
	 
}
