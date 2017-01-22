package com.centerm.t5.t5showdemo;

import android.app.ProgressDialog;
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
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.centerm.t5.newdispatch.t5showdemo.R;
import com.centerm.t5.util.common.CommonUtil;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.PinData;


public class FragmentPasswordKeyPad extends FragmentBase{

	private ArrayAdapter< String> adapter;
	private int iEncryType = 1, itimes = 1;
	private ProgressDialog pd;
	private Spinner spinner_pin_des, passTimes;
	private static final String[] encryType = { "不加密" };
	private static final String[] times = {"1", "2"};
	private EditText  passKeys, passLength;
	private TableRow keysRow;
	private TextView etPassword;
	private Button btnReadPassword,btnKeyAffusePin;
	private static final String TAG  = "FragmentPasswordKeyPad";
	private String lenNoNull;
	private PinData pinData;
	private EditText etMiYao1,etMiYao2,etMiYao3,etState;
	private String success = "成功";
	private  String tag = "KeyPadtest";
	private static int successtimes = 0;
	private  static int  failtimes = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.frag_password_keypad);
		//	View view = inflater.inflate(R.layout.frag_password_keypad, null);
		etPassword 		= (TextView)view.findViewById(R.id.TV_Password);
		btnReadPassword = (Button)view.findViewById(R.id.bt_readKeyPad);
		btnKeyAffusePin	= (Button)view.findViewById(R.id.bt_keyAffusePin);
		passKeys 		= (EditText) view.findViewById(R.id.passKeys);
		etTimeOut 		= (EditText) view.findViewById(R.id.timeout);
		passLength 		= (EditText) view.findViewById(R.id.passLength);
		spinner_pin_des = (Spinner) view.findViewById(R.id.spinner_pin_des);
		etMiYao1		= (EditText)view.findViewById(R.id.et_miyao1);
		etMiYao2		= (EditText)view.findViewById(R.id.et_miyao2);
		etMiYao3		= (EditText)view.findViewById(R.id.et_miyao3);
		etState			= (EditText)view.findViewById(R.id.et_state);
		adapter 		= new ArrayAdapter<String>(mainActivity, R.layout.my_spinner_item, encryType);
		lenNoNull		= getString(R.string.lenNoNull);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_pin_des.setAdapter(adapter);
		spinner_pin_des.setOnItemSelectedListener((OnItemSelectedListener) new SpinnerSelectedListener());

		passTimes = (Spinner)view.findViewById(R.id.passTimes);
		adapter = new ArrayAdapter<String>(mainActivity, R.layout.my_spinner_item, times);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		passTimes.setAdapter(adapter);
		passTimes.setOnItemSelectedListener((OnItemSelectedListener) new SpinnerSelectedListener());

		keysRow = (TableRow)view.findViewById(R.id.keysRow);
		btnReadPassword.setOnClickListener(this);
		btnKeyAffusePin.setOnClickListener(this);
		pinData = new PinData();
		return view;
	}

	private  class SpinnerSelectedListener implements OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if (arg0 == passTimes) {
				String times = arg0.getItemAtPosition(position).toString();
				if (times.equals("2")) {
					itimes = 2;
				}else{
					itimes=1;
				}
			} else {
				String type = arg0.getItemAtPosition(position).toString();
				if (type.equals("DES")) {
					iEncryType = 2;
					//	keysRow.setVisibility(View.VISIBLE);
					passKeys.setText("1112131415161718");
				} else if (type.equals("3DES")) {
					iEncryType = 3;
					//		keysRow.setVisibility(View.VISIBLE);
					passKeys.setText("11121314151617182122232425262728");
				} else if (type.equals( "不加密") ){
					iEncryType = 1;
					keysRow.setVisibility(View.INVISIBLE);
					passKeys.setText("");
				}
			}
		}
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	}
	@Override
	public void setData(Object data) {

		String [] dataList = (String[])data;

		if(dataList[0].equals(sRight)){
			successtimes++;
			//Log.e(tag, "successtimes= "+successtimes);

			if(pinData.style==1){
				if(dataList.length==2){
					etPassword.setText(dataList[1]);
					Log.e(tag,"dataList[1]="+dataList[1]);
				}else if(dataList.length==3){
					StringBuilder builder = new StringBuilder();
					builder.append(dataList[1]);
					builder.append('\n');
					builder.append(dataList[2]);
					etPassword.setText(builder.toString());
					//Log.e(tag, "dataList[2]="+dataList[2]);
				}
			}else if(pinData.style==2){
				etState.setText(success);
			}
		}else{

			failtimes++;
			//Log.e(tag, "failtimes= "+failtimes);

			if(pinData.style==1)
			{
				etPassword.setText(mainActivity.getErrorMsg(dataList[1]));
			}else if(pinData.style==2){
				etState.setText(mainActivity.getErrorMsg(dataList[1]));
			}
			//
		}

	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch(id)
		{
		case R.id.bt_readKeyPad:

			String strLen = passLength.getText().toString();
			if(strLen.length()==0){
				CommonUtil.showTip(mainActivity, lenNoNull);
				return ;
			}
			pinData.timeOut = etTimeOut.getText().toString();	
			int time =getTime(pinData.timeOut);
			pinData.style 		= 1;
			pinData.iEncryType 	=iEncryType;
			pinData.iTimes		= itimes;
			//Log.e("pin", "itemes is "+itimes+" time is");
			int passwordLen 	= getLen(passLength.getText().toString());
			pinData.iLength 	= passwordLen;


			// int i = 0;
			//int cnt =100;
			//for( i =0 ;i<cnt; i++)
			//{
			mainActivity.sendMessage(DeviceOperatorData.KEYPAD,pinData,time);
			//    try {
			//	Thread.sleep(500);
			//} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//}

			//}

			// Log.e(tag, "successtimes="+ successtimes + ", failtimes=" + failtimes);
			break;
		case R.id.bt_keyAffusePin:
			String[] keys= {"1111111","11111111","2222222",""};
			pinData.style = 2;
			pinData.keys[0] = etMiYao1.getText().toString();
			pinData.keys[1] = etMiYao2.getText().toString();
			pinData.keys[2] = etMiYao3.getText().toString();
			mainActivity.sendMessage(DeviceOperatorData.KEYPAD,pinData,-1);
			break;
		};
	}


	private int getLen(String sLen)
	{
		int len =0;
		try{
			len = Integer.parseInt(sLen);
		}catch(NumberFormatException e){

		}


		return len;
	}
}
