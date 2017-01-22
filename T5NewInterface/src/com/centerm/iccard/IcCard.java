package com.centerm.iccard;

import android.R.integer;
import android.util.Log;

import com.centerm.financial.FinancialBase;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.ICCardData;


/**
 * IC��
 * @author ysz
 * @since 2016-01-14
 */
public class IcCard extends FinancialBase{


	public static final int IC_INFO		=1; 
	public static final int IC_ARQC		=2; 
	public static final int IC_DETAIL	=3; 
	public static final int IC_RW		=4; 
	//private  static  int successtimes =0;
	//private  static int failtimes =0;
	static{
		System.loadLibrary("IcCard");
	}

	private int style = 1;
	
	public IcCard()
	{
	
	}
	
	private String [] getError(int style)
	{
		
		StringBuilder builder= new StringBuilder();
		builder.append(sError);
		builder.append(split);
		switch (style) {
			case -1:
			case -203:
				builder.append(RetUtil.Param_Err);
				break;
			case -3:
				builder.append(RetUtil.Timeout_Err);
				break;
			case -101:
				builder.append(RetUtil.ShangDian_ERROR);
				break;
			case -204:
				builder.append(RetUtil.ARQC_ERROR);
				break;
			default:
				builder.append(RetUtil.Unknown_Err);
			break;
		}
		return builder.toString().split(split);
	}
	/**
	* ��ȡIC�б���Ŀͻ���Ϣ
	* 
	* @param iIcFlag    : 1���Ӵ�ʽIC��  2���ǽӴ�ʽIC 3���Զ�
	* @param aryTagList : ��ǩ�������� ��x41,x42��
	* @param strAIDList
	* @param strTimeout
	*/
	public String[] getICCardInfo(int iIcFlag, String aryTagList, String strAIDList, String strTimeout)
	{
		int timeOut = Integer.parseInt(strTimeout);
        
		if(timeOut==-1){
			return getParamErr();
		}
		start();
		byte[] data = new byte[2048];
		int ret = CT_GetIccInfo(iIcFlag,strAIDList,aryTagList,data,timeOut);
	
		
		String[] dataList = null;
		if(ret==0){

			
			//successtimes++;
			String str = new String(data,0,data.length).trim();
			StringBuilder builder = new StringBuilder();
			builder.append(sRight);
			builder.append(split);
			builder.append(""+iIcFlag);
			builder.append(split);
			builder.append(str);
			dataList = builder.toString().split(split);
		}else{
			//failtimes++;
			dataList = getError(ret);
		}
		//Log.e("ICCardTest", "successtimes="+ successtimes + ", failtimes=" + failtimes);
		quit();
		return dataList;
	}
	
	/**
	* ���ݹ�Ա�������͡����׽�����ʱ��Ƚ������ݣ���PBOC2.0�涨��
	* ��IC��ִ��һϵ�в���������ARQC���ṩ����̨����IC��������֤��
	* 
	* @param iIcFlag      : 1���Ӵ�ʽIC��  2���ǽӴ�ʽIC 3���Զ�
	* @param strInput     : ��ǩ�������� ��x41,x42��
	* @param strAIDList   �� Ӧ���б�����
	* @param strTimeout   ����ʱ�����Ĭ��20��
	*/
	public String[] genARQC(int iIcFlag, String strARQC, String strAIDList, String strTimeout)
	{
		int timeOut = Integer.parseInt(strTimeout);

		if(timeOut==-1){
			return getParamErr();
		}
		start();
		byte[] arqcData = new byte[1024];
		String[] dataList=null;
		int ret = CT_GenerateARQC(iIcFlag,strAIDList,strARQC,arqcData,timeOut);
		if(ret==0){
			String str = new String(arqcData,0,arqcData.length).trim();
			StringBuilder builder = new StringBuilder();
			builder.append(sRight);
			builder.append(split);
			builder.append(""+iIcFlag);
			builder.append(split);
			builder.append(str);
			dataList = builder.toString().split(split);
		}else{
			dataList = getError(ret);
		}
		quit();
			return dataList;
	}
	
	/**
	* IC����֤��̨��ARPC�������֤�ɹ�����IC������̨���ɵ�IC���ű�����ɽ��ס�
	* 
	* @param iIcFlag     : 1���Ӵ�ʽIC��  2���ǽӴ�ʽIC 3���Զ�
	* @param strInput    : ��ǩ�������� ��x41,x42��
	* @param strARPC     ����Ȩ��Ӧ����
	* @param iStatus     ������״̬
	* @param strARQC     ��ARQC
	* @param strTimeout  ����ʱ�����Ĭ��20��
	*/
	public String[] ARPC_ExeICScript(int iIcFlag, String strARQC1, String strARPC, int iStatus, String strARQC, String strTimeout)
	{
		style = iIcFlag;
			return null;
	}
	
