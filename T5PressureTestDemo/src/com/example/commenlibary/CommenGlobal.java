package com.example.commenlibary;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.widget.Toast;

public class CommenGlobal{
	public static void sendBroadcast(Context context,String action)
	{
		Intent intent = new Intent(action);
		context.sendBroadcast(intent);
		
	}
	
	/**
	 * 获取SD卡挂接位置
	 * @return
	 */
	public static String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
		if (sdCardExist){
			sdDir = Environment.getExternalStorageDirectory();//获取跟目录
			return sdDir.toString() + "/";
		}
		
		return "";
	}
	
	
	//判断wifi是否有开启
	public static boolean wifiIsOpen(Context context)
	{
		WifiManager myWifiManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
		return myWifiManager.isWifiEnabled();
		//return true;
	}
	
	public static boolean  isInternetConnect(Context context)//判断是否有网络可以连接
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager telephonyManager       = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
		NetworkInfo info						= connectivityManager.getActiveNetworkInfo();
		if(info==null||!connectivityManager.getBackgroundDataSetting()){
			return false;
		}
		int netType 		= info.getType();
		int netSubtype		= info.getSubtype();
		if(netType==ConnectivityManager.TYPE_WIFI){//wifi网络
			return info.isConnected();
		//	return true;
		}else if(netType==ConnectivityManager.TYPE_MOBILE&&
				netSubtype==TelephonyManager.NETWORK_TYPE_UMTS
				&&!telephonyManager.isNetworkRoaming()){//3G网络
			return info.isConnected();
		//	return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 错误提示窗口
	 * @param str为提示的内容
	 */
	public static void WarningDialog(String str,Context context)
	{
		if(context!=null){
		//	Looper.prepare();
			Toast toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
		//	Toast toast = new Toast(context);
		//	toast.setText(str);
		//	toast.setDuration(Toast.LENGTH_LONG);  
			toast.setGravity(Gravity.CENTER,0,0);
			toast.show();
		//	Looper.loop();
		//	toast = null;
		}
	
	//	return toast;
	}
	
	
	//查找连上wifi后本机的地址
		public static String getBroadcastAddress(Context context) throws IOException {
			WifiManager myWifiManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);

			
			DhcpInfo myDhcpInfo = myWifiManager.getDhcpInfo();
			if (myDhcpInfo == null) {
				return null;
			}
			int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask)
					| ~myDhcpInfo.netmask;
			byte[] quads = new byte[4];
			for (int k = 0; k < 4; k++) {
				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			}
			return InetAddress.getByAddress(quads).toString();
		}

		public static String getBroadIpAddress(Context context) {
			String localIp = null;
			try {
				localIp = getBroadcastAddress(context);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (localIp == null)
				return null;

			byte ip[] = localIp.getBytes();

			String ipStr = new String(ip, 1, ip.length - 1);
			String ipStr1 = ipStr.replace('.', '#');
			String str[] = ipStr1.split("#");

			StringBuffer buffer = new StringBuffer();
			buffer.append(str[0]);
			buffer.append('.');
			buffer.append(str[1]);
			buffer.append('.');
			buffer.append(str[2]);
			buffer.append('.');
			buffer.append("255");

			return buffer.toString();

		}
		
		public static void killProcess()
		{
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		
		/**
		 * 获取扩展名
		 * 
		 * @param sFile
		 * @return
		 */
		public static String getExtension(String sFile) {
			String ext = null;
			if (sFile != null) {
				int i = sFile.lastIndexOf('.');
				if (i > 0 && i < sFile.length() - 1)
					ext = sFile.substring(i + 1).toLowerCase();
			}
			return ext;
		}
		
		/**
		 * 从assets目录复制文件到指定的目录
		 * @param fileName,要复制的文件
		 * @param floade，指定目录
		 */
		public static boolean copyFileFormAssets(Context context,String fileName,String floader,String toFileName)
		{
			
			if(!floader.endsWith("/"))
				floader = floader+"/";
			String newFileName = floader+toFileName;
		
			  try {   
	              InputStream in = context.getResources().getAssets().open(fileName);   
	              //获取文件的字节数   
	              int lenght = in.available();   
	   
	              //将文件中的数据读到byte数组中   
	              
	              FileOutputStream fos = new FileOutputStream(newFileName);
	              
	              int copyLength = 1024*4;
	              byte[] buffer = new byte[copyLength]; 
	              int count = 0; 
	              
	              while (true) { //whilw
	            	 int len = in.read(buffer); 
	            	 if (len == -1) { 
	            		 break; 
	            	 } 
	            	 fos.write(buffer, 0, len); 
	              }//while

	              in.close(); 
	              fos.close(); 
	          } catch (Exception e) {   
	              e.printStackTrace();   
	              return false;
	          }   
			  
			  return true;
		}
		
		/**
		 * 从assets目录复制文件到指定的目录
		 * @param fileName,要复制的文件
		 * @param floade，指定目录
		 */
		public static boolean copyFileFormAssets(Context context,String fromFileName,String toFileName)
		{
			

		
			  try {   
	              InputStream in = context.getResources().getAssets().open(fromFileName);   
	              //获取文件的字节数   
	              int lenght = in.available();   
	   
	              //将文件中的数据读到byte数组中   
	              
	              FileOutputStream fos = new FileOutputStream(toFileName);
	              
	              int copyLength = 1024*4;
	              byte[] buffer = new byte[copyLength]; 
	              int count = 0; 
	              
	              while (true) { //whilw
	            	 int len = in.read(buffer); 
	            	 if (len == -1) { 
	            		 break; 
	            	 } 
	            	 fos.write(buffer, 0, len); 
	              }//while

	              in.close(); 
	              fos.close(); 
	          } catch (Exception e) {   
	              e.printStackTrace();   
	              return false;
	          }   
			  
			  return true;
		}

		
//		Android中判断SD卡是否存在，并且可以进行写操作，可以使用如下代码

		public static boolean SdCardExist()
		{
			String sdPath = android.os.Environment.getExternalStorageState();
			String path = android.os.Environment.MEDIA_MOUNTED;
		
			if(sdPath!=null&&sdPath.equals(path)){
				return true;
			}
			return false;
		}
}