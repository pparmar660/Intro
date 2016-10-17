package com.intro.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsee.Appsee;
import com.google.gson.JsonElement;
import com.intro.adapter.LikesBreadcrumbAdapter;
import com.intro.adapter.MatchesAdapter;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomDialog;
import com.intro.customviews.CustomTextView;
import com.intro.customviews.HorizontalListView;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GPSTracker;
import com.intro.utils.GlobalVariables;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by vinove on 24/8/16.
 */
public class NewMatchesActivity extends BaseActivity {

    private Context context;
    private ImageButton back;
    private LinearLayout buttonsLayout;
    public CustomTextView breadcrumbHeader, liveBtn, favBtn, addTime;
    private HorizontalListView breadcrumbList;
    private ListView matchesList;
    public MatchesAdapter matchAdapter;
    public LikesBreadcrumbAdapter breadcrumbAdapter;
    private SharedPreferences userData, toolTipPreferences;
    private double lat, log;
    private String until;
    private ConnectionDetector cd;
    public ArrayList<JSONObject> matchesArrayList = new ArrayList<>();
    public ArrayList<JSONObject> breadcrumbArrayList = new ArrayList<>();
    private ArrayList<JSONObject> favArrayList = new ArrayList<>();
    private JSONObject nearMatchObject;
    private RelativeLayout mainLayout, layoutBelowText, uperListText, blackCover;
    private TextView okey, okeybelow;
    private LinearLayout belowList, uperListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);
        Appsee.start(getString(R.string.appsee_apikey));
        context = NewMatchesActivity.this;
        back = (ImageButton) findViewById(R.id.back_arrow);
        buttonsLayout = (LinearLayout) findViewById(R.id.ll_buttons);
        breadcrumbHeader = (CustomTextView) findViewById(R.id.breadcrumb_header);
        liveBtn = (CustomTextView) findViewById(R.id.liveBtn);
        favBtn = (CustomTextView) findViewById(R.id.favBtn);
        addTime = (CustomTextView) findViewById(R.id.addTimeBtn);
        breadcrumbList = (HorizontalListView) findViewById(R.id.breadcrumbHorizontalList);
        matchesList = (ListView) findViewById(R.id.matchesList);
