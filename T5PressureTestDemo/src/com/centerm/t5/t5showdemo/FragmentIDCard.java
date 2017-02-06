package com.centerm.t5.t5showdemo;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.IDCardData;

public class FragmentIDCard extends FragmentBase{


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

	private IDCardData idCardData;
	private Object lockObj;
	private String tag = "IDCardtest";
	private int time;
	private ReadThrad readThrad;

	private int readTime = 1;//测试次数
	private Handler handler;
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.id_card);

		readBtn 			= (Button) view.findViewById(R.id.readBtn);
		iv_id_person		=(ImageView) view.findViewById(R.id.iv_id_img);
		et_id_address		=(TextView) view.findViewById(R.id.et_id_address);
		et_id_birthday		=(TextView) view.findViewById(R.id.et_id_birthday);
		et_id_effectiveday	=(TextView) view.findViewById(R.id.et_id_effectiveday);
		et_id_government	=(TextView) view.findViewById(R.id.et_id_government);
		et_id_name			=(TextView) view.findViewById(R.id.et_id_name);
		et_id_nation		=(TextView) view.findViewById(R.id.et_id_nation);
		et_id_num			=(TextView) view.findViewById(R.id.et_id_num);
		et_id_sex			=(TextView) view.findViewById(R.id.et_id_sex);
		et_path				= (TextView)view.findViewById(R.id.et_head_path);
		etTimeOut			= (EditText)view.findViewById(R.id.et_IdTimeOut);
		idCardData 			= new IDCardData();
		lockObj = new Object();
		readBtn.setOnClickListener(this);
		if(headBitmap!=null){
			iv_id_person.setImageBitmap(headBitmap);
		}

		setInformation(informations);
		readThrad = new ReadThrad();
		readThrad.start();
		readThrad.setPriority(Thread.MAX_PRIORITY);
		//readThrad.set
		//	setHandler();
		setHandler(Looper.getMainLooper());
		return view;
	}

	private boolean setInformation(String[] strList)
	{
		if(strList!=null){
			if(!strList[0].trim().equals(sRight)){
				et_id_name.setText(mainActivity.getErrorMsg(strList[1]));
				et_id_sex.setText(null);
				et_id_nation.setText(null);
				et_id_birthday.setText(null);
				et_id_address.setText(null);
				et_id_num.setText(null);
				et_id_effectiveday.setText(null);
				et_id_government.setText(null);
				et_path.setText(null);
				iv_id_person.setImageBitmap(null);
				if(headBitmap!=null&&!headBitmap.isRecycled()){
					headBitmap.recycle();
				}
				return false;
			}

			et_id_name.setText(strList[1]);
			et_id_sex.setText(strList[2]);
			et_id_nation.setText(strList[3]);
			et_id_birthday.setText(strList[4]);
			et_id_address.setText(strList[5]);
			et_id_num.setText(strList[6]);
			et_id_effectiveday.setText(strList[8]);
			et_id_government.setText(strList[7]);
			imgPath = strList[9];
			et_path.setText(imgPath);

			return true;
		}
		return false;
	}
	@Override
	public void setData(Object data) {

		informations = (String[])data;
		//	et_id_government.setText((String)data);
		if(!setInformation(informations)){
			unLock();
			sendMessage();
			return;
		}

		//显示照片
		iv_id_person.setImageBitmap(null);
		if(headBitmap!=null&&!headBitmap.isRecycled()){
			headBitmap.recycle();
		}
		headBitmap = BitmapFactory.decodeFile(imgPath);
		iv_id_person.setImageBitmap(headBitmap);
		sendMessage();
	}

	private void lock()
	{
		synchronized (lockObj) {
			try {
				Log.e(tag, "wait");
				lockObj.wait();
				Log.e(tag, "wait1122");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void unLock()
	{
		synchronized (lockObj) {
			lockObj.notify();
		}
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if(!isTimeOut(etTimeOut)){
			return;
		}
		idCardData.timeOut = etTimeOut.getText().toString();	
		time = getTime(idCardData.timeOut);
		switch(id)
		{
		case R.id.readBtn:
			idCardData.style=2;

			readTime = 0;
			
			mainActivity.sendMessage(DeviceOperatorData.IDCARD,idCardData,time);
			//			   sendMessage();
			break;
		}
	}

	private void setHandler(Looper looper)
	{
		handler = new Handler(looper){
			public void handleMessage(Message msg)
			{
				et_id_sex.setText(null);
				et_id_nation.setText(null);
				et_id_birthday.setText(null);
				et_id_address.setText(null);
				et_id_num.setText(null);
				et_id_effectiveday.setText(null);
				et_id_government.setText(null);
				et_path.setText(null);
				iv_id_person.setImageBitmap(null);
				mainActivity.sendMessage(DeviceOperatorData.IDCARD,idCardData,time);
			}
		};
	}

	public void sendMessage()
	{
		if(readTime==0)return;
		readTime--;
		//mainActivity.sendMessage1(DeviceOperatorData.IDCARD,idCardData,time);
		handler.sendEmptyMessageDelayed(0,2000);
		//handler.sendEmptyMessage(0,200);
	}

	private class ReadThrad extends Thread{
		//做测试用，先留着

		private int time = 10;

		private Looper myLooper;
		private ReadThrad()
		{
			//this.setPriority(Thread.MIN_PRIORITY); // 1
		}
		public void run()
		{
			super.run();

			Looper.prepare();
			myLooper = Looper.myLooper();

			Looper.loop();
			/*
			for(int i=0;i<10;i++){
				lock();
				mainActivity.sendMessage1(DeviceOperatorData.IDCARD,idCardData,time);
			}*/
		}

		public void quitThread()
		{
			if(myLooper!=null)
				myLooper.quit();
		}

		public void unLock()
		{
			synchronized (this) {
				this.notify();
			}
		}
	}

	public void onDestroy()
	{
		super.onDestroy();
		if(readThrad!=null)
			readThrad.quitThread();
	}
}
