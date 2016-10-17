package com.intro.webservice;

import com.appsee.Appsee;
import com.intro.android.IntroApplication;
import com.intro.android.ProfileActivity;
import com.intro.android.R;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomAlartDialogTwoBtn;
import com.intro.customviews.CustomDialog;
import com.intro.utils.GPSTracker;
import com.intro.utils.GlobalVariables;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.widget.RemoteViews;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseActivity extends Activity {

	protected AuthService mAuthService;
	protected GPSTracker gpsTracker;
    protected Location gpsLocation;
    protected CustomDialog progressDia;
    protected CustomAlartDialog alart;
	protected CustomAlartDialogTwoBtn alartTwoBtn;
	public static long baseTime;
    public static boolean bothdropBreadcrumb;
    protected Boolean updateMyData =true;

	public static int[] maleImages = new int[]{R.drawable.bond, R.drawable.funnyface, R.drawable.jackskelington, R.drawable.joker,
    R.drawable.mario, R.drawable.pimpinaintsasy, R.drawable.scooby, R.drawable.smokingmickey, R.drawable.spock, R.drawable.stormtrooper,
    R.drawable.techwarrior, R.drawable.uglyface, R.drawable.waldo};

    public static int[] femaleImages = new int[]{R.drawable.brunette, R.drawable.cyclopsbuddha, R.drawable.fairy, R.drawable.medusa,
    R.drawable.owl, R.drawable.scream, R.drawable.supergirl};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		IntroApplication myApp = (IntroApplication) getApplication();
		myApp.setCurrentActivity(this);
		mAuthService = myApp.getAuthService();

	}

    public static int getRandomMale(){
        int rand = new Random().nextInt(maleImages.length);
        return rand;
    }

    public static int getRandomFemale(){
        int rand = new Random().nextInt(femaleImages.length);
        return rand;
    }

    public static boolean isAppInForeground(Context context) {
        List<ActivityManager.RunningTaskInfo> tasks =
                ((ActivityManager) context.getSystemService(
                        Context.ACTIVITY_SERVICE))
                        .getRunningTasks(1);
        if (tasks.isEmpty()) {
            return false;
        }
        return tasks
                .get(0)
                .topActivity
                .getPackageName()
                .equalsIgnoreCase(context.getPackageName());
    }

    public static String encodeUTF(String s) {
        String data = "";
        try {
            byte[] bytes = s.getBytes("UTF-8");
            data = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String decodeUTF(String s) {
        String data = "";
        try {
            if(s.contains("\n")){
                s = s.replaceAll("\n", "");
            }
            if(checkForEncode(s)) {
                byte[] bytes = Base64.decode(s, Base64.DEFAULT);
                data = new String(bytes, "UTF-8");
            }else{
                data = s;
            }
           // data = URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            data = s;
        }
        if(data.contains("''")){
            data = data.replaceAll("''", "'");
        }
        return data;
    }

    public static boolean checkForEncode(String string) {
        String pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)";//"^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(string);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }
}
