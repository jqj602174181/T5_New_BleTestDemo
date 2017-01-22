package com.centerm.rdcard;

public class MsgCardDef {
	
	//���󷵻�ֵ
		public static final  int ERR_SUCCESS         = 0 ;//�ɹ�
		public static final  int  ERR_OPENFAILED     = -1;//���豸ʧ��
		public static final  int  ERR_WRITE          = -2;//����ָ��ʧ��----���豸ͨѶʧ��
		public static final  int  ERR_READ           = -3;//��������ʧ��----���豸ͨѶʧ��
		public static final  int  ERR_PACKAGE_FORMAT = -4;//���յ��ı��ĸ�ʽ����
		public static final  int  ERR_READ_CARD		= -5;//��������ʧ��
		public static final  int  ERR_WRITE_CARD		= -6;//д������ʧ��
		public static final  int  ERR_CANCELED       =  -7;//�û�ȡ��
		public static final  int  ERR_TIMEOUT        =  -8;//��ʱ
		public static final  int  ERR_OTHER			= -100;//��������
		
		
		
		
		//ʹ������ָ�
		public static final byte SOH				          =(0x01);
		public static final byte EOT			              = (0x04);
		public static final byte[] CENT_COMMAND_SEPARATOR  = {0x20,0x01,0x7C };  //���÷ָ��,'|'
		public static final byte[] CENT_COMMAND_RD123      = { 0x20,0x02 };  //���ŵ�123
		public static final byte[] CENT_COMMAND_WT123      ={ 0x20,0x03 };  //д�ŵ�123
		public static final byte[] CENT_COMMAND_TIMEOUT	  ={ 0x20, 0x04 };  //���ó�ʱ

		//����ſ���������
		public static final byte[] COMMAND_RESET        = { 0x1B, 0x30 } ;//��λ
		public static final byte[] COMMAND_STATUS       = { 0x1B , 0x6A};//״̬��ѯ

		public static final byte[] COMMAND_ISO          = { 0x1B,0x3D };//���ôŵ�ΪISO
		public static final byte[] COMMAND_IBM          = { 0x1B,0x27 }; //���ôŵ���ʽΪIBM
		public static final byte[] COMMAND_ISO_D        = { 0x1B,0x4F };//���ôŵ���ʽΪISO(�¿�)
		public static final byte[] COMMAND_IBM_D        = { 0x1B, 0x41 };//���ôŵ���ʽΪIBM(�¿�)

		//�����������
		public static final byte[] COMMAND_RD1          = { 0x1B, 0x72}; 
		public static final byte[] COMMAND_RD2          = { 0x1B, 0x5D };
		public static final byte[] COMMAND_RD3          = { 0x1B,0x54,0x5D };
		public static final byte[] COMMAND_RD12         = { 0x1B,0x44, 0x5D };
		public static final byte[] COMMAND_RD23         = { 0x1B, 0x42,0x5D }; 

	



}
