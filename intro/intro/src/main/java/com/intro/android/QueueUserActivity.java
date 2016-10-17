package com.intro.android;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsee.Appsee;
import com.google.gson.JsonElement;
import com.intro.adapter.MatchesAdapter;
import com.intro.adapter.SlidingMenuAdapter;
import com.intro.customviews.BlurBuilder;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomAlartDialogTwoBtn;
import com.intro.customviews.CustomDialog;
import com.intro.customviews.CustomTextView;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GPSTracker;
import com.intro.utils.GlobalVariables;
import com.intro.utils.SlidingMenuUtils;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;


/**
 * This class is show all users who are online in near places in a queue and all
 * users images one by one download and show in queue.
 * <p/>
 * Dislike button dislike the user and remove user to queue.
 * <p/>
 * Like button send a notification to the liked user if that user also like our
 * profile than it send to the MatchActivity.
 * <p/>
 * By Ankit Kumar and Mirza Faisal
 */

public class QueueUserActivity extends BaseActivity implements OnClickListener {

    private SlidingMenu slidingMenuRight;
    private LayoutInflater inflater;
    private TextView headerTextview;
    private ListView sideMenuListviewRight;
    public static Context context;
    private MatchesAdapter matchAdapter;
    private CustomTextView name, age, threewordtext, onewordtext_blur, twowordtext_blur, threewordtext_blur, bio, queue_null_text;
    private ImageView menu, matches, imageDp, introLogo, imageDpBlur, report;
    private Bitmap bitmap[] = new Bitmap[3];
    int countResult = 0, arrayCount = 0;
    private RelativeLayout header, queue_rel, blur_rel, null_user_rel, refresh, see_previous_visitors_rel, main_rel, footer, breadcrumb_like_rel;
    private ImageButton dislike_btn, like_btn;
    private ConnectionDetector conDec;
    private SharedPreferences userData;
    private double lat, log;
    private String maleStr, femaleStr, nameResult, ageResult, bioResult = "", startTimeResult, endTimeResult, matchID, mode = "0";  // 0 - LiveEvent / 1 - Breadcrumb
    private ArrayList<String> imageList = new ArrayList<String>();
    private ArrayList<String> haikuList = new ArrayList<String>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private JSONArray jsonArray;
    public static ArrayList<JSONObject> matchesList = new ArrayList<JSONObject>();
    private boolean breadcrumb, breadcrumbtoggle = false, breadcrumbLike = false, move = true;
    private Bundle bundle;
    static final String STATE_USER = "move";
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_ID_PERMISSIONS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = savedInstanceState;
        checkFocus(bundle);
        setContentView(R.layout.activity_queue_user);
        Appsee.start(getString(R.string.appsee_apikey));
        context = this;
        menu = (ImageView) findViewById(R.id.menu);
        matches = (ImageView) findViewById(R.id.queue_matches);
        matches.setOnClickListener(this);
        menu.setOnClickListener(this);
        name = (CustomTextView) findViewById(R.id.name);
        age = (CustomTextView) findViewById(R.id.age);
        report = (ImageView) findViewById(R.id.report);
        introLogo = (ImageView) findViewById(R.id.introLogo);
        threewordtext = (CustomTextView) findViewById(R.id.threewordtext);
        imageDp = (ImageView) findViewById(R.id.image_dp);
        onewordtext_blur = (CustomTextView) findViewById(R.id.onewordtext_blur);
        twowordtext_blur = (CustomTextView) findViewById(R.id.twowordtext_blur);
        threewordtext_blur = (CustomTextView) findViewById(R.id.threewordtext_blur);
        bio = (CustomTextView) findViewById(R.id.bio);
        imageDpBlur = (ImageView) findViewById(R.id.image_dp_blur);
        main_rel = (RelativeLayout) findViewById(R.id.activity_queue_relative_layout);
        blur_rel = (RelativeLayout) findViewById(R.id.blur_rel);
        queue_rel = (RelativeLayout) findViewById(R.id.queue_rel);
        null_user_rel = (RelativeLayout) findViewById(R.id.null_user_rel);
        see_previous_visitors_rel = (RelativeLayout) findViewById(R.id.see_previous_visitors);
        dislike_btn = (ImageButton) findViewById(R.id.dislike_btn);
        like_btn = (ImageButton) findViewById(R.id.like_btn);
        queue_null_text = (CustomTextView) findViewById(R.id.queue_null_text);
        header = (RelativeLayout) findViewById(R.id.header);
        refresh = (RelativeLayout) findViewById(R.id.refresh);
        footer = (RelativeLayout) findViewById(R.id.footer);
        breadcrumb_like_rel = (RelativeLayout) findViewById(R.id.breadcrumb_like_rel);
        conDec = new ConnectionDetector(context);
        userData = getSharedPreferences("UserData", MODE_PRIVATE);

