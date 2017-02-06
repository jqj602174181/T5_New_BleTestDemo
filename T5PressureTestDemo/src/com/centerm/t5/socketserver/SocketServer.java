package com.centerm.t5.socketserver;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;  
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;  
import java.net.Socket;  
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.centerm.t5.socketclient.DesUtil;
import com.centerm.t5.socketclient.ErrorUtil;
import com.centerm.t5.socketclient.StringUtil;
import com.centerm.t5.t5showdemo.MyApp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class SocketServer extends Service   
{  
	private ServerSocket ss = null;  
	private static SocketServer instance = null;  

	private String dir = "/mnt/internal_sd/jrz";

	//	private Handler handler;
	int num = 0;

	private byte msgType = 0x01;			//操作码
	private byte subType = 0x01;			//操作子类型
	private String resContent;				//接收到的数据内容

	public final static byte SUBTYPE_EXCEPTION = 102;		//发生异常
	private String errorinfo = "";			//错误信息
	public final static String TAG_LOG = "dev_commit";		//日志标志

	//	private SocketServer(){
	//		//创建目录
	//		File file = new File(dir);
	//		if(!file.exists()){
	//			file.mkdir();
	//		}
	//	}  

	@Override
	public void onCreate() {
		super.onCreate();

		start();
	}

	//	public void setHandler(Handler handler){
	//		this.handler = handler;
	//	}

	public static SocketServer getInstance(){
		if(instance == null){
			instance = new SocketServer();
		}
		return instance;
	}

	public void start(){
		new Thread(){
			public void run() {
				try   
				{  
					ss = new ServerSocket(3535);  
					System.out.println("The server is waiting your input...");  

					while(true)   
					{  
						num++;
						Socket socket = ss.accept();  
						new Thread(new ServerThread(socket),"Client "+num).start();
					}  
				} catch (IOException e) {  
					e.printStackTrace();  
				} finally{
					try{
						ss.close();
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	/**
	 * @author JQJ
	 * 服务器处理客户端会话的线程
	 */
	class ServerThread implements Runnable {

		Socket socket = null;
		public ServerThread(Socket socket){
			System.out.println("Create a new ServerThread...");
			this.socket = socket;
		}

		@Override
		public void run() {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = socket.getInputStream();
				out  = socket.getOutputStream();
				//处理客户端发来的数据
				doRead2(in);
				System.out.println("send Message to client.");
				//发送数据回客户端
				//				doWrite(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void doRead2(InputStream in){ //有照片数据的解析
			try{
				//读取数据包长度
				byte[] dataByte = new byte[4];
				int readlen  = readMessage(in, dataByte, 4, 5 );//先读取头四个字节，来计算接下来要读的长度
				if( readlen == 4 ){//读取成功
					int readLen = StringUtil.bytesToIntByBigEndian(dataByte, 0);//转为int

					//接收实际的数据
					byte[] buffer = new byte[readLen];
					int msglen = readMessage(in, buffer, readLen, 15);
					Log.i("SocketServer", "msglen:" + msglen);
					if( msglen == readLen ){
						//判断内容是否有效
						if( buffer[0] == msgType ){
							byte[] lengthByte = new byte[4];
							lengthByte[0] = buffer[2];
							lengthByte[1] = buffer[3];
							lengthByte[2] = buffer[4];
							lengthByte[3] = buffer[5];
							int lenght = StringUtil.bytesToIntByBigEndian(lengthByte, 0);//信息长度
							//取出内容，并解密
							String data = new String( buffer, 6, lenght );//舍去2个类型字节,信息长度4个字节和校验字节	
							byte[] encryptedData = StringUtil.StringToHexA(data);
							byte[] plainData = DesUtil.trides_decrypt( DesUtil.KEYBYTES, encryptedData);
							resContent = new String( plainData, 0, plainData.length);
							Log.e("socket","resContent is "+resContent); //json字符串

							JSONObject object = new JSONObject(resContent);
							String userName = object.getString("name"); //客户姓名
							String portraitszie = object.getString("portraitsize"); //头像长度
							String portraitname = object.getString("portraitname"); 
							String fileszie = object.getString("filesize"); //图片长度
							String filename = object.getString("filename"); 

							String path = Environment.getExternalStorageDirectory().getAbsolutePath();
							//头像数据解析
							byte[] portraiImgByte = new byte[Integer.parseInt(portraitszie)];
							System.arraycopy(buffer, 6+lenght, portraiImgByte, 0, Integer.parseInt(portraitszie));
							System.out.println("头像图片长度：" + portraiImgByte.length);
							byte2File(portraiImgByte, path, portraitname);

							//图片数据解析
							byte[] fileImgByte = new byte[Integer.parseInt(fileszie)];
							System.arraycopy(buffer, 6+lenght+Integer.parseInt(portraitszie), fileImgByte, 0, Integer.parseInt(fileszie));
							System.out.println("文件图片长度：" + fileImgByte.length);
							byte2File(fileImgByte, path, filename);

							//							HashMap<String, String> map = new HashMap<String, String>();
							//							map.put("content", resContent);
							//							map.put("path", path+File.separator+name);

							if( buffer[1] == subType ){//正确
								errorinfo = "";

								//发广播
								Intent intent = new Intent(MyApp.ACTION);
								intent.putExtra("userName", userName);
								intent.putExtra("portraiPath", path+File.separator+portraitname);
								intent.putExtra("filePath", path+File.separator+filename);
								sendBroadcast(intent);

								//								if(handler != null){
								//									//通知展示
								//									Message msg = new Message();
								//									msg.obj = map;
								//									handler.sendMessage(msg);
								//								}
							}
							else if( buffer[1] == SUBTYPE_EXCEPTION  ){//异常
								JSONObject jsexception = new JSONObject(resContent);
								Log.e( TAG_LOG, "exception:" + jsexception.getString( "exception" ));
								errorinfo = jsexception.getString( "exception" );
							}
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		//		private void doRead2(InputStream in){ //没有照片数据
		//			try{
		//				//读取数据包长度
		//				byte[] dataByte = new byte[4];
		//				int readlen  = readMessage(in, dataByte, 4, 5 );//先读取头四个字节，来计算接下来要读的长度
		//				if( readlen == 4 ){//读取成功
		//					int readLen = StringUtil.bytesToIntByBigEndian(dataByte, 0);//转为int
		//
		//					//接收实际的数据
		//					byte[] buffer = new byte[readLen];
		//					int msglen = readMessage(in, buffer, readLen, 15);
		//					Log.i("SocketServer", "msglen:" + msglen);
		//					if( msglen == readLen ){
		//						//判断内容是否有效
		//						if( buffer[0] == msgType ){
		//							byte[] lengthByte = new byte[4];
		//							lengthByte[0] = buffer[2];
		//							lengthByte[1] = buffer[3];
		//							lengthByte[2] = buffer[4];
		//							lengthByte[3] = buffer[5];
		//							int lenght = StringUtil.bytesToIntByBigEndian(lengthByte, 0);//信息长度
		//							//取出内容，并解密
		//							String data = new String( buffer, 6, lenght );//舍去2个类型字节,信息长度4个字节和校验字节	
		//							byte[] encryptedData = StringUtil.StringToHexA(data);
		//							byte[] plainData = DesUtil.trides_decrypt( DesUtil.KEYBYTES, encryptedData);
		//							resContent = new String( plainData, 0, plainData.length);
		//							Log.e("socket","resContent is "+resContent); //json字符串
		//
		//							JSONObject object = new JSONObject(resContent);
		//							String userName = object.getString("name"); //客户姓名
		//							String fileszie = object.getString("filesize"); //图片长度
		//							String filename = object.getString("filename"); 
		//
		//							String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		//
		//							//图片数据解析
		//							byte[] fileImgByte = new byte[Integer.parseInt(fileszie)];
		//							System.arraycopy(buffer, 6+lenght, fileImgByte, 0, Integer.parseInt(fileszie));
		//							System.out.println("文件图片长度：" + fileImgByte.length);
		//							byte2File(fileImgByte, path, filename);
		//							if( buffer[1] == subType ){//正确
		//								errorinfo = "";
		//
		//								//发广播
		//								Intent intent = new Intent(MyApp.ACTION);
		//								intent.putExtra("userName", userName);
		//								intent.putExtra("filePath", path+File.separator+filename);
		//								sendBroadcast(intent);
		//
		//								//								if(handler != null){
		//								//									//通知展示
		//								//									Message msg = new Message();
		//								//									msg.obj = map;
		//								//									handler.sendMessage(msg);
		//								//								}
		//							}
		//							else if( buffer[1] == SUBTYPE_EXCEPTION  ){//异常
		//								JSONObject jsexception = new JSONObject(resContent);
		//								Log.e( TAG_LOG, "exception:" + jsexception.getString( "exception" ));
		//								errorinfo = jsexception.getString( "exception" );
		//							}
		//						}
		//					}
		//				}
		//			}catch(Exception e){
		//				e.printStackTrace();
		//			}
		//		}

		public int readMessage(InputStream in, byte[] buf, int len, int timeout) {

			long startTime = System.currentTimeMillis();
			int nHasRead = 0;
			int nCurRead = 0;

			while (nHasRead < len) {
				// 超时
				if ((System.currentTimeMillis() - startTime) > timeout * 1000) {
					return ErrorUtil.ERR_TIMEOUT;
				}
				// 读数据
				try {
					nCurRead = in.read(buf, nHasRead, len - nHasRead);
					nHasRead += nCurRead;
				} catch (IOException e1) {
					e1.printStackTrace();
					return ErrorUtil.ERR_READ;
				}
				try {
					if( nHasRead < len )//避免读到定长数据时仍然会睡眠
						Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("recv msg " + nHasRead + " byte");
			return nHasRead;
		}

		/**
		 * 读取数据
		 * @param in
		 * @return
		 */
		public boolean doRead(InputStream in){
			byte[] inputByte = null;
			int length = 0;
			DataInputStream dis = null;
			FileOutputStream fos = null;
			try {
				try {
					dis = new DataInputStream(socket.getInputStream());
					String path = dir + File.separator + "jrz.png";
					fos = new FileOutputStream(new File(path));
					inputByte = new byte[1024];
					System.out.println("开始接收数据...");
					while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
						fos.write(inputByte, 0, length);
						fos.flush();
					}
					System.out.println("完成接收");

					//					if(handler != null){
					//						//通知展示
					//						Message msg = new Message();
					//						msg.obj = path;
					//						handler.sendMessage(msg);
					//					}
				} finally {
					if (fos != null)
						fos.close();
					if (dis != null)
						dis.close();
					if (socket != null)
						socket.close();
				}
			} catch (Exception e) {
			}

			return true;
		}

		/**
		 * 写入数据
		 * @param out
		 * @return
		 */
		public boolean doWrite(OutputStream out){
			try {
				out.write("welcome you client.".getBytes());
				out.flush();		
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}

	public int bytesToIntByBigEndian(byte[] src, int offset) {  
		int value;    
		value = (int) ((src[offset+3] & 0xFF)   
				| ((src[offset+2] & 0xFF)<<8)   
				| ((src[offset+1] & 0xFF)<<16)   
				| ((src[offset] & 0xFF)<<24));  
		return value;  
	}

	public void byte2File(byte[] buf, String filePath, String fileName)  
	{  
		BufferedOutputStream bos = null;  
		FileOutputStream fos = null;  
		File file = null;  
		try  
		{  
			File dir = new File(filePath);  
			if (!dir.exists())  
			{  
				dir.mkdirs();  
			}  
			file = new File(filePath + File.separator + fileName);  
			fos = new FileOutputStream(file);  
			bos = new BufferedOutputStream(fos);  
			bos.write(buf);  
		}  
		catch (Exception e)  
		{  
			e.printStackTrace();  
		}  
		finally  
		{  
			if (bos != null)  
			{  
				try  
				{  
					bos.close();  
				}  
				catch (IOException e)  
				{  
					e.printStackTrace();  
				}  
			}  
			if (fos != null)  
			{  
				try  
				{  
					fos.close();  
				}  
				catch (IOException e)  
				{  
					e.printStackTrace();  
				}  
			}  
		}  
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	} 
}  