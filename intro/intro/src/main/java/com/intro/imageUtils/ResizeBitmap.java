package com.intro.imageUtils;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;


public class ResizeBitmap {

	public static String TAG = "ResizeBitmap";
	
	/**
	 * @param path
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
		Log.d("path", path);
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	/**
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

//	/**
//	 * @param imagePath
//	 * @return circular bitmap
//	 */
//	public static Bitmap getCroppedBitmap(String imagePath) {
//
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inSampleSize = 4;
//		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
//
//		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
//				bitmap.getHeight(), Config.ARGB_8888);
//		Canvas canvas = new Canvas(output);
//
//		final int color = 0xff424242;
//		final Paint paint = new Paint();
//		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//		paint.setAntiAlias(true);
//		canvas.drawARGB(0, 0, 0, 0);
//		paint.setColor(color);
//		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
//				bitmap.getWidth() / 2, paint);
//		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
//		canvas.drawBitmap(bitmap, rect, rect, paint);
//		Bitmap _bmp = Bitmap.createScaledBitmap(output,
//				GlobalVariable.SHORTCUT_BITMAP_WIDTH,
//				GlobalVariable.SHORTCUT_BITMAP_HEIGHT, false);
//
//		return _bmp;
//	}

	/**
	 * @param res
	 * @param id
	 * @return Scaled down Bitmap
	 */
	public static Bitmap decodeResource(Resources res, int id) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		for (options.inSampleSize = 1; options.inSampleSize <= 32; options.inSampleSize++) {
			try {
				bitmap = BitmapFactory.decodeResource(res, id, options);
				Log.d("TAG", "Decoded successfully for sampleSize " + options.inSampleSize);
				break;
			} catch (OutOfMemoryError outOfMemoryError) {
				Log.e("TAG", "outOfMemory while reading file for sampleSize " + options.inSampleSize + " retrying with higher value");
			}
		}
		return bitmap;
	}

	/**
	 * @param source
	 * @param angle
	 * @param imagePath
	 * @return
	 */
	public static Bitmap rotateImage(Bitmap source, float angle, String imagePath) {
		Bitmap bitmap = null;
		Matrix matrix = new Matrix();
		matrix.preRotate(angle);
		//matrix.postRotate(angle);
		try {
			bitmap = sampleBitmap(source.getWidth(), source.getHeight(), imagePath, matrix);
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
		} catch (Exception e) {
		}
		return bitmap;
	}
	
	 public static Bitmap decodeUri(Context mContext, Uri selectedImage) throws FileNotFoundException {
	        Bitmap realImage = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(selectedImage), null, null);
	        ExifInterface exif = null;
	        try {
	            exif = new ExifInterface(selectedImage.getPath());
	        } catch (IOException e1) {
	            e1.printStackTrace();
	        }

	        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

	        int rotate = 0;
	        
	        Log.v(TAG, "Orientation="+exifOrientation);

	        switch (exifOrientation) {
		        case ExifInterface.ORIENTATION_ROTATE_90:
		            rotate = 90;
		            break;
		        case ExifInterface.ORIENTATION_ROTATE_180:
		            rotate = 180;
		            break;
		        case ExifInterface.ORIENTATION_ROTATE_270:
		            rotate = 270;
		            break;
	        }

	        if (rotate != 0) {
	            int w = realImage.getWidth();
	            int h = realImage.getHeight();
	            Matrix mtx = new Matrix();
	            mtx.preRotate(rotate);
	            // Rotating Bitmap & convert to ARGB_8888, required by tess
	            realImage = Bitmap.createBitmap(realImage, 0, 0, w, h, mtx, false);
	        }
	        return realImage;
	    } 

	/**
	 * @param photoPath
	 * @param bitmap
	 * @return
	 * @throws IOException
	 */
	public static Bitmap handleRotation(String photoPath, Bitmap bitmap) throws IOException {
		ExifInterface ei = new ExifInterface(photoPath);
		int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		Log.v(TAG, "orientation="+orientation);
		Bitmap newBitmap = null;
		switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				newBitmap = ResizeBitmap.rotateImage(bitmap, 90, photoPath);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				newBitmap = ResizeBitmap.rotateImage(bitmap, 180, photoPath);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				newBitmap = ResizeBitmap.rotateImage(bitmap, 270, photoPath);
				break;
			default:
				newBitmap = bitmap;
				break;
		}
		return newBitmap;
	}

	/**
	 * @param targetW
	 * @param targetH
	 * @param imagePath
	 * @param matrix
	 * @return
	 */
	public static Bitmap sampleBitmap(int targetW, int targetH, String imagePath, Matrix matrix) {
		targetW = targetW>0 ? targetW : 480;
		targetH = targetH>0 ? targetH : 640;
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
		Bitmap src = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		//bitmap.recycle();
		bitmap = null;
		//System.gc();

		OutputStream out = null;
		try {
			out = new FileOutputStream(imagePath);
			src.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return src;

	}
	
	/**
	 * @param targetW
	 * @param targetH
	 * @param imagePath
	 * @param matrix
	 * @return
	 */
	public static Bitmap sampleDrawable(int targetW, int targetH, Drawable drawable, Matrix matrix) {
		targetW = targetW>0 ? targetW : 480;
		targetH = targetH>0 ? targetH : 640;
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;
		bmOptions.inJustDecodeBounds = true;
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
		Bitmap src = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		bitmap.recycle();
		bitmap = null;
		System.gc();
		
		return src;

	}

}
