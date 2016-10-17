package com.intro.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.appsee.Appsee;
import com.intro.customviews.CustomAlartDialog;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;

import org.jsoup.Jsoup;

/**
 * This class is to display a splash
 * By Ankit Kumar
 */
public class SplashActivity extends BaseActivity {

	private Context context;
	Handler handler = new Handler();
	private SharedPreferences userData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
        Appsee.start(getString(R.string.appsee_apikey));
		context = this;
		userData = getSharedPreferences("UserData", MODE_PRIVATE);
      //  startService(new Intent(this, NotificationService.class));
	}

	@Override
	protected void onResume() {
		super.onResume();
		Runnable r = new Runnable() {
			public void run() {
                checkVersion();
				if (userData.getString("userid", "").equalsIgnoreCase("")
						|| userData.getString("userid", "").equalsIgnoreCase("null")) {
					Intent intent = new Intent(context, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					overridePendingTransition(0, R.anim.exit_slide_right);
					mAuthService.logout(false, context);
					finish();
				} else if (userData.getString("age", "") == null|| userData.getString("age", "").equalsIgnoreCase("null")
						|| userData.getString("age", "").equalsIgnoreCase("")) {
					Intent intent = new Intent(context,SetProfileActivity.class);
                    intent.putExtra(GlobalVariables.comeFrom, "Splash");
                    intent.putExtra("cancel", true);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					overridePendingTransition(0, R.anim.exit_slide_right);
					finish();
				} else {
					Intent intent = new Intent(context, ProfileActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					overridePendingTransition(0, R.anim.exit_slide_right);
					finish();
				}
			}
		};
		handler.postDelayed(r, 3000);
	}

    private void checkVersion() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String new_version = Jsoup.connect("https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=it")
                            .timeout(30000)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com")
                            .get()
                            .select("div[itemprop=softwareVersion]")
                            .first()
                            .ownText();
                    threadMsg(new_version);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void threadMsg(String msg) {
                if (!msg.equals(null) && !msg.equals("")) {
                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", msg);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                }
            }

            private final Handler handler = new Handler() {

                public void handleMessage(Message msg) {
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        String currentVersion = pInfo.versionName;

                        String appVersion = msg.getData().getString("message");
                        if ((null != appVersion)) {
                            if (!currentVersion.equalsIgnoreCase(appVersion)) {
                                alart = new CustomAlartDialog(context, GlobalVariables.intro, getString(R.string.new_version_msg));
                                alart.show();
                                CustomAlartDialog.okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alart.dismiss();
                                        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                        try {
                                            startActivity(goToMarket);
                                            overridePendingTransition(0, R.anim.exit_slide_right);
                                        } catch (ActivityNotFoundException e) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Utility.validationByToastMessage(context, "Could not open Android market, please install the market app.");
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                       // e.printStackTrace();
                    }
                }
            };

        });
        thread.start();
    }
}
