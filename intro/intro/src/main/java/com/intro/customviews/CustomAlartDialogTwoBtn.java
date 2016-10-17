package com.intro.customviews;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.intro.android.R;

/*
 * By Mirza 
 */

public class CustomAlartDialogTwoBtn extends Dialog implements android.view.View.OnClickListener{
	public static TextView cancelBtn, okBtn ;
	private String title, message, cancelTxt, okTxt;
	private Context context;
	
	public CustomAlartDialogTwoBtn(Context contxt, int theme) {
		super(contxt, theme);
	}

	public CustomAlartDialogTwoBtn(Context context, String title, String message, String cancelTxt, String okTxt) {
		super(context);
		 this.context = context;
		 this.title = title;
		 this.message = message;
		 this.cancelTxt = cancelTxt;
		 this.okTxt = okTxt;
	}

	public void setMessage(CharSequence message) {
		if (message != null && message.length() > 0) {

			TextView txt = (TextView) findViewById(R.id.message_tv);
			txt.setText(message);
			txt.invalidate();
		}
	}
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
	    setContentView(R.layout.custom_alart_dialogtwobutton);
	    TextView title_txt = (TextView) findViewById(R.id.title_tv);
		title_txt.setText(title);
		TextView messagetxt = (TextView) findViewById(R.id.message_tv);
		messagetxt.setText(message);
		cancelBtn = (TextView) findViewById(R.id.cancel_btn_tv);
		cancelBtn.setText(cancelTxt);
	    cancelBtn.setOnClickListener(this);
	    okBtn = (TextView) findViewById(R.id.ok_btn_tv);
	    okBtn.setText(okTxt);
	    okBtn.setOnClickListener(this);
	    
	  }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel_btn_tv:
			dismiss();
			break;
		default:
			break;
		}
		
	}
}
