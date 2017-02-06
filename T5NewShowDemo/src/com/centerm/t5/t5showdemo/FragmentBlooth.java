package com.centerm.t5.t5showdemo;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.centerm.blecentral.ConnectType;
import com.centerm.blecentral.blelibrary.bluetooth.BLEClient;
import com.centerm.blecentral.blelibrary.bluetooth.IBLECallback;
import com.centerm.blecentral.blelibrary.bluetooth.LEScanner;
import com.centerm.blecentral.blelibrary.utils.ScanData;
import com.centerm.blecentral.blelibrary.utils.ScanListAdapter;
import com.centerm.device.CommService;
import com.centerm.t5.newdispatch.t5showdemo.R;
import com.centerm.t5.util.bluetooth.BluetoothOperator;
import com.centerm.t5.util.dev.DeviceOperatorData;
import com.example.commenlibary.Interface.CommenLibaryMessageOperator;
import com.example.commenlibary.ui.ListDialog.SelectListDialog;

public class FragmentBlooth extends FragmentBase implements View.OnClickListener,OnCheckedChangeListener,
CommenLibaryMessageOperator{

	private Button btnConnect;
	private TextView tvState;
	private EditText etMac;

	private RadioGroup rgConn;
	private String connectSuccess = "连接成功";
	private String connectFail = "连接失败";

	private String connectCancel = "连接断开";
	private String noCheck = "未检测";
	static String currentState = "未检测";

	private final static  String macAddr ="macAddr"; 
	private  ArrayList<String> bluetoothList =null;
	private ArrayList<String> macList = null;
	private SelectListDialog selectListDialog;
	BluetoothReceiver bluetoothReceiver;
	private int selectIndex = 0;
	private Context mContext;
	private static final String TAG = FragmentBlooth.class.getSimpleName();

	private Button btnMore;
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState, R.layout.frag_communication_conn);
		tvState 	= (TextView)view.findViewById(R.id.tvBTStat);
		btnConnect 	= (Button)view.findViewById(R.id.bt_conn_open);
		etMac		= (EditText)view.findViewById(R.id.etMac);
		rgConn		= (RadioGroup)view.findViewById(R.id.rg_conn);
		btnMore 	= (Button)view.findViewById(R.id.btnMore);
		btnMore.setOnClickListener(this);
		rgConn.setOnCheckedChangeListener(this);
		btnConnect.setOnClickListener(this);
		bluetoothList = new ArrayList<String>();
		macList		  = new ArrayList<String>();
		selectListDialog = new SelectListDialog(mainActivity, 300, bluetoothList);
		selectListDialog.setCommenLibaryMessageOperator(this);
		CommService.type = 1;

		mContext = this.getActivity();

		tvState.setText(currentState);
		btnConnect.setEnabled(!isConnect);

		SharedPreferences Pre = mainActivity.getSharedPreferences(macAddr, Context.MODE_PRIVATE);
		String name = Pre.getString("name", getString(R.string.macAddr));
		String address = Pre.getString("mac",  getString(R.string.macAddr));
		etMac.setText(name);
		mainActivity.setMacAddr(name);
		macList.add(address);
		selectListDialog.addSelectObject(name);

		bluetoothReceiver = new BluetoothReceiver(mainActivity);
		bluetoothReceiver.registBroadcast();

		initBle(view);

		return view;
	}

	/**
	 * 对于设备连接状态的处理
	 * */
	public void setConnectState(int state)
	{
		switch (state) {
		case DeviceOperatorData.CONNECTSUCCESS://连接成功
			Editor editor = mainActivity.getSharedPreferences(macAddr, Context.MODE_PRIVATE).edit();
			editor.putString("mac", macList.get(selectIndex));
			editor.putString("name", etMac.getText().toString());
			editor.commit();
			tvState.setText(connectSuccess);
			btnConnect.setEnabled(false);
			isConnect = true;
			currentState = connectSuccess;
			break;
		case DeviceOperatorData.CONNECTFAIL://连接失败
			tvState.setText(connectFail);
			btnConnect.setEnabled(true);
			isConnect = false;
			currentState = connectFail;
			break;
		case DeviceOperatorData.CONNECTCANCEL://取消连接
			tvState.setText(connectCancel);
			btnConnect.setEnabled(true);
			isConnect = false;
			currentState = connectCancel; 
			break;
		case DeviceOperatorData.MESSAGE:
			break;
		case DeviceOperatorData.NODEVICE:
			break;
		default:
			tvState.setText(noCheck);
			btnConnect.setEnabled(true);
			isConnect = false;
			currentState = noCheck; 
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_conn_open:
			btnConnect.setEnabled(false);
			mainActivity.connectBluetooth( null );
			//mainActivity.connectBluetooth(etMac.getText().toString());//连接蓝牙 
			break;
		case R.id.btnMore:
			selectListDialog.setWidgetWidth(etMac.getWidth());
			selectListDialog.ShowDialog(etMac);
			break;
		case R.id.ble_find: //ble搜索
			startScan();
			break;
		case R.id.ble_lianjie: //ble连接
			String mac = mac_value.getText().toString().trim();
			if(mac.equals("") || mac.equals("55:66:99:88:33:22")){
				Toast.makeText(mContext, "请选择蓝牙！", Toast.LENGTH_SHORT).show();
				return;
			}
			leScanner.stopScan();
			mainActivity.startConnect(mac_value.getText().toString());
			bluetoothOperator.checkBleConnect();
			ble_lianjie.setEnabled(false);
			break;
		case R.id.ble_stop: //ble断开
			mainActivity.stopConnect();
			break;
		default:
			break;
		}
	}

	private void startScan() {
		scanList.clear();
		nHandler.sendEmptyMessage(MyHandler.REFRESH_SCAN_LIST);
		leScanner.startScan();
		connectType = ConnectType.CENTRAL;
		nHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				leScanner.stopScan();
			}
		}, SCAN_DURATION);
	}

	/**
	 * 激活连接按钮 
	 */
	public void setConnect(boolean is)
	{
		if(btnConnect!=null){
			btnConnect.setEnabled(is);
		}
	}

	@Override
	public void setData(Object object) {

	}

	/**
	 *连接方式选择按钮
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.bt_conn:
			btnConnect.setEnabled(true);
			btnConnect.setVisibility(View.VISIBLE);
			secondBlock.setVisibility(View.GONE);

			Log.e("Dev","bt type ");
			Log.e("Dev", "CommService.type =" + CommService.type );
			CommService.type = 1;//蓝牙连接方式
			break;
		case R.id.ble_conn:
			btnConnect.setVisibility(View.GONE);
			secondBlock.setVisibility(View.VISIBLE);

			Log.e("Dev","ble type ");
			Log.e("Dev", "CommService.type =" + CommService.type );
			CommService.type = 3;//BLE连接方式
			break;
		case R.id.hid_conn:
			btnConnect.setEnabled(true);
			btnConnect.setVisibility(View.VISIBLE);
			secondBlock.setVisibility(View.GONE);

			Log.e("Dev","hid type ");
			Log.e("Dev", "CommService.type =" + CommService.type );
			CommService.type = 2;//HID连接连接方式
			break;
		default:
			break;
		}
	}

	public class BluetoothReceiver extends BroadcastReceiver{
		private Context context;
		public BluetoothReceiver(Context context)
		{
			this.context = context;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();        
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {   
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
				// If it's already paired, skip it, because it's been listed already           
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {                
					//	 mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());  
					//Log.e("tag","name is "+device.getName()+" addresss is "+device.getAddress());
					if(!macList.contains(device.getAddress())){
						macList.add(device.getAddress());
						selectListDialog.addSelectObject(device.getName());
					}

				}else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  

				}       
			}
		}

		public void registBroadcast()
		{
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			context.registerReceiver(this, filter);
			// Register for broadcasts when discovery has finished
			filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); 
			context.registerReceiver(this, filter); 
			filter.setPriority(Integer.MAX_VALUE); 
		}

		public void unregistBroadcast()
		{
			context.unregisterReceiver(this);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		bluetoothReceiver.unregistBroadcast();
	}


	@Override
	public void operatorMessage(int msg) {
		selectIndex = msg;
		etMac.setText(bluetoothList.get(msg));
		selectListDialog.CloseDialog();
	}

	/***************************************BLE*******************************************/
	private Button btnStartScan;
	private Button btnSendMsg;
	private Button btnCheckAdvertise;
	private Button btnStop;
	private EditText etMsg;
	private ListView listChat;

	private BluetoothOperator bluetoothOperator = BluetoothOperator.getInstance();

	private Button ble_find, ble_lianjie, ble_stop;
	private TextView mac_value;
	private ListView scan_listView;
	public static final int SCAN_DURATION = 10000;//扫描时长

	private LEScanner leScanner;
	private List<ScanData> scanList;
	private BaseAdapter chatListAdapter;
	private BaseAdapter scanListAdapter;
	private ConnectType connectType;
	private MyHandler nHandler;

	private LinearLayout secondBlock;

	private void initBle(View view){
		secondBlock = (LinearLayout)view.findViewById(R.id.secondBlock);
		ble_find = (Button) view.findViewById(R.id.ble_find);
		ble_find.setOnClickListener(this);
		ble_lianjie = (Button) view.findViewById(R.id.ble_lianjie);
		ble_lianjie.setOnClickListener(this);
		ble_stop = (Button) view.findViewById(R.id.ble_stop);
		ble_stop.setOnClickListener(this);

		mac_value = (TextView) view.findViewById(R.id.mac_value);
		scan_listView = (ListView) view.findViewById(R.id.list_scan_result);

		scanList = new ArrayList<ScanData>();
		scanListAdapter = new ScanListAdapter(scanList, mContext);
		nHandler = new MyHandler();
		scan_listView.setAdapter(scanListAdapter);
		leScanner = LEScanner.getInstance(mContext, new LEScanner.IScanResultListener() {
			@Override
			public void onResultReceived(String deviceName, String deviceAddress) {
				for(ScanData data : scanList){
					if(deviceAddress.equals(data.getAddress())){
						return;
					}
				}
				Log.i(TAG, "deviceName, deviceAddress:" + deviceName+","+deviceAddress);
				scanList.add(new ScanData(deviceName, deviceAddress));
				nHandler.sendEmptyMessage(MyHandler.REFRESH_SCAN_LIST);
			}

			@Override
			public void onScanFailed(int errorCode) {
				Log.e(TAG, "scan failed");
			}
		});

		scan_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mac_value.setText(scanList.get(position).getAddress());
				//				bleClient.startConnect(scanList.get(position).getAddress());
				leScanner.stopScan();
			}
		});

		nHandler.attach(chatListAdapter, scanListAdapter);
	}

	//Handler来刷新UI
	private class MyHandler extends Handler {
		//可以使用runOnUiThread或者持有View使用View.post来简化代码
		public static final int REFRESH_SCAN_LIST = 250;
		public static final int REFRESH_CHAT_LIST = 38;

		private BaseAdapter chatAdapter;
		private BaseAdapter scanAdapter;

		public void attach(BaseAdapter chatAdapter, BaseAdapter scanAdapter) {
			this.chatAdapter = chatAdapter;
			this.scanAdapter = scanAdapter;
		}

		public void detach() {
			chatAdapter = null;
			scanAdapter = null;
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == REFRESH_CHAT_LIST) {
				if (chatAdapter != null) {
					chatAdapter.notifyDataSetChanged();
				}
			} else if (msg.what == REFRESH_SCAN_LIST) {
				if (scanAdapter != null) {
					scanAdapter.notifyDataSetChanged();
				}
			} else if(msg.what == 1){
				tvState.setText(connectSuccess);
				currentState = connectSuccess;
			} else if(msg.what == 2){
				tvState.setText(connectFail);
				currentState = connectFail;
			}
		}
	}
}
