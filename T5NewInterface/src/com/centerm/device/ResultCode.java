package com.centerm.device;

public class ResultCode {
	// ͨ�Ŵ�����
	public static final int DEVICE_SUCCESS = 0; // �ɹ�
	public static final int DEVICE_OPEN_FAILED = -1; // ���豸ʧ��
	public static final int DEVICE_WRITEDEVICE_FAILED = -2; // ����ָ��ʧ��
	public static final int DEVICE_READDEVICE_FAILED = -3; // ��������ʧ��
	public static final int DEVICE_PACKAGE_ERROR = -4; // ���յ��ı��ĸ�ʽ����
	public static final int DEVICE_TIMEOUT = -5; // ��ʱ
	public static final int DEVICE_CANCELED = -6; // �û�ȡ��
	public static final int DEVICE_DATA_ERROR = -8; // ���ݲ���ȷ
	public static final int DEVICE_PARAM_ERROR = -9; // ��������ȷ
	public static final int DEVICE_COMMUNICATE = -10; // ͨ��ʧ��
	public static final int DEVICE_OPERATE_FAILED = -11; // ����ʧ��
	public static final int DEVICE_OUTOF_BUFFER = -12; // ����Խ��

	// USB������
	public static final int USB_NO_PERMISSION = -90;
	public static final int USBMANAGER_IS_NULL = -91;
	public static final int DEVICE_LIST_NULL = -92;
	public static final int USB_FIND_DEVICE_FAIL = -93;
	public static final int USB_FIND_INTERFACE_FAIL = -94;
	public static final int USB_CONNECT_FAIL = -95;
	public static final int USB_GET_EPIN_FAIL = -96;
	public static final int USB_GET_EPOUT_FAIL = -97;
	public static final int USB_CLAIMED_FAILED = -98;
	public static final int USB_EPIN_OR_EPOUT_NULL = -99;
	public static final int USB_REQ_MESSSAGE_LEN_ERR = -100;
	public static final int USB_SEND_MESSSAGE_FAIL = -101;
	public static final int USB_READ_TIMEOUT = -102;
	public static final int USB_PRAM_REQ_OR_RES_NULL = -103;
	
	// ����������
	public static final int BLUETOOTH_ADAPTER_NOT_FIND = -200;
	public static final int BLUETOOTH_MAC_NULL = -201;
	public static final int BLUETOOTH_MAC_ERROR = -202;
	public static final int BLUETOOTH_OPEN_FAIL = -203;
	public static final int BLUETOOTH_DEVICE_NOT_FIND = -204;
	public static final int BLUETOOTH_SOCKET_GET_FAIL = -205;
	public static final int BLUETOOTH_SOCKET_CLOSE_FAIL = -206;
	public static final int BLUETOOTH_TRANSFER_PARAM_ERR = -207;
	public static final int BLUETOOTH_READ_TIMEOUT = -208;
	public static final int BLUETOOTH_READ_EXCEPTION = -209;
	public static final int BLUETOOTH_READ_MESS_HEAD_FAIL = -210;
	public static final int BLUETOOTH_READ_MESS_LEN_FAIL = -211;
	public static final int BLUETOOTH_READ_MESS_DATA_FAIL = -212;
	public static final int BLUETOOTH_TRANSFER_EXCEPTION = -213;
}
