package com.centerm.idcard;

//���˻�����Ϣ
public class PersonInfo {
	public  byte[] name = new byte[80]; //����
	public  byte[] sex  = new byte[10]; //�Ա�
	public  byte[] nation = new byte[50]; //����
	public  byte[] birthday = new byte[30]; //����
	public  byte[] address = new byte[180];//סַ
	public  byte[] cardId =  new byte[50]; //���֤��
	public  byte[] police = new byte[80];//ǩ������
	public  byte[] validStart = new byte[30];//��ʼ����
	public  byte[] validEnd = new byte[30];//��������
	public  byte[] sexCode = new byte[10];//�Ա���
	public  byte[] nationCode = new byte[10];//������
	public  byte[] appendMsg = new byte[180];//������Ϣ
	
}
