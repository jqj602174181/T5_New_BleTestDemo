package com.centerm.util;

public class MessageDefUtil {
	
	/* 密码键盘两个接口 */
	public final static int readPin = 1001;
	public final static int keyAffusePin  = 1002;
	
	/* 指纹仪两个接口 */
	public final static int registerFinger = 2001;
	public final static int readFinger  = 2002;
	
	/* 二代证两个接口 */
	public final static int getIDCardInfo = 3001;
	public final static int getIDFullInfo  = 3002;
	
	/* 磁卡 */
	public final static int getBookAcct = 4001;
	
	/* 电子签名 */
	public final static int getSignature = 5001;
	public final static int keyAffuseSign = 5002;
	public final static int getEncrySignature = 5003;
	public final static int getSignPhotoData = 5004;
	
	/* IC卡 */
	public final static int getICCardInfo = 6001;
	public final static int genARQC  = 6002;
	public final static int ARPC_ExeICScript = 6003;
	public final static int getTxDetail  = 6004;
	public final static int getCardRdWrtCap = 6005;
}
