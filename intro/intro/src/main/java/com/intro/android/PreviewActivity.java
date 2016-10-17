package com.intro.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsee.Appsee;
import com.google.gson.JsonElement;
import com.intro.adapter.MatchesAdapter;
import com.intro.adapter.SlidingMenuAdapter;
import com.intro.customviews.BlurBuilder;
import com.intro.customviews.CustomTextView;
import com.intro.imageUtils.ImageLoader;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GPSTracker;
import com.intro.utils.GlobalVariables;
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
 * Created by vinove on 18/4/16.
 */
public class PreviewActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private ConnectionDetector conDec;
    private LayoutInflater inflater;
    private SlidingMenu slidingMenuRight;
    private SlidingMenuAdapter menuAdapter;
    private TextView headerTextview;
    private ListView sideMenuListviewRight;
    private CustomTextView name, age, threewordtext, onewordtext_blur, twowordtext_blur, threewordtext_blur, bio;
    private ImageView menu, matches, imageDp, introLogo, imageDpBlur, report;
    private RelativeLayout queue_rel, main_rel, blur_rel;
    private Bitmap bitmap[] = new Bitmap[3];
    private ArrayList<String> imageList = new ArrayList<String>();
    private Drawable resDrawable;
    private SharedPreferences userData;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    public static ArrayList<JSONObject> matchesList = new ArrayList<JSONObject>();
    private MatchesAdapter matchAdapter;
    private String haiKu;
    ImageLoader imgL;
    private double lat, log;
    Handler handler = new Handler();
    private ArrayList<String> haikuList = new ArrayList<String>();
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_ID_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Appsee.start(getString(R.string.appsee_apikey));
        context = PreviewActivity.this;
        setContentView(R.layout.activity_preview);
        queue_rel = (RelativeLayout) findViewById(R.id.queue_rel);
        menu = (ImageView) findViewById(R.id.menu);
        matches = (ImageView) findViewById(R.id.matches);
        threewordtext = (CustomTextView) findViewById(R.id.threewordtext);
        onewordtext_blur = (CustomTextView) findViewById(R.id.onewordtext_blur);
        twowordtext_blur = (CustomTextView) findViewById(R.id.twowordtext_blur);
        threewordtext_blur = (CustomTextView) findViewById(R.id.threewordtext_blur);
        imageDp = (ImageView) findViewById(R.id.image_dp);
        name = (CustomTextView) findViewById(R.id.name);
        age = (CustomTextView) findViewById(R.id.age);
        report = (ImageView) findViewById(R.id.report);
        bio = (CustomTextView) findViewById(R.id.bio);
        imageDpBlur = (ImageView) findViewById(R.id.image_dp_blur);
        main_rel = (RelativeLayout) findViewById(R.id.activity_queue_relative_layout);
        blur_rel = (RelativeLayout) findViewById(R.id.blur_rel);

        if (!Utility.hasPermissions(PreviewActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(PreviewActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
        } else {
            setData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ID_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setData();
                }
                break;
        }
    }

    public void setData() {
        gpsTracker = new GPSTracker(context);
        lat = gpsTracker.getLatitude();
        log = gpsTracker.getLongitude();

        matches.setOnClickListener(this);
        menu.setOnClickListener(this);

        haiKu = getIntent().getStringExtra("haiKu");
        if (!haiKu.equalsIgnoreCase("")) {
            String[] threeWordsIntro = new String[]{"", "", ""};
           /* if (haiKu.contains("'")) {
                haiKu = haiKu.replaceAll("'", "''");
            }*/
            threeWordsIntro = haiKu.split(" ");
            haikuList.add(threeWordsIntro[0]);
            haikuList.add(threeWordsIntro[1]);
            haikuList.add(threeWordsIntro[2]);
        }
        conDec = new ConnectionDetector(context);
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        imgL = new ImageLoader(context);
        imageList.add(GlobalVariables.Image_URL + userData.getString("userid", "") + "-1");
        imageList.add(GlobalVariables.Image_URL + userData.getString("userid", "") + "-2");
        imageList.add(GlobalVariables.Image_URL + userData.getString("userid", "") + "-3");
        try {
            queue_rel.setVisibility(View.GONE);

            if (conDec.isConnectingToInternet()) {
                bitmap = new Bitmap[3];
                for (int i = 0; i < imageList.size(); i++) {
                    new DownloadImageTask().execute(imageList.get(i));
                    System.out.println("DownloadImageTask" + i);
                }
            } else {
                Utility.validationByToastMessage(context,
                        GlobalVariables.network_error);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
       /* try {
            inflater = LayoutInflater.from(this);
            slidingMenuRight = new SlidingMenu(this);
            initSlidingMenuRight();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    int counter = 0;
    boolean stillShow = true;
    boolean firstTime = true;

    private void startCountDown() {
//Flash pic 1 for 1 second, ½ second black screen, pic 2 for 1 second, ½ second black screen, pic 3 for 1 second, blur screen with
        //  like/dislike buttons
        new CountDownTimer(4500, 500) {
            public void onTick(long millisUntilFinished) {
                try {
                    if (firstTime) {
                        System.out.println("in thread firstTime show first image " + counter);
                        firstTime = false;
                        imageDp.setVisibility(View.VISIBLE);
                        imageDp.setImageBitmap(bitmap[counter]);
                        threewordtext.setVisibility(View.VISIBLE);
                        if (haikuList.size() > 0) {
                            threewordtext.setText(haikuList.get(counter));
                        }
                        counter++;

                    } else {
                        if (!imageDp.isShown()) {// no
                            imageDp.setVisibility(View.VISIBLE);
                            imageDp.setImageBitmap(bitmap[counter]);
                            threewordtext.setVisibility(View.VISIBLE);
                            if (haikuList.size() > 0) {
                                threewordtext.setText(haikuList.get(counter));
                            }
                            System.out.println("in thread show image " + counter);
                            counter++;
                        } else if (stillShow && imageDp.isShown()) {
                            System.out.println("in thread still show image & image !" + (counter - 1));
                            stillShow = false;
                            imageDp.setVisibility(View.VISIBLE);
                            imageDp.setImageBitmap(bitmap[counter - 1]);
                            threewordtext.setVisibility(View.VISIBLE);
                            if (haikuList.size() > 0) {
                                threewordtext.setText(haikuList.get(counter - 1));
                            }
                        } else {
                            System.out.println("in thread black");
                            imageDp.setVisibility(View.GONE);
                            threewordtext.setVisibility(View.GONE);
                            stillShow = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

    /*int counter = 0;

    private void startCountDown() {

        new CountDownTimer(3500, 500) {
            public void onTick(long millisUntilFinished) {
                try {
                    if (!imageDp.isShown()) {// no
                        imageDp.setVisibility(View.VISIBLE);
                        imageDp.setImageBitmap(bitmap[counter]);
                        threewordtext.setVisibility(View.VISIBLE);
                        if (haikuList.size() > 0) {
                            threewordtext.setText(haikuList.get(counter));
                        }
                        counter++;
                    } else {
                        imageDp.setVisibility(View.GONE);
                        threewordtext.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/

            public void onFinish() {
                try {

                    imageDp.setVisibility(View.VISIBLE);
                    queue_rel.setVisibility(View.GONE);
                    blur_rel.setVisibility(View.VISIBLE);
                    threewordtext.setVisibility(View.VISIBLE);
                    Bitmap originalBitmap = ((BitmapDrawable) imageDp.getDrawable()).getBitmap();
                    Bitmap blurredBitmap = BlurBuilder.blur(context, originalBitmap);
                    imageDpBlur.setImageBitmap(blurredBitmap);
                    report.setVisibility(View.VISIBLE);
                    bio.setText(userData.getString("bio", ""));
                    if (haikuList.size() > 0) {
                        onewordtext_blur.setText(haikuList.get(0));
                        twowordtext_blur.setText(haikuList.get(1));
                        threewordtext_blur.setText(haikuList.get(2));
                    }
                    Runnable r = new Runnable() {
                        public void run() {
                            finish();
                            overridePendingTransition(0, R.anim.exit_slide_left);
                        }
                    };
                    handler.postDelayed(r, 2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        String url;
        ImageLoader iLoad = new ImageLoader(context);

        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];

            return iLoad.getImage(url);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            try {
                if (result != null) {
                    for (int i = 0; i < imageList.size(); i++) {
                        if (url.equalsIgnoreCase(imageList.get(i))) {
                            bitmap[i] = result;
                        }
                    }
                }

                if (bitmap[0] != null && bitmap[1] != null && bitmap[2] != null) {
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                    queue_rel.setVisibility(View.VISIBLE);
                    name.setText(userData.getString("name", ""));
                    age.setText(" | " + userData.getString("age", ""));
                    startCountDown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu:
                Intent intent = new Intent(PreviewActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, R.anim.exit_slide_right);
                break;
            case R.id.matches:
//                slidingMenuRight.toggle();
                Intent intent1 = new Intent(this, NewMatchesActivity.class);
                startActivity(intent1);
                overridePendingTransition(0, R.anim.exit_slide_right);
                break;
            default:
                break;
        }
    }

    /*private void initSlidingMenuRight() {
        slidingMenuRight.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        slidingMenuRight.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenuRight.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenuRight.setFadeDegree(0.36f);
        try {
            slidingMenuRight.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        } catch (Exception e) {
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
                matchesList.clear();
                if (jsonElement != null) {
                    try {
                        System.out.println("NearMatchesResult = " + jsonElement.toString());
                        JSONArray nearMatchArray = new JSONArray(jsonElement.toString());

                        for (int i = 0; i < nearMatchArray.length(); i++) {
                            matchesList.add(nearMatchArray.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("NearMatchesException = " + exception.toString());
                }
                mSwipeRefreshLayout.setRefreshing(false);
                matchAdapter = new MatchesAdapter(context, matchesList);
                matchAdapter.notifyDataSetChanged();
                sideMenuListviewRight.setAdapter(matchAdapter);
            }
        });
    }
*/
}
