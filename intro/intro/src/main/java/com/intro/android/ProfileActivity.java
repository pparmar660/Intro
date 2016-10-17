package com.intro.android;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.appsee.Appsee;
import com.facebook.android.Util;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonElement;
import com.intro.adapter.MatchesAdapter;
import com.intro.adapter.ProfileImageViewPagerAdapter;
import com.intro.adapter.SlidingMenuAdapter;
import com.intro.customviews.AViewFlipper;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomAlartDialogTwoBtn;
import com.intro.customviews.CustomDialog;
import com.intro.imageUtils.ImageLoader;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GPSTracker;
import com.intro.utils.GlobalVariables;
import com.intro.utils.SlidingMenuUtils;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.notifications.NotificationsManager;


/**
 * This class is for User profile with both side menu
 * <p/>
 * go intro send user to GetReadyActivity to search its near matched users. Add
 * time button add half hour to the event of user.
 * <p/>
 * By Ankit Kumar and Mirza Faisal
 */

@SuppressWarnings("deprecation")
@SuppressLint({"SimpleDateFormat", "InflateParams"})
public class ProfileActivity extends BaseActivity implements OnClickListener {

    private double lat, log;
    public static Context context;
    private Drawable resDrawable;
    private ConnectionDetector cd;
    private LayoutInflater inflater;
    private SharedPreferences userData, toolTipPreferences;
    //    public static MatchesAdapter matchAdapter;
    private SlidingMenuAdapter menuAdapter;
    private Boolean showMatchesMenu = false;
    public static RelativeLayout goIntroRelative, breadcrumbRelative;
    public static ImageView goIntroImageView, addTimeImageView, bothBreadcrumbImageView;
    public static SlidingMenu slidingMenuLeft, slidingMenuRight;
    private ArrayList<String> imageURL = new ArrayList<String>();
    public static ListView sideMenuListviewLeft, sideMenuListviewRight;
    public static ImageView toolbarMenuImageView, toolbarMatchesImageView;
    private TextView headerTextview, personName, personAge, personDescription;
    public static TextView eventTime, okayLeft, decription, okay, okayBothBreadcrumb;
    public static String until = null;
    public static MobileServiceClient mClient;
    public static JSONArray nearMatchArray;
    public static ArrayList<JSONObject> matchesList = new ArrayList<>();
    public static SwipeRefreshLayout mSwipeRefreshLayout = null;
    public NotificationService service;
    private GoogleCloudMessaging gcm;
    private NotificationHub hub;
    private AViewFlipper viewFlipper;
    private GestureDetector mGestureDetector;
    private ImageLoader imgL;
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_ID_PERMISSIONS = 1;
    private RelativeLayout mainLayout, blackCover, breadcrumbLayout, goIntroLayout;
    private View viewVertical, viewVertical2, viewHorizontal;

