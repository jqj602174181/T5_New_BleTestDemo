package com.centerm.t5.t5showdemo;

import android.R.integer;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.centerm.t5.newdispatch.t5showdemo.R;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.FingerData;

public class FragmentFingerprint extends FragmentBase{

	private TextView FingerInfo;
	private Button ReadInfo;
	private int style =  DeviceOperatorData.FINGER;//指纹仪模块
	private static final String[] n = {"维尔"};
	private Button bt_fp_model;
	private TextView  et_fp_model;
	private Spinner spinner_ic_card;
	private ArrayAdapter<String> adapter;
	private FingerData fingerData;
	//测试需要
	private  String tag1 = "fingertest";
	private static  int failtimes = 0;
	private static   int successtimes = 0; 
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
	//	View view = inflater.inflate(R.layout.fingerprints, null);
		View view 		= super.onCreateView(inflater, container, savedInstanceState, R.layout.fingerprints);
		FingerInfo 		= (TextView)view.findViewById(R.id.et_fp2);
		ReadInfo 		= (Button)view.findViewById(R.id.bt_fp2);
		bt_fp_model 	= (Button)view.findViewById(R.id.bt_fp_model);
		et_fp_model 	= (TextView)view.findViewById(R.id.et_fp_model);
		etTimeOut		= (EditText)view.findViewById(R.id.et_timeout);
		ReadInfo.setOnClickListener(this);
		bt_fp_model.setOnClickListener(this);
		
		spinner_ic_card = (Spinner) view.findViewById(R.id.spinner_ic_card);
		adapter = new ArrayAdapter<String>(getActivity(), R.layout.my_spinner_item, n);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_ic_card.setAdapter(adapter);
		fingerData = new FingerData();
		return view;
	}
	@Override
	public void setData(Object data) {
		String[] dataList =(String[])data;
		if(!dataList[0].equals(sRight)){//如果失败，则返回错误信息
			String err = null;
			//failtimes ++;
			Log.e(tag1,   "failtimes = " + failtimes);
			if(dataList.length==1){
				err = dataList[0];
				
			}else{
				err = dataList[1];
			}
			if(fingerData.style==1){
				et_fp_model.setText(mainActivity.getErrorMsg(err));
			
				
			}else if(fingerData.style==2){
				FingerInfo.setText(mainActivity.getErrorMsg(err));
			
			}
			return;
		}
		//successtimes++;
		Log.e(tag1, "successtimes= " + successtimes);
		StringBuilder builder = new StringBuilder();
		builder.append("指纹仪产商代码:");
		builder.append(dataList[1]);
		builder.append('\n');
		builder.append("指纹特征码:");
		builder.append(dataList[2]);
		if(fingerData.style==1){
			et_fp_model.setText(builder.toString());
		
	
		}else if(fingerData.style==2){
			FingerInfo.setText(builder.toString());
		
		}
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		int i = 0;
		if(!isTimeOut(etTimeOut)){
			return;
		}
		fingerData.timeOut = etTimeOut.getText().toString();
		int time = getTime(fingerData.timeOut);
		switch(id)
		{
			case R.id.bt_fp2:
				int cnt =100;
			//	for( i = 0; i < cnt; i++ )
				//{
					
					fingerData.style = 2;//指纹模板登记
				    FingerInfo.setText("");
				    mainActivity.sendMessage(style,fingerData,time);
				   
			/*				try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				}*/
				//Log.e(tag1, "successtimes="+ successtimes + ", failtimes=" + failtimes);
				break;
			case R.id.bt_fp_model:
				
					et_fp_model.setText("");
				
				    fingerData.style = 1; //指纹特征
				    mainActivity.sendMessage(style,fingerData,time);
				
				break;
		}
	}
}