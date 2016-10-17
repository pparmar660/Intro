package com.intro.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.google.gson.JsonElement;
import com.intro.adapter.MatchesAdapter;
import com.intro.customviews.CustomAlartDialog;
import com.intro.utils.GPSTracker;
import com.intro.utils.GlobalVariables;
import com.intro.webservice.AuthService;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

/**
 * This class is for the
 * By Ankit Kumar and Mirza Faisal
 */


public class TimerService extends Service {

    public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 seconds
    public static final int NOTIFICATION_ID = 1;
    private Context context;
    private NotificationManager mNotificationManager;
    private Handler mHandler = new Handler();
    public static Timer mTimer = null;
    private Date date = null;
    private int i = 0;
    private double lat, log;
    private SharedPreferences userData;
    private long diffMinutes, diffHours;
    public static boolean isRunningService = false;
    private CustomAlartDialog alart1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return START_STICKY;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate() {
        context = getApplicationContext();
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        try {
            if (!isAppIsInBackground(context)) {
                if (!userData.getString("expires2", "").equalsIgnoreCase("null")
                        && userData.getString("expires2", "") != null
                        && !userData.getString("expires2", "").contains("-")) {
                    ProfileActivity.eventTime.setVisibility(View.VISIBLE);
                } else {
                    ProfileActivity.eventTime.setText("");
                    ProfileActivity.eventTime.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH 'hrs' mm 'min'");
            format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            date = format.parse("1970-01-01 " + userData.getString("expires2", ""));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            mTimer = new Timer();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public TimerService getServerInstance() {
            return TimerService.this;
        }
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        isRunningService = true;
                        BaseActivity.baseTime = date.getTime() - (i * 10000);
                        System.out.println("time" + BaseActivity.baseTime);
                        i++;
                        diffMinutes = BaseActivity.baseTime / (60 * 1000) % 60;
                        diffHours = BaseActivity.baseTime / (60 * 60 * 1000) % 24;
                        if (!isAppIsInBackground(context)) {
                            ProfileActivity.eventTime.setVisibility(View.VISIBLE);
                            ProfileActivity.eventTime.setText(diffHours + " hrs " + (diffMinutes + 1) + " min");
                        }
                        System.out.println("time service = " + BaseActivity.baseTime);
                        if (BaseActivity.baseTime == (310 * 1000)) {
                            if (!isAppIsInBackground(context)) {
                                alart1 = new CustomAlartDialog(getApplicationContext(),
                                        GlobalVariables.intro, GlobalVariables.event_timer_alart);
                                alart1.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                alart1.show();
                            } else {
                                scheduleNotification(getNotification(GlobalVariables.event_timer_alart));
                            }
                        }

                        if (BaseActivity.baseTime == 0) {
                            if (!isAppIsInBackground(context)) {
                                ProfileActivity.eventTime.setVisibility(View.GONE);
                                ProfileActivity.addTimeImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.breadcrumb));
                            }
                            dropBreadCrumb();
                            clearEvent();
                            System.out.println("Timeout");

                            if (MatchesAdapter.timer != null) {
                                MatchesAdapter.timer.cancel();
                            }
                            isRunningService = false;
                            stopService(new Intent(TimerService.this, TimerService.class));
                            if (!isAppIsInBackground(context)) {
                                final CustomAlartDialog alart = new CustomAlartDialog(getApplicationContext(),
                                        GlobalVariables.intro, GlobalVariables.event_expired);
                                if (Build.VERSION.SDK_INT >= 23) {
                                    alart.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                                } else {
                                    alart.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                }
                                alart.show();
                                alart.okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alart.dismiss();
                                        SharedPreferences settings = context.getSharedPreferences("UserData", 0);
                                        SharedPreferences.Editor preferencesEditor = settings.edit();
                                        preferencesEditor.putString("expires", "");
                                        preferencesEditor.putString("expires2", "");
                                        preferencesEditor.commit();
                                        if (!GlobalVariables.profileView) {
                                            Intent intent = new Intent(context, ProfileActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            ProfileActivity.eventTime.setVisibility(View.GONE);
                                            if (MatchesAdapter.timer != null) {
                                                MatchesAdapter.timer.cancel();
                                            }
                                        }
                                    }
                                });
                            } else {
                                scheduleNotification(getNotification(GlobalVariables.event_expired));
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });
        }

    }

    @Override
    public boolean stopService(Intent name) {
        System.out.println("Stop Service");
        isRunningService = false;
        mTimer.cancel();
        mTimer = null;
        if (!isAppIsInBackground(context)) {
            ProfileActivity.eventTime.setVisibility(View.GONE);
        }
        return super.stopService(name);
    }

    private void scheduleNotification(Notification notification) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        int num = (int) System.currentTimeMillis();
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), num, new Intent(getApplicationContext(),
                ProfileActivity.class), 0);
        RemoteViews mContentView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        mContentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
        mContentView.setTextViewText(R.id.title, GlobalVariables.intro);
        mContentView.setTextViewText(R.id.text, content);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext()).setSmallIcon(R.drawable.ic_launcher)
                .setContent(mContentView).setDefaults(Notification.DEFAULT_VIBRATE).setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX).setVibrate(new long[]{1, 1, 1});
        mBuilder.setContentIntent(contentIntent);
        return mBuilder.build();
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

    public void dropBreadCrumb() {
        AuthService mAuthService = new AuthService(context);
        GPSTracker gpsTracker = new GPSTracker(context);
        lat = gpsTracker.getLatitude();
        log = gpsTracker.getLongitude();
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
        parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));
        mAuthService.dropBreadcrumb(parameters, new ApiJsonOperationCallback() {

            @Override
            public void onCompleted(JsonElement jsonElement,
                                    Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    System.out.println("DropBreadcrumbResult = " + jsonElement.toString());
                } else {
                    System.out.println("DropBreadcrumbException = " + exception.toString());
                }
            }
        });
    }

    public void clearEvent() {
        AuthService mAuthService = new AuthService(context);
        GPSTracker gpsTracker = new GPSTracker(context);
        lat = gpsTracker.getLatitude();
        log = gpsTracker.getLongitude();
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userID", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
        parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));
        mAuthService.clearEvent(parameters, new ApiJsonOperationCallback() {

            @Override
            public void onCompleted(JsonElement jsonElement,
                                    Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    System.out.println("ClearEventResult = " + jsonElement.toString());
                } else {
                    System.out.println("ClearEventException = " + exception.toString());
                }
            }
        });
    }

}