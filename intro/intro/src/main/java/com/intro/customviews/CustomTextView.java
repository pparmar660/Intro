package com.intro.customviews;

import com.intro.android.R;
import com.intro.utils.Typefaces;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
/**
 * This class is used to provide custom fonts with TextView
 * 
 * By Ankit Kumar
 * **/
public class CustomTextView extends TextView {
	private static final String TAG = "TextView";

	public CustomTextView(Context context) {
		super(context);
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
	}

	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
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
			return false;
		}

		setTypeface(tf);
		return true;

	}
}
