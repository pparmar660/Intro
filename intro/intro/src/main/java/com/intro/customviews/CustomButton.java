package com.intro.customviews;

import com.intro.android.R;
import com.intro.utils.Typefaces;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
/**
 * This class is used to provide custom fonts with ButtonView
 * 
 * By Ankit Kumar
 * **/

public class CustomButton extends Button {
	private static final String TAG = "CustomButtonView";

	public CustomButton(Context context) {
		super(context);
	}

	public CustomButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
	}

	public CustomButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCustomFont(context, attrs);
	}

	private void setCustomFont(Context ctx, AttributeSet attrs) {
		TypedArray a = ctx.obtainStyledAttributes(attrs,
				R.styleable.CustomTextView);
		String customFont = a.getString(R.styleable.CustomTextView_customFont);
		setCustomFont(ctx, customFont);
		a.recycle();
	}

	public boolean setCustomFont(Context ctx, String asset) {
		Typeface tf = null;
		try {
			tf = Typefaces.get(ctx, asset);
		} catch (Exception e) {
			Log.e(TAG,
					"Could not get typeface: " + " : Message : "
							+ e.getMessage());
			e.printStackTrace();
			return false;
		}
		setTypeface(tf);
		setTextSize(25);
		return true;
	}

}
