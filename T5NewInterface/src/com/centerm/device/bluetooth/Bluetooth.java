/*!
 * @file Bluetooth.java
 * @brief ����
 */
package com.centerm.device.bluetooth;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;


/*!
 * @brief �����豸�Ĳ���
 * @par ˵��:
 * 			�����Ƕ������豸����(BluetoothDevice)�ķ���
 * 		�����࣬ͨ���������豸�������˽�л������ط�����
 * 		����ʵ�ֶ������豸���Զ���ԵĲ�����
 */
public class Bluetooth 
{

	/*!
	 * @brief ���豸�������
	 * @par ˵��:
	 * 		�÷����Ƕ�android.bluetooth.BluetoothDevice���createBond�ķ��䡣
	 * @param [in] dev �����豸����
	 * @return ����Ƿ�ɹ��� ture- �ɹ���false-ʧ��
	 * @throws Exception ���亯��ʹ���쳣
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
	 *  @brief ����ƥ������
	 *  @par ˵��:
	 * 		�÷����Ƕ�android.bluetooth.BluetoothDevice���setPasskey�ķ��䡣
	 * 	�÷���������Թ���������setPasskey��
	 *  @param [in] dev �����豸����
	 *  @param [in] passkey ƥ���ֵ
	 *  @return ����ƥ�������Ƿ�ɹ�  true -��ʾ�ɹ��� false -��ʾʧ��
	 *  @throws Exception ���亯��ʹ���쳣
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
	 * @brief ����pin��
	 * @par ˵��:
	 * 		�÷����Ƕ�android.bluetooth.BluetoothDevice���setPin�ķ��䡣
	 * 	�÷���������Թ���������pin�룬�����ԡ�
	 * @param [in] dev �����豸
	 * @param [in] pin pin��
	 * @return ���������Ƿ�ɹ� ture-��ʾ�ɹ��� false -��ʾʧ��
	 * @throws Exception ���亯��ʹ���쳣
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
	 * @brief ȡ���û�����
	 * @par ˵��:
	 * 		�÷����Ƕ�android.bluetooth.BluetoothDevice���cancelPairingUserInput�ķ��䡣
	 * ��Թ����лᵯ�����û�ѡ����ԣ����ô˺�����ȡ������Թ����е������û�ѡ�������ԡ�
	 * @param [in] dev �����豸
	 * @retval true ���óɹ���
	 * @retval false ����ʧ�ܡ�
	 * @throws Exception ���亯��ʹ���쳣
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
	 * @brief ���ý����Ƿ����
	 * @par ˵��:
	 * 		�÷����Ƕ�android.bluetooth.BluetoothDevice���setPairingConfirmation�ķ��䡣
	 * 	�ڽ������ʱ����ʾ������Ի��߲���Եĵ��ѡ��������¼������ô˺������е���¼���
	 * @param [in] dev �����豸
	 * @param [in] confirm �Ƿ�������
	 * @retval true ���óɹ���
	 * @retval false ����ʧ�ܡ�
	 * @throws Exception ���亯��ʹ���쳣
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
	 * @brief �����ָ���豸�����
	 * @par ˵��:
	 * 		�÷����Ƕ�android.bluetooth.BluetoothDevice���removeBond�ķ��䡣
	 * 	���ô˺�����ǿ���Ƴ�ϵͳ���Ѿ�����һ���󶨲�����
	 * @param [in] dev �����豸
	 * @return �����Ƴ��Ƿ�ɹ���
	 * @throws Exception ���亯��ʹ���쳣
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
	 * @brief ȡ�����ڽ��е����
	 * @par ˵��:
	 * 		�÷����Ƕ�android.bluetooth.BluetoothDevice���cancelBondProcess�ķ��䡣
	 * 	������ִ�а󶨼�createBond����ʱ��ϵͳ������һ���ڲ�������ɰ󶨲�����
	 * 	���ô˺�����ǿ�ƽ����ý��̡�
	 * @param [in] dev �����豸
	 * @return ȡ����Խ����Ƿ�ɹ���true-�ɹ���
	 * @throws Exception ���亯��ʹ���쳣
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
