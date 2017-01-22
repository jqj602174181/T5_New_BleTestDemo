package com.centerm.util;

/**
 * 字符格式化工具
 */
public class Formater {	
	/**
     * 
     * 将字节数组指定的范围内的字节转换为整数，转换最多读取数组的4位
     * 
     * @param bytes  待转换的字节数组
     * @param start  待转换的开始位置
     * @param length 最多转换的字节数组的长度
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
     * 将整型数字转换为长度为4的字节数组
     * 
     * @param num 待转换的数字
     * @return 获得的结果
     */
    public static byte[] IntegerToByteArray(int num) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) (num >>> ((3 - i) * 8));
        }
        return bytes;
    }
    
    
    /**
	 * 字节拆分
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
	  * 字节合并
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
	   * 获取报文体
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
