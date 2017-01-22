package com.centerm.util;

public class RedundancyUtil 
{
	/**
	 * ��������У��ֵ
	 * @param data	У������
	 * @return	У��ֵ
	 */
	public static byte RedundancyCheck(byte[] data)
	{
		byte checkCode = (byte)0x00;
		for (int i = 0; i < data.length; i++)
		{
			checkCode = (byte)(checkCode ^ data[i]);
		}
		
		return checkCode;
	}
}
