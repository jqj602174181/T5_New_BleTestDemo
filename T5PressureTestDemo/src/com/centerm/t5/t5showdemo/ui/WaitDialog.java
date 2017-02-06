package com.centerm.t5.t5showdemo.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.centerm.t5.t5showdemo.common.OnMessageListener;
import com.centerm.t5.util.dev.DeviceOperatorData;

public class WaitDialog extends Dialog{

	private Context context;
	private View mainView;
	private TextView textView;
	private String text;
	private OnMessageListener messageListener;
	public WaitDialog(Context context,boolean isCan,OnMessageListener messageListener)
	{
		super(context,R.style.wait_dialog);
		this.context = context;
		mainView =LayoutInflater.from(context).inflate(R.layout.wait_dialog, null);
		textView = (TextView)mainView.findViewById(R.id.iqclass_dialog_loading_name);
		LinearLayout.LayoutParams fill_parent = new LinearLayout.LayoutParams(

				LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.FILL_PARENT);
	
		setContentView(mainView,fill_parent);
		setCancelable(isCan);
		
		this.messageListener = messageListener;
		
	}
	
	public void setText(String text)
	{
		this.text = text;
		if(textView!=null)
			textView.setText(text);
	}
	
	public void showDialog()
	{
		if(!isShowing()){
			show();
		
		}
	}
	
	public void closeDialog()
	{
		if(isShowing()){
			dismiss();
			//indexThread.flag = false;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	
		if(messageListener!=null){
			messageListener.onMessageChange(DeviceOperatorData.CONNECTCANCEL);
		}
	}
	
	
}
