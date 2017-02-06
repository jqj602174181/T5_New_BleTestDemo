package com.example.commenlibary.bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapOption {

	private String filePath;
	private int width, height;

	public BitmapOption(String path, int width, int height) {
		// TODO Auto-generated constructor stub
		filePath = path;
		this.width = width;
		this.height = height;
	}

	public static Bitmap getBitmap(String filePath, int width, int height) {

		File file = new File(filePath);

		if (!file.exists())
			return null;
		int len = (int) file.length();
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, options);

			options.inSampleSize = computeSampleSize(options, -1, width
					* height);
			options.inJustDecodeBounds = false;

			options.inDither = false;
			options.inPurgeable = true;
			options.inTempStorage = new byte[len];

			Bitmap bitmap = null;
			FileInputStream fStream = null;

			fStream = new FileInputStream(file);
			bitmap = BitmapFactory.decodeFileDescriptor(fStream.getFD(), null,
					options);
			fStream.close();
			return bitmap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			// TODO: handle exception

		}
		return null;

	}

	// bitmap图片压缩
	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	// 对图片进行缩放处理
	public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {// 1
		int Width = bitmap.getWidth();
		int Height = bitmap.getHeight();
		if (Width < newWidth && Height < newHeight) {// 屏幕放的下,不缩放if
			return bitmap;
		} else {// 超过屏幕的大小
			float scaleWidth = 1;
			float scaleHeight = 1;
			if (Width > newWidth) {
				// Width = newWidth;
				scaleWidth = ((float) newWidth) / Width;
				// Width = newWidth;
			}
			if (Height > newHeight) {
				// Height = newHeight;
				scaleHeight = ((float) newHeight) / Height;
				// Height = newHeight;
			}

			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, Width, Height,
					matrix, true);

			// recycleleBitmap(bitmap);
			return bitmap1;
		}// endif

	}// 1

	// 对drawable下的图片进行缩放
	public static Bitmap getBitmap(Context context, int id, int width,
			int height) {
		java.io.InputStream is;
		is = context.getResources().openRawResource(id);

		BitmapFactory.Options opts = new BitmapFactory.Options();
		Bitmap bm;

		opts.inJustDecodeBounds = true;
		bm = BitmapFactory.decodeStream(is, null, opts);

		// now opts.outWidth and opts.outHeight are the dimension of the
		// bitmap, even though bm is null

		opts.inJustDecodeBounds = false; // this will request the bm
		opts.inSampleSize = computeSampleSize(opts, -1, width * height); // scaled
																			// down
																			// by
																			// 4
		bm = BitmapFactory.decodeStream(is, null, opts);
		return bm;
	}

	public static Bitmap getBitmap(Context context, int id) {
		java.io.InputStream is;
		is = context.getResources().openRawResource(id);
		Bitmap bm;
		bm = BitmapFactory.decodeStream(is);
		return bm;
	}

	public static Bitmap bitmapZoom(Bitmap bitmap, int width, int height) {

		if (bitmap == null)
			return null;
		int width1 = bitmap.getWidth();
		int height1 = bitmap.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) width) / width1;
		float scaleHeight = ((float) height) / height1;
		float scale = 1;

		scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		// 得到新的图片
		Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width1, height1,
				matrix, true);
		// ToolPublicFunction.recycleleBitmap(bitmap);
		return newbm;
	}

	public static Bitmap getBitmap(byte[] data, int width, int height) {

		if (data == null) {
			return null;
		}
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		Bitmap newBitmap = bitmapZoom(bitmap, width, height);
		recycleleBitmap(bitmap);
		return newBitmap;
	}

	public static void recycleleBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			if (bitmap.isRecycled()) {
				bitmap.recycle();

			}
			bitmap = null;
		}
	}

}
