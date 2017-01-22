package com.centerm.finger.wellcom;

public class FingerPrintDef {

	
	public static final  int  ERR_SUCCESS			 =  0; //�ɹ�
	public static final  int   ERR_OPENFAILED     = -1;//���豸ʧ��
	public static final  int   ERR_WRITE			 = -2;//����ָ��ʧ��----���豸ͨѶʧ��
	public static final  int   ERR_READ			 = -3;//��������ʧ��----���豸ͨѶʧ��
	public static final  int   ERR_PACKAGE_FORMAT	 = -4;//���յ��ı��ĸ�ʽ����
	public static final  int   ERR_READ_CARD	     = -5;//��������ʧ��
	public static final  int   ERR_WRITE_CARD	     = -6;//д������ʧ��
	public static final  int   ERR_CANCELED       = -7;//�û�ȡ��
	public static final  int   ERR_TIMEOUT        = -8;//��ʱ
	public static final  int   ERR_OTHER		     = -100;//��������


	//ʹ������ָ�
	public static final  byte  SOH				= (0x02);
	public static final  byte  EOT			    = (0x03);
	
	
	
	public static final  byte[] COMMAND_GETDEVINFO = { 0x09, 0x00, 0x00, 0x00 }; //��ȡ�豸��Ϣ
	public static final byte[]  COMMAND_GETENROLL =  {  0x0B, 0x00 ,0x00 , 0x00 } ;//�Ǽ�ָ����Ϣ
	
	public static final byte[] COMMAND_GETFINGRCH =  {  0x0C, 0x00, 0x00, 0x00};
}
