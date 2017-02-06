package com.centerm.t5.t5showdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.MsgCardData;

public class FragmentMsgCard extends FragmentBase{


	private TextView track2;
	private TextView track3;
	private Button ReadCard;
	private MsgCardData msgCardData;
	private  String tag = "MsgCardtest";
	private static int successtimes = 0;
	private  static int  failtimes = 0;
	public View onCreateView(LayoutInflater inflater,ViewGroup container ,Bundle savedInstanceState)
	{
	//	View view = inflater.inflate(R.layout.mag_card, null);
		View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.mag_card);
	
		msgCardData = new MsgCardData();
		track2 = (TextView)view.findViewById(R.id.et_track2);
		track3 = (TextView)view.findViewById(R.id.et_track3);
		ReadCard = (Button)view.findViewById(R.id.bt_readMsgCard);
		etTimeOut = (EditText)view.findViewById(R.id.et_msgTimeOut);
		ReadCard.setOnClickListener(this);
		
		return view;
	}
	@Override
	public void setData(Object data) {
		// TODO Auto-generated method stub
		
		String[] strList = (String[])data;
		if(strList[0].equals(sRight)){
			successtimes++;
			String[] strList1 = strList[1].split("#");
			track2.setText(strList1[0]+'\n'+strList1[1]);
		    Log.e(tag, "successtimes = "+ successtimes);
		}else{
			failtimes++;
			Log.e(tag, "failtimes= "+failtimes);
			track2.setText(mainActivity.getErrorMsg(strList[1]));
		}
		
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch(id)
		{
			case R.id.bt_readMsgCard:
				if(!isTimeOut(etTimeOut)){
					return;
				}
				
				//int i = 0;
				//int cnt = 100;
				
				//for( i =0; i< cnt;  i++)
			//	{
						msgCardData.timeOut = etTimeOut.getText().toString();
					
					   int time = Integer.parseInt(msgCardData.timeOut);
					   mainActivity.sendMessage(DeviceOperatorData.MAGCARD3,msgCardData,time );
				/*	   try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
				//Log.e(tag, "successtimes="+ successtimes + ", failtimes=" + failtimes);

				break;
		}
	}

}
