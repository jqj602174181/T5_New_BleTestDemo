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
	private boolean isConnect = false;//�����ж��Ƿ�������
	private String readWait;
	private FingerData fingerData;
	private ReadWaitTime readWaitTime;

	private int style = DeviceOperatorData.FINGER;//ָ����ģ��

	private String connectSuccess = "���ӳɹ�";
	private String currentState = "δ����";
	private String connectFail = "����ʧ��";
	private String connectCancel = "���ӶϿ�";
	private String noCheck = "δ���";

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
				setConnectState(msg.what);//�ж�����״̬
				Log.e("LoginActivity","setHandler: msg.what = "+ msg.what );
				switch (msg.what) {
				case DeviceOperatorData.CONNECTFAIL://�豸����ʧ��
					closeWaitDialog();
					isConnect = false;
					break;
				case DeviceOperatorData.CONNECTSUCCESS://�豸���ӳɹ�
					isConnect= true;
					waitDialog.setText(readWait);
					closeWaitDialog();
					break;
				case DeviceOperatorData.NODEVICE://û���豸
					isConnect = false;
					closeWaitDialog();
					CommonUtil.showTip(LoginActivity.this, LoginActivity.this.getString(R.string.checkBluetooth));
					break;
				case DeviceOperatorData.CONNECTCANCEL://�豸����ʧ��
					isConnect = false;
					closeWaitDialog();
					break;
				case DeviceOperatorData.MESSAGE://��ȡ��Ϣ
					//					currentFragment.setData(msg.obj);//����ȡ����Ϣ��ʾ��UI����
					Intent mIntent = new Intent();
					mIntent.setClass(LoginActivity.this, BusinessActivity.class);
					LoginActivity.this.startActivity(mIntent);

					Toast.makeText(LoginActivity.this, "��¼�ɹ�", Toast.LENGTH_SHORT).show();

					waitDialog.closeDialog();
					readWaitTime.stopRun();

					finish();
					break;
				case DeviceOperatorData.READ_FAIL://��ȡʧ��
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
		case DeviceOperatorData.CONNECTSUCCESS://���ӳɹ�
			tvState.setText(connectSuccess);
			connFlag.setBackgroundResource(R.drawable.conn_flag);
			connectTxt.setEnabled(false);
			isConnect = true;
			currentState = connectSuccess;
			break;
		case DeviceOperatorData.CONNECTFAIL://����ʧ��
			tvState.setText(connectFail);
			connFlag.setBackgroundResource(R.drawable.not_conn_flag);
			connectTxt.setEnabled(true);
			isConnect = false;
			currentState = connectFail;
			break;
		case DeviceOperatorData.CONNECTCANCEL://ȡ������
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
				Toast.makeText(LoginActivity.this, "���������豸��", Toast.LENGTH_SHORT).show();
				return;
			}
			String account = accountEdit.getEditableText().toString();
			String password = passwordEdit.getEditableText().toString();
			if(TextUtils.isEmpty(account)){
				Toast.makeText(LoginActivity.this, "�ʺŲ���Ϊ�գ�", Toast.LENGTH_SHORT).show();
				return;
			}
			if(TextUtils.isEmpty(password)){
				Toast.makeText(LoginActivity.this, "���벻��Ϊ�գ�", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!account.equals(ACCOUNT)){
				Toast.makeText(LoginActivity.this, "�ʺŲ��ԣ�", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!password.equals(PASSWORD)){
				Toast.makeText(LoginActivity.this, "���벻�ԣ�", Toast.LENGTH_SHORT).show();
				return;
			}

			//���ö�ȡָ��������
			fingerData.style = 1; //ָ������
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

		if(!bluetoothOperator.getIsConnect()){//���豸û�����ӳɹ���������������ִ��
			Toast.makeText(LoginActivity.this, "�ѶϿ�����������", Toast.LENGTH_SHORT).show();
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
			bluetoothOperator.sendMessage(0, style, object);// ģ������� style�����ж������ֽ���ģ�������
		}
	}

	/**
	 * �豸����
	 * */
	void connectBluetooth(String mac)
	{
		isPressConnect = true; 
		if(waitDialog == null){
			waitDialog = new WaitDialog(this, false, this);
		}
		bluetoothOperator.setBluetoothMac(mac);//����������ַ
		bluetoothOperator.startBluetooth();//���������豸
		waitDialog.show();
	}

	@Override
	public void onMessageChange(int msg) {
		switch (msg) {
		case DeviceOperatorData.CONNECTCANCEL://ȡ������
			if(isConnect){

			}else{
				bluetoothOperator.quitBluetooth();
				connectTxt.setEnabled(true);
				waitDialog.closeDialog();
			}
			break;
		case 1:
			finish();//�˳� 
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
			CommService.type = 1;//�������ӷ�ʽ

			Log.e("Dev", "CommService.type =" + CommService.type );
			break;
		case R.id.hid_conn:
			connectTxt.setEnabled(true);

			Log.e("Dev","hid type ");
			CommService.type = 2;//HID�������ӷ�ʽ

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
