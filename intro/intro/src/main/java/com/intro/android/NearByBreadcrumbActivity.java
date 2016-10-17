package com.intro.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsee.Appsee;
import com.google.gson.JsonElement;
import com.intro.adapter.MatchesAdapter;
import com.intro.adapter.NearByBreadvrumbAdapter;
import com.intro.adapter.SlidingMenuAdapter;
import com.intro.customviews.CustomButton;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GPSTracker;
import com.intro.utils.SlidingMenuUtils;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vinove on 13/4/16.
 */
public class NearByBreadcrumbActivity extends BaseActivity {

    private Context context;
    private CustomButton goChat, viewPriofiles;
    private ListView breadcrumbList;
    private TextView headerTextview;
    private double lat, log;
    private SharedPreferences userData;
    private ConnectionDetector cd;
    private String data;
    private ArrayList<JSONObject> nearBreadcrumbList;
    private NearByBreadvrumbAdapter adapter;
    public static SlidingMenu slidingMenuRight;
    private LayoutInflater inflater;
    public static ListView sideMenuListviewRight;
    public static SwipeRefreshLayout mSwipeRefreshLayout = null;
    public static MatchesAdapter matchAdapter;
    public static ArrayList<JSONObject> matchesList = new ArrayList<>();
    public static JSONArray nearMatchArray;
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_ID_PERMISSIONS = 1;
    private static String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_ID_LOCATION_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Appsee.start(getString(R.string.appsee_apikey));
        context = NearByBreadcrumbActivity.this;
        setContentView(R.layout.activity_nearby_breadcrumb);
        breadcrumbList = (ListView) findViewById(R.id.breadcrumb_list);
        goChat = (CustomButton) findViewById(R.id.go_chat);
        viewPriofiles = (CustomButton) findViewById(R.id.view_profiles);

        data = getIntent().getStringExtra("data");
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        cd = new ConnectionDetector(context);

        if (!Utility.hasPermissions(NearByBreadcrumbActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(NearByBreadcrumbActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
        } else {
            gpsTracker = new GPSTracker(context);
            lat = gpsTracker.getLatitude();
            log = gpsTracker.getLongitude();
            /*inflater = LayoutInflater.from(this);
            slidingMenuRight = new SlidingMenu(this);
            try {
                initSlidingMenuRight();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            setdata(data);
        }
        addListner();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ID_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker = new GPSTracker(context);
                    lat = gpsTracker.getLatitude();
                    log = gpsTracker.getLongitude();
                    inflater = LayoutInflater.from(this);
                    /*slidingMenuRight = new SlidingMenu(this);
                    try {
                        initSlidingMenuRight();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    setdata(data);
                }
                break;
            case REQUEST_ID_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                  //  getNearmatches();
                }
                break;
        }
    }

    public void setdata(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data.toString());
            int code = jsonObject.getInt("code");
            if (code == 200) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                nearBreadcrumbList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    nearBreadcrumbList.add(jsonArray.getJSONObject(i));
                }
                adapter = new NearByBreadvrumbAdapter(context, nearBreadcrumbList);
                breadcrumbList.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListner() {
        goChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*slidingMenuRight.toggle();
                if (baseTime != 0) {
                    getNearmatches();
                }*/
                Intent intent = new Intent(NearByBreadcrumbActivity.this, NewMatchesActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, R.anim.exit_slide_right);
            }
        });

        viewPriofiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, QueueUserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(0, R.anim.exit_slide_right);
            }
        });
    }

  /*  private void initSlidingMenuRight() {
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

  /*  public void getNearmatches() {
        if (!Utility.hasPermissions(NearByBreadcrumbActivity.this, LOCATION_PERMISSION)) {
            ActivityCompat.requestPermissions(NearByBreadcrumbActivity.this, LOCATION_PERMISSION, REQUEST_ID_LOCATION_PERMISSION);
        } else {
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
        }
    }*/
}
