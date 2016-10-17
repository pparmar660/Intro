package com.intro.android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appsee.Appsee;
import com.facebook.Session;
import com.google.gson.JsonElement;
import com.intro.adapter.MatchesAdapter;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomAlartDialogTwoBtn;
import com.intro.customviews.CustomTextView;
import com.intro.customviews.RangeSeekBar;
import com.intro.customviews.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

/**
 * This class is for setting in application like age limit/ gender/ rate /
 * contact / sign out / and delete account
 * <p/>
 * By Ankit Kumar and Mirza Faisal
 */

public class SettingsActivity extends BaseActivity {

    private ImageView back_arrow;
    private CustomTextView versionNo;
    private CheckBox chk_guys, chk_girls;
    private LinearLayout edit_lay, rate_lay, contact_lay, signout_lay, delete_lay;
    private Context context;
    private RangeSeekBar<Integer> rangeSeekBar;
    private LinearLayout lay_girls, lay_guys;
    private SharedPreferences userData;
    private String wantMale, wantFemale, high, low;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_settings);
        Appsee.start(getString(R.string.appsee_apikey));

        back_arrow = (ImageView) findViewById(R.id.back_arrow);
        chk_guys = (CheckBox) findViewById(R.id.chk_guys);
        chk_girls = (CheckBox) findViewById(R.id.chk_girls);
        edit_lay = (LinearLayout) findViewById(R.id.edit_lay);
        rate_lay = (LinearLayout) findViewById(R.id.rate_lay);
        contact_lay = (LinearLayout) findViewById(R.id.contact_lay);
        signout_lay = (LinearLayout) findViewById(R.id.signout_lay);
        delete_lay = (LinearLayout) findViewById(R.id.delete_lay);
        rangeSeekBar = (RangeSeekBar<Integer>) findViewById(R.id.rangeSeekBar);
        lay_girls = (LinearLayout) findViewById(R.id.lay_girls);
        lay_guys = (LinearLayout) findViewById(R.id.lay_guys);
        versionNo = (CustomTextView) findViewById(R.id.versionNo);

        versionNo.setText("Version "+getString(R.string.version));
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        wantFemale = userData.getString("wantsFemales", "1");
        wantMale = userData.getString("wantsMales", "1");
        if (wantFemale.equalsIgnoreCase("1")) {
            chk_girls.setChecked(true);
        } else {
            chk_girls.setChecked(false);
        }
        if (wantMale.equalsIgnoreCase("1")) {
            chk_guys.setChecked(true);
        } else {
            chk_guys.setChecked(false);
        }
        addlistener();
    }

    private void addlistener() {
        try {
            low = userData.getString("low", "18");
            high = userData.getString("high", "100");
            int lower_double = Integer.valueOf(low);
            int higher_double = Integer.valueOf(high);
            // Set the range
            rangeSeekBar.setSelectedMaxValue(higher_double);
            rangeSeekBar.setSelectedMinValue(lower_double);
            rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
                @Override
                public void onRangeSeekBarValuesChanged(
                        RangeSeekBar<?> bar, Integer minValue,
                        Integer maxValue) {
                    low = minValue.toString();
                    high = maxValue.toString();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        back_arrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                updateUserData();
            }
        });
        edit_lay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences.Editor preferencesEditor = userData.edit();
                preferencesEditor.putString("twitter", "");
                preferencesEditor.commit();
                Intent inteni = new Intent(context, SetProfileActivity.class);
                inteni.putExtra(GlobalVariables.comeFrom, "Setting");
                inteni.putExtra("signUp", false);
                inteni.putExtra("cancel", false);
                inteni.putExtra(GlobalVariables.comeFrom, GlobalVariables.settingActivity);
                context.startActivity(inteni);
                ((Activity) context).overridePendingTransition(0,
                        R.anim.exit_slide_right);
            }
        });

        rate_lay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Rate my app
                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    context.startActivity(goToMarket);
                    ((Activity) context).overridePendingTransition(0, R.anim.exit_slide_right);
                } catch (ActivityNotFoundException e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            Utility.validationByToastMessage(context, "Could not open Android market, please install the market app.");
                        }
                    });
                }
            }
        });

        contact_lay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    String to = GlobalVariables.to_contact;
                    String subject = "!ntro";
                    String message = GlobalVariables.to_body;
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
                    email.putExtra(Intent.EXTRA_SUBJECT, subject);
                    email.putExtra(Intent.EXTRA_TEXT, message);
                    email.setType("message/rfc822");
                    startActivity(Intent.createChooser(email, "Choose an Email client"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        signout_lay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    try {
                        updateDeviceId();
                        GlobalVariables.customLogin = false;
                        if (TimerService.isRunningService) {
                            TimerService.isRunningService = false;
                            TimerService.mTimer.cancel();
                        }
                        stopService(new Intent(context, TimerService.class));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        mAuthService.logout(false, context);
                        Session session = Session.getActiveSession();
                        if (session != null) {
                            if (!session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        } else {
                            session = new Session(context);
                            Session.setActiveSession(session);
                            session.closeAndClearTokenInformation();
                        }
                        Session.setActiveSession(null);

                        try {
                            MatchesAdapter.timer.cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        delete_lay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    CustomAlartDialogTwoBtn alart = new CustomAlartDialogTwoBtn(context, GlobalVariables.intro,
                            GlobalVariables.delete_user, "No", "Yes");
                    alart.show();
                    CustomAlartDialogTwoBtn.okBtn.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            deleteUser();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        lay_girls.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (chk_girls.isChecked()) {
                    chk_girls.setChecked(false);
                    wantFemale = "0";
                } else {
                    chk_girls.setChecked(true);
                    wantFemale = "1";
                }
            }
        });

        lay_guys.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (chk_guys.isChecked()) {
                    chk_guys.setChecked(false);
                    wantMale = "0";
                } else {
                    chk_guys.setChecked(true);
                    wantMale = "1";
                }
            }
        });
    }

    private void updateDeviceId() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("deviceid", ""));
        mAuthService.updateDeviceId(parameters,
                new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(final JsonElement jsonElement, Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            System.out.println("DevieceResult = " + jsonElement.toString());
                        } else {
                            System.out.println("DeviceException = " + exception.toString());
                        }
                    }
                });
    }

    public void deleteUser() {
        try {
            updateDeviceId();
            GlobalVariables.customLogin = false;
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
            mAuthService.deleteUser(parameters, new ApiJsonOperationCallback() {

                @Override
                public void onCompleted(JsonElement jsonElement, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        System.out.println("DeleteUserResult = " + jsonElement.toString());
                        mAuthService.logout(false, context);
                    } else {
                        System.out.println("DeleteUserException = " + exception.toString());
                    }
                }
            });
            updateDeviceId();
            if (TimerService.isRunningService) {
                TimerService.isRunningService = false;
                TimerService.mTimer.cancel();
            }
            stopService(new Intent(context, TimerService.class));
            if(MatchesAdapter.timer != null){
                MatchesAdapter.timer.cancel();
            }
            mAuthService.logout(false, context);
            Session session = Session.getActiveSession();
            if (session != null) {
                if (!session.isClosed()) {
                    session.closeAndClearTokenInformation();
                }
            } else {
                session = new Session(context);
                Session.setActiveSession(session);
                session.closeAndClearTokenInformation();
            }
            Session.setActiveSession(null);
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            overridePendingTransition(0, R.anim.exit_slide_right);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUserData() {
        try {
            SharedPreferences.Editor preferencesEditor = userData.edit();
            preferencesEditor.putString("high", high);
            preferencesEditor.putString("low", low);
            preferencesEditor.putString("wantsMales", wantMale);
            preferencesEditor.putString("wantsFemales", wantFemale);
            preferencesEditor.commit();
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, R.anim.exit_slide_left);
            String firebaseID = userData.getString("firebaseid", "");
            if (firebaseID.equalsIgnoreCase("null")) {
                firebaseID = "";
            }
            final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
            parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
            parameters.add(new Pair<String, String>("email", userData.getString("email", "")));
            parameters.add(new Pair<String, String>("bio", encodeUTF(userData.getString("bio", ""))));
            parameters.add(new Pair<String, String>("age", userData.getString("age", "")));
            parameters.add(new Pair<String, String>("deviceid", userData.getString("userid", "").replaceAll("-", "")));
            parameters.add(new Pair<String, String>("gender", userData.getString("gender", "")));
            parameters.add(new Pair<String, String>("name", encodeUTF(userData.getString("name", ""))));
            parameters.add(new Pair<String, String>("birthday", userData.getString("birthday", "")));
            parameters.add(new Pair<String, String>("high", high));
            parameters.add(new Pair<String, String>("low", low));
            parameters.add(new Pair<String, String>("wants_males", wantMale));
            parameters.add(new Pair<String, String>("wants_females", wantFemale));
            parameters.add(new Pair<String, String>("firebaseid", firebaseID));
            parameters.add(new Pair<String, String>("facebookid", userData.getString("facebookid", "")));
            parameters.add(new Pair<String, String>("image1_uploaded", userData.getString("Image1Uploaded", "0")));
            parameters.add(new Pair<String, String>("image2_uploaded", userData.getString("Image2Uploaded", "0")));
            parameters.add(new Pair<String, String>("image3_uploaded", userData.getString("Image3Uploaded", "0")));
            mAuthService.setUserProfile(parameters,
                    new ApiJsonOperationCallback() {
                        @Override
                        public void onCompleted(JsonElement jsonElement, Exception error, ServiceFilterResponse serviceFilterResponse) {
                            if (error != null) {
                                alart = new CustomAlartDialog(context, GlobalVariables.oh_snap, error.toString());
                                alart.show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        updateUserData();
        return;
    }

}