	/**
	* ��IC��������ϸ
	* 
	* @param iIcFlag     : 1���Ӵ�ʽIC��  2���ǽӴ�ʽIC 3���Զ�
	* @param strAIDList  ��Ӧ���б�
	* @param strTimeout  ����ʱ�����Ĭ��30��
	*/
	public String[] getTxDetail(int iIcFlag, String strAIDList, String strTimeout)
	{
		int timeOut = Integer.parseInt(strTimeout);

		if(timeOut==-1){
			return getParamErr();
		}
		start();

		byte[] detailData = new byte[4096];
		String[] dataList=null;
		int ret = CT_GetTxDetail(iIcFlag,strAIDList,detailData,timeOut);
	
		if(ret==0){
			String str = new String(detailData,0,detailData.length).trim();
			StringBuilder builder = new StringBuilder();
			builder.append(sRight);
			builder.append(split);
			builder.append(""+iIcFlag);
			builder.append(split);
			builder.append(str);
			dataList = builder.toString().split(split);
		}else{
			dataList = getError(ret);
		}
		quit();
		return dataList;
	}
	
	/**
	* ����IC��һ����̵Ķ�д������֧�ִ������Ķ�д���Ӵ�ʽ���߷ǽӴ�ʽIC����д��
	* ͨ�����ӿڿ��Ի�ö�д����д�����������Ϣ��ʵ�ֱ��ӿ�ʱ��Ĭ��5�볬ʱ��
	*/
	public String[] getCardRdWrtCap()
	{
			return null;
	}

	@Override
	protected int readData(byte[] data, int len, int timeOut) {
		// TODO Auto-generated method stub
		int l = read_buffer(timeOut, len, data);
		if(l>0){
			byte[] tempData = new byte[l];
			System.arraycopy(data, 0, tempData, 0, l);
		
		
		}
		
		return l; 
	}

	@Override
	protected int writeData(byte[] data, int len) {
		// TODO Auto-generated method stub
		
		int realLength = getRealReadedLength(data, len);
		byte[] tempData = new byte[realLength];
		System.arraycopy(data, 0, tempData, 0, realLength);
		return write_buffer(data,realLength);
	}

	@Override
	public byte[] getFinancialOpenCommad() {
		
		return null;
	}

	@Override
	public byte[] getFinancialCloseCommad() {
		// TODO Auto-generated method stub
		
		if(style==1){
			return  BusinessInstruct.CloseBusiness((byte)0x31);
		}else if(style==2){
			return  BusinessInstruct.CloseBusiness((byte)0x32);
		}
		return  BusinessInstruct.CloseBusiness((byte)0x31);	
	//	return null;
	}
	@Override
	public Object getTestData(Object object) {
		// TODO Auto-generated method stub
		ICCardData data = (ICCardData)object;
		
		
		String[] dataList = null;
		style = data.cardStyle;
	switch (data.style) {
	case IC_INFO:
		dataList = getICCardInfo(data.cardStyle,data.tag,data.list,data.timeOut);
		break;
	case IC_ARQC:
		dataList = genARQC(data.cardStyle,data.ARQC,data.list,data.timeOut);
		break;
	case IC_DETAIL:
		dataList = getTxDetail(data.cardStyle, data.list, data.timeOut);
		break;
	default:
		break;
	}
		

		return dataList;
	}
	
	
	/*!
	*\brief  дbuffer
	*\data  ����
	*\len    ���ݳ���
	*\return 0�ɹ���С��0ʧ��
	*/

	@Override
	protected void sendEnd() {
		// TODO Auto-generated method stub
		super.sendEnd();
		
		int time = 5;
		byte[] data = new byte[packLength];
		while(true){
			
			time--;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(time==0){
				break;
			}
			
			int len = readData(data, packLength, 1);
			if(len==3&&data[0]==0x02&&data[1]==(byte)0x0f&&data[2]==0x03){
				break;
			}else{
			//	sendCommData(data, len);
			}
		}
			
	}

	private native int write_buffer(byte[] data,int len);
	/*!
	*\brief  ��data
	*\timeout  ʱ��
	*\len      �������ݳ���
	*\data     ����buff
	*\return ����0�ɹ���С��0ʧ��
	*/
	private native int read_buffer(int timeOut,int len ,byte[] data );
	
	

	private native int CT_GetIccInfo(int iIcFlag, String jstrAidList, String jstrTaglist,byte[] jstrUserInfo, int ntimeout);
	private native int CT_GenerateARQC(int iIcFlag, String pszAidList, String arqcData,  byte[] pszARQC, int ntimeout);
	
	private native int CT_ExeICScript(int iIcFlag, String jstrTxData, String jstrARPC, int iStatus, byte[] jstrSptResult, byte[] jstrTc, int ntimeout);
	
	private native int CT_GetTxDetail(int iIcFlag, String jstrAidList, byte[] jstrTxDetail, int ntimeout);
}
