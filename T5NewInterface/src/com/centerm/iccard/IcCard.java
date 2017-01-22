package com.centerm.iccard;

import android.R.integer;
import android.util.Log;

import com.centerm.financial.FinancialBase;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;
import com.centerm.util.financial.ICCardData;


/**
 * IC卡
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
	* 读取IC中保存的客户信息
	* 
	* @param iIcFlag    : 1：接触式IC卡  2：非接触式IC 3：自动
	* @param aryTagList : 标签编码数组 “x41,x42”
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
	* 根据柜员交易类型、交易金额、交易时间等交易数据，按PBOC2.0规定，
	* 对IC卡执行一系列操作，生成ARQC，提供给后台进行IC卡联机认证。
	* 
	* @param iIcFlag      : 1：接触式IC卡  2：非接触式IC 3：自动
	* @param strInput     : 标签编码数组 “x41,x42”
	* @param strAIDList   ： 应用列表数组
	* @param strTimeout   ：超时间隔，默认20秒
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
	* IC卡验证后台的ARPC，如果验证成功，向IC卡发后台生成的IC卡脚本，完成交易。
	* 
	* @param iIcFlag     : 1：接触式IC卡  2：非接触式IC 3：自动
	* @param strInput    : 标签编码数组 “x41,x42”
	* @param strARPC     ：授权相应密文
	* @param iStatus     ：返回状态
	* @param strARQC     ：ARQC
	* @param strTimeout  ：超时间隔，默认20秒
	*/
	public String[] ARPC_ExeICScript(int iIcFlag, String strARQC1, String strARPC, int iStatus, String strARQC, String strTimeout)
	{
		style = iIcFlag;
			return null;
	}
	
	/**
	* 读IC卡交易明细
	* 
	* @param iIcFlag     : 1：接触式IC卡  2：非接触式IC 3：自动
	* @param strAIDList  ：应用列表串
	* @param strTimeout  ：超时间隔，默认30秒
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
	* 金融IC卡一体键盘的读写器可以支持磁条卡的读写，接触式或者非接触式IC卡读写。
	* 通过本接口可以获得读写器读写能力的类别信息。实现本接口时，默认5秒超时。
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
	*\brief  写buffer
	*\data  数据
	*\len    数据长度
	*\return 0成功，小于0失败
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
	*\brief  读data
	*\timeout  时延
	*\len      接收数据长度
	*\data     接收buff
	*\return 大于0成功，小于0失败
	*/
	private native int read_buffer(int timeOut,int len ,byte[] data );
	
	

	private native int CT_GetIccInfo(int iIcFlag, String jstrAidList, String jstrTaglist,byte[] jstrUserInfo, int ntimeout);
	private native int CT_GenerateARQC(int iIcFlag, String pszAidList, String arqcData,  byte[] pszARQC, int ntimeout);
	
	private native int CT_ExeICScript(int iIcFlag, String jstrTxData, String jstrARPC, int iStatus, byte[] jstrSptResult, byte[] jstrTc, int ntimeout);
	
	private native int CT_GetTxDetail(int iIcFlag, String jstrAidList, byte[] jstrTxDetail, int ntimeout);
}
