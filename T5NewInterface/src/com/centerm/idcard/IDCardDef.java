package com.centerm.idcard;

//���֤����
public class IDCardDef {
	//���󷵻�ֵ
	public static final  int  ERR_SUCCESS = 0;   //�ɹ�
	public static final  int  ERR_DEVICE  = -1;    //�豸����
	public static final  int  ERR_RECV    = -2;   //���մ���
	public static final  int  ERR_SEND    = -3;    //���ʹ���
	public static final  int  ERR_CHECK   = -4;    //У�����
	public static final  int  ERR_TIMEOUT = -5;    //��ʱ
	public static final  int  ERR_READ    = -6;    //��������
	public static final  int  ERR_IMAGE   = -7;    //����ͷ�����
	public static final  int  ERR_SAVEIMG = -8;    //����ͼ�����
	public static final  int  ERR_CANCEL  = -9;    //ȡ������
	public static final  int  ERR_OTHER   = -100;  //��������
		
	//��������Ҫ��ȡ�����֤ͼƬ���Ͷ���
	public static final  int  IMG_HEAD     = 0;
	public static final  int  IMG_FRONT	  = 1;
	public static final  int  IMG_BACK	  = 2;
	public static final  int  IMG_IDCARD   = 3;
}
