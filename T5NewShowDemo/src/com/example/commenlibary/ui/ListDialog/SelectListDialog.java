package com.example.commenlibary.ui.ListDialog;

import java.util.ArrayList;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.centerm.t5.newdispatch.t5showdemo.R;
import com.example.commenlibary.Interface.CommenLibaryMessageOperator;

public class SelectListDialog extends PopupWindow{
	private View mainView;
	private Context context;
	private ListView listView;
	private SelectListAdapter selectListAdapter;
//	private ArrayList<String> selectList;
	public SelectListDialog(Context context,int width,ArrayList<String> selectList)
	{
		super(context);
		this.context = context;
		mainView = ((Activity)context).getLayoutInflater().inflate(R.layout.select_list_dialog, null);
		listView = (ListView)mainView.findViewById(R.id.SelectListDialog_List);
		listView.getLayoutParams().width = width;
		selectListAdapter = new SelectListAdapter(context, selectList);
		listView.setAdapter(selectListAdapter);
		this.setContentView(mainView);
		setWidth(width);
		setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		//setBackgroundDrawable(context.getResources().getDrawable(R.drawable.));
	}
	
	
	public void setWidgetWidth(int width)
	{
		listView.getLayoutParams().width = width;
		setWidth(width);
	}
	public void setListViewBackground(int drawable)
	{
		listView.setBackgroundResource(drawable);
	}
	public void setCommenLibaryMessageOperator(CommenLibaryMessageOperator listener)
	{
		selectListAdapter.setCommenLibaryMessageOperator(listener);
	}
	
	public void setDeleteListener(CommenLibaryMessageOperator delectListener)
	{
		selectListAdapter.setDeleteListener(delectListener);
	}
	public void ShowDialog(View showView){
		if(!isShowing()){
			this.showAsDropDown(showView);
		}
	}
	
	
	public void CloseDialog()
	{
		if(isShowing()){
			dismiss();
		}
	}

	
	public void addSelectObject(String object)
	{
		selectListAdapter.addObject(object);
	}


}
