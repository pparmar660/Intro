package com.intro.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.intro.adapter.MatchesAdapter;
import com.intro.customviews.CustomAlartDialog;
import com.intro.utils.GlobalVariables;
import com.intro.webservice.AuthService;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is NotificationsHandler class it manages all notification comes from server.
 * By Mirza Faisal
 */


public class MyHandler extends NotificationsHandler {

    public static final int NOTIFICATION_ID = 1;
    public static int NOTIFICATION_COUNT = 0;
    private NotificationManager mNotificationManager;
    private Context ctx;
    static public ProfileActivity mainActivity;
    private String alartData, userID, userName, senderid = "", type, messageCount;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        NOTIFICATION_COUNT = NOTIFICATION_COUNT + 1;
        try {
            String nhMessage = bundle.getString("message");
            JSONObject jsonObject = new JSONObject(nhMessage);
            JSONObject jsonObject2 = jsonObject.getJSONObject("aps");
            alartData = jsonObject2.getString("alert");
            type = jsonObject.getString("type");
            System.out.println("Bundle : " + bundle.toString());
            System.out.println("responseMyhandler" + nhMessage);

            SharedPreferences settings = context
                    .getSharedPreferences("UserData", 0);
            SharedPreferences.Editor preferencesEditor = settings.edit();

            switch (type) {
                case "message":
                    senderid = jsonObject.getString("senderid");
                    messageCount = jsonObject2.getString("badge");
                    if (settings.getString("userid", "").equalsIgnoreCase(jsonObject.getString("ReciverId"))) {
                        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                        parameters.add(new Pair<String, String>("ReciverId", settings.getString("userid", "")));
                        parameters.add(new Pair<String, String>("SenderId", senderid));
                        AuthService mAuthService = new AuthService(ctx);
                        mAuthService.clearConversation(parameters, new ApiJsonOperationCallback() {
                            @Override
                            public void onCompleted(JsonElement jsonElement, Exception e, ServiceFilterResponse serviceFilterResponse) {
                                System.out.println("Clear :" + jsonElement.toString());
                            }
                        });
                        System.out.print("ChatActivity : " + GlobalVariables.chatActivity);
                        System.out.print("ChatID : " + GlobalVariables.ChatId);
                        if (!GlobalVariables.chatActivity.equalsIgnoreCase(GlobalVariables.ChatResume)) {
                            if (!GlobalVariables.ChatId.equalsIgnoreCase(senderid)) {
                                if (!settings.getString("senderid", "").contains(senderid)) {
                                    preferencesEditor.putString("senderid", settings.getString("senderid", "") + "," + senderid);
                                    preferencesEditor.putString("messageCount", settings.getString("badge", "") + "," + messageCount);
                                }
                                sendMessageNotification(alartData);
                                try {
                                    ((NewMatchesActivity)context).matchAdapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    break;
                case "matchNotification":
                    sendMessageNotification(alartData);
                    break;
                case "match":
                    userID = jsonObject.getString("userID");
                    userName = jsonObject.getString("userName");
                    preferencesEditor.putString("matchName", userName);
                    preferencesEditor.putString("matchID", userID);
                    preferencesEditor.putString("senderid", senderid);
                    if (GlobalVariables.queueActivity.equalsIgnoreCase(GlobalVariables.Resume)) {
                        Intent intent = new Intent(ctx, MatchActivity.class);
                        intent.putExtra(GlobalVariables.comeFrom, "match");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    } else {
                        sendNotification(alartData, "match");
                    }
                    break;
                case "breadcrumb":
                    preferencesEditor.putBoolean("dropBreadcrumb", true);
                    sendMessageNotification(alartData);
                    if (!isAppIsInBackground(context)) {
                        if (TimerService.mTimer == null) {
                            ProfileActivity.addTimeImageView.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.bread_new));
                        }
                    }
                    break;
                case "breadcrumbBothmatch":
                    BaseActivity.bothdropBreadcrumb = true;
                    userID = jsonObject.getString("userID");
                    userName = jsonObject.getString("userName");
                    preferencesEditor.putString("matchName", userName);
                    preferencesEditor.putString("matchID", userID);
                    preferencesEditor.putString("senderid", senderid);
                    if (GlobalVariables.queueActivity.equalsIgnoreCase(GlobalVariables.Resume)) {
                        preferencesEditor.apply();
                        Intent intent = new Intent(ctx, MatchActivity.class);
                        intent.putExtra(GlobalVariables.comeFrom, "breadcrumbBothmatch");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    } else {
                        sendNotification(alartData, "breadcrumbBothmatch");
                    }
                    break;
                case "favourite":
                    senderid = jsonObject.getString("userID");
                    sendMessageNotification(alartData);
                    break;
                case "favourite_both":
                    sendMessageNotification(alartData);
                    break;
            }

            preferencesEditor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/

    private void sendNotification(String msg, String comeFrom) {
        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        int num = (int) System.currentTimeMillis();
        Intent intent = new Intent(ctx, MatchActivity.class);
        intent.putExtra(GlobalVariables.comeFrom, comeFrom);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, num, intent, 0);
        RemoteViews mContentView = new RemoteViews(ctx.getPackageName(), R.layout.notification_layout);
        mContentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
        mContentView.setTextViewText(R.id.title, GlobalVariables.intro);
        mContentView.setTextViewText(R.id.text, msg);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx).setSmallIcon(getNotificationIcon())
                .setContent(mContentView).setDefaults(Notification.DEFAULT_VIBRATE).setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(new long[]{1, 1, 1});
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void sendMessageNotification(String msg) {
        mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        int num = (int) System.currentTimeMillis();
        Intent intent = new Intent(ctx, ProfileActivity.class);
        intent.putExtra("notification", NOTIFICATION_COUNT);
        intent.putExtra("notificationType", type);
        intent.putExtra("notificationData", msg);
        intent.putExtra("senderId", senderid);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, num, intent, 0);
        RemoteViews mContentView = new RemoteViews(ctx.getPackageName(), R.layout.notification_layout);
        mContentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
        mContentView.setTextViewText(R.id.title, GlobalVariables.intro);
        mContentView.setTextViewText(R.id.text, msg);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx).setSmallIcon(getNotificationIcon())
                .setContent(mContentView).setDefaults(Notification.DEFAULT_VIBRATE).setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(new long[]{1, 1, 1});
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private int getNotificationIcon() {
        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return whiteIcon ? R.drawable.ic_launcher : R.drawable.ic_launcher;
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

}
