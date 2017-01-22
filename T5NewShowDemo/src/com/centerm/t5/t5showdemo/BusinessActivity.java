package com.centerm.t5.t5showdemo;

import com.centerm.t5.newdispatch.t5showdemo.R;
import com.centerm.t5.t5showdemo.common.OnMessageListener;
import com.centerm.t5.t5showdemo.ui.SureDialog;
import com.centerm.t5.util.bluetooth.BluetoothOperator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class BusinessActivity extends Activity implements OnMessageListener{

	private SureDialog sureDialog;
	private BluetoothOperator bluetoothOperator = BluetoothOperator.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_buiness);

		ImageButton btn = (ImageButton)findViewById(R.id.btn1);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent(BusinessActivity.this, OpenCardActivity.class);
				BusinessActivity.this.startActivity(mIntent);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(sureDialog==null){
			sureDialog = new SureDialog(this, this);
		}
		sureDialog.showDialog();
	}

	@Override
	public void onMessageChange(int msg) {
		switch (msg) {
		case 1:
			finish();//ÍË³ö 
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bluetoothOperator.quitBluetooth();
	}
}
