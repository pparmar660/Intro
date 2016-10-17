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


public class CustomAlartDialog extends Dialog implements android.view.View.OnClickListener{
	public static TextView okBtn ;
	private String title, message, btnName = "Ok";
	Context context;
	public CustomAlartDialog(Context contxt, int theme) {
		super(contxt, theme);
		// context = contxt;
	}

	public CustomAlartDialog(Context contxt, String title, String message) {
		super(contxt);
		 this.context = contxt;
		 this.title = title;
		 this.message = message;
	}

    public CustomAlartDialog(Context contxt, String title, String message, String btnName) {
        super(contxt);
        this.context = contxt;
        this.title = title;
        this.message = message;
        this.btnName = btnName;
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
	    
	    setContentView(R.layout.custom_alart_dailog);
	    TextView title_txt = (TextView) findViewById(R.id.title_tv);
		title_txt.setText(title);
		TextView messagetxt = (TextView) findViewById(R.id.message_tv);
		messagetxt.setText(message);
	    okBtn = (TextView) findViewById(R.id.ok_btn_tv);
        okBtn.setText(btnName);
	    okBtn.setOnClickListener(this);
	    
	  }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok_btn_tv:
			dismiss();
			break;

		default:
			break;
		}
		
	}
}
