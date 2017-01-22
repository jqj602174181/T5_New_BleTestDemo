package com.centerm.idcard;

//个人基本信息
public class PersonInfo {
	public  byte[] name = new byte[80]; //姓名
	public  byte[] sex  = new byte[10]; //性别
	public  byte[] nation = new byte[50]; //民族
	public  byte[] birthday = new byte[30]; //生日
	public  byte[] address = new byte[180];//住址
	public  byte[] cardId =  new byte[50]; //身份证号
	public  byte[] police = new byte[80];//签发机关
	public  byte[] validStart = new byte[30];//起始日期
	public  byte[] validEnd = new byte[30];//结束日期
	public  byte[] sexCode = new byte[10];//性别码
	public  byte[] nationCode = new byte[10];//民族码
	public  byte[] appendMsg = new byte[180];//附加信息
	
}
