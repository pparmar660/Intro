package com.intro.android;

import java.util.Locale;
import java.util.Random;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends IntentService {

    //    int badgeno;
    private final IBinder mBinder = new MyBinder();

    public NotificationService() {
        super("NotificationService");
    }

    public class MyBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        Log.d("CHECK", "NotificationService");

        // The manufacturer of the product/hardware. 

        String manufactureStr = Build.MANUFACTURER;

        Log.d("CHECK", "manufacture : " + manufactureStr);

        //badgeno = MyHandler.NOTIFICATION_COUNT;
        //new Random().nextInt(100);
        int badgeno = intent.getIntExtra("count",0);

        Log.d("CHECK", "badgeno : " + badgeno);

        if (manufactureStr != null) {

            boolean bool2 = manufactureStr.toLowerCase(Locale.US).contains("htc");
            boolean bool3 = manufactureStr.toLowerCase(Locale.US).contains("sony");
            boolean bool4 = manufactureStr.toLowerCase(Locale.US).contains("samsung");

            // Sony Ericssion
            if (bool3) {
                try {
                    intent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
                    intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", "com.intro.android.MyHandler");
                    intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", true);
                    intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", badgeno);
                    intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", "com.intro.android");

                    sendBroadcast(intent);
                } catch (Exception localException) {
                    Log.e("CHECK", "Sony : " + localException.getLocalizedMessage());
                }
            }

            // HTC
            if (bool2) {
                try {
                    Intent localIntent1 = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
                    localIntent1.putExtra("packagename", "com.intro.android");
                    localIntent1.putExtra("count", badgeno);
                    sendBroadcast(localIntent1);

                    Intent localIntent2 = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
                    ComponentName localComponentName = new ComponentName(this, "com.intro.android.MyHandler");
                    localIntent2.putExtra("com.htc.launcher.extra.COMPONENT", localComponentName.flattenToShortString());
                    localIntent2.putExtra("com.htc.launcher.extra.COUNT", 10);
                    sendBroadcast(localIntent2);
                } catch (Exception localException) {
                    Log.e("CHECK", "HTC : " + localException.getLocalizedMessage());
                }
            }
            if (bool4) {
                // Samsung
                try {
                    ContentResolver localContentResolver = getContentResolver();
                    Uri localUri = Uri.parse("content://com.sec.badge/apps");
                    ContentValues localContentValues = new ContentValues();
                    localContentValues.put("package", "com.intro.android");
                    localContentValues.put("class", "com.intro.android.MyHandler");
                    localContentValues.put("badgecount", Integer.valueOf(badgeno));
                    String str = "package=? AND class=?";
                    String[] arrayOfString = new String[2];
                    arrayOfString[0] = "com.intro.android";
                    arrayOfString[1] = "com.intro.android.MyHandler";

                    int update = localContentResolver.update(localUri, localContentValues, str, arrayOfString);

                    if (update == 0) {
                        localContentResolver.insert(localUri, localContentValues);
                    }

                } catch (IllegalArgumentException localIllegalArgumentException) {
                    Log.e("CHECK", "Samsung1F : " + localIllegalArgumentException.getLocalizedMessage());
                } catch (Exception localException) {
                    Log.e("CHECK", "Samsung : " + localException.getLocalizedMessage());
                }
            }
        }
    }
}
