package com.centerm.t5.t5showdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.SignData;

@SuppressLint("SdCardPath") public class FragmentSign extends FragmentBase{

	private Spinner spinner_encrpt;
	private Context context;
	private int iEncryType = 1;
	private EditText passKeys, encrySign,etPath;
	private Button ReadInfo,btKeyAffuse;
	private ImageView SignResult;
	private LinearLayout passKeysRow;
	private static final String[] encryType = { "不加密", "DES", "3DES" };
	private static final String PIC_PATH = "/mnt/sdcard/hw.png";
	private Bitmap bmp;
	private SignData signData;
	private boolean isSetWH =  false;
	private TextView tvPassKeys;
	private TextView tvState;
	
	private  String tag = "Signtest";
	private static  int successtimes = 0;
	private  static int  failtimes = 0;
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		//View view = inflater.inflate(R.layout.frag_sign, null);
		View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.frag_sign);
		spinner_encrpt = (Spinner) view.findViewById(R.id.spinner_encrpt);
		context = inflater.getContext();
		ArrayAdapter< String > adapter = new ArrayAdapter<String>(context, R.layout.my_spinner_item, encryType);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_encrpt.setAdapter(adapter);
		spinner_encrpt.setOnItemSelectedListener((OnItemSelectedListener) new SpinnerSelectedListener());
		passKeysRow 	= (LinearLayout) view.findViewById(R.id.passKeysRow);
		passKeys 		= (EditText) view.findViewById(R.id.passKeys);
		ReadInfo 		= (Button)view.findViewById(R.id.bt_elec_tag);//读取签名按钮
		etTimeOut		= (EditText)view.findViewById(R.id.timeout);
		etPath			= (EditText)view.findViewById(R.id.et_path);
		tvPassKeys		= (TextView)view.findViewById(R.id.tvPassKeys);
		tvState			= (TextView)view.findViewById(R.id.tvKeyState);
		btKeyAffuse		= (Button)view.findViewById(R.id.bt_signKeyAffuse);
		
		btKeyAffuse.setOnClickListener(this);
		ReadInfo.setOnClickListener(this);
		SignResult = (ImageView)view.findViewById(R.id.iv_img);//签名结果image
		signData = new SignData();
	
		return view;
	}
	
	
	private void setImgWH()
	{
		/*
		if(isSetWH)return;
		isSetWH = true;
		int w = SignResult.getLayoutParams().width;
		int h = SignResult.getLayoutParams().height;
		int width = w>h?h:w;
		SignResult.getLayoutParams().width = width;
		SignResult.getLayoutParams().height = width;
		*/
	}
	class SpinnerSelectedListener implements OnItemSelectedListener{
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
                long arg3) {
        	String type = arg0.getItemAtPosition(position).toString();
        	if (type == "DES") {
        		passKeysRow.setVisibility(View.VISIBLE);
        		passKeys.setText("1112131415161718");
        		iEncryType = 2;
        		Log.i("Sign","DES");
        	} else if (type == "3DES") {
        		passKeysRow.setVisibility(View.VISIBLE);
        		passKeys.setText("11121314151617182122232425262728");
        		iEncryType = 3;
        		Log.i("Sign","3DES");
        	} else if (type == "不加密") {
        		passKeysRow.setVisibility(View.INVISIBLE);
        		iEncryType = 1;
        		Log.i("Sign","不加密");
        	}
        }

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	}
	
	@Override
	public void setData(Object data) {
		String[] dataList = (String[])data;
		if(!dataList[0].equals(sRight)){
			
			//failtimes++;
		//	Log.e(tag, "failtimes=" + failtimes);
			
			if(signData.style==2){
				tvState.setText(mainActivity.getErrorMsg(dataList[1]));
			}else if(signData.style==1){
				//CommonUtil.showTip(context, ""+dataList[0]);
				if(dataList.length>1){
					etPath.setText(mainActivity.getErrorMsg(dataList[1]));
				}else{
					etPath.setText(mainActivity.getErrorMsg(dataList[0]));
				}
			
			}
			
			return;
		}
		
		//successtimes++;
		
	//	Log.e(tag, "successtimes=" + successtimes);
		if(signData.style==2){
			
			tvState.setText(success);
			return;
		}
		if(bmp!=null){
			SignResult.setImageBitmap(null);
			bmp.recycle();
		}
		
		 etPath.setText(dataList[2]);
		 bmp = BitmapFactory.decodeFile(dataList[1]);
		 SignResult.setVisibility(View.VISIBLE);
		 SignResult.setImageBitmap(bmp);
		//bmp.recycle();
	}

 //按钮控件
	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch(id)
		{
			case R.id.bt_elec_tag:
				if(!isTimeOut(etTimeOut)){
					return;
				}
				setImgWH();
				signData.style = 1;//设置手写签名调用方式
				int style =  DeviceOperatorData.SIGN;//手写签名模块选择
				signData.timeOut = etTimeOut.getText().toString();//获取超时时间
				int time = getTime(signData.timeOut)+60;
				
				//int j = 0;
				//int  cnt =10;
				
				//for( j =0; j <cnt ; j++)
				//{
				   mainActivity.sendMessage(style,signData,time);
					/*   try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   
				}*/
				break;
			case R.id.bt_signKeyAffuse:
				signData.style =2;
				String keyStr = tvPassKeys.getText().toString();
				String tempStr = keyStr.replace(',', '@');
				
				String values[] = tempStr.split("@");
				for(int i=0;i<values.length;i++){
					//Log.e("value","value is "+values[i]);
				}
				signData.keyAffuseList =values;
				mainActivity.sendMessage(DeviceOperatorData.SIGN,signData,-1);
				break;
		}
	}
	
	private byte[] valueToByte(String value)
	{
		byte index = (byte)30;
		byte[] data = new byte[value.length()/2];
		int  j =0;
		for(int i=1;i<value.length();i=i+2){
			data[j++] = (byte)(value.getBytes()[i]-index);
		}
		return data;
	}
}

