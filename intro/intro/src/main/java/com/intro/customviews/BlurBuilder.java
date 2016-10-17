package com.intro.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

// requires min Api 11
/*
 * By Mirza 
 */

public final class BlurBuilder {
	private static final float BITMAP_SCALE = 1.0f; // scalability of Image
	private static final float BLUR_RADIUS = 12.5f; // Blur_radius must be < 25
													// for blur effect 

	@SuppressLint("NewApi")
	public static Bitmap blur(Context context, Bitmap image) {
		int width = Math.round(image.getWidth() * BITMAP_SCALE);
		int height = Math.round(image.getHeight() * BITMAP_SCALE);

		Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height,
				false);
		Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

		RenderScript rs = RenderScript.create(context);
		try{
		ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs,
				Element.U8_4(rs)); // only three are showing effects : A8-- >
									// left side blur , U8-- > soomething
									// similar to A8 // U8 for full image blur
									// effect
		Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
		Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
		theIntrinsic.setRadius(BLUR_RADIUS);
		theIntrinsic.setInput(tmpIn);
		theIntrinsic.forEach(tmpOut);
		tmpOut.copyTo(outputBitmap);
		}catch(Exception e){
			e.printStackTrace();
		}
		

		return outputBitmap; 
	}
}