package com.centerm.blecentral.blelibrary.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import com.centerm.blecentral.blelibrary.utils.MsgReceiver;
import com.centerm.blecentral.blelibrary.utils.MsgSender;
import com.centerm.device.DeviceIntf;


/**
 * Created by jqj on 2016/5/24.
 * 对bluetoothGatt进行了封装
 */
@SuppressLint("NewApi")
public final class BLEClient implements DeviceIntf{
	private static final String TAG = BLEClient.class.getSimpleName();

	private WeakReference<Context> contextWeakReference;

	private BluetoothGattCallback gattCallback;
	private BluetoothGatt bluetoothGatt;

	private MsgReceiver msgReceiver;
	private MsgSender msgSender;

	private IBLECallback ibleCallback;

	private BluetoothGattCharacteristic writeChannel;

	private boolean connected;

	//	private MsgQueue<byte[]> msgQueue;//使用消息队列达到异步处理数据发送的问题
	private LinkedList<byte[]> msgQueue; //使用消息队列达到异步处理数据发送的问题

	private boolean onWrite;//是否正在发送数据

	private static BLEClient instance = null;

	private int timeOut = 0;

	public static BLEClient getInstance(){
		if(instance == null){
			return null;
		}
		return instance;
	}

	public BLEClient(Context mContext, IBLECallback IBLECallback) {
		instance = this;
		contextWeakReference=new WeakReference<>(mContext);
		this.ibleCallback = IBLECallback;
		connected = false;
		msgSender = new MsgSender(new MsgSender.ISender() {
			@Override
			public void inputData(byte[] bytes) {
				msgQueue.offer(Arrays.copyOf(bytes,bytes.length));
			}

			@Override
			public void start() {
				startWrite();
			}
		});
		msgReceiver = new MsgReceiver(new MsgReceiver.IReceiver() {
			@Override
			public void receiveData(byte[] data) {
				ibleCallback.onDataReceived(data);
			}
		});

		msgQueue = new LinkedList<byte[]>();
		initGattCallback();
	}

	/**
	 * 开始使用Gatt连接
	 *
	 * @param address 要连接的蓝牙设备的地址
	 * @return 是否连接成功
	 */
	public boolean startConnect(String address) {
		Context mContext=contextWeakReference.get();
		if(mContext==null){
			return false;
		}
		BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
		bluetoothGatt = bluetoothAdapter.getRemoteDevice(address).connectGatt(mContext, false, gattCallback);
		if (bluetoothGatt.connect()) {
			connected = true;
			return true;
		}
		return false;
	}


	/**
	 * 断开连接
	 */
	public void stopConnect() {
		if (connected) {
			bluetoothGatt.disconnect();
			bluetoothGatt.close();
		}
		connected = false;
	}


	/**
	 * 发送消数据
	 *
	 * @param data 要发送的数据
	 */
	public boolean sendData(byte[] data) {
		if (connected) {
			msgSender.sendMessage(data);
			return true;
		}
		return false;
	}


	private void initGattCallback() {
		gattCallback = new BluetoothGattCallback() {
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				super.onConnectionStateChange(gatt, status, newState);
				Log.i(TAG, "onConnectionStateChange");
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					Log.i(TAG, "Client success & start discover services");
					bluetoothGatt.discoverServices();
					connected = true;
					ibleCallback.onConnected();
				}
				if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					Log.e(TAG, "connect closed");
					connected = false;
					ibleCallback.onDisconnected();
				}
			}

			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				super.onServicesDiscovered(gatt, status);
				Log.i(TAG, "onServicesDiscovered");

				if (status != BluetoothGatt.GATT_SUCCESS) {
					Log.e(TAG, "Discover Services failed");
					return;
				}
				//				bluetoothGatt.requestMtu(50);
				BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(BLEProfile.UUID_SERVICE));
				if (service != null) {
					BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(BLEProfile.UUID_CHARACTERISTIC));
					if (characteristic != null) {
						//订阅通知，这段代码对iOS的peripheral也能订阅
						bluetoothGatt.setCharacteristicNotification(characteristic, true);
					} else {
						Log.e(TAG, "The notify characteristic is null");
					}
					writeChannel = service.getCharacteristic(UUID.fromString(BLEProfile.UUID_CHARACTERISTIC));
					if (characteristic == null) {
						Log.e(TAG, "The write characteristic is null");
					}
				} else {
					Log.e(TAG, "The special service is null");
				}
			}

			@Override
			public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
				super.onCharacteristicRead(gatt, characteristic, status);
				Log.i(TAG, "onCharacteristicRead");
				if (status != BluetoothGatt.GATT_SUCCESS) {
					Log.e(TAG, "Read Characteristic failed");
				}
			}

			@Override
			public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
				super.onCharacteristicWrite(gatt, characteristic, status);
				nextWrite();
				Log.i(TAG, "onCharacteristicWrite");
			}

			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
				//处理notify
				super.onCharacteristicChanged(gatt, characteristic);
				byte[] value = characteristic.getValue();
				msgReceiver.outputData(value);//将数据整合成String
			}

			@Override
			public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
				super.onDescriptorRead(gatt, descriptor, status);
				Log.i(TAG, "onDescriptorRead");
			}

			@Override
			public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
				super.onDescriptorWrite(gatt, descriptor, status);
				Log.i(TAG, "onDescriptorWrite");
			}

			@Override
			public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
				super.onReliableWriteCompleted(gatt, status);
				Log.i(TAG, "onReliableWriteCompleted");
			}

			@Override
			public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
				super.onReadRemoteRssi(gatt, rssi, status);
				Log.i(TAG, "onReadRemoteRssi");
			}

			@Override
			public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
				super.onMtuChanged(gatt, mtu, status);
				Log.i(TAG, "onMtuChanged");
			}
		};
	}

	/*
	 * 控制稳定write区域
	 */
	private void startWrite() {
		if (onWrite) {
			return;
		}
		nextWrite();
	}

	/*
	 * 控制稳定write区域
	 */
	private void nextWrite() {
		if (msgQueue.isEmpty()) {
			onWrite = false;
			return;
		}
		write();
	}

	/*
	 * 控制稳定write区域
	 */
	private void write() {
		onWrite = true;
		byte[] bytes = msgQueue.poll();
		try {
			writeChannel.setValue(bytes);
			bluetoothGatt.writeCharacteristic(writeChannel);
		} catch (NullPointerException e) {
			Log.e(TAG, "null pointer on characteristic");
		}
	}

	@Override
	public boolean openDevice(Context context, String addr) {
		return false;
	}

	@Override
	public boolean isConnect() {
		return connected;
	}

	@Override
	public boolean connectDevice() {
		return false;
	}

	@Override
	public int writeData(byte[] data, int len) {
		boolean isSend = sendData(data);
		return isSend?len:0;
	}

	@Override
	public int writeData(byte data) {
		return 0;
	}

	@Override
	public int readData(byte[] buffer, int timeOut) {
		//循环等待获取数据,确保数据收完
		//		int time = timeOut;
		//		while(true){
		//			if(mBuffer != null){
		//				break;
		//			}
		//
		//			try {
		//				Thread.sleep(10);
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
		//
		//			time -= 10;
		//			Log.e("BLE", "time = in while: "+ time);
		//			if(time < 0){
		//				return -2; //超时
		//			}
		//		}
		//		buffer = mBuffer;
		//		mBuffer = null;
		//		return buffer.length;

		return 0;
	}

	@Override
	public int getState() {
		return 0;
	}

	@Override
	public void quitRead() {

	}

	@Override
	public void setObject(Object object) {

	}

	@Override
	public void closeDevice() {

	}
}
