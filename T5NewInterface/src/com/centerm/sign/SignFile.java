package com.centerm.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import android.util.Log;

import com.centerm.financial.FinancialBase;
import com.centerm.t5.util.common.DesUtil;
import com.centerm.util.RetUtil;
import com.centerm.util.StringUtil;

 class SignFile {

	 
	 int fileStyle = 0;//成功
	 final static int FILE_CREATE_ERR 	= -1;//文件创建失败
	 final static int UNKNOW 			= -2;//未知
	 final static int RWERR				= -3;//读写错误
	 final static int COMMERROR 		= -4;//通信错误
	 final static int TIMEOUT 			= -5;//超时
	 final static int ParamErr    		= -6;
	 private 	FileOutputStream os;
	 private String fileName;
	 private String tempName = "/mnt/sdcard/hw_temp";
	int fileLen = -2;
	int readLen = 0;

	private int tempSum = 0;
	
	private boolean isDes = false;
	
	 public SignFile(String fileName,boolean isDes)
	 {
		 this.fileName =fileName;
		 this.isDes = isDes;
		 
	 }
	 
	 public void init()
	 {
		 fileLen = -2;
		 readLen = 0;
	 }
	 
	 public void setFileName()
	 {
		 
	 }
	 
	 
	 
	 
	//创建文件
	public  boolean touchFile(){
		
		File file = null;

	    try {
	    
	        file = new File( tempName );
	        if(file.exists()){
	        	file.delete();
	        }
	       
	     //   file.deleteOnExit();
	        boolean is = file.createNewFile();
	        if(!is){//文件创建失败
	        	
	        	
	        	return false;
	        }
	        

	        os = new FileOutputStream(file);
	    } catch (Exception e) {
	        e.printStackTrace();
	        fileStyle = RWERR;
	        return false;
	    }
	    fileStyle =0;
	    fileLen = -2;
	    readLen = 0;
	    tempSum = 0;

	    return true;
	}
		
		public boolean writeData(byte[] writeData,int len)
		{
		
			
			if(fileLen == -2)
			{
				/*
				 * 表示还没有初始化
				 */
				touchFile();//创建文件
				fileLen = Integer.parseInt( StringUtil.AsciiToString(writeData) );//获取文件长度，fileLen表示文件长度
				Log.e( "Sign", "writeData: touchFile:fileLen = "+ fileLen);
			
	
			}else if(fileLen>0)
			{
			
				tempSum = tempSum+len;
				
				try {	    				
					
					
					if(os!=null)
					{
					   
						  if(fileLen-readLen>len)
				          {
							     
								byte tempData[] = new byte[len];
								System.arraycopy(writeData, 0, tempData, 0,tempData.length);
					
								os.write( tempData, 0, tempData.length);
						  }
				          else if((readLen<fileLen)&&(fileLen-readLen)<=len)//这里代表最后一包
				          {
							byte[] tempData = new byte[fileLen-readLen];
							System.arraycopy(writeData, 0, tempData, 0,tempData.length);
					//		Log.e( "Sign", "writeData: last package: len ="+  (fileLen-readLen ) ) ;
							os.write( tempData, 0, fileLen-readLen);
					
						}
						//}
					//	os.write( data, 0, len );
						
					}
					
					readLen += len;//总共收取的长度
				//	Log.e( "Sign", "writeData: realDlen = "+readLen + ",fileLen="+fileLen  );
					if(readLen >= fileLen)//当收取的长度比文件长度大时，代表结束 
					{
						Log.e("Sign","file 112233 edn end ");
						os.close();
						os=null;
						fileStyle  = 0;
						if(isDes)
						{
							saveFileForDes(tempName,fileName);
						}
						
						return true;//代表文件写入成功
					}
			
					
					
				} catch (Exception e) {
					e.printStackTrace();
					fileStyle = RWERR;
				}
				
			}
			else
			{
				tempSum = tempSum+len;
			
			}
			
			return false;//代表文件没有写入成功
		}
		
		
		
		public void setFileStyle(int style)
		{
			fileStyle = style;
			if(os!=null){
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public String[] getError()
		{
			return getError(fileStyle);
		}
		public static String[] getError(int fileStyle)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("1");
			builder.append("@");
			switch (fileStyle) {
			case FILE_CREATE_ERR:
				
			case UNKNOW:
				builder.append(RetUtil.Unknown_Err);
				break;
			case RWERR:
				builder.append(RetUtil.Send_Mess_Err);
				break;
			case COMMERROR:
				builder.append(RetUtil.Recv_Error_Mess);
				break;
			case TIMEOUT:
				builder.append(RetUtil.Timeout_Err);
				break;
			case ParamErr:
				builder.append(RetUtil.Param_Err);
				break;

		default:
			
			break;
		}
			return builder.toString().split("@");
		}
		
		
		
		//将数据解密后保存
		private void saveFileForDes(String fileName,String saveName)
		{
			File file = new File(fileName);
			int length = (int)file.length();
			byte[] temp = new byte[1024];
			InputStream inputStream=null;
			

			File saveFile = null;
			FileOutputStream saveOs = null;
			
			saveFile = new File( saveName );
	        if(saveFile.exists()){
	        	saveFile.delete();
	        }
			
	     //   file.deleteOnExit();
	       
		  
			int c= 0;
			try {
				 boolean is = saveFile.createNewFile();
			        if(!is){//文件创建失败
			        	
			        	
			        	return;
			        }
				inputStream = new FileInputStream(fileName);
			    saveOs = new FileOutputStream(saveFile);
			    byte[] lenBuffer = new byte[4];
			    inputStream.read(lenBuffer);
			    int fileLen = StringUtil.bytesToInt(lenBuffer,0);
		
			    Log.e("file"," fileLen is "+fileLen);
				c = inputStream.read(temp);
				int sum = 0;
				byte tempData[] = null;
				while (c>0) {
			
					tempData = new byte[c];
					System.arraycopy(temp, 0, tempData, 0,c);
					byte[] data = DesUtil.desDecrypt(DesUtil.SIGNDATA_KEY,tempData);
					if(fileLen-sum>data.length){
					//	break;
						saveOs.write(data, 0, data.length);
					}else if(fileLen-sum<=data.length){//最后一包
						saveOs.write(data, 0, fileLen-sum);
						//break;
					}
					
					sum = sum+data.length;
					//Log.e("file"," sum is "+sum);
					c = inputStream.read(temp);
					
				}
				inputStream.close();
				saveOs.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		
			}
			//传完图片删掉，避免下次什么都不传的时候再次传上次签名的图片
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		
			}
			
			file.delete();
		}

}
