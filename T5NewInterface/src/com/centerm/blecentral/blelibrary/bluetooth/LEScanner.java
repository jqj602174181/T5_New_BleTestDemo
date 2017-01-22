package com.centerm.blecentral.blelibrary.bluetooth;

import java.lang.ref.WeakReference;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

public class LEScanner {
	private static final String TAG = LEScanner.class.getSimpleName();
	private static LEScanner instance;

	private WeakReference<Context> contextWeakReference;
	private IScanResultListener scanResultListener;
	private BluetoothAdapter bluetoothAdapter;

	/**
	 * ����ģʽ
	 *
	 * @param context  ����context������
	 * @param listener ɨ������listener
	 * @return BLEScanner��ʵ��
	 */
	public static LEScanner getInstance(Context context, IScanResultListener listener) {
		if (instance == null) {
			instance = new LEScanner(context);
		} else {
			instance.contextWeakReference = new WeakReference<Context>(context);
		}
		instance.scanResultListener = listener;
		return instance;
	}

	public void stopScan() {
		if (bluetoothAdapter == null) {
			return;
		}
		bluetoothAdapter.stopLeScan(mLeScanCallback);
	}

	private LEScanner(Context context) {
		contextWeakReference = new WeakReference<>(context);
	}

	/**
	 * ��ʼɨ����Χ���豸
	 *
	 * @return ��ʼɨ��ɹ�����true, ���򷵻�false
	 */
	public boolean startScan() {
		Context context = contextWeakReference.get();
		if (context == null) {
			return false;
		}

		bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
		if (bluetoothAdapter == null) {
			Log.e(TAG, "bluetoothAdapter is null");
			return false;
		}

		bluetoothAdapter.startLeScan(mLeScanCallback);
		return true;
	}

	/**
	 * ����ɨ��ص����� ʵ��ɨ�������豸���ص�����BluetoothDevice�����Ի�ȡname MAC����Ϣ
	 * 
	 * **/
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord)
		{
			String name = device.getName();
			String address = device.getAddress();
			scanResultListener.onResultReceived(name, address);
		}
	};

	public interface IScanResultListener {
		/**
		 * ����������ڳɹ����յ�ɨ����ʱ����
		 *
		 * @param deviceName    �豸����
		 * @param deviceAddress �豸��ַ
		 */
		void onResultReceived(String deviceName, String deviceAddress);

		/**
		 * �����������ɨ��ʧ��ʱ���ã�
		 *
		 * @param errorCode �����ScanCallback���API
		 */
		void onScanFailed(int errorCode);
	}
}
