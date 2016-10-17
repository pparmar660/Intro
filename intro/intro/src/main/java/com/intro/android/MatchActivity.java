package com.intro.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsee.Appsee;
import com.intro.customviews.CustomButton;
import com.intro.customviews.ImageViewRounded;
import com.intro.imageUtils.ImageLoader;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;


/**
 * This class is for going to chat and show images which are selected
 * 
 * By Ankit Kumar and Mirza Faisal
 */

public class MatchActivity extends BaseActivity {

	private TextView header, topTxt, middleTxt, wanttomeet;
	private ImageViewRounded rounded_image_1, rounded_image_2, rounded_image_3;
	private CustomButton send_btn, continue_btn;
    private RelativeLayout matchLayout;
	private Context context;
	private SharedPreferences userData;
	private ImageLoader imgL; 
	private String matchUserId, comeFrom;
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_ID_PERMISSIONS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_screen);
        Appsee.start(getString(R.string.appsee_apikey));
        context = this;
        matchLayout = (RelativeLayout) findViewById(R.id.matchLayout);
        header = (TextView) findViewById(R.id.match_header);
        topTxt = (TextView) findViewById(R.id.makeyourmove);
        middleTxt = (TextView) findViewById(R.id.hurry_tv);
        wanttomeet = (TextView) findViewById(R.id.whowanttomeet_tv);
        rounded_image_1 = (ImageViewRounded) findViewById(R.id.rounded_img_1);
        rounded_image_2 = (ImageViewRounded) findViewById(R.id.rounded_img_2);
        rounded_image_3 = (ImageViewRounded) findViewById(R.id.rounded_img_3);
        send_btn = (CustomButton) findViewById(R.id.send_btn);
        continue_btn = (CustomButton) findViewById(R.id.continue_btn);

        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        comeFrom = getIntent().getStringExtra(GlobalVariables.comeFrom);
        imgL = new ImageLoader(context);
        matchUserId = userData.getString("matchID", "");

        if(!Utility.hasPermissions(MatchActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(MatchActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
        }else {
            imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-2", rounded_image_2);
            imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-3", rounded_image_3);
            imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-1", rounded_image_1);
        }
        if(comeFrom.equalsIgnoreCase("breadcrumbBothmatch")){
            header.setVisibility(View.VISIBLE);
            matchLayout.setBackgroundColor(getResources().getColor(R.color.breadcrumb_like_color));
           // topTxt.setPadding(0, R.dimen.default_triple_padding, 0, 0);
            topTxt.setText(getString(R.string.breadcrumb_top_msg));
            middleTxt.setText(getString(R.string.breadcrumb_middle_msg));
            wanttomeet.setTextColor(getResources().getColor(R.color.white));
            wanttomeet.setText(userData.getString("matchName", "") + " "+ getString(R.string.like_you));
            send_btn.setText(getString(R.string.view_profile));
            send_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.navigate, 0,0,0);
        }else {
            header.setVisibility(View.GONE);
            matchLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.sunday_night_bg));
            topTxt.setText(getString(R.string.make_your_move));
            middleTxt.setText(getString(R.string.hurry));
            wanttomeet.setText(userData.getString("matchName", "") + " "+ getString(R.string.want_meet));
            send_btn.setText(getString(R.string.send_message));
            send_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.navigate, 0, 0, 0);
        }
		adlistener();
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode){
            case REQUEST_ID_PERMISSIONS:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-2", rounded_image_2);
                    imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-3", rounded_image_3);
                    imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-1", rounded_image_1);
//                }
                break;
        }
    }

	private void adlistener() {
		send_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                if(comeFrom.equalsIgnoreCase("breadcrumbBothmatch")) {
                    Intent intent = new Intent(context, MatchedUserProfileActivity.class);
                    intent.putExtra("matchUserId", matchUserId);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                }else{
                    Intent intent = new Intent(context, NewMatchesActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                    /*Intent next = new Intent(context, ProfileActivity.class);
                    next.putExtra("showMatchesMenu", true);
                    startActivity(next);
                    overridePendingTransition(0, R.anim.exit_slide_right);*/
                }
			}
		});
		continue_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                if (bothdropBreadcrumb){
                    Intent next = new Intent(context, QueueUserActivity.class);
                    next.putExtra("breadcrumbLike", true);
                    startActivity(next);
                }else{
                    Intent next = new Intent(context, QueueUserActivity.class);
                    next.putExtra("breadcrumbLike", false);
                    startActivity(next);
                }
				finish();
				overridePendingTransition(0, R.anim.exit_slide_right);
			}
		});
	}

}