package com.centerm.t5.t5showdemo;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.centerm.device.CommService;
import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.centerm.t5.socketclient.JsonUtil;
import com.centerm.t5.socketclient.MessageComm;
import com.centerm.t5.socketclient.PinYin;
import com.centerm.t5.t5showdemo.common.OnMessageListener;
import com.centerm.t5.t5showdemo.ui.WaitDialog;
import com.centerm.t5.util.bluetooth.BluetoothOperator;
import com.centerm.t5.util.common.CommonUtil;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.centerm.util.RetUtil;
import com.centerm.util.financial.FingerData;
import com.centerm.util.financial.ICCardData;
import com.centerm.util.financial.IDCardData;
import com.centerm.util.financial.MsgCardData;
import com.centerm.util.financial.PinData;
import com.centerm.util.financial.SignData;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.util.Log;
import android.view.View;

public class OpenCardActivity extends Activity implements View.OnClickListener, OnMessageListener,OnCheckedChangeListener{

	private ReadWaitTime readWaitTime;
	private BluetoothOperator bluetoothOperator = BluetoothOperator.getInstance();
	private WaitDialog waitDialog;
	private Handler mainHandler;

	private HashMap<String, String> errorMap = new HashMap<String, String>();
	public final static String Unknown_Err = "DRV0000001";
	public final static String Timeout_Err = "DRV0000002";
	public final static String Open_Serial_Err = "DRV0000003";	
	public final static String Send_Mess_Err = "DRV0000004";

	public final static String Param_Err = "DRV0000005";
	public final static String Not_Find_So_Err = "DRV0000006";
	public final static String Load_So_Err = "DRV0000007";
	public final static String Device_Not_Connect = "DRV0000008";
	public final static String Recv_Error_Mess = "DRV0000009";
	public final static String Device_Connect_Broken = "DRV0000010";
	public final static String ARQC_ERROR = "DRV0000204";
	public final static String ShangDian_ERROR = "DRV0000101";
	public final static String ARQC_ERROR_Msg = "��ȡARQCʧ��";
	public final static String ShangDian_ERROR_Msg = "�ϵ�ʧ��";

	public final static String Unknown_Err_Msg = "δ֪����";
	public final static String Timeout_Err_Msg = "��ʱ����";
	public final static String Open_Serial_Err_Msg = "�򿪴���ʧ��";
	public final static String Send_Mess_Err_Msg = "���ͻ���ձ���ʧ��";

	public final static String Param_Err_Msg = "�ӿڲ�������";
	public final static String Not_Find_So_Err_Msg = "�Ҳ�����̬���ӿ�";
	public final static String Load_So_Err_Msg = "��̬���ӿ���ش���";
	public final static String Device_Not_Connect_Msg = "ͨѶ����û�н���";
	public final static String Recv_Error_Mess_Msg = "���յı��ĸ�ʽ����";
	public final static String Device_Connect_Broken_Msg = "ͨѶ�����ƻ�";

	private String readWait;

	private Button readIdBtn;
	private int type = -1;
	private Bitmap headBitmap;
	private TextView tv_person_info;
	private ImageView iv_photo; //���֤

	private Button readIcBtn;
	private EditText editIc; //ic��

	private Button readMsgBtn;
	private EditText editMsg; //�ſ�

	private Button passwordBtn;
	private Button sign;
	private EditText editPassword; //�������

	private RadioGroup rg;
	private Button commitBtn;

	//	private Button fingerBtn;
	//	private EditText editFinger; //ָ����

	private IDCardData idCardData;
	private MsgCardData msgCardData;
	private PinData pinData;
	private FingerData fingerData;
	private ICCardData icCardData;
	private SignData signData;

	private boolean isHaveIDInfo = false;

	private int iIcFlag = 1;
	public static final int IC_INFO		= 1; 
	public static final int IC_ARQC		= 2; 

	TreeMap<String, String> map = new TreeMap<String, String>();

