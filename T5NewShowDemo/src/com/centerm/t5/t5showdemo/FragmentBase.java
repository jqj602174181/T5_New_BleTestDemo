package com.centerm.t5.t5showdemo;

import java.util.HashMap;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.centerm.t5.newdispatch.t5showdemo.R;
import com.centerm.t5.util.common.CommonUtil;

public abstract class FragmentBase extends Fragment implements OnClickListener{
	protected final String sRight="0";
	protected MainActivity mainActivity;
	protected String inputTime; 
	protected String success="�ɹ�";
	protected EditText etTimeOut;
	static boolean isConnect = false;//�����ж������Ƿ�������
	protected String useTime = "��ʱ:";
	protected String second = "��";
	protected long currentTime = 0;
	protected long endTime = 0;
	protected char split = '\n';
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState,int layoutId) {
		mainActivity = (MainActivity)getActivity();
		View view = inflater.inflate(layoutId, null);
		inputTime = getString(R.string.inputTime);
		return view;
	}

	/*
	 * �ж�ʱ��������Ƿ�Ϊ��
	 */
	protected boolean isTimeOut(EditText etTime)
	{
		if(etTime.getText().length()==0){

			CommonUtil.showTip(mainActivity, inputTime);
			return false;
		}
		return true;
	}
	protected int getTime(String timeOut)
	{
		try {
			int time = Integer.parseInt(timeOut);
			return time;
		} catch (NumberFormatException e) {
			// TODO: handle exception
		}

		return 20;
	}
	public abstract void setData(Object data);
}
