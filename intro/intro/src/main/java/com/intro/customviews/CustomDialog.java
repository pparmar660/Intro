package com.intro.customviews;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.intro.android.R;

/**
 * This class is used for custom dialog of pink color and also take screenshot
 * and blur the backgroung of dialog.
 * 
 * By Ankit Kumar and Mirza Faisal
 */

public class CustomDialog extends Dialog {

	public CustomDialog(Context contxt, int theme) {
		super(contxt, theme);
	}

	public CustomDialog(Context contxt) {
		super(contxt);
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		ImageView imageView = (ImageView) findViewById(R.id.spinnerImageView);
		AnimationDrawable spinner = (AnimationDrawable) imageView
				.getBackground();
		spinner.start();
	}

	public void setMessage(CharSequence message) {
		if (message != null && message.length() > 0) {

			TextView txt = (TextView) findViewById(R.id.message);
			txt.setVisibility(View.VISIBLE);
			txt.setText(message);
			txt.invalidate();
		}
	}

	public static CustomDialog show(Context context, CharSequence message) {
		CustomDialog dialog = new CustomDialog(context, R.style.customDialog);
		dialog.setTitle("");
		dialog.setContentView(R.layout.custom_dialog);
		dialog.setCancelable(false);

		if (message == null || message.length() == 0) {
			dialog.findViewById(R.id.message).setVisibility(View.GONE);
		} else {
			TextView txt = (TextView) dialog.findViewById(R.id.message);
			txt.setText(message);
		}
		int DisplayWidth = context.getResources().getDisplayMetrics().widthPixels;
		int DisplayHeight = context.getResources().getDisplayMetrics().heightPixels;

		// Set your dialog width and height dynamically as per your screen.

		Window window = dialog.getWindow();
		window.setLayout(DisplayWidth, DisplayHeight);
		window.setGravity(Gravity.CENTER);
		dialog.show();
		return dialog;
	}
    public static CustomDialog show(Context context, CharSequence message, int w, int h) {
        CustomDialog dialog = new CustomDialog(context, R.style.customDialog);
        dialog.setTitle("");
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(false);

        if (message == null || message.length() == 0) {
            dialog.findViewById(R.id.message).setVisibility(View.GONE);
        } else {
            TextView txt = (TextView) dialog.findViewById(R.id.message);
            txt.setText(message);
        }
        int DisplayWidth = w;
        int DisplayHeight = h;

        // Set your dialog width and height dynamically as per your screen.

        Window window = dialog.getWindow();
        window.setLayout(DisplayWidth, DisplayHeight);
        window.setGravity(Gravity.CENTER);
        dialog.show();
        return dialog;
    }
}
