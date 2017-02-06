package com.centerm.t5.t5showdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.centerm.t5.newdispatch.t5pressuretest.R;

public class MyPushActivity extends Activity {

	private ImageView portraiImage;
	private ImageView fileImage;
	private TextView username;
	private Button close;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.setContentView(R.layout.mypush);
		this.setFinishOnTouchOutside(false);

		Intent intent = getIntent();
		String portraiPath = null;
		String filePath = null;
		String userName = null;
		if(intent != null){
			portraiPath = intent.getStringExtra("portraiPath");
			filePath = intent.getStringExtra("filePath");
			userName = intent.getStringExtra("userName");
		}

		//		portraiImage = (ImageView)findViewById(R.id.portraiImage);
		fileImage = (ImageView)findViewById(R.id.fileImage);
		//		username = (TextView)findViewById(R.id.username);
		close = (Button)findViewById(R.id.close);
		close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MyPushActivity.this.finish();
			}
		});

		init(portraiPath, filePath, userName);

		MyApp.getInstance().list.add(this);
	}

	private void init(String filePath, String userName) {
		fileImage.setImageDrawable(Drawable.createFromPath(filePath));
		username.setText(userName);
	}

	private void init(String portraiPath, String filePath, String userName) {
		//		portraiImage.setImageDrawable(Drawable.createFromPath(portraiPath));
		fileImage.setImageDrawable(Drawable.createFromPath(filePath));
		//		username.setText(userName);
	}
}
