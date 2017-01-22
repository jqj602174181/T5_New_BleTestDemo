/*!
 * @file Bluetooth.java
 * @brief 蓝牙
 */
package com.centerm.device.bluetooth;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;


/*!
 * @brief 蓝牙设备的操作
 * @par 说明:
 * 			该类是对蓝牙设备对象(BluetoothDevice)的反射
 * 		操作类，通过对蓝牙设备对象类的私有或者隐藏方法的
 * 		反射实现对蓝牙设备的自动配对的操作。
 */
public class Bluetooth 
{

	/*!
	 * @brief 与设备进行配对
	 * @par 说明:
	 * 		该方法是对android.bluetooth.BluetoothDevice类的createBond的反射。
	 * @param [in] dev 蓝牙设备对象
	 * @return 配对是否成功， ture- 成功，false-失败
	 * @throws Exception 反射函数使用异常
	 */
	public static boolean createBond( BluetoothDevice dev )throws Exception
	{
		try
		{
			Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
			Boolean ret = (Boolean)createBondMethod.invoke( dev );
			return ret.booleanValue();
		}catch( Exception e )
		{
			throw new UtilException(e.getMessage());
		}
	}
	
	/*!
	 *  @brief 设置匹配密码
	 *  @par 说明:
	 * 		该方法是对android.bluetooth.BluetoothDevice类的setPasskey的反射。
	 * 	该方法是在配对过程中设置setPasskey。
	 *  @param [in] dev 蓝牙设备对象
	 *  @param [in] passkey 匹配键值
	 *  @return 设置匹配密码是否成功  true -表示成功， false -表示失败
	 *  @throws Exception 反射函数使用异常
	 */
	public static boolean setPasskey(BluetoothDevice dev, int passkey)throws Exception
	{
		try
		{
			Method setKeyMethod = BluetoothDevice.class.getMethod("setPasskey", int.class );
			Boolean ret = (Boolean)setKeyMethod.invoke( dev, passkey );
			return ret.booleanValue();
		}catch( Exception e )
		{
			throw new UtilException(e.getMessage());
		}
	}
	
	/*!
	 * @brief 设置pin码
	 * @par 说明:
	 * 		该方法是对android.bluetooth.BluetoothDevice类的setPin的反射。
	 * 	该方法是在配对过程中设置pin码，完成配对。
	 * @param [in] dev 蓝牙设备
	 * @param [in] pin pin码
	 * @return 返回设置是否成功 ture-表示成功， false -表示失败
	 * @throws Exception 反射函数使用异常
	 */
	public static boolean setPin( BluetoothDevice dev, byte[] pin )throws Exception
	{
		try
		{
			Method setPinMethod = BluetoothDevice.class.getMethod("setPin", byte[].class );
			Boolean ret = (Boolean)setPinMethod.invoke( dev, pin );
			return ret.booleanValue();
		}catch( Exception e )
		{
			throw new UtilException(e.getMessage());
		}
	}
	
	/*!
	 * @brief 取消用户输入
	 * @par 说明:
	 * 		该方法是对android.bluetooth.BluetoothDevice类的cancelPairingUserInput的反射。
	 * 配对过程中会弹框让用户选择配对，调用此函数会取消在配对过程中弹框让用户选择进行配对。
	 * @param [in] dev 蓝牙设备
	 * @retval true 设置成功。
	 * @retval false 设置失败。
	 * @throws Exception 反射函数使用异常
	 * 		
	 */
	public static boolean cancelPairingUserInput(BluetoothDevice dev)throws Exception
	{
		try
		{
			Method cancelInputMethod = BluetoothDevice.class.getMethod("cancelPairingUserInput" );
			Boolean ret = (Boolean)cancelInputMethod.invoke( dev );
			return ret.booleanValue();
		}catch( Exception e )
		{
			throw new UtilException(e.getMessage());
		}
	}
	
	/*!
	 * @brief 设置进行是否配对
	 * @par 说明:
	 * 		该方法是对android.bluetooth.BluetoothDevice类的setPairingConfirmation的反射。
	 * 	在进行配对时会提示进行配对或者不配对的点击选择操作的事件。调用此函数进行点击事件。
	 * @param [in] dev 蓝牙设备
	 * @param [in] confirm 是否进行配对
	 * @retval true 设置成功。
	 * @retval false 设置失败。
	 * @throws Exception 反射函数使用异常
	 */
	public static boolean setPairingConfirmation(BluetoothDevice dev, boolean confirm)throws Exception
	{
		try
		{
			Method confirmMethod = BluetoothDevice.class.getMethod("setPairingConfirmation", boolean.class );
			Boolean ret = (Boolean)confirmMethod.invoke( dev, confirm );
			return ret.booleanValue();
		}catch( Exception e )
		{
			throw new UtilException(e.getMessage());
		}
	}
	
	/*!
	 * @brief 解除与指定设备的配对
	 * @par 说明:
	 * 		该方法是对android.bluetooth.BluetoothDevice类的removeBond的反射。
	 * 	调用此函数将强制移除系统中已经创建一个绑定操作。
	 * @param [in] dev 蓝牙设备
	 * @return 返回移除是否成功。
	 * @throws Exception 反射函数使用异常
	 */
	public static boolean removeBond(BluetoothDevice dev)throws Exception
	{
		try
		{
			Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond" );
			Boolean ret = (Boolean)removeBondMethod.invoke( dev );
			return ret.booleanValue();
		}catch( Exception e )
		{
			throw new UtilException(e.getMessage());
		}
	}
	
	/*!
	 * @brief 取消正在进行的配对
	 * @par 说明:
	 * 		该方法是对android.bluetooth.BluetoothDevice类的cancelBondProcess的反射。
	 * 	当蓝牙执行绑定即createBond请求时，系统将创建一个内部进程完成绑定操作。
	 * 	调用此函数将强制结束该进程。
	 * @param [in] dev 蓝牙设备
	 * @return 取消配对进行是否成功。true-成功。
	 * @throws Exception 反射函数使用异常
	 */
	public static boolean cancelBondProcess(BluetoothDevice dev) throws Exception
	{
		try
		{
			Method cancelBondMethod = BluetoothDevice.class.getMethod("cancelBondProcess" );
			Boolean ret = (Boolean)cancelBondMethod.invoke( dev );
			return ret.booleanValue();
		}catch( Exception e )
		{
			throw new UtilException(e.getMessage());
		}
	}
}
