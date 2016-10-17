package com.intro.android;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.appsee.Appsee;
import com.google.gson.JsonElement;
import com.intro.adapter.NearByBreadvrumbAdapter;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomDialog;
import com.intro.customviews.CustomTextView;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GPSTracker;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

/**
 * This class is used for ready the user how long in hours it want to online and alse select
 * miles to find its matched uesr in its selected radius.
 * <p/>
 * 3 words are filed to show other users when they search matched users.
 * <p/>
 * By Ankit Kumar and Mirza Faisal
 */

@SuppressLint("SimpleDateFormat")
public class GetReadyActivity extends BaseActivity implements OnClickListener {
    private CustomTextView BtnTime1, BtnTime2, BtnTime3, BtnTime4, BtnRad1, BtnRad2, BtnRad3, BtnRad4, BtnGoIntro, preview;
    private EditText et_threeWordsIntro;
    private int selectedTime = 1, counter = 0;
    private double selectedRaduis = .25;
    private Context context;
    private ImageButton back_arrow;
    private SharedPreferences userData;
    private String endTime, maleStr, femaleStr;
    private double lat, log;
    private ConnectionDetector cd;
    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_ID_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getready);
        Appsee.start(getString(R.string.appsee_apikey));
        context = this;
        cd = new ConnectionDetector(context);
        BtnTime1 = (CustomTextView) findViewById(R.id.btn_time1);
        BtnTime2 = (CustomTextView) findViewById(R.id.btn_time2);
        BtnTime3 = (CustomTextView) findViewById(R.id.btn_time3);
        BtnTime4 = (CustomTextView) findViewById(R.id.btn_time4);
        BtnRad1 = (CustomTextView) findViewById(R.id.btn_rad1);
        BtnRad2 = (CustomTextView) findViewById(R.id.btn_rad2);
        BtnRad3 = (CustomTextView) findViewById(R.id.btn_rad3);
        BtnRad4 = (CustomTextView) findViewById(R.id.btn_rad4);
        back_arrow = (ImageButton) findViewById(R.id.back_arrow);
        BtnGoIntro = (CustomTextView) findViewById(R.id.goIntroButton);
        preview = (CustomTextView) findViewById(R.id.previewButton);
        et_threeWordsIntro = (EditText) findViewById(R.id.et_threeWordsIntro);
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        BtnTime1.setOnClickListener(this);
        BtnTime2.setOnClickListener(this);
        BtnTime3.setOnClickListener(this);
        BtnTime4.setOnClickListener(this);
        BtnRad1.setOnClickListener(this);
        BtnRad2.setOnClickListener(this);
        BtnRad3.setOnClickListener(this);
        BtnRad4.setOnClickListener(this);
        BtnGoIntro.setOnClickListener(this);
        back_arrow.setOnClickListener(this);
        preview.setOnClickListener(this);
        et_threeWordsIntro.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(final CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int spaceCount = spacecount(et_threeWordsIntro.getText().toString());
                System.out.println("Count: " + spaceCount);
                if (spaceCount == 3) {//count == 0 && wordsLength >= 3
                    setCharlimit(et_threeWordsIntro, et_threeWordsIntro.getText().length());
                    final CustomAlartDialog alart = new CustomAlartDialog(getApplicationContext(),
                            GlobalVariables.intro, GlobalVariables.only3words);
                    alart.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alart.show();
                } else {
                    removeFilter(et_threeWordsIntro);
                }
            }
        });