        maleStr = userData.getString("wantsMales", "1");
        femaleStr = userData.getString("wantsFemales", "1");
        gpsTracker = new GPSTracker(context);
        lat = gpsTracker.getLatitude();
        log = gpsTracker.getLongitude();

        if (userData.getString("Image1Uploaded", "0").equals("0") || userData.getString("Image2Uploaded", "0").equals("0")
                || userData.getString("Image3Uploaded", "0").equals("0")) {
            alartTwoBtn = new CustomAlartDialogTwoBtn(context, getString(R.string.go_intro), getString(R.string.image_warning),
                    getString(R.string.later), getString(R.string.upload));
            alartTwoBtn.show();
            CustomAlartDialogTwoBtn.cancelBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alartTwoBtn.dismiss();
                    ((RelativeLayout) findViewById(R.id.activity_queue_relative_layout))
                            .post(new Runnable() {

                                @Override
                                public void run() {
                                    progressDia = CustomDialog.show(QueueUserActivity.this,
                                            "Finding nearby profiles...");
                                    System.out.println("bread" + getIntent().getBooleanExtra("breadcrumbLike", false));
                                    if (!Utility.hasPermissions(QueueUserActivity.this, PERMISSIONS)) {
                                        ActivityCompat.requestPermissions(QueueUserActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
                                    } else {
                                        if (getIntent().getBooleanExtra("breadcrumbLike", false)) {
                                            menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow));
                                            breadcrumbLike = true;
                                            breadcrumbLikedUser();
                                        } else {
                                            getQueueofUserImage();
                                        }
                                    }
                                }
                            });
                }
            });
            CustomAlartDialogTwoBtn.okBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    alartTwoBtn.dismiss();
                    Intent inteni = new Intent(context, SetProfileActivity.class);
                    inteni.putExtra(GlobalVariables.comeFrom, "Setting");
                    inteni.putExtra("signUp", false);
                    inteni.putExtra("cancel", false);
                    inteni.putExtra(GlobalVariables.comeFrom, GlobalVariables.settingActivity);
                    startActivity(inteni);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                }
            });
        } else {
            ((RelativeLayout) findViewById(R.id.activity_queue_relative_layout))
                    .post(new Runnable() {

                        @Override
                        public void run() {
                            progressDia = CustomDialog.show(QueueUserActivity.this,
                                    "Finding nearby profiles...");
                            System.out.println("bread" + getIntent().getBooleanExtra("breadcrumbLike", false));
                            if (!Utility.hasPermissions(QueueUserActivity.this, PERMISSIONS)) {
                                ActivityCompat.requestPermissions(QueueUserActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
                            } else {
                                if (getIntent().getBooleanExtra("breadcrumbLike", false)) {
                                    menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow));
                                    breadcrumbLike = true;
                                    breadcrumbLikedUser();
                                } else {
                                    getQueueofUserImage();
                                }
                            }
                        }
                    });
        }
        /*try {
            inflater = LayoutInflater.from(this);
            slidingMenuRight = new SlidingMenu(this);
            initSlidingMenuRight();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        adlistener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ID_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (getIntent().getBooleanExtra("breadcrumbLike", false)) {
                        menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow));
                        breadcrumbLike = true;
                        breadcrumbLikedUser();
                    } else {
                        getQueueofUserImage();
                    }
                } else {
                    alartTwoBtn = new CustomAlartDialogTwoBtn(context, getString(R.string.go_intro), getString(R.string.image_warning),
                            getString(R.string.cancel), getString(R.string.provide));
                    alartTwoBtn.show();
                    CustomAlartDialogTwoBtn.cancelBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alartTwoBtn.dismiss();
                            finish();
                        }
                    });
                    CustomAlartDialogTwoBtn.okBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alartTwoBtn.dismiss();
                            if (!Utility.hasPermissions(QueueUserActivity.this, PERMISSIONS)) {
                                ActivityCompat.requestPermissions(QueueUserActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
                            }
                        }
                    });
                }
                break;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_USER, move);
        bundle = savedInstanceState;
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void checkFocus(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Intent intent = new Intent(QueueUserActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, R.anim.exit_slide_left);
        } else {
            move = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalVariables.queueActivity = GlobalVariables.Resume;
        if (!getIntent().getBooleanExtra("breadcrumbLike", false)) {
            checkFocus(bundle);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalVariables.queueActivity = GlobalVariables.Pause;
    }

    public void getQueueofUserImage() {
        try {
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
            parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));
            parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
            parameters.add(new Pair<String, String>("high", userData.getString("high", "100")));
            parameters.add(new Pair<String, String>("low", userData.getString("low", "18")));
            parameters.add(new Pair<String, String>("wants_males", maleStr));
            parameters.add(new Pair<String, String>("wants_females", femaleStr));
            mAuthService.getQueueData(parameters,
                    new ApiJsonOperationCallback() {

                        @Override
                        public void onCompleted(JsonElement jsonElement,
                                                Exception exception,
                                                ServiceFilterResponse response) {
                            try {
                                if (exception == null) {
                                    System.out.println("QueueResult = " + jsonElement.toString());
                                    if (progressDia != null && progressDia.isShowing())
                                        progressDia.dismiss();
                                    if (jsonElement != null && !jsonElement.toString().equalsIgnoreCase("null")) {
                                        jsonArray = new JSONArray(jsonElement.toString());
                                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                                        if (jsonObject.getInt("code") == 201) {
                                            queue_null_text.setText(R.string.queue_null_text2);
                                            null_user_rel.setVisibility(View.VISIBLE);
                                            footer.setVisibility(View.GONE);
                                            see_previous_visitors_rel.setVisibility(View.VISIBLE);
                                        } else {
                                            arrayCount = jsonArray.length();
                                            mode = "0";
                                            queue_null_text.setText(R.string.queue_null_text1);
                                            null_user_rel.setVisibility(View.GONE);
                                            footer.setVisibility(View.VISIBLE);
                                            see_previous_visitors_rel.setVisibility(View.GONE);
                                            showQueue(jsonArray, countResult);
                                        }
                                    } else {
                                        queue_null_text.setText(R.string.queue_null_text2);
                                        null_user_rel.setVisibility(View.VISIBLE);
                                        footer.setVisibility(View.GONE);
                                        see_previous_visitors_rel.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    queue_null_text.setText(R.string.queue_null_text2);
                                    System.out.println("QueueResponse = " + exception.toString());
                                    if (progressDia != null && progressDia.isShowing())
                                        progressDia.dismiss();
                                    null_user_rel.setVisibility(View.VISIBLE);
                                    footer.setVisibility(View.GONE);
                                    see_previous_visitors_rel.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                if (progressDia != null && progressDia.isShowing())
                                    progressDia.dismiss();

                                e.printStackTrace();
                            }
                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showQueue(final JSONArray jsonArray, int count) {
        countResult = count;
        JSONObject jsonObject;
        matchID = "";
        if (count < arrayCount) {
            try {
                onewordtext_blur.setText("");
                twowordtext_blur.setText("");
                threewordtext_blur.setText("");
                threewordtext.setText("");
                imageList.clear();
                haikuList.clear();
                imageList = new ArrayList<String>();
                jsonObject = jsonArray.getJSONObject(count);
                imageList.add(GlobalVariables.Image_URL + jsonObject.getString("id") + "-1");
                imageList.add(GlobalVariables.Image_URL + jsonObject.getString("id") + "-2");
                imageList.add(GlobalVariables.Image_URL + jsonObject.getString("id") + "-3");
                nameResult = decodeUTF(jsonObject.getString("name"));
                matchID = jsonObject.getString("id");
                ageResult = jsonObject.getString("age");
                if (jsonObject.has("bio")) {
                    bioResult = decodeUTF(jsonObject.getString("bio"));
                }
                if (jsonObject.has("haiku1")) {
                    if (jsonObject.getString("haiku1").equalsIgnoreCase("null") || jsonObject.getString("haiku1").equalsIgnoreCase("")
                            || jsonObject.getString("haiku1") == null) {
                        haikuList.add("");
                    } else {
                        haikuList.add(decodeUTF(jsonObject.getString("haiku1")));
                    }
                }
                if (jsonObject.has("haiku2")) {
                    if (jsonObject.getString("haiku2").equalsIgnoreCase("null") || jsonObject.getString("haiku2").equalsIgnoreCase("")
                            || jsonObject.getString("haiku2") == null) {
                        haikuList.add("");
                    } else {
                        haikuList.add(decodeUTF(jsonObject.getString("haiku2")));
                    }
                }
                if (jsonObject.has("haiku3")) {
                    if (jsonObject.getString("haiku3").equalsIgnoreCase("null") || jsonObject.getString("haiku3").equalsIgnoreCase("")
                            || jsonObject.getString("haiku3") == null) {
                        haikuList.add("");
                    } else {
                        haikuList.add(decodeUTF(jsonObject.getString("haiku3")));
                    }
                }

                queue_rel.setVisibility(View.GONE);
                if (conDec.isConnectingToInternet()) {
                    bitmap = new Bitmap[3];
                    progressDia = CustomDialog.show(QueueUserActivity.this,
                            "Loading...");
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

        } else {
            /*if (baseTime != 0) {
                getNearmatches();
            }*/
            null_user_rel.setVisibility(View.VISIBLE);
            if (bothdropBreadcrumb || breadcrumb) {
                null_user_rel.setVisibility(View.GONE);
                breadcrumb_like_rel.setVisibility(View.VISIBLE);
                header.setBackgroundColor(getResources().getColor(R.color.breadcrumb_like_header_color));
                menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow));
                if (breadcrumb)
                    breadcrumbtoggle = true;
            }
            if (userData.getBoolean("dropBreadcrumb", false)) {
                SharedPreferences.Editor editor = userData.edit();
                editor.putBoolean("dropBreadcrumb", false);
                editor.commit();
            }
            footer.setVisibility(View.GONE);
            see_previous_visitors_rel.setVisibility(View.VISIBLE);
        }

    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        String url;

        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            return download_Image(url);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (progressDia != null && progressDia.isShowing())
                progressDia.dismiss();
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
                    name.setText(nameResult);
                    age.setText(" | " + ageResult);
                    startCountDown();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

                public void onFinish() {
                    try {
                        imageDp.setVisibility(View.VISIBLE);
                        threewordtext.setVisibility(View.VISIBLE);
                        queue_rel.setVisibility(View.GONE);
                        blur_rel.setVisibility(View.VISIBLE);
                        Bitmap originalBitmap = ((BitmapDrawable) imageDp.getDrawable()).getBitmap();
                        Bitmap blurredBitmap = BlurBuilder.blur(context, originalBitmap);
                        imageDpBlur.setImageBitmap(blurredBitmap);
                        report.setVisibility(View.VISIBLE);
                        bio.setText(bioResult);
                        if (haikuList.size() > 0) {
                            onewordtext_blur.setText(haikuList.get(0));
                            twowordtext_blur.setText(haikuList.get(1));
                            threewordtext_blur.setText(haikuList.get(2));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        private Bitmap download_Image(String url) {
            Bitmap bmp = null;
            try {
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection) ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp)
                    return bmp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bmp;
        }
    }

    private void adlistener() {
        like_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mode.equalsIgnoreCase("0")) {
                    likeUser();
                } else {
                    breadcrumbLike();
                }
            }
        });

        dislike_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mode.equalsIgnoreCase("0")) {
                    disLikeUser();
                } else {
                    breadcrumbDisLike();
                }
            }
        });

        refresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDia = CustomDialog.show(QueueUserActivity.this, GlobalVariables.loading);
                null_user_rel.setVisibility(View.GONE);
                see_previous_visitors_rel.setVisibility(View.GONE);
                getQueueofUserImage();
            }
        });

        see_previous_visitors_rel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDia = CustomDialog.show(QueueUserActivity.this, GlobalVariables.loading);
                null_user_rel.setVisibility(View.GONE);
                see_previous_visitors_rel.setVisibility(View.GONE);
                getBreadcrumb();
            }
        });

        report.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final CustomAlartDialogTwoBtn alart = new CustomAlartDialogTwoBtn(context, GlobalVariables.intro,
                            GlobalVariables.reportMsg, "No", "Yes");
                    alart.show();
                    CustomAlartDialogTwoBtn.okBtn.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            reportInappropriate();
                            alart.dismiss();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu:
                if (bothdropBreadcrumb || breadcrumb) {
                    if (bothdropBreadcrumb) {
                        Intent intent = new Intent(QueueUserActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, R.anim.exit_slide_left);
                    } else if (breadcrumb) {
                        breadcrumb = false;
                        header.setBackgroundColor(getResources().getColor(R.color.black));
                        menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.white_profile));
                        breadcrumb_like_rel.setVisibility(View.GONE);
                        queue_rel.setVisibility(View.GONE);
                        blur_rel.setVisibility(View.GONE);
                        null_user_rel.setVisibility(View.VISIBLE);
                        see_previous_visitors_rel.setVisibility(View.VISIBLE);
                        name.setText("");
                        age.setText("");

                        return;
                    }
                } else {
                    Intent intent = new Intent(QueueUserActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.exit_slide_left);
                }
                break;
            case R.id.queue_matches:
                Intent intent = new Intent(QueueUserActivity.this, NewMatchesActivity.class);
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

    @Override
    public void onBackPressed() {
        if (bothdropBreadcrumb || breadcrumb) {
            if (bothdropBreadcrumb) {
                Intent intent = new Intent(QueueUserActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, R.anim.exit_slide_left);
            } else if (breadcrumb) {
                breadcrumb = false;
                header.setBackgroundColor(getResources().getColor(R.color.black));
                menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu));
                breadcrumb_like_rel.setVisibility(View.GONE);
                queue_rel.setVisibility(View.GONE);
                blur_rel.setVisibility(View.GONE);
                null_user_rel.setVisibility(View.VISIBLE);
                see_previous_visitors_rel.setVisibility(View.VISIBLE);
                name.setText("");
                age.setText("");
                return;
            }
        } else {
            Intent intent = new Intent(QueueUserActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, R.anim.exit_slide_left);
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
    }*/

    public void getBreadcrumb() {
        try {
            String low = userData.getString("low", "18");
            String high = userData.getString("high", "100");
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
            parameters.add(new Pair<String, String>("high", high));
            parameters.add(new Pair<String, String>("low", low));
            parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));
            parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
            parameters.add(new Pair<String, String>("wants_males", userData.getString("wantsMales", "1")));
            parameters.add(new Pair<String, String>("wants_females", userData.getString("wantsFemales", "1")));
            mAuthService.getBreacrumb(parameters,
                    new ApiJsonOperationCallback() {

                        @Override
                        public void onCompleted(JsonElement jsonElement,
                                                Exception exception,
                                                ServiceFilterResponse response) {
                            try {
                                if (exception == null) {
                                    System.out.println("BreadcrumbResult = " + jsonElement.toString());
                                    if (progressDia != null && progressDia.isShowing())
                                        progressDia.dismiss();
                                    breadcrumb = true;
                                    if (jsonElement != null && !jsonElement.toString().equalsIgnoreCase("null")) {
                                        jsonArray = new JSONArray(jsonElement.toString());
                                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                                        if (jsonObject.getInt("code") == 201) {
                                            breadcrumbtoggle = true;
                                            null_user_rel.setVisibility(View.GONE);
                                            breadcrumb_like_rel.setVisibility(View.VISIBLE);
                                            header.setBackgroundColor(getResources().getColor(R.color.breadcrumb_like_header_color));
                                            menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow));
                                            footer.setVisibility(View.GONE);
                                        } else {
                                            arrayCount = jsonArray.length();
                                            null_user_rel.setVisibility(View.GONE);
                                            footer.setVisibility(View.VISIBLE);
                                            see_previous_visitors_rel.setVisibility(View.GONE);
                                            menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow));
                                            mode = "1";
                                            countResult = 0;
                                            showQueue(jsonArray, countResult);
                                        }
                                    } else {
                                        null_user_rel.setVisibility(View.GONE);
                                        breadcrumb_like_rel.setVisibility(View.VISIBLE);
                                        header.setBackgroundColor(getResources().getColor(R.color.breadcrumb_like_header_color));
                                        menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow));
                                        footer.setVisibility(View.GONE);
                                    }
                                } else {
                                    System.out.println("BreadcrumbResponse = " + exception.toString());
                                    if (breadcrumb) {
                                        null_user_rel.setVisibility(View.GONE);
                                        breadcrumb_like_rel.setVisibility(View.VISIBLE);
                                        header.setBackgroundColor(getResources().getColor(R.color.breadcrumb_like_header_color));
                                        menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrow));
                                    }
                                    if (progressDia != null && progressDia.isShowing())
                                        progressDia.dismiss();
                                    null_user_rel.setVisibility(View.VISIBLE);
                                    footer.setVisibility(View.GONE);
                                    see_previous_visitors_rel.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                if (progressDia != null && progressDia.isShowing())
                                    progressDia.dismiss();
                                e.printStackTrace();
                            }
                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void breadcrumbLikedUser() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        mAuthService.breacrumbLikedUser(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement, Exception exception, ServiceFilterResponse response) {
                try {
                    if (exception == null) {
                        System.out.println("BreadcrumbLikedUserResult = " + jsonElement.toString());
                        if (progressDia != null && progressDia.isShowing())
                            progressDia.dismiss();
                        if (jsonElement != null && !jsonElement.toString().equalsIgnoreCase("null")) {
                            jsonArray = new JSONArray(jsonElement.toString());
                            if (jsonArray == null || jsonArray.length() == 0) {
                                Intent intent = new Intent(QueueUserActivity.this, ProfileActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(0, R.anim.exit_slide_left);
                            } else {
                                arrayCount = jsonArray.length();
                                null_user_rel.setVisibility(View.GONE);
                                footer.setVisibility(View.VISIBLE);
                                see_previous_visitors_rel.setVisibility(View.GONE);
                                mode = "1";
                                showQueue(jsonArray, countResult);
                            }
                        } else {
                            Intent intent = new Intent(QueueUserActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(0, R.anim.exit_slide_left);
                        }
                    } else {
                        System.out.println("BreadcrumbLikedUserResponse = " + exception.toString());
                        Intent intent = new Intent(QueueUserActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, R.anim.exit_slide_left);
                    }
                } catch (Exception e) {
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                    e.printStackTrace();
                }
            }
        });
    }

    public void reportInappropriate() {
        try {
            GlobalVariables.customLogin = false;
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("msg", "Report"));
            parameters.add(new Pair<String, String>("reported_id", matchID));
            parameters.add(new Pair<String, String>("reporter_id", userData.getString("userid", "")));
            mAuthService.reportInappropriate(parameters, new ApiJsonOperationCallback() {

                @Override
                public void onCompleted(JsonElement jsonElement, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        System.out.println("ReportResult = " + jsonElement.toString());
                        final CustomAlartDialog alart = new CustomAlartDialog(context, GlobalVariables.intro,
                                GlobalVariables.inappropriateMsg, "Okay");
                        alart.show();
                        CustomAlartDialog.okBtn.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                disLikeUser();
                                alart.dismiss();
                            }
                        });
                    } else {
                        System.out.println("ReportException = " + exception.toString());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disLikeUser() {
        blur_rel.setVisibility(View.GONE);
        imageDp.setImageBitmap(null);
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("joanie", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("chachi", matchID));
        parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));
        parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
        // parameters.add(new Pair<String, String>("mode", mode));
        System.out.println("chachi : " + matchID);
        mAuthService.dislikeUser(parameters,
                new ApiJsonOperationCallback() {
                    @Override
                    public void onCompleted(JsonElement jsonElement,
                                            Exception exception,
                                            ServiceFilterResponse response) {
                        if (exception == null) {
                            System.out.println("DislikeUserResult = " + jsonElement.toString());
                            name.setText("");
                            age.setText("");
                            report.setVisibility(View.GONE);
                            showQueue(jsonArray, countResult + 1);
                        } else {
                            System.out.println("DislikeUserException = " + exception.toString());
                        }
                    }
                });
    }

    public void likeUser() {
        blur_rel.setVisibility(View.GONE);
        imageDp.setImageBitmap(null);
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("joanie", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("chachi", matchID));
        parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));
        parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
        // parameters.add(new Pair<String, String>("mode", mode));
        mAuthService.likeUser(parameters,
                new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(JsonElement jsonElement,
                                            Exception exception,
                                            ServiceFilterResponse response) {

                        if (exception == null) {
                            System.out.println("LikeUserResult = " + jsonElement.toString());
                            name.setText("");
                            age.setText("");
                            report.setVisibility(View.GONE);
                            showQueue(jsonArray, countResult + 1);
                        } else {
                            System.out.println("LikeUserException = " + exception.toString());
                        }
                    }
                });
    }

    public void breadcrumbDisLike() {
        blur_rel.setVisibility(View.GONE);
        imageDp.setImageBitmap(null);
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("joanie", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("chachi", matchID));
        parameters.add(new Pair<String, String>("latitude", String.valueOf(lat)));
        parameters.add(new Pair<String, String>("longitude", String.valueOf(log)));
        // parameters.add(new Pair<String, String>("mode", mode));
        System.out.println("chachi : " + matchID);
        mAuthService.breadcrumbDislike(parameters,
                new ApiJsonOperationCallback() {
                    @Override
                    public void onCompleted(JsonElement jsonElement,
                                            Exception exception,
                                            ServiceFilterResponse response) {
                        if (exception == null) {
                            System.out.println("BreadcrumbDislikeUserResult = " + jsonElement.toString());
                            name.setText("");
                            age.setText("");
                            report.setVisibility(View.GONE);
                            showQueue(jsonArray, countResult + 1);
                        } else {
                            System.out.println("BreadcrumbDislikeUserException = " + exception.toString());
                        }
                    }
                });
    }

    public void breadcrumbLike() {
        blur_rel.setVisibility(View.GONE);
        imageDp.setImageBitmap(null);
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("joanie", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("chachi", matchID));
        parameters.add(new Pair<String, String>("latitude", String.valueOf(lat)));
        parameters.add(new Pair<String, String>("longitude", String.valueOf(log)));
        // parameters.add(new Pair<String, String>("mode", mode));
        mAuthService.breadcrumbLike(parameters,
                new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(JsonElement jsonElement,
                                            Exception exception,
                                            ServiceFilterResponse response) {

                        if (exception == null) {
                            System.out.println("BreadcrumbLikeUserResult = " + jsonElement.toString());
                            name.setText("");
                            age.setText("");
                            report.setVisibility(View.GONE);
                            showQueue(jsonArray, countResult + 1);
                        } else {
                            System.out.println("BreadcrumbLikeUserException = " + exception.toString());
                        }
                    }
                });
    }
}
