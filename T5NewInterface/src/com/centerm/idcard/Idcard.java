package com.centerm.idcard;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;


import android.R.array;
import android.R.integer;
import android.app.LocalActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import com.centerm.device.CommService;
import com.centerm.device.ResultCode;
import com.centerm.financial.FinancialBase;
import com.centerm.t5.util.dev.BusinessInstruct;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;
/*
 * //返回结果
#define     ERR_SUCCESS       0    //成功
#define     ERR_DEVICE       -1    //设备错误
#define     ERR_RECV         -2    //接收错误
#define     ERR_SEND         -3    //发送错误
#define     ERR_CHECK        -4    //校验错误
#define     ERR_TIMEOUT      -5    //超时
#define     ERR_READ         -6    //读卡错误
#define     ERR_IMAGE        -7    //解析头像错误
#define     ERR_SAVEIMG      -8    //保存图像错误
#define     ERR_CANCEL       -9    //取消操作
#define     ERR_OTHER        -100  //其他错误
 */
import com.centerm.util.financial.IDCardData;

/**
 * 二代证
 */
public class Idcard extends FinancialBase {

	private final static String sdPath = "/mnt/sdcard";
	private final String CODING = "utf-8";
	private boolean isSuccess = false;;
	private String errorStr="";
	private static boolean s_bCancel = false;
	private final int TIMES_READCARD = 1;
	private final int TRANSPROC_DEFAULT_TIMEOUT = 1;
	private final int MAX_LEN = 1024*10;
	private  final int MAX_DATA_LEN = 4096;
	private String TAG = "IDCard";
	private  static  boolean T5_device = true;//T5 设备
	//private  static int successtimes = 0;
	//  private  static int failtimes = 0;
	/*
	 * 读取数据
	 */
	public native int SaveHeadImg(  byte[] szHeadDir, byte[] szData, byte[] CardId);

	static
	{
		System.loadLibrary("idcaread");
		System.loadLibrary("IDCard");
	}

	//结束判断

	public  int endofRead( byte[] szBuff, int nRdLen)
	{
		int nLen   = 7;
		int nBytes = 0;

		if( nRdLen >= nLen )
		{
			nBytes = szBuff[ nLen-2 ] * 256 + szBuff[ nLen-1 ];
			//Log.e( TAG, "endofRead: nBytes" + nBytes );
			if( nRdLen - nLen >= nBytes )
			{
				return 0;
			}
		}

		return 1;
	}

