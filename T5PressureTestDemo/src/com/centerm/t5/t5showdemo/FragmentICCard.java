package com.centerm.t5.t5showdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.ICCardData;

public class FragmentICCard extends FragmentBase {

	public static final String TAG = "ICCardTest";
	public static final int IC_INFO		=1; 
	public static final int IC_ARQC		=2; 
	public static final int IC_DETAIL	=3; 
	public static final int IC_RW		=4; 
	private TextView tv_ReadInfo;
	private TextView tv_ReadARQC;
	private TextView tv_ReadDetail;
	private TextView tv_ReadRW;
	private EditText etList;
	private EditText etARQC;
	private EditText etTag;
	private Button btReadInfo;
	private Button btReadARQC;
	private Button btnReadDetail;
	private Button btnReadRW;//读写能力
	private ArrayAdapter<String> adapter;
	public Spinner spinner_type;
	private static final String[] selectData = { "接触式ic卡", "非接触式ic卡"};
	private int selectIndex = 0;
	private int style = DeviceOperatorData.ICCARD1;	
	private int iIcFlag = 1;
	private int successtimes  = 0;
	private Object lockObj;
	private ICCardData icCardData;
	private  int time;
	private Handler handler;
	private int readTime = 1;//测试次数
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		View view 		= super.onCreateView(inflater, container, savedInstanceState, R.layout.frag_ic_card);
		tv_ReadDetail	= (TextView)view.findViewById(R.id.EDIT_GetDetail);
		tv_ReadRW		= (TextView)view.findViewById(R.id.EDIT_RW);
		tv_ReadInfo 	= (TextView)view.findViewById(R.id.EDIT_ReadInfo);
		tv_ReadARQC 	= (TextView)view.findViewById(R.id.EDIT_ReadARQC);
		btReadInfo 		= (Button)view.findViewById(R.id.Btn_ReadInfo);
		btReadARQC		= (Button)view.findViewById(R.id.Btn_ReadARQC);
		btnReadDetail	= (Button)view.findViewById(R.id.Btn_GetDetail);
		btnReadRW		= (Button)view.findViewById(R.id.Btn_RW);
		etList			= (EditText)view.findViewById(R.id.EDIT_List);
		etARQC			= (EditText)view.findViewById(R.id.EDIT_ARQC);
		etTag			= (EditText)view.findViewById(R.id.et_ic_tag);
		etTimeOut		= (EditText)view.findViewById(R.id.et_ic_timeout);
		btReadInfo.setOnClickListener(this);
		btReadARQC.setOnClickListener(this);
		btnReadRW.setOnClickListener(this);
		btnReadDetail.setOnClickListener(this);

		spinner_type=(Spinner) view.findViewById(R.id.spinner_iccard_type);
		
		adapter=new ArrayAdapter<String>(getActivity(), R.layout.my_spinner_item,selectData);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_type.setAdapter(adapter);
        spinner_type.setOnItemSelectedListener((OnItemSelectedListener) new SpinnerSelectedListener());
        spinner_type.setVisibility(View.VISIBLE);
        icCardData = new ICCardData();
        lockObj = new Object();
        setHandler();
		return view;
	}
	
	private void setHandler()
	{
		handler = new Handler(){
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);
				mainActivity.sendMessage(style,icCardData,time);
			}
		};
	}
	
	private void sendMessage()
	{
		if(readTime==0)return;
		readTime--;
		handler.sendEmptyMessageDelayed(0, 2000);
	}
	
	@Override
	public void setData(Object data) {
		/*
		if(style== DeviceOperatorData.ICCARD1){
			tv_ReadInfo.setText((String)data);
		}else if(style== DeviceOperatorData.ICCARD2){
			tv_ReadARQC.setText((String)data);
		}*/
		endTime = System.currentTimeMillis();
		String[] dataList = (String[])data;
		String result = null;
		if(!dataList[0].equals(sRight)){
			result = mainActivity.getErrorMsg(dataList[1]);
		}else{
			result = dataList[2];
		}
		long time = (endTime-currentTime)/1000;
		switch (icCardData.style) {
			case IC_INFO:
		//		tv_ReadInfo.setText(result+","+useTime+time+second);
				tv_ReadInfo.setText(result);
				break;
			case IC_ARQC:
		//		tv_ReadARQC.setText(useTime+time+second+split+result);
				tv_ReadARQC.setText(result);
				break;
			case IC_DETAIL:
		//		tv_ReadDetail.setText(useTime+time+second+split+result);
				tv_ReadDetail.setText(result);
				break;
		default:
			break;
		}
		//unLock();
		sendMessage();
	}
	 //使用数组形式操作
    class SpinnerSelectedListener implements OnItemSelectedListener{
 
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
                long arg3) {
        	selectIndex = position;
        	if(position==0){//接触式ic卡
        		iIcFlag = 1;
        	}else if(position==1){//"非接触式ic卡"
        		iIcFlag = 2;
        	}else if(position==2){//自动
        		iIcFlag = 3;
        	}
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
	
    private void lock()
    {
    	synchronized (lockObj) {
			try {
				lockObj.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
    
    private void unLock()
    {
    	synchronized (lockObj) {
			lockObj.notifyAll();
		}
    }
	@Override
	public void onClick(View view) {
		if(!isTimeOut(etTimeOut)){
			return;
		}
		icCardData.timeOut =etTimeOut.getText().toString(); 
		time = getTime(icCardData.timeOut);
		int id = view.getId();
		currentTime = System.currentTimeMillis();
		readTime = 0;
		switch(id){
			case R.id.Btn_ReadInfo:
				
		
				tv_ReadInfo.setText("");
				icCardData.list = etList.getText().toString();
				icCardData.cardStyle = iIcFlag;
				icCardData.tag = etTag.getText().toString().trim();
				icCardData.style = IC_INFO;
				style =  DeviceOperatorData.ICCARD1;
				mainActivity.sendMessage(DeviceOperatorData.ICCARD1,icCardData,time);
				
				
				break;
			case R.id.Btn_ReadARQC:
				tv_ReadARQC.setText("");
				style =  DeviceOperatorData.ICCARD2;
				icCardData.cardStyle = iIcFlag;
				icCardData.style = IC_ARQC;
				icCardData.ARQC = etARQC.getText().toString();
				icCardData.list = etList.getText().toString();
				mainActivity.sendMessage(DeviceOperatorData.ICCARD2,icCardData,time);
				break;
			case R.id.Btn_GetDetail:
				tv_ReadDetail.setText("");
				icCardData.list = etList.getText().toString();
				icCardData.cardStyle = iIcFlag;
				icCardData.style = IC_DETAIL;
				mainActivity.sendMessage(DeviceOperatorData.ICCARD1,icCardData,time);
				break;
		}
	
	}
	
}
