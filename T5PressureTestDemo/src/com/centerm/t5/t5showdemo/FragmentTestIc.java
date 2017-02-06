package com.centerm.t5.t5showdemo;

import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.ICCardData;
import com.centerm.util.financial.IDCardData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

//压力测试类 ic
public class FragmentTestIc extends FragmentBase {

	private Button readBtn = null;
	private String sdPath ="/mnt/sdcard/";

	private String imgPath = null;
	private Bitmap headBitmap;
	private Context context;
	private TextView et_id_address, et_id_birthday, et_id_effectiveday, timeout,
	et_id_government, et_id_name, et_id_nation, et_id_num, et_id_sex, et_photo_path,
	et_path;
	private ImageView iv_id_person;

	private String informations[];

	private ICCardData icCardData;
	private String tag = "IcCardtest";
	private int time;

	private int readTime = 0; //测试次数
	private Handler handler;

	private TextView mTextView1;
	private TextView mTextView2, cardNo;

	private int mSucess = 0;
	private int mFail = 0;
	private int iIcFlag = 1;
	public static final int IC_INFO	=1; 

	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.test_ic_card);

		mTextView1		=(TextView) view.findViewById(R.id.test_success);
		mTextView2		=(TextView) view.findViewById(R.id.test_fail);

		readBtn 		=(Button) view.findViewById(R.id.readBtn);
		cardNo 			=(TextView) view.findViewById(R.id.test_card_no);
		etTimeOut		=(EditText)view.findViewById(R.id.et_IdTimeOut);
		icCardData = new ICCardData();
		readBtn.setOnClickListener(this);

		setHandler(Looper.getMainLooper());

		mTextView1.setText(String.valueOf(mSucess));
		mTextView2.setText(String.valueOf(mFail));
		return view;
	}

	private void setHandler(Looper looper)
	{
		handler = new Handler(looper){
			public void handleMessage(Message msg)
			{
				mainActivity.sendMessage(DeviceOperatorData.ICCARD1, icCardData, time);
			}
		};
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if(!isTimeOut(etTimeOut)){
			return;
		}
		icCardData.timeOut = etTimeOut.getText().toString();	
		time = getTime(icCardData.timeOut);
		switch(id)
		{
		case R.id.readBtn:
			icCardData.list = null;
			icCardData.cardStyle = iIcFlag;
			icCardData.tag = "A";
			icCardData.style = IC_INFO;
			readTime = 10000;
			//			mainActivity.sendMessage(DeviceOperatorData.IDCARD,idCardData,time);
			sendMessage();
			break;
		}
	}

	public void sendMessage()
	{
		if(readTime==0)return;
		readTime--;
		handler.sendEmptyMessageDelayed(0, 1000);
	}

	@Override
	public void setData(Object data) {
		String[] dataList = (String[])data;
		String result = null;
		if(!dataList[0].equals(sRight)){
			result = mainActivity.getErrorMsg(dataList[1]);
			cardNo.setText(result);
			mFail++;
			mTextView2.setText(String.valueOf(mFail));
		}else{
			result = dataList[2];
			cardNo.setText(result);
			mSucess++;
			mTextView1.setText(String.valueOf(mSucess));
		}
		sendMessage();
	}
}