	/**
	 * 寻卡
	 * @param  nTimeOut [in]-----超时，单位：ms
	 * @return
	 * ERR_SUCCESS---成功
	 *   其他---寻卡错误 
	 * */
	public int  searchCard( int nTimeOut )
	{   

		//Log.e(TAG, "-------------------searchCard---------------");
		long dwStart   = System.currentTimeMillis();
		byte[] szReq = { (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96,0x69,0x00,0x03,0x20,0x01,0x22};
		byte[] szCRes = { (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96,0x69,0x00,0x08,0x00, 0x00,(byte)0x9F,0x00,0x00,0x00,0x00,(byte)0x97 };

		int nReq = szReq.length;
		int nCRes = szCRes.length;
		int[] nRes = new int[2];
		int nRet = IDCardDef.ERR_SUCCESS;

		while( !s_bCancel )
		{
			byte[] szRes = new byte[ MAX_LEN ];
			nRes[0] =  szRes.length;

			nRet = transProc( szReq, nReq, szRes, nRes, TRANSPROC_DEFAULT_TIMEOUT +2 );		
			//Log.e(TAG, "searchCard");
			if( nRet == IDCardDef.ERR_SUCCESS )
			{
				if( (nRes[0] >= nCRes && StringUtil.isEquals(szCRes,szCRes.length, szRes) ) ) //成功寻卡
				{
					return IDCardDef.ERR_SUCCESS;
				}
			}

			if( System.currentTimeMillis()-dwStart > nTimeOut*1000 ) //无卡，且超时
			{
				return IDCardDef.ERR_TIMEOUT;
			}

			try {
				Thread.sleep( 200 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return IDCardDef.ERR_CANCEL;
	}

	/**
	 *  选卡
	 * @param   无
	 *  
	 * @return ERR_SUCCESS---成功
	 *          其他---选卡错误
	 */
	public int  selectIDCard(  ) 
	{      
		byte[] szReq= { (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69,0x00,0x03,0x20,0x02,0x21 } ;
		byte[] szCRes ={(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96,0x69,0x00,0x0C,0x00,0x00,(byte)0x90,
				0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0x9C };

		byte[] szRes = new byte[ MAX_LEN ];
		int nReq = szReq.length;
		int nCRes = szCRes.length;
		int[] nRes = new int[2];
		int nRet = IDCardDef.ERR_SUCCESS;


		//Log.e(TAG, "-------------------selectIDCard---------------");
		nRet = transProc(szReq, nReq, szRes, nRes, TRANSPROC_DEFAULT_TIMEOUT );

		if( nRet == IDCardDef.ERR_SUCCESS )
		{
			if( nRes[0] >= nCRes && StringUtil.isEquals( szCRes, szCRes.length, szRes ) ) 
			{
				try {
					Thread.sleep( 200 );
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return IDCardDef.ERR_SUCCESS ;
			}
			else
			{
				return IDCardDef.ERR_READ;
			}
		}

		return nRet;
	}


	/**
	 * 函数名：readIDCard
	 *  
	 * 功能：  读卡
	 *  
	 *@param   pData[OUT]--------用于保存数据的缓冲区
	 *         npDataLen[OUT]----返回的数据长度
	 *  
	 *@return ERR_SUCCES---成功
	 *         ERR_READ---读卡错误
	 */
	private int readIDCard( byte[] pData, int[] npDataLen, int timeOut ) 
	{
		byte[] szReq =  { (byte)0xAA ,(byte)0xAA, (byte)0xAA, (byte)0x96,0x69, 0x00,0x03,0x30,0x01,0x32};
		byte[] szURes = { (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96,0x69,0x00,0x04,0x00,0x00,0x41,0x45 };
		byte[] szRes = new byte[ MAX_LEN ] ;
		int  nReq = szReq.length ;

		long dwStart   = System.currentTimeMillis();
		int nRet = 0;
		int pos = 0;
		int recvlen = 0;


		//Log.e(TAG, "-------------------readIDCard---------------");
		//下发指令
		if(WriteDataToTransPort( szReq, nReq ) < nReq )
		{
			return IDCardDef.ERR_SEND;
		}




		while( true)
		{
			//Log.e(TAG, "readIDCard: in while");
			recvlen = ReadDataFromTransPort( szRes,  TRANSPROC_DEFAULT_TIMEOUT);	
			if( recvlen > 0  )
			{   

				System.arraycopy(szRes, 0, pData, pos, recvlen);

				if( StringUtil.isEquals(szURes ,szURes.length, pData ) == true)
				{
					nRet = IDCardDef.ERR_READ;
					break;
				}



				if( endofRead( pData,pos+recvlen ) == 0 )
				{  
					//Log.e(TAG, "endofRead") ;
					nRet = IDCardDef.ERR_SUCCESS;
					break;
				}

				pos += recvlen;
				//Log.e(TAG, "readCard: len = "+ pos );
			}
			else
			{

				nRet = IDCardDef.ERR_READ;
				//break;
			}


			if( System.currentTimeMillis()-dwStart > timeOut*1000 ) //读不到信息，且超时
			{
				return IDCardDef.ERR_TIMEOUT;
			}

			try {
				Thread.sleep( 200 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}


		return  nRet;
	}



	/**
	 *   读卡信息
	 *  
	 *@param nTimeOut[in]---------超时时间
	 *         pData[out]-------用于保存的二代证数据
	 *         npDataLen[out]-------用于保存的二代证数据最大长度
	 *  
	 *@return  >=0---二代证数据长度
	 *         ERR_DEVICE---设备错误
	 *         ERR_SEND-----发送错误
	 *         ERR_RECV-----接收错误
	 *         ERR_TIMEOUT--超时
	 *         ERR_CHECK----校验错误
	 */
	private int CT_getIDCardInfo(int nTimeOut, byte[] pData, int[] npDataLen )
	{
		int nRet = IDCardDef.ERR_READ;
		int nReadTime = 0;

		while( nReadTime++ < nTimeOut )
		{
			//寻卡
			nRet = searchCard( nTimeOut );
			Log.e(TAG, "searchCard:nRet = "+ nRet);
			if( nRet != IDCardDef.ERR_SUCCESS )
			{
				break;
			}

			//选卡
			nRet = selectIDCard(  );
			Log.e(TAG , "selectIDCard: nRet = "+ nRet);
			if( nRet != IDCardDef.ERR_SUCCESS )
			{
				break;
			}

			//读卡
			nRet = readIDCard(pData, npDataLen, nTimeOut);
			Log.e(TAG, "readIDCard :  data = " + StringUtil.bytesToHexString(pData).trim() );
			//Log.e( TAG, "readIDCard: nRet = "+ nRet);
			if( nRet == IDCardDef.ERR_SUCCESS ) //通用
			{
				break;
			}
		}

		return nRet;
	}


	/**
	 * 保存头像信息
	 * @param  szData[in] ----二代证原始信息
	 * @param  pInfo[in] -----二代证文字信息
	 * @param   path[in] -----头像保存路径
	 * @return =0 成功，否则失败
	 * 
	 * */
	private int CT_SaveHeadImg(byte[] szData, PersonInfo pInfo,  String path )
	{
		int nRet =0;
		String temp = StringUtil.Byte2Unicode(pInfo.cardId,  0,   36 ).trim();
		byte[] cardId = temp.getBytes();

		byte[] HeadDir = new byte[260];
		System.arraycopy(path.getBytes(), 0,HeadDir , 0, path.getBytes().length);

		Log.e(TAG, "CT_SaveHeadImg：path =  "+ path);
		nRet = SaveHeadImg(HeadDir , szData,  cardId);

		//		try{
		//			Bitmap bitmap = BitmapFactory.decodeByteArray(HeadDir, 0, HeadDir.length);
		//			ByteArrayOutputStream imageByteArray = new ByteArrayOutputStream();
		//			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, imageByteArray);
		//			byte[] imageData = imageByteArray.toByteArray();
		//
		//			FileOutputStream fileOutputStream = new FileOutputStream(path);
		//			fileOutputStream.write(imageData);
		//			fileOutputStream.close();
		//		}catch(Exception e){
		//			e.printStackTrace();
		//		}

		return  nRet;

	}



	/**
	 *   将民族代码转为民族内容
	 *
	 *@param     nIndex[ out ]--民族代码
	 *@param     Nation[ in ]--存放转换后民族内容
	 *@return 无
	 */

	private void getNation( int nIndex, byte[] Nation )
	{
		int len = 0;
		byte[] szNation = new byte[256];
		String[] nationName={"汉", "蒙古", "回", "藏", "维吾尔", 
				"苗",  "彝",  "壮",  "布依",  "朝鲜",
				"满",  "侗",  "瑶",  "白",  "土家",
				"哈尼", "哈萨克",  "傣",  "黎",  "傈僳",
				"佤",  "畲",  "高山",  "拉祜",  "水",
				"东乡",  "纳西",  "景颇",  "柯尔克孜",  "土",
				"达斡尔",  "仫佬",  "羌",  "布朗",  "撒拉",
				"毛南",  "仡佬",  "锡伯",  "阿昌",  "普米",
				"塔吉克",  "怒",  "乌孜别克",  "俄罗斯",  "鄂温克", 
				"德昂",  "保安",  "裕固",  "京",  "塔塔尔",
				"独龙",  "鄂伦春",  "赫哲",  "门巴",  "珞巴",  "基诺","穿青人" };

		if( nIndex>0 && nIndex<58 )
		{

			szNation = nationName[ nIndex -1].getBytes();
			len = nationName[ nIndex -1].getBytes().length;

		}
		else if( nIndex==98)
		{
			String str = "外国血统中国籍人士";
			szNation = str.getBytes();
			len = str.getBytes().length;
		}
		else if( nIndex==97)
		{
			String str = "其他";
			szNation = str.getBytes();
			len = str.getBytes().length;
		}
		else
		{
			String str =  " ";
			szNation =str.getBytes() ;
			len = str.getBytes().length;
		}

		System.arraycopy(szNation, 0, Nation , 0,  len );


	}


	/**
	 *  获取身份证参数
	 *
	 *@param   pSrc[ in ]--需要转换的内容
	 *@param   szDes[ out ]--存放转换后内容
	 *@param   nLen[ in ]--pSrc要转的长度
	 *@return  pSrc转换的长度
	 */
	private int getPersonParam( byte[] pSrc,int nCur,  byte[] szDes, int nLen )
	{
		if(nLen > 0)
		{
			System.arraycopy( pSrc,  nCur,  szDes,  0, nLen );
		}
		return nLen;
	}



	/**
	 *  读取二代证个人数据
	 *  
	 *@param   pData[in]---------二代证数据
	 *@param   pInfo[out]--------二代证文字数据结构
	 *  
	 *@return 
	 * @throws UnsupportedEncodingException 
	 */
	private void GetPersonInfo( byte[] pData,  PersonInfo pInfo )
	{
		int nHeader = 10;
		int nTextLenLen = 2;
		int nPhotoLenLen = 2;

		int nCur = nHeader + nTextLenLen + nPhotoLenLen;

		//获取各字段参数
		nCur += getPersonParam( pData, nCur, pInfo.name,       30 );
		nCur += getPersonParam( pData, nCur, pInfo.sexCode,    2  );
		nCur += getPersonParam( pData, nCur, pInfo.nationCode, 4  );
		nCur += getPersonParam( pData, nCur, pInfo.birthday,   16 );
		nCur += getPersonParam( pData, nCur, pInfo.address,    70 );
		nCur += getPersonParam( pData, nCur, pInfo.cardId,     36 );
		nCur += getPersonParam( pData, nCur, pInfo.police,     30 );
		nCur += getPersonParam( pData, nCur, pInfo.validStart, 16 );
		nCur += getPersonParam( pData, nCur, pInfo.validEnd,   16 );
		nCur += getPersonParam( pData, nCur, pInfo.appendMsg,  70 );

		String nationCode =  StringUtil.Byte2Unicode(pInfo.nationCode, 0, 4);
		//Log.e( TAG, "nationCode= " + nationCode );

		getNation( Integer.parseInt(nationCode), pInfo.nation);




		//获取性别
		String sexCode =  StringUtil.Byte2Unicode( pInfo.sexCode, 0, 2);
		if( Integer.parseInt(sexCode ) == 1 )
		{
			String temp = "男";
			pInfo.sex = temp.getBytes();
		}
		else
		{
			String temp = "女";
			pInfo.sex = temp.getBytes();
		}
	}

	/**
	 *    读二代证信息
	 *  
	 *@param  nTimeOut[in]--超时，单位：ms
	 *@param  szData[out]--二代证原始数据
	 * @param  pInfo[out]---解析之后的二代证数据
	 *@return  >=0---pData数据长度
	 *         ERR_DEVICE---设备错误
	 *         ERR_SEND-----发送错误
	 *         ERR_RECV-----接收错误
	 *         ERR_TIMEOUT--超时
	 *         ERR_CHECK----校验错误
	 *         ERR_NOCARD---无二代证
	 * @throws UnsupportedEncodingException 
	 */
	public  int CT_readIDCardInfo(int nTimeOut, byte[] szData, PersonInfo pInfo  ) 
	{
		int nRet = IDCardDef.ERR_DEVICE;
		int[] szDataLen = new int[2];


		//开机动画指令
		if(T5_device)
		{
			nRet = T5_StartPlayDemo( TIMES_READCARD );
			Log.e(TAG,"T5_StartPlayDemo: nRet is = " + nRet);
			if(nRet != IDCardDef.ERR_SUCCESS)
			{
				T5_ClosePlayDemo( );
				nRet = IDCardDef.ERR_DEVICE;
				return nRet;
			}

		}
		szDataLen[0] = szData.length;
		nRet = CT_getIDCardInfo( nTimeOut , szData, szDataLen);
		Log.e(TAG, "getIDCardInfo nRet is = "+  nRet);
		if(nRet == IDCardDef.ERR_SUCCESS)
		{
			GetPersonInfo(szData , pInfo);
		}

		//关闭动画指令
		if(T5_device)
		{

			int retval  = T5_ClosePlayDemo( );
			Log.e(TAG,"T5_ClosePlayDemo: nRet is = " + retval);
			if(retval != IDCardDef.ERR_SUCCESS)
			{
				nRet = IDCardDef.ERR_DEVICE;
				return nRet;
			}
		}

		return nRet;
	}




	/**
	 *    获得二代证信息
	 *  
	 *@param  nTimeOut[in]--超时，单位：ms
	 *  
	 *@return 身份证信息:之间用"|分隔"
	 */
	public  int  CT_getIDCardInfo( int nTimeOut, StringBuilder  str, String path )
	{
		PersonInfo pInfo = new PersonInfo();
		int nRet = 0;
		byte[] szData = new byte[MAX_DATA_LEN];
		String result = null;
		nRet = CT_readIDCardInfo( nTimeOut, szData, pInfo );
		if(nRet == IDCardDef.ERR_SUCCESS )
		{
			result =  StringUtil.Byte2Unicode(pInfo.name, 0, 30).trim() + "|";

			//result = result + StringUtil.Byte2Unicode( pInfo.sexCode, 0, 2).trim() + "|";

			result = result + (new String(pInfo.sex)).trim() + "|" ;

			//result = result + StringUtil.Byte2Unicode(pInfo.nationCode, 0, 4).trim() + "|";	

			result = result + (new String(pInfo.nation)).trim() + "|" ;


			result = result +  StringUtil.Byte2Unicode(pInfo.birthday, 0, 16).trim() + "|";

			result = result + StringUtil.Byte2Unicode( pInfo.address,  0,  70).trim() + "|";

			result = result + StringUtil.Byte2Unicode(pInfo.cardId,  0,   36 ).trim() + "|";

			result = result + StringUtil.Byte2Unicode( pInfo.police,  0,   30 ).trim() + "|";

			result = result + StringUtil.Byte2Unicode(pInfo.validStart,0, 16 ).trim() + "|";

			result = result + StringUtil.Byte2Unicode( pInfo.validEnd, 0,  16 ).trim();
			str.append(result);

		}
		else 
		{ 
			return nRet;

		}
		//将头像进行保存
		if(path != null)
		{
			nRet = CT_SaveHeadImg(szData, pInfo , path );

		}
		//Log.e(TAG, "getIdCardData: result" + result);
		Log.e(TAG, "getIdCardData: nret" + nRet);

		return nRet;

	}


	@SuppressWarnings("null")
	private Object getFinancialData(int timeOut,String path)
	{
		String szHeadDir = path;
		StringBuilder pInfo = new StringBuilder();
		int 	retval = CT_getIDCardInfo(timeOut, pInfo, szHeadDir);
		//Log.e("re","retval is "+retval);
		if(retval==0){
			return pInfo.toString().trim();
		}else{

			StringBuilder builder = new StringBuilder();
			builder.append(sError);
			builder.append(split);

			switch (retval) {
			case -1:
				builder.append(RetUtil.Unknown_Err);
				break;
			case -2:
			case -3:
				builder.append(RetUtil.Send_Mess_Err);
				break;
			case -5:
				builder.append(RetUtil.Timeout_Err);
				break;
			case -7:
				builder.append(RetUtil.Load_So_Err);
				break;
			default:
				builder.append(RetUtil.Unknown_Err);
				break;
			}

			errorStr = builder.toString();
		}
		return null;
	}

	/**
	 * 读取二代身份证的信息
	 * 
	 * @param strTimeout     : 超时间隔
	 */
	public String[] getIDCardInfo(String strTimeout){

		int timeOut = getTime(strTimeout);
		if(timeOut==-1){
			return getParamErr();
		}
		isSuccess = false;

		Object object = getFinancialData(timeOut,sdPath);
		String[] dataList = null;
		if(object!=null){
			String data = (String)object;
			String tempData = getIdCardData(data);
			tempData = sRight+split+tempData;
			dataList = tempData.split(split);
			//successtimes++;

		}else{
			dataList = errorStr.split(split);
			//failtimes++;
		}

		//Log.e("IDCardTest1", "successtimes="+ successtimes + ", failtimes=" + failtimes);
		return dataList;
	}

	private String  getIdCardData(String data)
	{
		byte[] bDatas = data.getBytes();
		bDatas[bDatas.length-9]='-';

		String data1 = new String(bDatas,0,bDatas.length);
		String tempData = data1.replace('|', '@');

		return tempData;


	}

	/**
	 * 读取证件全部信息
	 * 
	 * @param strPhotoPath 	: 头像图片存放路径
	 * @param strTimeout     : 超时间隔
	 */
	public String[] getIDFullInfo(String strPhotoPath, String strTimeout){
		int timeOut = getTime(strTimeout);
		if(timeOut==-1){
			return getParamErr();
		}
		isSuccess = false;

		Object object = getFinancialData(timeOut,strPhotoPath);
		String[] dataList = null;
		if(object!=null){
			String data = (String)object;
			String tempData = getIdCardData(data);
			tempData = sRight+split+tempData+split+strPhotoPath;
			dataList = tempData.split(split);	
			//successtimes++;
		}else{
			dataList = errorStr.split(split);
			//failtimes++;
		}

		//	Log.e("IDCardTest2", "successtimes="+ successtimes + ", failtimes=" + failtimes);

		return dataList;
	}

	@Override
	public byte[] getFinancialOpenCommad() {
		return BusinessInstruct.OpenBusiness((byte)0x30);
	}

	@Override
	public byte[] getFinancialCloseCommad() {
		return BusinessInstruct.CloseBusiness((byte)0x30);
	}

	@Override
	public Object getTestData(Object object) {
		IDCardData data = (IDCardData)object;
		if(data.style==1){
			return getIDCardInfo(data.timeOut);
		}else if(data.style==2){
			return getIDFullInfo(data.path, data.timeOut);
		}
		return null;
	}

	@Override
	protected int readData(byte[] data, int len, int timeOut) {
		return 0;
	}

	@Override
	protected int writeData(byte[] data, int len) {
		return 0;
	}

}
