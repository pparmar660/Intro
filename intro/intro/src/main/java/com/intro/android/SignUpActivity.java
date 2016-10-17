package com.intro.android;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.appsee.Appsee;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomDialog;
import com.intro.customviews.CustomEditText;
import com.intro.customviews.CustomTextView;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableJsonOperationCallback;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

/**
 * This class is for the Register via !ntro / fb / twitter
 * <p/>
 * By Ankit Kumar and Mirza Faisal
 */

public class SignUpActivity extends BaseActivity {

    private Context context;
    private ConnectionDetector cd;
    private CustomEditText userEmail, password, conpassword;
    private CustomTextView BtnCreateAcc;
    private ImageButton btn_Facebook, btn_Twitter, back_arrow;
    private String fName = "", lName = "", email_address = "", facebookID = "",birthday = "", deviceid = "",loginAction = "";
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    private LoginButton facebookButton;
    private TwitterLoginButton twitterButton;
    private SharedPreferences userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Appsee.start(getString(R.string.appsee_apikey));
        context = this;
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        cd = new ConnectionDetector(getApplicationContext());
        facebookButton = (LoginButton) findViewById(R.id.facebook_button);
        back_arrow = (ImageButton) findViewById(R.id.back_arrow);
        try {
            twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_button);
            twitterButton.setCallback(new Callback<TwitterSession>() {

                @SuppressWarnings({"unchecked", "rawtypes"})
                @Override
                public void success(Result<TwitterSession> result) {
                    try {
                        progressDia = CustomDialog.show(context,GlobalVariables.loading);
                        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                        parameters.add(new Pair<String, String>("twitterid",String.valueOf(result.data.getUserId())));
                        parameters.add(new Pair<String, String>("email", ""));
                        parameters.add(new Pair<String, String>("bio", ""));
                        parameters.add(new Pair<String, String>("gender", "3"));
                        parameters.add(new Pair<String, String>("name", result.data.getUserName()));
                        parameters.add(new Pair<String, String>("birthday", ""));
                        parameters.add(new Pair<String, String>("device", ""));
                        mAuthService.loginUserWithTwitter(parameters,
                                new ApiJsonOperationCallback() {

                                    @Override
                                    public void onCompleted(
                                            final JsonElement jsonElement,
                                            Exception exception,
                                            ServiceFilterResponse response) {

                                        if (progressDia != null&& progressDia.isShowing())
                                            progressDia.dismiss();
                                        if (exception == null) {
                                            System.out.println("TwitterResult "+ jsonElement.toString());
                                            try {
                                                JSONObject jsonObject = new JSONObject(jsonElement.toString());
                                                mAuthService.saveTwitterData(jsonObject, context, true);
                                            } catch (Exception e) {
                                                System.out.println(jsonElement.toString());
                                            }

                                        } else {
                                            if (progressDia != null&& progressDia.isShowing())
                                                progressDia.dismiss();
                                            System.out.println("exception "+ exception.toString());
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (progressDia != null&& progressDia.isShowing())
                                    progressDia.dismiss();
                            }
                        });
                    }
                }

                @Override
                public void failure(TwitterException exception) {
                    exception.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        BtnCreateAcc = (CustomTextView) findViewById(R.id.create_acc);
        btn_Facebook = (ImageButton) findViewById(R.id.facebook);
        btn_Twitter = (ImageButton) findViewById(R.id.btn_Twitter);
        userEmail = (CustomEditText) findViewById(R.id.email);
        password = (CustomEditText) findViewById(R.id.password);
        conpassword = (CustomEditText) findViewById(R.id.conpassword);
        conpassword.setTransformationMethod(new PasswordTransformationMethod());
        password.setTransformationMethod(new PasswordTransformationMethod());
        addlistener();

    }

    private void addlistener() {
        back_arrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.exit_slide_left);
            }
        });

