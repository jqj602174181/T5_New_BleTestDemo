package com.centerm.pin;

public class KeyPadDef {

	
	//��д�������
	//���󷵻�ֵ
		public static final  int   ERR_SUCCESS        =  (0);//�ɹ�
		public static final  int  ERR_OPENFAILED      =  (-1);//���豸ʧ��
		public static final  int  ERR_SEND            =  (-2);//����ָ��ʧ��----���豸ͨѶʧ��
		public static final  int   ERR_READ			 =	(-3);//��������ʧ��----���豸ͨѶʧ��
		public static final  int  ERR_PACKAGE_FORMAT	 =	(-4);//���յ��ı��ĸ�ʽ����
		public static final  int  ERR_READ_CARD		 =	(-5);//������ʧ��
		public static final  int  ERR_WRITE_CARD		 =	(-6);//д����ʧ��
		public static final  int  ERR_CANCELED        =   (-7);//�ͻ�ȡ����������
		public static final  int  ERR_TIMEOUT         =   (-8);//��ʱ
		public static final  int  ERR_PARAM			 =	 (-9); //��������
		public static final  int  ERR_DIFFERENT       =   (-10);//�����������벻һ��
		public static final  int  ERR_OTHER			 =	(-100);//��������
}
