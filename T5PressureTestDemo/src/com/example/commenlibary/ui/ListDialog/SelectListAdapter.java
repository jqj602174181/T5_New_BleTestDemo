package com.example.commenlibary.ui.ListDialog;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.centerm.t5.newdispatch.t5pressuretest.R;
import com.example.commenlibary.Interface.CommenLibaryMessageOperator;

public class SelectListAdapter extends ArrayAdapter<String>{
	private LayoutInflater mInflater;

	private CommenLibaryMessageOperator listener;
	private CommenLibaryMessageOperator deletListener;
	private boolean isShowClose = true;
	  public SelectListAdapter(Context context,
	      List<String> objects) {
	    super(context,0, objects);
	    isShowClose = false;
	    mInflater = LayoutInflater.from(context);
	  }
	  public SelectListAdapter(Context context,
		      List<String> objects,boolean isShowClose) {
		    super(context,0, objects);
		    mInflater = LayoutInflater.from(context);
		    this.isShowClose = isShowClose;
		  }
	  
	  public void setCommenLibaryMessageOperator(CommenLibaryMessageOperator listener)
	  {
		  this.listener = listener;
	  }
	  
	  public void setDeleteListener(CommenLibaryMessageOperator deleteListener)
	  {
		  this.deletListener = deleteListener;
	  }
	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    final ViewHolder viewHolder;

	    if (convertView == null) {
	       convertView = mInflater.inflate(R.layout.select_list_dialog_item, parent,
	          false);

	      viewHolder = new ViewHolder();
	      
	      viewHolder.textView = (TextView) convertView.findViewById(R.id.SelectDialogListItem_textView);
	      viewHolder.deleteImageView = (ImageView)convertView.findViewById(R.id.SelectDialogListItem_imageView);
	      convertView.setTag(viewHolder);

	    } else {
	      viewHolder = (ViewHolder) convertView.getTag();
	    }
	   
	    
	    final int index = position;
	    final String text = getItem(position);
	    viewHolder.textView.setText(text);
	    viewHolder.textView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(listener!=null){
					listener.operatorMessage(index);
				}
			}
		});
	    
	    if(isShowClose){//show
	    	viewHolder.deleteImageView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(deletListener!=null){
						deletListener.operatorMessage(index);
					}
	
					remove(text);
					notifyDataSetChanged();
				}
			});
		   
	    }else{
	    	viewHolder.deleteImageView.setVisibility(View.GONE);
	    }
	    
	    return convertView;
	  }

	  private static class ViewHolder {
	    public TextView textView;
	    public ImageView deleteImageView;
	  
	  }

	  
	  public void addObject(String text)
	  {
		  this.add(text);
		  this.notifyDataSetChanged();
	  }
}