//for cover layout
        layoutBelowText = (RelativeLayout) findViewById(R.id.layoutBelowText);
        uperListText = (RelativeLayout) findViewById(R.id.uperListText);
        belowList = (LinearLayout) findViewById(R.id.belowList);
        uperListLayout = (LinearLayout) findViewById(R.id.uperListLayout);
        okey = (TextView) findViewById(R.id.okay);
        okeybelow = (TextView) findViewById(R.id.uperOkey);
        blackCover = (RelativeLayout) findViewById(R.id.blackCover);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        toolTipPreferences = getSharedPreferences("toolTipPreferences", MODE_PRIVATE);

        cd = new ConnectionDetector(context);

        addListner();
    }

    public void setData() {
        until = userData.getString("expires2", "");

        gpsTracker = new GPSTracker(context);
        lat = gpsTracker.getLatitude();
        log = gpsTracker.getLongitude();
        progressDia = CustomDialog.show(NewMatchesActivity.this, GlobalVariables.loading);

        if (!until.equalsIgnoreCase("") && !until.equalsIgnoreCase("null")
                && !until.contains("-") && !until.equalsIgnoreCase("0 hrs 0 min")) {
            addTime.setVisibility(View.VISIBLE);
        }
        if (baseTime != 0) {
            getNearmatches();
        } else {
            getLikesBreadcrumb();
        }

        getFavmatches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toolTipPreferences.getBoolean(GlobalVariables.first_time_match, true)) {
            enableDisableView(mainLayout, false);
            blackCover.setVisibility(View.VISIBLE);
        } else {
            blackCover.setVisibility(View.GONE);
            setData();
        }
    }

    public void addListner() {
        okey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uperListLayout.setVisibility(View.GONE);
                layoutBelowText.setVisibility(View.GONE);
                uperListText.setVisibility(View.VISIBLE);
                belowList.setVisibility(View.VISIBLE);

            }
        });

        okeybelow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                blackCover.setVisibility(View.GONE);
                enableDisableView(mainLayout, true);
                SharedPreferences.Editor editor = toolTipPreferences.edit();
                editor.putBoolean(GlobalVariables.first_time_match, false);
                editor.commit();
                setData();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.exit_slide_left);
            }
        });

        liveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonsLayout.setBackgroundColor(getResources().getColor(R.color.pink_btn));
                sortMatches();
                matchAdapter = new MatchesAdapter(context, matchesArrayList, "matches");
                matchesList.setAdapter(matchAdapter);
                matchAdapter.notifyDataSetChanged();
            }
        });

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonsLayout.setBackgroundColor(getResources().getColor(R.color.blue));
                matchAdapter = new MatchesAdapter(context, favArrayList, "fav");
                matchesList.setAdapter(matchAdapter);
                matchAdapter.notifyDataSetChanged();
            }
        });

        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTime();
            }
        });
    }

    public void sortMatches() {
        Collections.sort(matchesArrayList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                int diff = 0;
                try {
                    diff = lhs.getString("expires").compareTo(rhs.getString("expires"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return diff;
            }
        });

    }

    public void getLikesBreadcrumb() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
        parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));

        mAuthService.getLikesBreadcrumb(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement,
                                    Exception exception, ServiceFilterResponse response) {
                if (progressDia != null && progressDia.isShowing()) {
                    progressDia.dismiss();
                }
                if (jsonElement != null && !jsonElement.toString().equalsIgnoreCase("null")) {
                    try {
                        breadcrumbArrayList.clear();
                        System.out.println("LikeBreadcrumbResult = " + jsonElement.toString());
                        JSONObject breadcrumbObject = new JSONObject(jsonElement.toString());
                        if (breadcrumbObject.getInt("code") == 200) {
                            GlobalVariables.bothArrayList = new ArrayList<>();
                            JSONArray breadcrumbArray = breadcrumbObject.getJSONArray("data");
                            for (int i = 0; i < breadcrumbArray.length(); i++) {
                                breadcrumbArrayList.add(breadcrumbArray.getJSONObject(i));
                            }
                            if (matchesArrayList.size() > 0) {
                                for (int i = 0; i < breadcrumbArrayList.size(); i++) {
                                    breadcrumbObject = breadcrumbArrayList.get(i);
                                    for (int j = 0; j < matchesArrayList.size(); j++) {
                                        nearMatchObject = matchesArrayList.get(j);
                                        if (breadcrumbObject.getString("firebaseid").equalsIgnoreCase(nearMatchObject.getString("firebaseid"))) {
                                            GlobalVariables.bothArrayList.add(breadcrumbArrayList.get(i));
                                            breadcrumbArrayList.remove(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (breadcrumbArrayList.size() > 1) {
                                breadcrumbHeader.setText("BREADCRUMBS (" + (breadcrumbArrayList.size()) + ")");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                Collections.sort(breadcrumbArrayList, new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject lhs, JSONObject rhs) {
                        int diff = 0;
                        try {
                            diff = rhs.getString("liketime").compareTo(lhs.getString("liketime"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return diff;
                    }
                });
                JSONObject jsonObject = new JSONObject();
                breadcrumbArrayList.add(jsonObject);
                breadcrumbAdapter = new LikesBreadcrumbAdapter(context, breadcrumbArrayList);
                breadcrumbList.setAdapter(breadcrumbAdapter);
                breadcrumbAdapter.notifyDataSetChanged();
            }
        });
    }

    public void getNearmatches() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
        parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));

        mAuthService.getNearMatches(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement,
                                    Exception exception, ServiceFilterResponse response) {
                getLikesBreadcrumb();
                if (jsonElement != null && !jsonElement.toString().equalsIgnoreCase("null")) {
                    try {
                        matchesArrayList.clear();
                        System.out.println("NearMatchesResult = " + jsonElement.toString());
                        JSONArray nearMatchArray = new JSONArray(jsonElement.toString());
                        if (nearMatchArray.length() > 0) {
                            liveBtn.setText("LIVE (" + nearMatchArray.length() + ")");
                        }
                        for (int i = 0; i < nearMatchArray.length(); i++) {
                            matchesArrayList.add(nearMatchArray.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                sortMatches();
                matchAdapter = new MatchesAdapter(context, matchesArrayList, "matches");
                matchesList.setAdapter(matchAdapter);
                matchAdapter.notifyDataSetChanged();
            }
        });
    }

    public void getFavmatches() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userID", userData.getString("userid", "")));

        mAuthService.getFavMatches(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement,
                                    Exception exception, ServiceFilterResponse response) {
                favArrayList.clear();
                if (progressDia != null && progressDia.isShowing())
                    progressDia.dismiss();
                if (jsonElement != null && !jsonElement.toString().equalsIgnoreCase("null")) {
                    try {
                        JSONObject breadcrumbObject = new JSONObject(jsonElement.toString());
                        System.out.println("FavMatchesResult = " + jsonElement.toString());
                        if (breadcrumbObject.getInt("code") == 200) {
                            JSONArray favMatchArray = breadcrumbObject.getJSONArray("data");
                            if (favMatchArray.length() > 0) {
                                favBtn.setText("FAVS (" + favMatchArray.length() + ")");
                            }
                            for (int i = 0; i < favMatchArray.length(); i++) {
                                favArrayList.add(favMatchArray.getJSONObject(i));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
                                //  until = jsonObject.getString("expires2");
                                SharedPreferences.Editor preferencesEditor = userData.edit();
                                preferencesEditor.putString("expires", jsonObject.getString("expires"));
                                preferencesEditor.putString("expires2", jsonObject.getString("expires2"));
                                preferencesEditor.commit();
                                Intent intent = new Intent(NewMatchesActivity.this, TimerService.class);
                                if (TimerService.isRunningService) {
                                    TimerService.isRunningService = false;
                                    TimerService.mTimer.cancel();
                                    TimerService.mTimer = null;
                                    baseTime = 0;
                                    stopService(intent);
                                }
                                startService(intent);
                                getNearmatches();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("UpdateTimeException = " + exception.toString());
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
        super.onBackPressed();
        finish();
        overridePendingTransition(0, R.anim.exit_slide_left);
    }
}
