package fr.ravenfeld.livewallpaper.slideshow.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

/**
 * File: Util.java Autor: Yesid Lazaro Mayoriano
 */

public class Util {

	public static Bitmap rotateBitmap(Bitmap bitmapOriginal, int grados,
			boolean recycle) {
		if (grados != 0) {
			int width = bitmapOriginal.getWidth();
			int height = bitmapOriginal.getHeight();
			Matrix matrix = new Matrix();
			matrix.postRotate(grados);
			Bitmap result = Bitmap.createBitmap(bitmapOriginal, 0, 0, width,
					height, matrix, true);
			if (recycle) {
				bitmapOriginal.recycle();
			}

			bitmapOriginal = result;
		}

		return bitmapOriginal;
	}

	public static Bitmap compressBitmap(Bitmap bitmapOriginal) {

		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		bitmapOriginal.compress(CompressFormat.JPEG, 90, arrayOutputStream);
		byte[] data = arrayOutputStream.toByteArray();
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		return bitmap;
	}

	public static Bitmap decodeUri(Context context, Uri selectedImage)
			throws FileNotFoundException {
		WallpaperManager wallpaperManager = WallpaperManager
				.getInstance(context);
		final int REQUIRED_SIZE_WIDHT = wallpaperManager
				.getDesiredMinimumWidth();
		final int REQUIRED_SIZE_HEIGHT = wallpaperManager
				.getDesiredMinimumHeight();
		return Util.decodeUri(context, selectedImage, REQUIRED_SIZE_WIDHT, REQUIRED_SIZE_HEIGHT);
	}
	
	public static Bitmap decodeUri(Context context, Uri selectedImage,
			int widthMinimum, int heightMinimum)
			throws FileNotFoundException {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(context.getContentResolver()
				.openInputStream(selectedImage), null, o);
		

		int width_tmp = o.outWidth;
		int height_tmp = o.outHeight;

		int scale = 1;
		while (width_tmp > widthMinimum || height_tmp > heightMinimum) {

			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(context.getContentResolver()
				.openInputStream(selectedImage), null, o2);
	}
}
