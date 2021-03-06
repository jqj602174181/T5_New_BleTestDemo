package com.centerm.t5.t5showdemo;

import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.ICCardData;
import com.centerm.util.financial.IDCardData;
import com.centerm.util.financial.TestCommandData;

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
import android.widget.Toast;

//压力测试类 ic
public class FragmentTestBigFile extends FragmentBase {

	private Button startBtn = null;
	private String sdPath ="/mnt/sdcard/";

	private String imgPath = null;
	private Bitmap headBitmap;
	private TextView et_id_address, et_id_birthday, et_id_effectiveday, timeout,
	et_id_government, et_id_name, et_id_nation, et_id_num, et_id_sex, et_photo_path,
	et_path;
	private ImageView iv_id_person;

	private String informations[];

	private TestCommandData testCommandData;
	private String tag = "IcCardtest";
	private int time;

	private int readTime = 0; //测试次数
	private Handler handler;

	private TextView mTextView1;
	private TextView mTextView2, cardNo;

	private EditText editText11, editText22;

	private int mSucess = 0;
	private int mFail = 0;
	private int iIcFlag = 1;
	public static final int IC_INFO	=1; 

	private long time1 = System.currentTimeMillis();
	private long time2 = System.currentTimeMillis();

	private StringBuffer buffer = new StringBuffer();

	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.test_big_file);
		startBtn 		=(Button) view.findViewById(R.id.startBtn);
		editText22		=(EditText)view.findViewById(R.id.et_IdTime22);
		startBtn.setOnClickListener(this);
		setHandler(Looper.getMainLooper());
		testCommandData = new TestCommandData();
		return view;
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch(id)
		{
		case R.id.startBtn:
			String command = null;;
			testCommandData.COMMAND = command;
			readTime = 100;
			sendMessage();
			break;
		}
	}

	private void setHandler(Looper looper)
	{
		handler = new Handler(looper){
			public void handleMessage(Message msg)
			{
				mainActivity.sendMessage(DeviceOperatorData.TESTBIGFILE, testCommandData, time);
			}
		};
	}

	public void sendMessage()
	{
		time1 = System.currentTimeMillis();
		if(readTime==0)return;
		readTime--;
		handler.sendEmptyMessageDelayed(0, 1000);
	}

	@Override
	public void setData(Object data) {
		time2 = System.currentTimeMillis();

		long time = time2-time1;
		String value = String.valueOf(time);
		buffer.append(value);
		buffer.append(",");

		editText22.setText(buffer.toString());

		String[] result = (String[])data;
		if(result[0].equals("0")){
			Toast.makeText(mainActivity, "下载成功", Toast.LENGTH_SHORT).show();
			
			sendMessage();
		}
	}
}
