package com.centerm.t5.t5showdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.centerm.t5.socketclient.PinYin;
import com.centerm.t5.socketserver.SocketServer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class MyApp extends Application {

	private static MyApp instance;
	public static final String ACTION = "com.centerm.t5.push";
	MyPushReceiver receiver;
	MyConnection connection;

	public ArrayList<MyPushActivity> list = new ArrayList<MyPushActivity>();

	public static MyApp getInstance(){
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		instance = this;

		//拼音 初始化
		new Thread(){
			public void run() {
				PinYin.getPinYin( "中" );
			};
		}.start();

		//启动服务端
		//		connection = new MyConnection();
		//		connection.connected();
		//
		//		receiver = new MyPushReceiver();
		//		IntentFilter filter = new IntentFilter(ACTION);
		//		registerReceiver(receiver, filter);
		//		SocketServer.getInstance().setHandler(handler);
		//		SocketServer.getInstance().start();
	}

	private class MyConnection implements ServiceConnection{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.e(MyApp.class.getSimpleName(), "bind socketserver");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		private void connected(){
			Intent intent = new Intent();
			intent.setClass(MyApp.this, SocketServer.class);
			MyApp.this.bindService(intent, this, Context.BIND_AUTO_CREATE);
		}
	}

	private class MyPushReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(ACTION)){
				for(MyPushActivity activity : list){ //确保只有一个推送界面
					activity.finish();
				}

				String portraiPath = intent.getStringExtra("portraiPath");
				String filePath = intent.getStringExtra("filePath");
				String userName = intent.getStringExtra("userName");
				Intent mIntent = new Intent(MyApp.this, MyPushActivity.class);
				mIntent.putExtra("portraiPath", portraiPath); //头像路径
				mIntent.putExtra("filePath", filePath); //文件路径
				mIntent.putExtra("userName", userName); //客户名字
				mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MyApp.this.startActivity(mIntent);
			}
		}
	}

	public ComponentName getComponentName(){
		ComponentName component = null;
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
		RunningTaskInfo rti = runningTasks.get(0);
		component = rti.topActivity;
		return component;
	}

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			for(MyPushActivity activity : list){ //确保只有一个推送界面
				activity.finish();
			}

			HashMap<String, String> map = (HashMap<String, String>)msg.obj;
			Intent intent = new Intent(MyApp.this, MyPushActivity.class);
			intent.putExtra("path", map.get("path")); //图片路径
			intent.putExtra("content", map.get("content")); //文字信息
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MyApp.this.startActivity(intent);
		}
	};

}
