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
	private byte[] mBuffer = null;

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
				//				ibleCallback.onDataReceived(data);
				mBuffer = Arrays.copyOf(data, data.length);
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
				bluetoothGatt.requestMtu(500);
				bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
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
		mWrite();
	}

	/*
	 * 控制稳定write区域
	 */
	private void mWrite() {
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
		//		String test = "曼珠沙华，出自法华经：本名摩诃曼陀罗华曼珠沙华，意思是，开在天界之红花。又叫做彼岸花、天涯花、舍子花，美丽而又忧伤的名字。它盛开在阴历七月，花语是“悲伤的回忆”。传说此花是接引之花，花香有魔力，能唤起死者生前的记忆。"+
		//"曼珠沙华、摩诃曼珠沙华、曼陀罗华、摩诃曼陀罗华这四者被称为天界四华，好像是开在阿迦尼咤天上。曼珠沙华意为红色团花，曼陀罗华意为白色团花。认为曼珠沙华就是红莲花，曼陀罗华就是白莲花，摩诃曼殊沙华也就是大红莲花，摩诃曼陀罗华也就是大白莲花。这些词语出现在古梵文佛经中，意指天上之花。至于为什么非要是莲花，这个原因不提也罢，总之大家凭印象也可以猜到，诸佛陀与莲花之间有着莫大的渊源，几乎每个佛陀都端坐于莲花之上，而且他们一开金口，便“天花乱缀，地涌金莲”，这个意思就是说佛陀一开口，会有很多的“花”积极地向佛陀靠拢。"+
		//"曼珠沙华开在秋彼岸期间，非常的准时，所以，又叫彼岸花。她是开在黄泉之路的花朵，在那儿大批大批的开着，远远看上去就像是血所铺成的地毯，红的似火，因而被喻为“火照之路”，曼珠沙华是这长长黄泉路上唯一的风景与色彩，灵魂便籍由着这花的指引，走向天界……"+
		//"彼岸花在花落后叶才生，花和叶是不能见到的，于是有人煽情的用它来比喻没有结果的爱情。可是佛家却说“即使爱情没有结果,彼岸仍会开出盛放的花朵。”"+
		//"曾经在网络上听到的一段基督教广播中这样说到：“一个人生活下去的意义在于，他对生活的希望，不屈和勇气；如果他因为遭遇的困难，挫折和不幸而失去了生活的希望，不屈的精神和勇气的话，等待他的就只能是痛苦，绝望和颓废。”结果并不是结束，有了希望和勇气就如彼岸依然会盛放的花朵。爱情如此，事业如此，生活如此！"+
		//"曼珠沙华，又称彼岸花、荼蘼。春分前后三天叫春彼岸，秋分前后三天叫秋彼岸。是上坟的日子。彼岸花开在秋彼岸期间，非常准时，所以才叫彼岸花。一般认为曼珠沙华是生长在三途河边的接引之花。花香传说有魔力，能唤起死者生前的记忆。她是开在黄泉之路的花朵，在那儿大批大批的开着这花，远远看上去就像是血所铺成的地毯，又因其红的似火而被喻为”火照之路” 也是这长长黄泉路上唯一的风景与色彩. 人就踏着她的指引通向幽冥之狱。"+
		//"彼岸花，花开彼岸，花开时看不到叶子，有叶子时看不到花，花叶两不相见，生生相错。因此，她在日本的花语是「悲伤的回忆」，在韩国的花语则是「相互思念」";
		//		
		//		data = test.getBytes();
		System.out.println("发送数值长度:" + data.length);
		mBuffer = null;
		boolean isSend = sendData(data);
		return isSend?len:0;
	}

	@Override
	public int writeData(byte data) {
		return 0;
	}

	@Override
	public int readData(byte[] buffer, int timeOut) {
		//		System.out.println("readData currentThread name:" + Thread.currentThread().getName());
		//		
		//		int index = 0;
		//
		//		while(true){
		//			index++;
		//			System.out.println("打印次数：" + index);
		//
		//			try {
		//				Thread.sleep(1000);
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
		//
		//			if(index >= 100){
		//				break;
		//			}
		//		}

		//循环等待获取数据,确保数据收完
		//		int time = timeOut;
		
		while(onWrite){
			
		}
		
		while(mBuffer==null){
			//			if(mBuffer != null){
			//				break;
			//			}

			//			try {
			//				Thread.sleep(100);
			//			} catch (InterruptedException e) {
			//				e.printStackTrace();
			//			}

			//			time -= 10;
			//			Log.e("BLE", "time = in while: "+ time);
			//			if(time <= 0){
			//				return -2; //超时
			//			}
		}

		int i = 0;
		for(; i<mBuffer.length; i++){ //数据拷贝
			buffer[i] = mBuffer[i];
		}

		mBuffer = null;
		return i;
	}

	@Override
	public int readData2(byte[] buffer, int timeOut) {
		if(mBuffer==null){
			return -2; //超时
		}

		int i = 0;
		for(; i<mBuffer.length; i++){ //数据拷贝
			buffer[i] = mBuffer[i];
		}

		mBuffer = null;
		return i;
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
