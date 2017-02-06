package com.centerm.t5.t5showdemo;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.centerm.t5.newdispatch.t5pressuretest.R;

public class MyPushDialog extends Dialog {

	private Context context;
	private ImageView image;
	private TextView text;
	private Button close;

	public MyPushDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		initDialog();
	}

	public MyPushDialog(Context context) {
		super(context, R.style.CustomAlertDialog);
		this.context = context;
		initDialog();
	}

	private void initDialog() {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.mypush, null);
		this.addContentView(view, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

//		image = (ImageView)view.findViewById(R.id.portraiImage);
//		text = (TextView)view.findViewById(R.id.username);
		close = (Button)view.findViewById(R.id.close);

		close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MyPushDialog.this.dismiss();
			}
		});
	}

	public static class Builder {
		private Context context;

		public Builder(Context context) {
			this.context = context;
		}

		public MyPushDialog create() {
			MyPushDialog dialog = new MyPushDialog(context, R.style.CustomAlertDialog);
			return dialog;
		}
	}

	public void show(){
		super.show();
	}
}