	private final String str = "������δ֪����ƴ����δ֪\n�Ա�δ֪�������壺δ֪�����������ڣ�δ֪\n��֤���أ�δ֪����\n֤�������գ�δ֪\n���֤�ţ�δ֪\n��ַ��δ֪";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_opencard);

		readWait = getString(R.string.readWait);
		idCardData = new IDCardData();
		msgCardData = new MsgCardData();
		pinData = new PinData();
		fingerData = new FingerData();
		icCardData = new ICCardData();
		signData = new SignData();

		setHandler();
		bluetoothOperator.setHandler(mainHandler);

		initView();
		iIcFlag = 1;

		readWaitTime = new ReadWaitTime();

		errorMap.put(Unknown_Err, Unknown_Err_Msg);
		errorMap.put(Timeout_Err, Timeout_Err_Msg);
		errorMap.put(Open_Serial_Err, Open_Serial_Err_Msg);
		errorMap.put(Send_Mess_Err, Send_Mess_Err_Msg);
		errorMap.put(Param_Err, Param_Err_Msg);
		errorMap.put(Not_Find_So_Err, Not_Find_So_Err_Msg);
		errorMap.put(Load_So_Err, Load_So_Err_Msg);
		errorMap.put(Device_Not_Connect, Device_Not_Connect_Msg);
		errorMap.put(Open_Serial_Err_Msg, Recv_Error_Mess_Msg);
		errorMap.put(Device_Connect_Broken, Device_Connect_Broken_Msg);
		errorMap.put(Recv_Error_Mess, Recv_Error_Mess_Msg);
		errorMap.put(ARQC_ERROR, ARQC_ERROR_Msg);
		errorMap.put(ShangDian_ERROR, ShangDian_ERROR_Msg);
	}

	private void initView() {
		readIdBtn = (Button)findViewById(R.id.BTN_READIDCARD);
		tv_person_info = (TextView)findViewById(R.id.tv_person_info);
		iv_photo = (ImageView)findViewById(R.id.iv_photo);
		readIcBtn = (Button)findViewById(R.id.BTN_ReadICCard);
		editIc = (EditText)findViewById(R.id.EDIT_IC);
		readMsgBtn = (Button)findViewById(R.id.BTN_ReadMsg);
		editMsg = (EditText)findViewById(R.id.EDIT_Msg);
		passwordBtn = (Button)findViewById(R.id.BTN_ReadPassword);
		editPassword = (EditText)findViewById(R.id.EDIT_Password);
		rg = (RadioGroup)findViewById(R.id.RG_CARDPASS);
		commitBtn = (Button)findViewById(R.id.BTN_Commit);
		sign = (Button)findViewById(R.id.BTN_Sign);
		//		fingerBtn = (Button)findViewById(R.id.BTN_ReadFinger);
		//		editFinger = (EditText)findViewById(R.id.EDIT_Finger);

		readIdBtn.setOnClickListener(this);
		readIcBtn.setOnClickListener(this);
		readMsgBtn.setOnClickListener(this);
		passwordBtn.setOnClickListener(this);
		commitBtn.setOnClickListener(this);
		rg.setOnCheckedChangeListener(this);
		sign.setOnClickListener(this);
		//		fingerBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.BTN_READIDCARD: //�����֤
			tv_person_info.setText(str);
			iv_photo.setImageBitmap(null);

			idCardData.style = 2;
			idCardData.timeOut = "15";
			type = DeviceOperatorData.JRZIDCARD;
			sendMessage(DeviceOperatorData.JRZIDCARD, idCardData, 15);
			break;
		case R.id.BTN_ReadMsg: //��ȡ�ſ�
			editMsg.setText("");
			msgCardData.timeOut = "15";
			type = DeviceOperatorData.JRZMAGCARD;
			sendMessage(DeviceOperatorData.JRZMAGCARD, msgCardData, 15);
			break;
		case R.id.BTN_ReadPassword: //�������
			editPassword.setText("");
			pinData.timeOut = "15";	
			pinData.style 		= 1;
			pinData.iEncryType 	= 1; //������
			pinData.iTimes		= 1; //һ��
			pinData.iLength 	= 0; //���볤��
			type = DeviceOperatorData.KEYPAD;
			sendMessage(DeviceOperatorData.KEYPAD, pinData, 15);
			break;
		case R.id.BTN_ReadICCard: //��IC��
			editIc.setText("");
			if(iIcFlag == 1){
				icCardData.timeOut = "15";
				icCardData.cardStyle = iIcFlag;
				icCardData.style = IC_INFO;
				icCardData.tag = "A";
				type =  DeviceOperatorData.JRZICCARD;
				sendMessage(DeviceOperatorData.JRZICCARD, icCardData, 15);
			}else if(iIcFlag == 2){
				icCardData.timeOut = "15";
				icCardData.cardStyle = iIcFlag;
				icCardData.style = IC_INFO;
				icCardData.tag = "A";
				type =  DeviceOperatorData.JRZICCARD;
				sendMessage(DeviceOperatorData.JRZICCARD, icCardData, 15);
			}
			break;
		case R.id.BTN_ReadFinger: //��ָ��������
			fingerData.style = 1; 
			type = DeviceOperatorData.FINGER;
			sendMessage(DeviceOperatorData.FINGER, fingerData, 15);
			break;
		case R.id.BTN_Sign: //����ǩ��
			signData.style = 1;//������дǩ�����÷�ʽ
			signData.timeOut = "60";
			type =  DeviceOperatorData.SIGN;
			sendMessage(DeviceOperatorData.SIGN, signData, 60);
			break;
		case R.id.BTN_Commit:
			//����֤Ϊ��
			if(!isHaveIDInfo){
				Toast.makeText(OpenCardActivity.this, "��ˢ����֤", Toast.LENGTH_SHORT).show();
				return;
			}
			//IC��Ϊ��
			if(editIc.getText().toString().equals("")){
				Toast.makeText(OpenCardActivity.this, "������IC��", Toast.LENGTH_SHORT).show();
				return;
			}
			//�ſ�Ϊ��
			if(editMsg.getText().toString().equals("")){
				Toast.makeText(OpenCardActivity.this, "������ſ�", Toast.LENGTH_SHORT).show();
				return;
			}
			//�������Ϊ��
			if(editMsg.getText().toString().equals("")){
				Toast.makeText(OpenCardActivity.this, "����������", Toast.LENGTH_SHORT).show();
				return;
			}
			//�ύ
			//			commit();
			break;
		}
	}

	private void commit() {
		Thread commitThread = new Thread(){
			@Override
			public void run() {
				String icCard = editIc.getText().toString(); //ic��
				String msgCard = editMsg.getText().toString(); //�ſ�
				String password = editPassword.getText().toString(); //�������

				map.put("icCard", icCard);
				map.put("msgCard", msgCard);
				map.put("password", password);

				try{
					JSONStringer js = new JSONStringer();
					js.object().key("cardtype").value("1").key("cardno").value("350121198703020758").endObject();
					String content = js.toString();
					JSONObject objData = new JSONObject(content);
					JSONArray arrData = new JSONArray();
					arrData.put(objData);
					String data = arrData.toString();
					map.put("customernote", data);
				}catch(Exception e){
					e.printStackTrace();
				}

				String transData = JsonUtil.getSortedJsonString(map);
				Log.i("tranData:", transData);

				String ip = "192.168.2.64";
				int port = 8080;
				//�ύ����
				MessageComm comm = new MessageComm( ip, port );
				String serialNo = "pad001";
				String business = "jrz001";
				String outlets = "01";
				if( comm.setPushTransactionMsg( serialNo, business, outlets, transData)
						&& comm.commit()){//�ɹ�
					String response = comm.getResponseText();
					try{
						JSONObject obj = new JSONObject( response );
						String commitTime = obj.getString( "time" );
						String formNo = obj.getString( "code" );
					}
					catch(Exception e ){
						e.printStackTrace();
					}
				}
			}
		};
		commitThread.start();
	}

	private void setHandler()
	{
		mainHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Log.e("OpenCardActivity","setHandler: msg.what = "+ msg.what );
				switch (msg.what) {
				case DeviceOperatorData.CONNECTFAIL://�豸����ʧ��
					closeWaitDialog();
					break;
				case DeviceOperatorData.CONNECTSUCCESS://�豸���ӳɹ�
					//					waitDialog.setText(readWait);
					//					closeWaitDialog();
					break;
				case DeviceOperatorData.NODEVICE://û���豸
					closeWaitDialog();
					CommonUtil.showTip(OpenCardActivity.this, OpenCardActivity.this.getString(R.string.checkBluetooth));
					break;
				case DeviceOperatorData.CONNECTCANCEL://�豸����ʧ��
					closeWaitDialog();
					break;
				case DeviceOperatorData.MESSAGE://��ȡ��Ϣ
					setData(msg.obj);//����ȡ����Ϣ��ʾ��UI����
					waitDialog.closeDialog();
					readWaitTime.stopRun();
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

	public void setData(Object data) {
		if(type == -1){
			return;
		}
		if(type == DeviceOperatorData.JRZIDCARD){
			try{
				byte[] result = (byte[])data;
				JSONObject value = new JSONObject(new String(result));
				setInformation(value);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(type == DeviceOperatorData.JRZMAGCARD){
			try{
				String str = new String((byte[])data);
				JSONObject object = new JSONObject(str);
				String result = object.getString("data");
				String value = result.substring(0, result.indexOf("="));
				editMsg.setText(value);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(type == DeviceOperatorData.KEYPAD){
			String [] dataList = (String[])data;
			if(dataList[0].equals("0")){
				if(pinData.style==1){
					if(dataList.length==2){
						editPassword.setText(dataList[1]);
					}else if(dataList.length==3){
						StringBuilder builder = new StringBuilder();
						builder.append(dataList[1]);
						builder.append('\n');
						builder.append(dataList[2]);
						editPassword.setText(builder.toString());
					}
				}
			}
		}else if(type == DeviceOperatorData.JRZICCARD){
			try{
				String str = new String((byte[])data);
				JSONObject object = new JSONObject(str);
				String result = object.getString("data");
				editIc.setText(result);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(type == DeviceOperatorData.FINGER){
			//			String[] dataList =(String[])data;
			//			if(!dataList[0].equals("0")){//���ʧ�ܣ��򷵻ش�����Ϣ
			//				String err = null;
			//				if(dataList.length==1){
			//					err = dataList[0];
			//				}else{
			//					err = dataList[1];
			//				}
			//				if(fingerData.style==1){
			//					editFinger.setText(getErrorMsg(err));
			//					return;
			//				}
			//			}
			//			StringBuilder builder = new StringBuilder();
			//			builder.append("ָ���ǲ��̴���:");
			//			builder.append(dataList[1]);
			//			builder.append('\n');
			//			builder.append("ָ��������:");
			//			builder.append(dataList[2]);
			//			if(fingerData.style==1){
			//				editFinger.setText(builder.toString());
			//			}
		} else if(type == DeviceOperatorData.SIGN){ //ǩ��
			String[] dataList = (String[])data;
			if(!dataList[0].equals("0")){
				if(signData.style==1){
					Toast.makeText(OpenCardActivity.this, "ǩ������", Toast.LENGTH_SHORT).show();
					return;
				}
			}
			String path = dataList[1];
			Intent intent = new Intent(OpenCardActivity.this, MySignActivity.class);
			intent.putExtra("path", path);
			OpenCardActivity.this.startActivity(intent);
		}
	}

	public void closeWaitDialog()
	{
		waitDialog.closeDialog();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		readWaitTime.stopRun();
	}

	void sendMessage(int style,Object object,int time)
	{
		if(time != -1){
			readWaitTime.startRun(time+5);
		}

		final int style1 = style;
		final Object obj = object;

		if(waitDialog==null){
			waitDialog = new WaitDialog(this, false, this);
			waitDialog.setText(readWait);
		}
		waitDialog.showDialog();

		if(!bluetoothOperator.getIsConnect()){//���豸û�����ӳɹ���������������ִ��
			new Thread(){
				public void run()
				{
					bluetoothOperator.setBluetoothMac(null);
					bluetoothOperator.startBluetooth();
					connectAndSend(style1, obj);
				}
			}.start();
		}else{
			bluetoothOperator.sendMessage(0, style, object);// ģ������� style�����ж������ֽ���ģ�������
		}
	}

	private void connectAndSend(int style,Object object)
	{
		int waitTime = 10;
		//�ж�����״̬
		while(true){
			if(waitTime==0)
				break;

			if(bluetoothOperator.getIsConnect()){
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			waitTime--;

		}

		if(!bluetoothOperator.getIsConnect()){//�����ʱ��δ�����򷵻ش�����Ϣ
			String[] error= new String[2];
			error[0]="1";
			error[1]=RetUtil.Device_Not_Connect;
			Message message = mainHandler.obtainMessage(DeviceOperatorData.MESSAGE);
			message.obj = error;
			mainHandler.sendMessage(message);
			return;
		}
		bluetoothOperator.sendMessage(0,style,object);//����ģ�����
	}

	@Override
	public void onMessageChange(int msg) {

	}

	private void setInformation(JSONObject value)
	{
		try{
			if(value!=null){
				String str = "������%s����ƴ����%s\n�Ա�%s�������壺%s�����������ڣ�%s\n��֤���أ�%s����\n֤�������գ�%s\n���֤�ţ�%s\n��ַ��%s";
				String name = value.getString("name");
				String sex = value.getString("sex");
				String nation = value.getString("nation");
				String num = value.getString("num");
				String birthday = value.getString("birthday");
				String address = value.getString("address");
				String police = value.getString("issue");
				String validstart = value.getString("validstart");
				String validend = value.getString("validend");

				String pinyin = PinYin.getPinYin(name);
				str = String.format(str, name, pinyin, sex, nation, birthday, police, validstart+"-"+validend, num, address);
				tv_person_info.setText(str);

				isHaveIDInfo = true;

				String photo = value.getString("photo");
				byte[] headByte = hexStringToBytes(photo);
				Bitmap bitmap = BitmapFactory.decodeByteArray(headByte, 0, headByte.length);
				iv_photo.setImageBitmap(bitmap);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private byte[] hexStringToBytes(String orign){
		int length = orign.length()/2;
		byte[] result = new byte[length];
		for(int i=0; i<length; i++){
			result[i] = (byte) Integer.parseInt(orign.substring(i*2, i*2+2),16);
		}
		return result;
	}

	public String getErrorMsg(String key)
	{
		String error = errorMap.get(key);
		if(error!=null){
			return error;
		}else{
			return Unknown_Err_Msg;
		}
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

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.RB_Bankbook:
			iIcFlag = 1; //�Ӵ�
			break;
		case R.id.RB_MagneticCard:
			iIcFlag = 2; //�ǽӴ�
			break;
		default:
			break;
		}
	}
}
