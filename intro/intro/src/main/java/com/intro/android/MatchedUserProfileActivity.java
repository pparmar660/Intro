package com.intro.android;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsee.Appsee;
import com.google.gson.JsonElement;
import com.intro.adapter.ProfileImageViewPagerAdapter;
import com.intro.customviews.CustomAlartDialogTwoBtn;
import com.intro.customviews.CustomDialog;
import com.intro.customviews.CustomTextView;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;


/**
 * This class is used for show matched user profile and also give the options to block the users.
 * <p/>
 * By Ankit Kumar and Mirza Faisal
 */

public class MatchedUserProfileActivity extends BaseActivity {

    private PopupWindow dialog;
    private Context context;
    private ImageButton back_arrow;
    private ImageView popup;
    private TextView name_tv, age_tv;
    private CustomTextView personDesrciption, block, report;
    private SharedPreferences userData;
    private ViewPager profileImageViewPager;
    private ProfileImageViewPagerAdapter profileImageViewPagerAdapter;
    private ArrayList<String> imageURL = new ArrayList<String>();
    private String matchUserId, matchUserName;
    private ImageView bullet_one, bullet_two, bullet_three;
    private String[] imageArra = new String[3];
    private RelativeLayout header;
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_ID_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_matched_user_profile);
        Appsee.start(getString(R.string.appsee_apikey));
        profileImageViewPager = (ViewPager) findViewById(R.id.matchedUserImageViewPager);
        bullet_one = (ImageView) findViewById(R.id.bullet_one);
        bullet_two = (ImageView) findViewById(R.id.bullet_two);
        bullet_three = (ImageView) findViewById(R.id.bullet_three);
        back_arrow = (ImageButton) findViewById(R.id.back_arrow);
        popup = (ImageView) findViewById(R.id.popup);
        name_tv = (TextView) findViewById(R.id.name_tv);
        age_tv = (TextView) findViewById(R.id.age_tv);
        personDesrciption = (CustomTextView) findViewById(R.id.matchedUserDescriptionTextView);
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        header = (RelativeLayout) findViewById(R.id.header);

        if(!Utility.hasPermissions(MatchedUserProfileActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(MatchedUserProfileActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
        }else{
            setData();
        }
        adlistner();
    }

    public void setData(){
        ((RelativeLayout) findViewById(R.id.matchedprofile_Relative_Layout))
                .post(new Runnable() {

                    @Override
                    public void run() {
                        progressDia = CustomDialog.show(MatchedUserProfileActivity.this,GlobalVariables.loading);
                    }
                });

        matchUserId = getIntent().getStringExtra("matchUserId");
        imageURL.add(matchUserId + "-1");
        imageURL.add(matchUserId + "-2");
        imageURL.add(matchUserId + "-3");
        profileImageViewPagerAdapter = new ProfileImageViewPagerAdapter(context, imageURL);
        profileImageViewPager.setAdapter(profileImageViewPagerAdapter);
        profileImageViewPager.setCurrentItem(0);
        if (imageArra.length < 2) {
            bullet_three.setVisibility(View.GONE);
            bullet_two.setVisibility(View.GONE);
        } else if (imageArra.length < 3) {
            bullet_three.setVisibility(View.GONE);
        }

        getUserData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode){
            case REQUEST_ID_PERMISSIONS:
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setData();
//                }
                break;
        }
    }


    private void getUserData() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", matchUserId));
        mAuthService.getUserData(parameters, new ApiJsonOperationCallback() {

            @Override
            public void onCompleted(final JsonElement jsonElement,
                                    Exception exception, ServiceFilterResponse response) {
                System.out.println("GetUserResult = " + jsonElement.toString());
                if (progressDia != null && progressDia.isShowing())
                    progressDia.dismiss();
                if (exception == null) {
                    JSONArray jsonArray;
                    JSONObject jsonObject;
                    try {
                        jsonArray = new JSONArray(jsonElement.toString());
                        jsonObject = jsonArray.getJSONObject(0);
                        matchUserName = decodeUTF(jsonObject.getString("name"));
                        name_tv.setText(matchUserName);
                        age_tv.setText(" | " + jsonObject.getString("age"));
                        personDesrciption.setText(decodeUTF(jsonObject.getString("bio")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Utility.validationByToastMessage(context,jsonElement.toString());
                            }
                        });
                    }
                } else {
                    System.out.println("GetUserException = "+ exception.toString());
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void adlistner() {
        back_arrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(context, ChatActivity.class);
//                startActivity(intent);
                finish();
                overridePendingTransition(0, R.anim.exit_slide_left);
            }
        });

        popup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.dialog_user_profile,(ViewGroup) findViewById(R.id.popup_element));
                dialog = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                dialog.setOutsideTouchable(true);
                dialog.setBackgroundDrawable(new ColorDrawable());
                dialog.setTouchInterceptor(new View.OnTouchListener() { // or whatever you want
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) // here I want to close the pw when clicking outside it but at all this is just an example of how it works and you can implement the onTouch() or the onKey() you want
                        {
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }

                });
                dialog.showAtLocation(layout, Gravity.TOP | Gravity.RIGHT, 0, header.getHeight() + 50);
                block = (CustomTextView) layout.findViewById(R.id.block);
                report = (CustomTextView) layout.findViewById(R.id.report);
                report.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        sendEmail();
                    }
                });

                block.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            dialog.dismiss();
                            final CustomAlartDialogTwoBtn alart = new CustomAlartDialogTwoBtn(context, GlobalVariables.intro,
                                    GlobalVariables.blockUser + matchUserName+"?", "Cancel", "Yes");
                            alart.show();
                            CustomAlartDialogTwoBtn.okBtn.setOnClickListener(new OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            alart.dismiss();
                                            progressDia = CustomDialog.show(MatchedUserProfileActivity.this,GlobalVariables.loading);
                                            blockUser();
                                        }
                                    });
                            CustomAlartDialogTwoBtn.cancelBtn.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alart.dismiss();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        profileImageViewPager.setOnPageChangeListener(new OnPageChangeListener() {
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
        });

    }

    protected void sendEmail() {
        try {
            String to = GlobalVariables.to_contact;
            String subject = "Report this user";
            String message = "!ntro, A user named "+matchUserName+", ("+matchUserId+") is a real creep!";
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

    public void blockUser() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("joanie", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("chachi", matchUserId));
        mAuthService.blockUser(parameters, new ApiJsonOperationCallback() {

            @Override
            public void onCompleted(JsonElement jsonElement, Exception exception,ServiceFilterResponse response) {
                if (progressDia != null&& progressDia.isShowing())
                    progressDia.dismiss();
                System.out.println("BlockUserResult : " + jsonElement.toString());
                if (exception == null) {
                    Intent intent = new Intent(MatchedUserProfileActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.exit_slide_left);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(context, ChatActivity.class);
//        startActivity(intent);
        finish();
        overridePendingTransition(0, R.anim.exit_slide_left);
    }
}