        btn_Twitter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loginAction = "twitter";
                twitterButton.performClick();
            }
        });

        btn_Facebook.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loginAction = "facebook";
                facebookButton.performClick();
            }
        });
        BtnCreateAcc.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (userEmail.getText().toString().equalsIgnoreCase("")) {
                    alart = new CustomAlartDialog(context,GlobalVariables.oh_snap,GlobalVariables.user_mane_error);
                    alart.show();
                } else if (!Utility.checkEmail(userEmail.getText().toString().trim())) {
                    alart = new CustomAlartDialog(context,GlobalVariables.oh_snap, GlobalVariables.ValidEmail);
                    alart.show();
                } else if (password.getText().toString().equalsIgnoreCase("")) {
                    alart = new CustomAlartDialog(context,GlobalVariables.oh_snap,GlobalVariables.password_error);
                    alart.show();
                } else if (password.length() < 7) {
                    alart = new CustomAlartDialog(context,GlobalVariables.oh_snap,GlobalVariables.password_count_error);
                    alart.show();
                } else if (conpassword.getText().toString().trim().length() == 0) {
                    alart = new CustomAlartDialog(context,GlobalVariables.oh_snap,GlobalVariables.repassBlank);
                    alart.show();
                } else if (!password.getText().toString().trim().equals(conpassword.getText().toString().trim())) {
                    alart = new CustomAlartDialog(context,GlobalVariables.oh_snap,GlobalVariables.pwd_not_equal);
                    alart.show();
                } else if (!cd.isConnectingToInternet()) {
                    alart = new CustomAlartDialog(context,GlobalVariables.oh_snap,GlobalVariables.network_error);
                    alart.show();
                } else {
                    try {
                        progressDia = CustomDialog.show(context,GlobalVariables.loading);
                        mAuthService.registerUser(userEmail.getText().toString(), password.getText().toString(),
                                userEmail.getText().toString(),new TableJsonOperationCallback() {
                                    @Override
                                    public void onCompleted(
                                            final JsonObject jsonObject,
                                            Exception exception,
                                            ServiceFilterResponse response) {

                                        if (progressDia != null&& progressDia.isShowing())
                                            progressDia.dismiss();
                                        if (exception == null) {
                                            // If that was successful, set and save the user data
                                            System.out.println("resultjsonObject"+ jsonObject.toString());
                                            try {
                                                mAuthService.setUserAndSaveData(jsonObject,userEmail.getText().toString());
                                                Intent loggedInIntent = new Intent(context,SetProfileActivity.class);
                                                loggedInIntent.putExtra(GlobalVariables.comeFrom, "Signup");
                                                loggedInIntent.putExtra("signUp", true);
                                                loggedInIntent.putExtra("cancel", true);
                                                startActivity(loggedInIntent);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Utility.validationByToastMessage(context,jsonObject.toString());
                                                    }
                                                });
                                            }
                                            finish();
                                        } else {
                                            alart = new CustomAlartDialog(context,GlobalVariables.intro,GlobalVariables.user_already_exist);
                                            alart.show();
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (progressDia != null&& progressDia.isShowing())
                                    progressDia.dismiss();
                            }
                        });
                    }
                }
            }
        });
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        // Note: Your consumer key and secret should be obfuscated in your
        // source code before shipping.
        @Override
        public void call(Session session, SessionState state,Exception exception) {
            Log.e("FB", "Logged statusCallback...");
            onSessionStateChange(session, state, exception);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void onSessionStateChange(final Session session,SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.e("FB", "Logged in...");
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    Log.i("FB", "onCompleted");
                    if (user != null) {
                        String fbAccessToken = session.getAccessToken();
                        getFbDatatoRegister(user, fbAccessToken);
                    }
                }
            }).executeAsync();
        } else if (state.isClosed()) {
            Log.e("FB", "Logged out...");
        }
    }

    public void getFbDatatoRegister(GraphUser user, final String fbAccessToken) {
        try {
            progressDia = CustomDialog.show(context,GlobalVariables.loading);
            facebookID = user.getId();
            lName = user.getLastName();
            fName = user.getFirstName();
            String fbName = user.getName();
            birthday = user.getBirthday();
            String gender = user.asMap().get("gender").toString();
            try {
                email_address = user.asMap().get("email").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (email_address != null) {
                Log.e("FB", "Logged in.." + facebookID + fbName + "lName"+ lName + "fName" + fName);
                try {
                    Log.e("FB", "Logged in.." + facebookID + " -- " + fbName+ " -- " + birthday +
                            " -- " + deviceid + " -- "+ email_address + " -- " + gender);
                    int genderBit = 0;
                    if (gender.equalsIgnoreCase("male")) {
                        genderBit = 1;
                    }
                    final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                    parameters.add(new Pair<String, String>("facebookid"+ "", facebookID));
                    parameters.add(new Pair<String, String>("email",email_address));
                    parameters.add(new Pair<String, String>("bio", ""));
                    parameters.add(new Pair<String, String>("gender", genderBit+ ""));
                    parameters.add(new Pair<String, String>("name", fName));
                    if (birthday == null)
                        birthday = "";
                    parameters.add(new Pair<String, String>("birthday",birthday));
                    parameters.add(new Pair<String, String>("device", ""));
                    mAuthService.registerUserWithFb(parameters,new ApiJsonOperationCallback() {

                                @Override
                                public void onCompleted(
                                        final JsonElement jsonElement,
                                        Exception exception,
                                        ServiceFilterResponse response) {
                                    if (progressDia != null&& progressDia.isShowing())
                                        progressDia.dismiss();
                                    if (exception == null) {
                                        System.out.println("result "+ jsonElement.toString());
                                        try {
                                            JSONArray jarray = new JSONArray(jsonElement.toString());
                                            JSONArray jarray1 = jarray.getJSONArray(0);
                                            JSONObject jsonObject = jarray1.getJSONObject(0);
                                            mAuthService.saveUserData(jsonObject, birthday, fbAccessToken, false, context, true);
                                            // "" is accestoken only provided in fb login case,false is for myprofile paramter
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        System.out.println("exception "+exception.toString());
                                    }
                                }

                            });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (progressDia != null && progressDia.isShowing())
                                progressDia.dismiss();
                        }
                    });
                }
            } else {
                // Clear all session info & ask user to login again
                Session session = Session.getActiveSession();
                if (session != null) {
                    session.closeAndClearTokenInformation();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        try {
            if (loginAction.equalsIgnoreCase("twitter")) {
                twitterButton.onActivityResult(requestCode, responseCode,intent);
            } else if (loginAction.equalsIgnoreCase("facebook")) {
                uiHelper.onActivityResult(requestCode, responseCode, intent);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.exit_slide_left);
    }
}
