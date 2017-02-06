package com.centerm.t5.t5showdemo;

import com.centerm.t5.newdispatch.t5pressuretest.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class MySignActivity extends Activity {

	private String path = null;
	private ImageView img = null;
	private Button close;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.mysign);
		this.setFinishOnTouchOutside(false);

		Intent intent = getIntent();
		if(intent != null){
			path = intent.getStringExtra("path");
		}
		
		Log.i(MySignActivity.class.getSimpleName(), "path:" + path);
		
		close = (Button)findViewById(R.id.close);
		close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MySignActivity.this.finish();
			}
		});

		initView();
	}

	private void initView() {
		img = (ImageView)findViewById(R.id.fileImage);
		Bitmap bmp = BitmapFactory.decodeFile(path);
		img.setImageBitmap(bmp);
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Log.i(MySignActivity.class.getSimpleName(), "width, height:" + width +","+height);
	}
}
