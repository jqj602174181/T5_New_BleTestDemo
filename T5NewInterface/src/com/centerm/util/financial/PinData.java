package com.centerm.util.financial;

public class PinData extends FinancialDataBase{
	public int pinStyle = 1;
	public int iEncryType;
	public int iTimes;
	public int iLength;
	public String keys[];
	
	public PinData()
	{
		keys = new String[4];
		keys[3]="";
	}
}