//        gpsTracker = new GPSTracker(context);
//        lat = gpsTracker.getLatitude();
//        log = gpsTracker.getLongitude();
    }

    private int spacecount(String str) {
        int spaceCount = 0;
        for (char c : str.toCharArray()) {
            if (c == ' ') {
                spaceCount++;
            }
        }
        return spaceCount;
    }

    private int countwords(String s) {
        String trim = s.trim();
        if (trim.isEmpty())
            return 0;
        return trim.split("\\s+").length; // separate string around spaces
    }

    private InputFilter filter;

    private void setCharlimit(EditText et, int max) {
        filter = new InputFilter.LengthFilter(max);
        et.setFilters(new InputFilter[]{filter});
    }

    private void removeFilter(EditText et) {
        if (filter != null) {
            et.setFilters(new InputFilter[0]);
            filter = null;
        }
    }

    void userCheckins() {
        if (cd.isConnectingToInternet()) {
            try {
                gpsTracker = new GPSTracker(context);
                lat = gpsTracker.getLatitude();
                log = gpsTracker.getLongitude();
                progressDia = CustomDialog.show(context, "Checking for nearby breadcrumb matches...");
                String[] threeWordsIntro = new String[]{"", "", ""};
                String threewords = et_threeWordsIntro.getText().toString();
                if (threewords.contains("'")) {
                    threewords = threewords.replaceAll("'", "''");
                }
                threeWordsIntro = threewords.split(" ");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 2);
//                cal.add(Calendar.HOUR, selectedTime);
                Date date = cal.getTime();
                String startTime = sdf.format(new Date());
                endTime = sdf.format(date);

                maleStr = userData.getString("wantsMales", "1");
                femaleStr = userData.getString("wantsFemales", "1");

                final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
                parameters.add(new Pair<String, String>("start_time", startTime));
                parameters.add(new Pair<String, String>("end_time", endTime));
                parameters.add(new Pair<String, String>("longitude", String.valueOf(log)));
                parameters.add(new Pair<String, String>("latitude", String.valueOf(lat)));
                parameters.add(new Pair<String, String>("distance", selectedRaduis + ""));
                try {
                    parameters.add(new Pair<String, String>("haiku1", encodeUTF(threeWordsIntro[0])));
                } catch (Exception e) {
                    parameters.add(new Pair<String, String>("haiku1", ""));
                }
                try {
                    parameters.add(new Pair<String, String>("haiku2", encodeUTF(threeWordsIntro[1])));
                } catch (Exception e) {
                    parameters.add(new Pair<String, String>("haiku2", ""));
                }
                try {
                    parameters.add(new Pair<String, String>("haiku3", encodeUTF(threeWordsIntro[2])));
                } catch (Exception e) {
                    parameters.add(new Pair<String, String>("haiku3", ""));
                }
                parameters.add(new Pair<String, String>("wants_males", maleStr));
                parameters.add(new Pair<String, String>("wants_females", femaleStr));
                mAuthService.callUserCheckins(parameters,
                        new ApiJsonOperationCallback() {

                            @Override
                            public void onCompleted(JsonElement jsonElement,
                                                    Exception exception, ServiceFilterResponse arg2) {
                                if (exception == null) {
                                    System.out.println("GetReadyResult = " + jsonElement.toString());
                                    notificationService();
                                    updateTime();
                                } else {
                                    System.out.println("GetReadyException = " + exception.toString());
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (progressDia != null && progressDia.isShowing())
                progressDia.dismiss();
            alart = new CustomAlartDialog(context, GlobalVariables.oh_snap, GlobalVariables.network_error);
            alart.show();
        }
    }

    public void notificationService() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("latitude", String.valueOf(lat)));
        parameters.add(new Pair<String, String>("longitude", String.valueOf(log)));
        mAuthService.callUserCheckins2(parameters,
                new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(JsonElement jsonElement,
                                            Exception exception, ServiceFilterResponse arg2) {
                        if (exception == null) {
                            System.out.println("NotificationResult = " + jsonElement.toString());
                        } else {
                            System.out.println("NotificationException = " + exception.toString());
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
                            System.out.println("UpdateTimeResult = " + jsonElement.toString());
                            try {
                                JSONObject jsonObject = new JSONObject(jsonElement.toString());
                                SharedPreferences settings = context.getSharedPreferences("UserData", 0);
                                SharedPreferences.Editor preferencesEditor = settings.edit();
                                preferencesEditor.putString("expires", jsonObject.getString("expires"));
                                preferencesEditor.putString("expires2", jsonObject.getString("expires2"));
                                preferencesEditor.commit();
                                final Intent intent1 = new Intent(GetReadyActivity.this, TimerService.class);
                                startService(intent1);
                                nearByBreadCrumb();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("UpdateTimeException = " + exception.toString());
                        }
                    }
                });
    }

    public void nearByBreadCrumb() {
        try {
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
            parameters.add(new Pair<String, String>("lon", String.valueOf(log)));
            parameters.add(new Pair<String, String>("lat", String.valueOf(lat)));
            mAuthService.nearByBreadcrumb(parameters, new ApiJsonOperationCallback() {

                @Override
                public void onCompleted(JsonElement jsonElement,
                                        Exception exception, ServiceFilterResponse response) {
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                    if (exception == null) {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonElement.toString());
                            int code = jsonObject.getInt("code");
                            System.out.println("NearByBreadcrumbResult = " + jsonElement.toString());
                            if(code == 200) {
                                Intent intent = new Intent(context, NearByBreadcrumbActivity.class);
                                intent.putExtra("data", jsonElement.toString());
                                startActivity(intent);
                                finish();
                                overridePendingTransition(0, R.anim.exit_slide_right);
                            }else{
                                Intent intent = new Intent(context, QueueUserActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(0, R.anim.exit_slide_right);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("NearByBreadcrumbException = " + exception.toString());
                        Intent intent = new Intent(context, QueueUserActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, R.anim.exit_slide_right);
                    }
                }
            });
        }catch (Exception e){
            runOnUiThread(new Runnable() {
                public void run() {
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                }
            });
            e.printStackTrace();
        }
    }

  /*  public void preview(){
        if(!et_threeWordsIntro.getText().toString().isEmpty()) {
            String[] threeWordsIntro = new String[]{"", "", ""};
            String threewords = et_threeWordsIntro.getText().toString();
            if (threewords.contains("'")) {
                threewords = threewords.replaceAll("'", "''");
            }
            threeWordsIntro = threewords.split(" ");
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode){
            case REQUEST_ID_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    userCheckins();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back_arrow:
                finish();
                overridePendingTransition(0, R.anim.exit_slide_left);
                break;
            case R.id.previewButton:
                String haiKu = "";
                if(!et_threeWordsIntro.getText().toString().isEmpty()){
                    haiKu = et_threeWordsIntro.getText().toString();
                }
                Intent intent = new Intent(GetReadyActivity.this, PreviewActivity.class);
                intent.putExtra("haiKu", haiKu);
                startActivity(intent);
                overridePendingTransition(0, R.anim.exit_slide_right);
                break;
            case R.id.goIntroButton:
                if(!Utility.hasPermissions(GetReadyActivity.this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(GetReadyActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
                }else {
                    userCheckins();
                }
                break;
            case R.id.btn_time1:
                selectedTime = 1;
                BtnTime1.setBackgroundResource(R.drawable.left_button_selector);
                BtnTime1.setTextColor(getResources().getColor(R.color.pink));
                BtnTime2.setBackgroundResource(R.drawable.middle_border_button);
                BtnTime2.setTextColor(Color.WHITE);
                BtnTime3.setBackgroundResource(R.drawable.middle_border_button);
                BtnTime3.setTextColor(Color.WHITE);
                BtnTime4.setBackgroundResource(R.drawable.right_border_button);
                BtnTime4.setTextColor(Color.WHITE);
                break;
            case R.id.btn_time2:
                selectedTime = 2;
                BtnTime1.setBackgroundResource(R.drawable.left_border_button);
                BtnTime2.setBackgroundResource(R.drawable.middle_button_selector);
                BtnTime3.setBackgroundResource(R.drawable.middle_border_button);
                BtnTime4.setBackgroundResource(R.drawable.right_border_button);
                BtnTime1.setTextColor(Color.WHITE);
                BtnTime2.setTextColor(getResources().getColor(R.color.pink));
                BtnTime3.setTextColor(Color.WHITE);
                BtnTime4.setTextColor(Color.WHITE);
                break;
            case R.id.btn_time3:
                selectedTime = 3;
                BtnTime1.setBackgroundResource(R.drawable.left_border_button);
                BtnTime3.setBackgroundResource(R.drawable.middle_button_selector);
                BtnTime2.setBackgroundResource(R.drawable.middle_border_button);
                BtnTime4.setBackgroundResource(R.drawable.right_border_button);
                BtnTime1.setTextColor(Color.WHITE);
                BtnTime2.setTextColor(Color.WHITE);
                BtnTime3.setTextColor(getResources().getColor(R.color.pink));
                BtnTime4.setTextColor(Color.WHITE);
                break;
            case R.id.btn_time4:
                selectedTime = 4;
                BtnTime1.setBackgroundResource(R.drawable.left_border_button);
                BtnTime2.setBackgroundResource(R.drawable.middle_border_button);
                BtnTime3.setBackgroundResource(R.drawable.middle_border_button);
                BtnTime4.setBackgroundResource(R.drawable.right_button_selector);
                BtnTime1.setTextColor(Color.WHITE);
                BtnTime2.setTextColor(Color.WHITE);
                BtnTime4.setTextColor(getResources().getColor(R.color.pink));
                BtnTime3.setTextColor(Color.WHITE);
                break;
            case R.id.btn_rad1:
                selectedRaduis = .25;
                BtnRad1.setBackgroundResource(R.drawable.left_button_selector);
                BtnRad2.setBackgroundResource(R.drawable.middle_border_button);
                BtnRad3.setBackgroundResource(R.drawable.middle_border_button);
                BtnRad4.setBackgroundResource(R.drawable.right_border_button);
                BtnRad2.setTextColor(Color.WHITE);
                BtnRad3.setTextColor(Color.WHITE);
                BtnRad1.setTextColor(getResources().getColor(R.color.pink));
                BtnRad4.setTextColor(Color.WHITE);
                break;
            case R.id.btn_rad2:
                selectedRaduis = .5;
                BtnRad1.setBackgroundResource(R.drawable.left_border_button);
                BtnRad2.setBackgroundResource(R.drawable.middle_button_selector);
                BtnRad3.setBackgroundResource(R.drawable.middle_border_button);
                BtnRad4.setBackgroundResource(R.drawable.right_border_button);
                BtnRad1.setTextColor(Color.WHITE);
                BtnRad3.setTextColor(Color.WHITE);
                BtnRad2.setTextColor(getResources().getColor(R.color.pink));
                BtnRad4.setTextColor(Color.WHITE);
                break;
            case R.id.btn_rad3:
                selectedRaduis = 1;
                BtnRad1.setBackgroundResource(R.drawable.left_border_button);
                BtnRad3.setBackgroundResource(R.drawable.middle_button_selector);
                BtnRad2.setBackgroundResource(R.drawable.middle_border_button);
                BtnRad4.setBackgroundResource(R.drawable.right_border_button);
                BtnRad1.setTextColor(Color.WHITE);
                BtnRad2.setTextColor(Color.WHITE);
                BtnRad3.setTextColor(getResources().getColor(R.color.pink));
                BtnRad4.setTextColor(Color.WHITE);
                break;
            case R.id.btn_rad4:
                selectedRaduis = 2;
                BtnRad1.setBackgroundResource(R.drawable.left_border_button);
                BtnRad2.setBackgroundResource(R.drawable.middle_border_button);
                BtnRad3.setBackgroundResource(R.drawable.middle_border_button);
                BtnRad4.setBackgroundResource(R.drawable.right_button_selector);
                BtnRad1.setTextColor(Color.WHITE);
                BtnRad3.setTextColor(Color.WHITE);
                BtnRad4.setTextColor(getResources().getColor(R.color.pink));
                BtnRad2.setTextColor(Color.WHITE);
                break;
        }
    }
}
