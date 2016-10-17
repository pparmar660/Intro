package com.intro.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.firebase.client.Firebase;
import com.intro.webservice.AuthService;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class IntroApplication extends MultiDexApplication{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "fGHxmC5xD6kDFpCZDPnSSlbVm";
    private static final String TWITTER_SECRET = "uEdCoES9PCe9bBZ9i5buvKplgQ68IXJLsSjwcs87Cdvjc7fXJU";

	@Override
	public void onCreate() {
		super.onCreate();
        try {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
            Fabric.with(this, new Twitter(authConfig));
        }catch(Exception e){
            e.printStackTrace();
        }
		Firebase.setAndroidContext(this);
	}

	private AuthService mAuthService;
	private Activity mCurrentActivity;

	public IntroApplication() {
	}

	public AuthService getAuthService() {
		if (mAuthService == null) {
			mAuthService = new AuthService(this);
		}
		return mAuthService;
	}
	
	public void setCurrentActivity(Activity activity) {
		mCurrentActivity = activity;
	}

	public Activity getCurrentActivity() {
		return mCurrentActivity;
	}

}