    private static Boolean isVisible = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            service = ((NotificationService.MyBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_profile);
        Appsee.start(getString(R.string.appsee_apikey));
        viewFlipper = (AViewFlipper) findViewById(R.id.viewFlipper);
        goIntroImageView = (ImageView) findViewById(R.id.goIntroImageView);
        addTimeImageView = (ImageView) findViewById(R.id.addTimeImageView);
        personName = (TextView) findViewById(R.id.personNameTextView);
        personAge = (TextView) findViewById(R.id.personAgeTextView);
        personDescription = (TextView) findViewById(R.id.personDescriptionTextView);
        eventTime = (TextView) findViewById(R.id.eventTimeTextView);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        blackCover = (RelativeLayout) findViewById(R.id.blackCover);
        goIntroRelative = (RelativeLayout) findViewById(R.id.rl_goIntro);
        breadcrumbRelative = (RelativeLayout) findViewById(R.id.rl_breadcrumb);

        breadcrumbLayout = (RelativeLayout) findViewById(R.id.breadcrumbLayout);
        goIntroLayout = (RelativeLayout) findViewById(R.id.goIntroLayout);
        okayLeft = (TextView) findViewById(R.id.okayLeft);
        decription = (TextView) findViewById(R.id.decription);
        okay = (TextView) findViewById(R.id.okay);
        okayBothBreadcrumb = (TextView) findViewById(R.id.okay_both_breadcrumb);
        bothBreadcrumbImageView = (ImageView) findViewById(R.id.tooltip_both_breadcrumb_img);
        viewVertical = (View) findViewById(R.id.view_line_vertical);
        viewVertical2 = (View) findViewById(R.id.view_line_vertical2);
        viewHorizontal = (View) findViewById(R.id.view_line_horizontal);

        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        toolTipPreferences = getSharedPreferences("toolTipPreferences", MODE_PRIVATE);
        cd = new ConnectionDetector(context);
        imgL = new ImageLoader(context);

        init();
        if (!Utility.hasPermissions(ProfileActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(ProfileActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
        } else {
            setData();
        }

        if (getIntent().hasExtra("notificationType")) {
            String notificationType = "", notificationMessage = "", favUserId = "";
            notificationType = getIntent().getStringExtra("notificationType");
            if (notificationType.equalsIgnoreCase("favourite")) {
                notificationMessage = getIntent().getStringExtra("notificationData");
                favUserId = getIntent().getStringExtra("senderId");
                showFavDialog(favUserId, notificationMessage);
            }
        }


        if (toolTipPreferences.getBoolean(GlobalVariables.first_time_profile, true)) {
            enableDisableView(mainLayout, false);
            blackCover.setVisibility(View.VISIBLE);
        } else {
            blackCover.setVisibility(View.GONE);
        }

        okay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                okayLeft.setVisibility(View.VISIBLE);
                breadcrumbLayout.setVisibility(View.VISIBLE);
                okay.setVisibility(View.INVISIBLE);
                goIntroLayout.setVisibility(View.INVISIBLE);
                decription.setText(R.string.description_active_profile);
            }
        });
        okayBothBreadcrumb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                blackCover.setVisibility(View.GONE);
                enableDisableView(mainLayout, true);
                SharedPreferences.Editor editor = toolTipPreferences.edit();
                editor.putBoolean(GlobalVariables.first_time_profile_both_breadcrumb, false);
                editor.commit();
            }
        });

        okayLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                blackCover.setVisibility(View.GONE);
                enableDisableView(mainLayout, true);
                SharedPreferences.Editor editor = toolTipPreferences.edit();
                editor.putBoolean(GlobalVariables.first_time_profile, false);
                editor.commit();
            }
        });

      /*  mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });*/
    }

    public void showFavDialog(final String favUserId, String message) {
        final CustomAlartDialogTwoBtn alart = new CustomAlartDialogTwoBtn(context, GlobalVariables.intro,
                message, "Ok", "Favorite");
        alart.show();
        CustomAlartDialogTwoBtn.okBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                alart.dismiss();
                addFav(favUserId);
            }
        });
    }

    public void setData() {
        imageURL.add(userData.getString("userid", "") + "-1");
        imageURL.add(userData.getString("userid", "") + "-2");
        imageURL.add(userData.getString("userid", "") + "-3");
        for (int i = 0; i < imageURL.size(); i++) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.MATRIX);

            imgL.DisplayImage(GlobalVariables.Image_URL + imageURL.get(i), imageView);
            viewFlipper.addView(imageView);
        }
        try {
            CustomGestureDetector customGestureDetector = new CustomGestureDetector();
            mGestureDetector = new GestureDetector(this, customGestureDetector);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userData.getBoolean("dropBreadcrumb", false)) {
            addTimeImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.breadcrumb_new));
            if (toolTipPreferences.getBoolean(GlobalVariables.first_time_profile_both_breadcrumb, true)) {
                enableDisableView(mainLayout, false);
                blackCover.setVisibility(View.VISIBLE);
                okayLeft.setVisibility(View.INVISIBLE);
                breadcrumbLayout.setVisibility(View.VISIBLE);
                okay.setVisibility(View.INVISIBLE);
                okayBothBreadcrumb.setVisibility(View.VISIBLE);
                goIntroLayout.setVisibility(View.INVISIBLE);
                viewVertical.setVisibility(View.VISIBLE);
                viewVertical2.setVisibility(View.VISIBLE);
                viewHorizontal.setVisibility(View.VISIBLE);
                decription.setText(R.string.profile_both_breadcrumb_tooltip_txt);
                bothBreadcrumbImageView.setBackground(getResources().getDrawable(R.drawable.breadcrumb_new));
            } else {
                blackCover.setVisibility(View.GONE);
            }
        }
        try {
            mClient = new MobileServiceClient(GlobalVariables.ServerUrl, GlobalVariables.key, this);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        gpsTracker = new GPSTracker(context);
        lat = gpsTracker.getLatitude();
        log = gpsTracker.getLongitude();

        MyHandler.mainActivity = this;
        gcm = GoogleCloudMessaging.getInstance(this);
        hub = new NotificationHub(GlobalVariables.HubName, GlobalVariables.HubListenConnectionString, this);
        NotificationsManager.handleNotifications(this, GlobalVariables.SENDER_ID, MyHandler.class);
        registerWithNotificationHubs();
        updateDeviceId();
        /*if (baseTime != 0) {
            getNearmatches();
        }*/

        try {
            showMatchesMenu = getIntent().getBooleanExtra("showMatchesMenu", false);
            if (showMatchesMenu) {
                slidingMenuRight.toggle();
            }
        } catch (Exception e) {
            showMatchesMenu = false;
            e.printStackTrace();
        }
        until = userData.getString("expires2", "");
        personName.setText(userData.getString("name", ""));
        personAge.setText(" | " + userData.getString("age", ""));
        personDescription.setText(userData.getString("bio", ""));

        checkExpire();
        adlistsner();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ID_PERMISSIONS:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setData();
//                }
                break;
        }

    }

    public void checkExpire() {
        String expire = userData.getString("expires2", "");
        if (!expire.equalsIgnoreCase("") && !expire.equalsIgnoreCase("null")
                && !expire.contains("-") && !expire.equalsIgnoreCase("0 hrs 0 min")) {
            eventTime.setVisibility(View.VISIBLE);
            eventTime.setText("Loading...");
        } else {
            eventTime.setVisibility(View.GONE);
        }
    }

  /*  public void getNearmatches() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
        parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));

        mAuthService.getNearMatches(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement,
                                    Exception exception, ServiceFilterResponse response) {
                matchesList.clear();
                if (jsonElement != null && !jsonElement.toString().equalsIgnoreCase("null")) {
                    try {
                        System.out.println("NearMatchesResult = " + jsonElement.toString());
                        nearMatchArray = new JSONArray(jsonElement.toString());
                        for (int i = 0; i < nearMatchArray.length(); i++) {
                            matchesList.add(nearMatchArray.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mSwipeRefreshLayout.setRefreshing(false);
                matchAdapter = new MatchesAdapter(context, matchesList);
                matchAdapter.notifyDataSetChanged();
                sideMenuListviewRight.setAdapter(matchAdapter);
            }
        });
    }*/

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerWithNotificationHubs() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    hub.unregister();
                    String regid = gcm.register(GlobalVariables.SENDER_ID);
                    String[] tag = {userData.getString("userid", "").replaceAll("-", "")};
                    String id = hub.register(regid, tag).getRegistrationId();
                    System.out.println("Registrationid : " + id);
                    System.out.println("GCM : " + regid);
                } catch (Exception e) {
                    System.out.println("Exception : " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gpsTracker = new GPSTracker(context);
        lat = gpsTracker.getLatitude();
        log = gpsTracker.getLongitude();
        menuAdapter = new SlidingMenuAdapter(this);
        sideMenuListviewLeft.setAdapter(menuAdapter);
        GlobalVariables.profileView = true;
        checkExpire();
        getUserData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalVariables.profileView = false;
    }

    private void getUserData() {

        if (cd.isConnectingToInternet()) {
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));

            mAuthService.getUserData(parameters,
                    new ApiJsonOperationCallback() {

                        @Override
                        public void onCompleted(final JsonElement jsonElement,
                                                Exception exception,
                                                ServiceFilterResponse response) {
                            if (jsonElement != null) {
                                System.out.println("ProfileResult = " + jsonElement.toString());
                                if (jsonElement.toString() == null || jsonElement.toString().equalsIgnoreCase("null")) {
                                    mAuthService.logout(true, context);
                                }
                                JSONArray jsonArray;
                                JSONObject jsonObject;
                                try {
                                    jsonArray = new JSONArray(jsonElement.toString());
                                    jsonObject = jsonArray.getJSONObject(0);
                                    System.out.println("Images----> " + jsonObject.getString("__updatedAt") + "-----" + userData.getString("updatedAt", ""));
//                                    if (!jsonObject.getString("__updatedAt").equalsIgnoreCase(userData.getString("updatedAt", ""))) {
//                                        imgL.ClearImage();
//                                    }
                                    SharedPreferences.Editor editor = userData.edit();
                                    editor.putString("updatedAt", jsonObject.getString("__updatedAt"));
                                    editor.putString("expires2", jsonObject.getString("expires2"));
                                    editor.commit();

                                    //mAuthService.saveUserData(jsonObject, "","", true, context, false);
                                    // "" is birthday and accesstoken only provided in fb login case for loginActivity parameter

                                    personName.setText(userData.getString("name", ""));
                                    personAge.setText(" | " + userData.getString("age", ""));
                                    personDescription.setText(decodeUTF(jsonObject.getString("bio")));
//                                    personDescription.setText(userData.getString("bio", ""));

                                    until = jsonObject.getString("expires2");
                                    System.out.println("Until = " + until);
                                    Intent intent = new Intent(ProfileActivity.this, TimerService.class);
                                    if (!until.equalsIgnoreCase("") && !until.equalsIgnoreCase("null")
                                            && !until.contains("-") && !until.equalsIgnoreCase("0 hrs 0 min")) {
                                        eventTime.setVisibility(View.VISIBLE);
                                        addTimeImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.add_time_new));
                                        if (TimerService.isRunningService) {
                                            if (TimerService.mTimer != null)
                                                TimerService.isRunningService = false;
                                            TimerService.mTimer.cancel();
                                            TimerService.mTimer = null;
                                            baseTime = 0;
                                            stopService(intent);
                                        }
                                        startService(intent);

                                    } else {
                                        if (userData.getBoolean("dropBreadcrumb", false)) {
                                            addTimeImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bread_new));
                                            if (toolTipPreferences.getBoolean(GlobalVariables.first_time_profile_both_breadcrumb, true)) {
                                                enableDisableView(mainLayout, false);
                                                blackCover.setVisibility(View.VISIBLE);
                                                okayLeft.setVisibility(View.INVISIBLE);
                                                breadcrumbLayout.setVisibility(View.VISIBLE);
                                                okay.setVisibility(View.INVISIBLE);
                                                okayBothBreadcrumb.setVisibility(View.VISIBLE);
                                                goIntroLayout.setVisibility(View.INVISIBLE);
                                                viewVertical.setVisibility(View.VISIBLE);
                                                viewVertical2.setVisibility(View.VISIBLE);
                                                viewHorizontal.setVisibility(View.VISIBLE);
                                                decription.setText(R.string.profile_both_breadcrumb_tooltip_txt);
                                                bothBreadcrumbImageView.setBackground(getResources().getDrawable(R.drawable.bread_new));
                                            } else {
                                                blackCover.setVisibility(View.GONE);
                                            }
                                        } else {
                                            addTimeImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.breadcrumb));
                                        }
                                        if (TimerService.isRunningService) {
                                            if (TimerService.mTimer != null)
                                                TimerService.isRunningService = false;
                                            TimerService.mTimer.cancel();
                                            TimerService.mTimer = null;
                                            baseTime = 0;
                                            stopService(intent);
                                        }
                                        eventTime.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                alart = new CustomAlartDialog(context,
                                        GlobalVariables.oh_snap,
                                        GlobalVariables.network_error);
                                alart.show();
                            }
                        }
                    });
        } else {
            alart = new CustomAlartDialog(context, GlobalVariables.oh_snap,
                    GlobalVariables.network_error);
            alart.show();
        }
    }

    private void addTime() {
        if (cd.isConnectingToInternet()) {
            try {
                final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
                progressDia = CustomDialog.show(context, GlobalVariables.loading);
                mAuthService.addTime(parameters, new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(final JsonElement jsonElement,
                                            Exception exception, ServiceFilterResponse response) {
                        if (progressDia != null && progressDia.isShowing())
                            progressDia.dismiss();
                        if (exception == null) {
                            System.out.println("AddTimeResult = " + jsonElement.toString());
                            try {
                                getNextExpiration();
                            } catch (Exception e) {
                                e.printStackTrace();
                                updateTime();
                            }
                            try {
                                updateTime();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("AddTimeException = " + exception.toString());
                            updateTime();
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDia != null && progressDia.isShowing())
                            progressDia.dismiss();
                    }
                });
                e.printStackTrace();
            }
        } else {
            if (progressDia != null && progressDia.isShowing())
                progressDia.dismiss();
            alart = new CustomAlartDialog(context, GlobalVariables.oh_snap, GlobalVariables.network_error);
            alart.show();
        }
    }

    private void getNextExpiration() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        mAuthService.getNextExpiration(parameters,
                new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(final JsonElement jsonElement,
                                            Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            System.out.println("NextExpirationResult = " + jsonElement.toString());
                        } else {
                            System.out.println("NextExpirationException = " + exception.toString());
                        }
                    }
                });
    }

    private void updateTime() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("longitude", String.valueOf(log)));
        parameters.add(new Pair<String, String>("latitude", String.valueOf(lat)));
        mAuthService.getCurrentExpiration(parameters,
                new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(final JsonElement jsonElement,
                                            Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            try {
                                System.out.println("UpdateTimeResult = " + jsonElement.toString());
                                JSONObject jsonObject = new JSONObject(jsonElement.toString());
                                until = jsonObject.getString("expires2");
                                SharedPreferences.Editor preferencesEditor = userData.edit();
                                preferencesEditor.putString("expires", jsonObject.getString("expires"));
                                preferencesEditor.putString("expires2", jsonObject.getString("expires2"));
                                preferencesEditor.commit();
                                Intent intent = new Intent(ProfileActivity.this, TimerService.class);
                                if (TimerService.isRunningService) {
                                    TimerService.isRunningService = false;
                                    TimerService.mTimer.cancel();
                                    TimerService.mTimer = null;
                                    baseTime = 0;
                                    stopService(intent);
                                }
                                startService(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("UpdateTimeException = " + exception.toString());
                        }
                    }
                });

    }

    private void adlistsner() {
        goIntroImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                until = userData.getString("expires2", "");
                if (String.valueOf(lat).equalsIgnoreCase("0.0") && String.valueOf(log).equalsIgnoreCase("0.0")) {
                    System.out.println("a" + lat + log);
                    CustomAlartDialogTwoBtn alart = new CustomAlartDialogTwoBtn(context, GlobalVariables.intro,
                            "GPS is not enabled. Do you want to go to settings menu?", "Cancel", "Setting");
                    alart.show();
                    CustomAlartDialogTwoBtn.okBtn
                            .setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    // TODO Auto-generated method stub
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    context.startActivity(intent);
                                }
                            });
                } else {
                    System.out.println(lat + log);
                    if (until != null && !until.equalsIgnoreCase("") && !until.equalsIgnoreCase("null")
                            && !until.contains("-") && !until.equalsIgnoreCase("0 hrs 0 min")) {
                        if (cd.isConnectingToInternet()) {
                            Intent inteni = new Intent(context, QueueUserActivity.class);
                            startActivity(inteni);
                            overridePendingTransition(0, R.anim.exit_slide_right);
                        } else {
                            if (progressDia != null && progressDia.isShowing())
                                progressDia.dismiss();
                            alart = new CustomAlartDialog(context, GlobalVariables.oh_snap,
                                    GlobalVariables.network_error);
                            alart.show();
                        }
                    } else {
                        Intent inteni = new Intent(context, GetReadyActivity.class);
                        startActivity(inteni);
                        overridePendingTransition(0, R.anim.exit_slide_right);
                    }
                }
            }
        });
        addTimeImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                until = userData.getString("expires2", "");
                if (cd.isConnectingToInternet()) {
                    if (!until.equalsIgnoreCase("") && !until.equalsIgnoreCase("null")
                            && !until.contains("-") && !until.equalsIgnoreCase("0 hrs 0 min")) {
                        addTime();
                    } else {
                        if (userData.getBoolean("dropBreadcrumb", false)) {
                            Intent intent = new Intent(ProfileActivity.this, QueueUserActivity.class);
                            intent.putExtra("breadcrumbLike", true);
                            startActivity(intent);
                            overridePendingTransition(0, R.anim.exit_slide_right);
                        } else {
                            final CustomAlartDialogTwoBtn alart = new CustomAlartDialogTwoBtn(
                                    context, GlobalVariables.dropped_breacrumb,
                                    GlobalVariables.breadcrumb_popup, "Delete", "Okay");
                            alart.show();
                            CustomAlartDialogTwoBtn.okBtn
                                    .setOnClickListener(new OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            dropBreadCrumb();
                                            alart.dismiss();
                                        }
                                    });
                        }
                    }
                } else {
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                    alart = new CustomAlartDialog(context, GlobalVariables.oh_snap,
                            GlobalVariables.network_error);
                    alart.show();
                }
            }
        });

     /*   profileImageViewPager
                .setOnPageChangeListener(new OnPageChangeListener() {
                    @Override
                    public void onPageSelected(int pos) {

                        if (pos == 0) {
                            bullet_one.setBackgroundResource(R.drawable.slider_bullet);
                            bullet_two.setBackgroundResource(R.drawable.slider_bullet_2);
                            bullet_three.setBackgroundResource(R.drawable.slider_bullet_2);
                        } else if (pos == 1) {
                            bullet_one.setBackgroundResource(R.drawable.slider_bullet_2);
                            bullet_two.setBackgroundResource(R.drawable.slider_bullet);
                            bullet_three.setBackgroundResource(R.drawable.slider_bullet_2);
                        } else if (pos == 2) {
                            bullet_one.setBackgroundResource(R.drawable.slider_bullet_2);
                            bullet_two.setBackgroundResource(R.drawable.slider_bullet_2);
                            bullet_three.setBackgroundResource(R.drawable.slider_bullet);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }
                });

        bullet_one.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                bullet_one.setBackgroundResource(R.drawable.slider_bullet);
                bullet_two.setBackgroundResource(R.drawable.slider_bullet_2);
                bullet_three.setBackgroundResource(R.drawable.slider_bullet_2);
                profileImageViewPager.setCurrentItem(0);
            }
        });

        bullet_two.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                bullet_one.setBackgroundResource(R.drawable.slider_bullet_2);
                bullet_two.setBackgroundResource(R.drawable.slider_bullet);
                bullet_three.setBackgroundResource(R.drawable.slider_bullet_2);
                profileImageViewPager.setCurrentItem(1);
            }
        });

        bullet_three.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                bullet_one.setBackgroundResource(R.drawable.slider_bullet_2);
                bullet_two.setBackgroundResource(R.drawable.slider_bullet_2);
                bullet_three.setBackgroundResource(R.drawable.slider_bullet);
                profileImageViewPager.setCurrentItem(2);
            }
        });*/
    }

    public void dropBreadCrumb() {
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

    private void init() {
        toolbarMenuImageView = (ImageView) findViewById(R.id.menu);
        toolbarMatchesImageView = (ImageView) findViewById(R.id.matches);
        toolbarMenuImageView.setOnClickListener(this);
        toolbarMatchesImageView.setOnClickListener(this);
        inflater = LayoutInflater.from(this);
        slidingMenuLeft = new SlidingMenu(this);
        //slidingMenuRight = new SlidingMenu(this);
        try {
            initSlidingMenuLeft();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*try {
            initSlidingMenuRight();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            try {
                // Swipe left (next)
                if (e1.getX() > e2.getX()) {
                    viewFlipper.setInAnimation(ProfileActivity.this, R.anim.left_in);
                    viewFlipper.setOutAnimation(ProfileActivity.this, R.anim.left_out);
                    viewFlipper.showNext();
                }

                // Swipe right (previous)
                if (e1.getX() < e2.getX()) {
                    viewFlipper.setInAnimation(ProfileActivity.this, R.anim.right_in);
                    viewFlipper.setOutAnimation(ProfileActivity.this, R.anim.right_out);
                    viewFlipper.showPrevious();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    private void initSlidingMenuLeft() {
        slidingMenuLeft.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenuLeft.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenuLeft.setFadeDegree(0.36f);
        slidingMenuLeft.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        View slidemenuInflater = inflater.inflate(R.layout.slidingmenu, null);
        slidingMenuLeft.setMenu(slidemenuInflater);
        View slideMenuHeader = inflater.inflate(R.layout.side_list_header, null);
        headerTextview = (TextView) slideMenuHeader.findViewById(R.id.headerTextview);
        sideMenuListviewLeft = (ListView) slidingMenuLeft.findViewById(R.id.side_menu_list);
        sideMenuListviewLeft.addHeaderView(slideMenuHeader, null, false);
        headerTextview.setText("Menu");
        resDrawable = getResources().getDrawable(R.drawable.list_divider);
        sideMenuListviewLeft.setDivider(resDrawable);
        sideMenuListviewLeft.setDividerHeight(1);
        slidingMenuLeft.setMode(SlidingMenu.LEFT);
        menuAdapter = new SlidingMenuAdapter(this);
        sideMenuListviewLeft.setAdapter(menuAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setEnabled(false);
        sideMenuListviewLeft.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                view.setSelected(true);
                RelativeLayout rl = (RelativeLayout) view;
                rl.setBackgroundColor(getResources().getColor(R.color.blue));
                SlidingMenuUtils.WhereToGo(context, position);
            }
        });
    }

    /*private void initSlidingMenuRight() {
        slidingMenuRight.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        slidingMenuRight.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenuRight.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenuRight.setFadeDegree(0.36f);
        try {
            slidingMenuRight.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        } catch (Exception e
                ) {
            e.printStackTrace();
        }
        View slidemenuInflater = inflater.inflate(R.layout.slidingmenu, null);
        slidingMenuRight.setMenu(slidemenuInflater);
        View slideMenuHeader = inflater.inflate(R.layout.side_list_header, null);
        headerTextview = (TextView) slideMenuHeader.findViewById(R.id.headerTextview);
        sideMenuListviewRight = (ListView) slidingMenuRight.findViewById(R.id.side_menu_list);
        sideMenuListviewRight.addHeaderView(slideMenuHeader, null, false);
        headerTextview.setText("Matches");
        slidingMenuRight.setMode(SlidingMenu.RIGHT);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (TimerService.mTimer != null) {
                    mSwipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            getNearmatches();
                        }
                    });
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        matchAdapter = new MatchesAdapter(this, matchesList);
        matchAdapter.notifyDataSetChanged();
        sideMenuListviewRight.setAdapter(matchAdapter);
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu:
                slidingMenuLeft.toggle();
                break;
            case R.id.matches:
                Intent intent = new Intent(this, NewMatchesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, R.anim.exit_slide_right);
                /*slidingMenuRight.toggle();
                if (baseTime != 0) {
                    getNearmatches();
                }*/
                break;

            default:
                break;
        }
    }

    private void updateDeviceId() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("deviceid", userData.getString("userid", "").replaceAll("-", "")));
        mAuthService.updateDeviceId(parameters,
                new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(final JsonElement jsonElement,
                                            Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            System.out.println("DevieceResult = " + jsonElement.toString());
                        } else {
                            System.out.println("DeviceException = " + exception.toString());
                        }
                    }
                });
    }

    public void addFav(String favUserId) {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("favUserId", favUserId));
        mAuthService.addFav(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement, Exception e, ServiceFilterResponse serviceFilterResponse) {
                if (e == null) {
                    /*try {
                        System.out.println("AddFavResponse: " + jsonElement.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }*/
                } else {
                    System.out.println("AddFavError: " + e.toString());
                }
            }
        });
    }


    public static void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                enableDisableView(group.getChildAt(idx), enabled);

            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}