package com.centerm.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;


import android.R.bool;

public class StringUtil {
	public StringUtil(){
		
	}
	
	/**
	 * 十六进制形式输出byte[]
	 * 1->2 如 0x30 --> "30"
	 * @param b
	 */ 
	public static String bytesToHexString(byte[] bytes) {
		if (bytes == null)
		{
			return null;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
		{
			
			String hex = Integer.toHexString(bytes[i] & 0xff);
			if (hex.length() == 1)
			{
				hex = '0' + hex;
			}
			builder.append(hex.toUpperCase());
		}
		return builder.toString();
	}
	
	
	
	/**
	 * 转变十六进制形式的byte[]
	 * 2->1 如： "30" --> 0x30
	 * @param orign
	 * @return
	 */
	public static byte[] hexStringToBytes(String orign){
		int length = orign.length()/2;
		byte[] result = new byte[length];
		for(int i=0; i<length; i++){
			result[i] = (byte) Integer.parseInt(orign.substring(i*2, i*2+2),16);
		}
		return result;
	}
	
	public static String ByteToString(byte[] byBuf,int start,int length)
	{
		String strBuf = new String(byBuf, start, length); 
		return strBuf;
	}
	
	public static String ByteToString(byte[] byBuf)
	{
		int len = GetVailArrayLen(byBuf);
		String strBuf = new String(byBuf, 0, len); 
		return strBuf;
	}
	
	public static String ByteToString(byte[] byBuf, String charset)
	{
		int len = GetVailArrayLen(byBuf);
		String strBuf = null;
		try {
			strBuf = new String(byBuf, 0, len, charset);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return strBuf;
	}
	
	public static int GetVailArrayLen(byte[] byBuf)
	{
		for(int i=0; i<byBuf.length; i++)
		{
		//	if(byBuf[i] == 0)
		//		return i;
		}
		return byBuf.length;
	}
	
	// 1->2
	public static String HexToStringA(byte[] s)
	{
		String str="";
		int len = GetVailArrayLen(s);
		for (int i=0;i<len;i++)
		{
			int ch = (int)s[i];
			String s4 = Integer.toHexString(ch);
			if(s4.length() == 1)
			{
				s4 = "0" + s4;
			}
			else if(s4.length() > 2)
			{
				s4 = s4.substring(s4.length()-2, s4.length());
			}
			
			str = str + s4;
		}
		
		return str;
	} 
	
	// 2->1
	public static byte[] StringToHexA(String s)
	{
		byte[] baKeyword = new byte[s.length()/2];
		for(int i = 0; i < baKeyword.length; i++)
		{
			try
			{
				baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			s = new String(baKeyword, "utf-8");//UTF-16le:Not
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		//return s;
		return baKeyword;
	} 
	
   /* byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序
	* @param src byte数组  
	* @param offset 从数组的第offset位开始  
	* @return int数值  
	*/    
	public static int bytesToInt(byte[] src, int offset) {  
		int value;    
		value = (int) ((src[offset] & 0xFF)   
			| ((src[offset+1] & 0xFF)<<8)   
			| ((src[offset+2] & 0xFF)<<16)   
			| ((src[offset+3] & 0xFF)<<24));  
		return value;  
	}
	
	/* byte数组中取int数值，本方法适用于(高位在前，低位在后)的顺序
	* @param src byte数组  
	* @param offset 从数组的第offset位开始  
	* @return int数值  
	*/    
	public static byte[] intToBytes(int number) {  
		return ByteBuffer.allocate(4).putInt(number).array();
	}
	/**
	 * 将byte数组转换为int
	 * @param byteArray	byte数组
	 * @return	int数值
	 */
	public static int byteArrayToLength(byte[] byteArray)
	{
		int result = 0;
		for ( int i = 0; i < byteArray.length; i++ )
		{
			result = (byteArray[i] & 0x000000FF) ^ result;
			if ( i < byteArray.length - 1 )
			{
				result <<= 8;
			}
		}
		
		return result;
	}
	
	public static boolean isInt(String str)
	{
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	
	  
    /**
     * 比较两个byte数组前几个字节是否相等
     */
	   public static  boolean isEquals( byte[] src1,int bytesLen,  byte[]  src2 )
		{
			
			int len = bytesLen;
			
			int i = 0;
			for( i = 0; i < len; i++ )
			{
				if( (byte)src1[i] != (byte)src2[i] )
				{
					return false;
				}
			}
			return true;
		}
	   
	   /**
	    * 判断byte数组B是否在数组A中出现
	    * */
	   
	   public static boolean isSubByte(byte[] DataA , byte[] DataB)
	   {
		   int  lenA = DataA.length;
		   int  lenB = DataB.length;
		   int i = 0;
		   int j = 0;
		   boolean flag = true;
		   
		   if(lenB > lenA )
		   {
			   return false;
		   }
		   
		   for( i = 0 ; i <= lenA -lenB; i++ )
		   {
			   flag = true;
			   for( j = 0; j < lenB ; j++ )
			   {
				   if(DataA [i+j] != DataB[j])
				   {
					   flag = false;
					   break;
				   }
			   }
			   
			   if( flag == true)
			   {
				   break;
			   }
		   }
		   
		   return flag;
	   }
	   
	   /**
	    * 将unicode转化为byte 
	    */
	   public static byte[] Unicode2Byte(String s)
	   {
		   int len = s.length();
		   byte abyte[] = new byte[len << 1];
		   int j = 0;
		   for(int i = 0; i < len; i++)
		   {
			   char c = s.charAt(i);
			   abyte[j++] = (byte)(c & 0xff);
			   abyte[j++] = (byte)(c >> 8);
		   }
	
		   
		   return abyte;
	   }
	   
	   
	   /**
	    *将byte转化为unicode 
	   */
	   public static String Byte2Unicode(byte abyte[], int st, int bEnd) // 不包含bEnd
	   {
		   StringBuffer sb = new StringBuffer("");
		   for(int j = st; j < bEnd; )
		   {
		   int lw = abyte[j++];
		   if (lw < 0) lw += 256;
		   int hi = abyte[j++];
		   if (hi < 0) hi += 256;
		   char c = (char)(lw + (hi << 8));
		   sb.append(c);
	       }
		   
		   return sb.toString();
       }
	   
	   /**
		 * ASCII码字符串转数字字符串
		 * 
		 * @param String
		 *            ASCII字符串
		 * @return 字符串
		 */
		public static String AsciiToString(byte[] data)
		{
			int len = StringUtil.GetVailArrayLen(data);
			char[] result = new char[len];
			
			for(int i = 0;i< len;i++){
				result[i] = (char)data[i];
			}
			String strs = new String(result);

			return strs;

		}
		
		public static byte[] StringToHexAscii(String str){
			  char[] temp = str.toCharArray();
			  byte[] result = new byte[temp.length];
			  for(int i=0;i<temp.length;i++){ 
				  result[i] = (byte)temp[i];
			  }
			return result;
		}
}
