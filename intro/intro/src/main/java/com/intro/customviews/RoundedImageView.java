package com.intro.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class RoundedImageView extends LinearLayout {

	Context mContext;
	private Bitmap windowFrame;

	/**
	 * Creates a Google Map View with rounded corners Constructor when created
	 * in XML
	 * 
	 * @param context
	 * @param attrs
	 */
	public RoundedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	/**
	 * Creates a Google Map View with rounded corners Contructor when created in
	 * code
	 * 
	 * @param context
	 */
	public RoundedImageView(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas); // Call super first (this draws the map) we
									// then draw on top of it

		if (windowFrame == null) {
			createWindowFrame(); // Lazy creation of the window frame, this is
									// needed as we don't know the width &
									// height of the screen until draw time
		}

		canvas.drawBitmap(windowFrame, 0, 0, null);
	}

	protected void createWindowFrame() {

		windowFrame = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888); // Create a new image we will draw
											// over the map
		Canvas osCanvas = new Canvas(windowFrame); // Create a canvas to draw
													// onto the new image
		RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());
		RectF ringRectangle = new RectF(5, 5, getWidth() - 5, getHeight() - 5);
		RectF innerRingRectangle = new RectF(10, 10, getWidth() - 10,
				getHeight() - 10);
		RectF innerRectangle = new RectF(15, 15, getWidth() - 15,
				getWidth() - 15);

		float cornerRadius = getWidth() / 5f; // The angle of your corners

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Anti alias allows for
														// smooth corners
		paint.setColor(Color.WHITE); // This is the color of
														// your activity
														// background
		osCanvas.drawRect(outerRectangle, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
		osCanvas.drawRoundRect(outerRectangle, cornerRadius, cornerRadius,
				paint);
	}

	public void recycleBitmap() {
		try{
			windowFrame.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		windowFrame = null; // If the layout changes null our frame so it can be
							// recreated with the new width and height
	}
}
