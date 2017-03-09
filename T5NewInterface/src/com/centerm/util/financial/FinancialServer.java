package com.centerm.util.financial;


import com.centerm.financial.FinancialBase;
import com.centerm.finger.wellcom.IFpDevDriver;
import com.centerm.iccard.IcCard;
import com.centerm.idcard.Idcard;
import com.centerm.jrz.JRZICcard;
import com.centerm.jrz.JRZIDcard;
import com.centerm.jrz.JRZMagcard;
import com.centerm.pin.Pin;
import com.centerm.rdcard.Rdcard;
import com.centerm.sign.Sign;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.testbigfile.TestBigFileDownload;
import com.centerm.testcommand.TestCommand;

public class FinancialServer {

	private int style = 1;
	private Idcard idcard;
	private Rdcard rdcard;
	private IcCard iccard;
	private Sign sign;
	private FinancialBase current;
	private Pin pin;
	private IFpDevDriver ifpDevDriver;

	private JRZIDcard mJrzIdcard;
	private JRZICcard mJrzIccard;
	private JRZMagcard mJrzMagcard;
	
	private TestCommand mTestCommand;
	private TestBigFileDownload mTestBigFile;
	
	public FinancialServer()
	{

	}

	public int getStyle()
	{
		return style;
	}
	
	/*
	 * 设置类型,根据类型来判断当前是哪个操作
	 */
	public void setStyle(int styel)
	{
		this.style = styel;
		switch (styel) {
		case DeviceOperatorData.ICCARD1:
			if(iccard==null){
				iccard = new IcCard();
			}
			current = iccard;
			break;
		case DeviceOperatorData.ICCARD2:
			if(iccard==null){
				iccard = new IcCard();
			}
			current = iccard;
			break;
		case DeviceOperatorData.MAGCARD1:
			if(rdcard==null){
				rdcard = new Rdcard();
			}
			current = rdcard;
			break;
		case DeviceOperatorData.MAGCARD2:
			if(rdcard==null){
				rdcard = new Rdcard();
			}
			current = rdcard;
			break;
		case DeviceOperatorData.MAGCARD3:
			if(rdcard==null){
				rdcard = new Rdcard();
			}
			current = rdcard;
			break;
		case DeviceOperatorData.KEYPAD:
			if(pin==null){
				pin = new Pin();
			}
			current = pin;
			break;
		case DeviceOperatorData.FINGER:
			if(ifpDevDriver==null){
				ifpDevDriver = new IFpDevDriver();
			}
			current = ifpDevDriver;
			break;

		case DeviceOperatorData.IDCARD:
			if(idcard==null)
				idcard = new Idcard();
			current = idcard;
			break;
		case DeviceOperatorData.SIGN:
			if(sign==null){
				sign = new Sign();
			}
			current= sign;
			break;
		case DeviceOperatorData.JRZIDCARD: //金融展 二代证
			if(mJrzIdcard==null){
				mJrzIdcard = new JRZIDcard();
			}
			current= mJrzIdcard;
			break;
		case DeviceOperatorData.JRZMAGCARD: //金融展 磁卡
			if(mJrzMagcard==null){
				mJrzMagcard = new JRZMagcard();
			}
			current= mJrzMagcard;
			break;
		case DeviceOperatorData.JRZICCARD: //金融展 ic卡
			if(mJrzIccard==null){
				mJrzIccard = new JRZICcard();
			}
			current= mJrzIccard;
			break;
		case DeviceOperatorData.TESTCOMMAND: //测试指令
			if(mTestCommand==null){
				mTestCommand = new TestCommand();
			}
			current= mTestCommand;
			break;
		case DeviceOperatorData.TESTBIGFILE: //测试指令
			if(mTestBigFile==null){
				mTestBigFile = new TestBigFileDownload();
			}
			current= mTestBigFile;
			break;
		default:
			break;
		}

		//	currentFinancial.setStyle(styel);
	}

	/*
	 * 获取读到的数据,null表示获取失败
	 */
	public Object getFinalcialData(Object commData)
	{
		Object object = null;
		object = current.getTestData(commData);
		return object;
	}

}
