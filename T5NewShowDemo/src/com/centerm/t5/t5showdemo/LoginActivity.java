package com.centerm.t5.t5showdemo;

import java.io.File;

import com.centerm.device.CommService;
import com.centerm.t5.newdispatch.t5showdemo.R;
import com.centerm.t5.t5showdemo.common.OnMessageListener;
import com.centerm.t5.t5showdemo.ui.WaitDialog;
import com.centerm.t5.util.bluetooth.BluetoothOperator;
import com.centerm.t5.util.common.CommonUtil;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.financial.FingerData;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class LoginActivity extends Activity implements View.OnClickListener,OnMessageListener,OnCheckedChangeListener{

	private TextView connectTxt;
	private Button loginBtn;
	private RadioGroup rgConn;

	private EditText accountEdit;
	private EditText passwordEdit;

	private static final String ACCOUNT = "centerm";
	private static final String PASSWORD = "123456";

	private TextView tvState;
	private ImageView connFlag;

	private Handler mainHandler;
	private BluetoothOperator bluetoothOperator = BluetoothOperator.getInstance();
	private boolean isPressConnect = false;
	private WaitDialog waitDialog;
	private boolean isConnect = false;//用于判断是否连接上
	private String readWait;
	private FingerData fingerData;
	private ReadWaitTime readWaitTime;

	private int style = DeviceOperatorData.FINGER;//指纹仪模块

	private String connectSuccess = "连接成功";
	private String currentState = "未连接";
	private String connectFail = "连接失败";
	private String connectCancel = "连接断开";
	private String noCheck = "未检测";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		initView();

		readWait = getString(R.string.readWait);

		setHandler();

		bluetoothOperator.setContext(getApplicationContext());
		bluetoothOperator.setHandler(mainHandler);

		fingerData = new FingerData();
		readWaitTime 	= new ReadWaitTime();
		CommService.type = 1;
	}

	private void setHandler() {
		mainHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				setConnectState(msg.what);//判断连接状态
				Log.e("LoginActivity","setHandler: msg.what = "+ msg.what );
				switch (msg.what) {
				case DeviceOperatorData.CONNECTFAIL://设备连接失败
					closeWaitDialog();
					isConnect = false;
					break;
				case DeviceOperatorData.CONNECTSUCCESS://设备连接成功
					isConnect= true;
					waitDialog.setText(readWait);
					closeWaitDialog();
					break;
				case DeviceOperatorData.NODEVICE://没有设备
					isConnect = false;
					closeWaitDialog();
					CommonUtil.showTip(LoginActivity.this, LoginActivity.this.getString(R.string.checkBluetooth));
					break;
				case DeviceOperatorData.CONNECTCANCEL://设备连接失败
					isConnect = false;
					closeWaitDialog();
					break;
				case DeviceOperatorData.MESSAGE://获取消息
					//					currentFragment.setData(msg.obj);//将获取的信息显示在UI界面
					Intent mIntent = new Intent();
					mIntent.setClass(LoginActivity.this, BusinessActivity.class);
					LoginActivity.this.startActivity(mIntent);

					Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

					waitDialog.closeDialog();
					readWaitTime.stopRun();

					finish();
					break;
				case DeviceOperatorData.READ_FAIL://读取失败
					waitDialog.closeDialog();
					readWaitTime.stopRun();
					break;
				default:
					break;
				}
			}
		};
	}

	protected void setConnectState(int state)
	{
		switch (state) {
		case DeviceOperatorData.CONNECTSUCCESS://连接成功
			tvState.setText(connectSuccess);
			connFlag.setBackgroundResource(R.drawable.conn_flag);
			connectTxt.setEnabled(false);
			isConnect = true;
			currentState = connectSuccess;
			break;
		case DeviceOperatorData.CONNECTFAIL://连接失败
			tvState.setText(connectFail);
			connFlag.setBackgroundResource(R.drawable.not_conn_flag);
			connectTxt.setEnabled(true);
			isConnect = false;
			currentState = connectFail;
			break;
		case DeviceOperatorData.CONNECTCANCEL://取消连接
			tvState.setText(connectCancel);
			connFlag.setBackgroundResource(R.drawable.not_conn_flag);
			connectTxt.setEnabled(true);
			isConnect = false;
			currentState = connectCancel; 
			break;
		case DeviceOperatorData.MESSAGE:
			break;
		case DeviceOperatorData.NODEVICE:
			break;
		default:
			tvState.setText(currentState);
			connectTxt.setEnabled(true);
			connFlag.setBackgroundResource(R.drawable.not_conn_flag);
			isConnect = false;
			currentState = noCheck; 
			break;
		}
	}

	public void closeWaitDialog()
	{
		if(isPressConnect){
			waitDialog.closeDialog();
		}
	}

	private void initView() {
		connectTxt = (TextView)findViewById(R.id.bt_conn_open);
		loginBtn = (Button)findViewById(R.id.login_Btn);
		rgConn = (RadioGroup)findViewById(R.id.rg_conn);
		accountEdit = (EditText)findViewById(R.id.account);
		passwordEdit = (EditText)findViewById(R.id.password);

		tvState = (TextView)findViewById(R.id.tvBTState);
		tvState.setText(currentState);

		connFlag = (ImageView)findViewById(R.id.connection_flag);

		connectTxt.setOnClickListener(this);
		loginBtn.setOnClickListener(this);
		rgConn.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.bt_conn_open:
			connectTxt.setEnabled(false);
			connectBluetooth( null );
			break;
		case R.id.login_Btn:
			if(!isConnect){
				Toast.makeText(LoginActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
				return;
			}
			String account = accountEdit.getEditableText().toString();
			String password = passwordEdit.getEditableText().toString();
			if(TextUtils.isEmpty(account)){
				Toast.makeText(LoginActivity.this, "帐号不能为空！", Toast.LENGTH_SHORT).show();
				return;
			}
			if(TextUtils.isEmpty(password)){
				Toast.makeText(LoginActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!account.equals(ACCOUNT)){
				Toast.makeText(LoginActivity.this, "帐号不对！", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!password.equals(PASSWORD)){
				Toast.makeText(LoginActivity.this, "密码不对！", Toast.LENGTH_SHORT).show();
				return;
			}

			//调用读取指纹仪命令
			fingerData.style = 1; //指纹特征
			fingerData.timeOut = "15";
			sendMessage(style, fingerData, Integer.parseInt(fingerData.timeOut));
			break;
		}
	}

	void sendMessage(int style,Object object,int time)
	{
		if(time != -1){
			readWaitTime.startRun(time+5);

		}
		if(waitDialog==null){
			waitDialog = new WaitDialog(this, false, this);
			waitDialog.setText(readWait);
		}
		waitDialog.showDialog();

		if(!bluetoothOperator.getIsConnect()){//若设备没有连接成功，则重新连接再执行
			Toast.makeText(LoginActivity.this, "已断开，请重连！", Toast.LENGTH_SHORT).show();
			isPressConnect = false;
			new Thread(){
				public void run()
				{
					//					bluetoothOperator.setBluetoothMac(macAddr);
					//					bluetoothOperator.startBluetooth();
					//					connectAndSend(style1, obj);
				}
			}.start();
		}else{
			bluetoothOperator.sendMessage(0, style, object);// 模块操作（ style用于判断是哪种金融模块操作）
		}
	}

	/**
	 * 设备连接
	 * */
	void connectBluetooth(String mac)
	{
		isPressConnect = true; 
		if(waitDialog == null){
			waitDialog = new WaitDialog(this, false, this);
		}
		bluetoothOperator.setBluetoothMac(mac);//设置蓝牙地址
		bluetoothOperator.startBluetooth();//开启蓝牙设备
		waitDialog.show();
	}

	@Override
	public void onMessageChange(int msg) {
		switch (msg) {
		case DeviceOperatorData.CONNECTCANCEL://取消连接
			if(isConnect){

			}else{
				bluetoothOperator.quitBluetooth();
				connectTxt.setEnabled(true);
				waitDialog.closeDialog();
			}
			break;
		case 1:
			finish();//退出 
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.bt_conn:
			connectTxt.setEnabled(true);

			Log.e("Dev","bt type ");
			CommService.type = 1;//蓝牙连接方式

			Log.e("Dev", "CommService.type =" + CommService.type );
			break;
		case R.id.hid_conn:
			connectTxt.setEnabled(true);

			Log.e("Dev","hid type ");
			CommService.type = 2;//HID连接连接方式

			Log.e("Dev", "CommService.type =" + CommService.type );
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//		bluetoothOperator.quitBluetooth();
		readWaitTime.stopRun();
	}

	private class ReadWaitTime implements Runnable{
		private Handler handler;
		private int time = 30;
		private final static int delayTime = 1000;
		private boolean isStop = false;
		public ReadWaitTime()
		{
			time =60;
			handler = new Handler();
		}
		public void run() {
			if(isStop)return;
			handler.postDelayed(this, delayTime);

			time--;
			if(time==0){
				waitDialog.closeDialog();
				handler.removeCallbacks(this);
				isStop = false;
			}
		}

		public void startRun(int time)
		{
			isStop = false;
			this.time = time;
			handler.postDelayed(this, delayTime);
		}

		public void stopRun()
		{
			isStop = true;
			handler.removeCallbacks(this);
		}
	}
}
