package com.intro.android;

import java.util.ArrayList;
import java.util.Arrays;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
import com.intro.customviews.CustomTextView;
import com.intro.imageUtils.ImageLoader;
import com.intro.network.ConnectionDetector;
import com.intro.utils.GlobalVariables;
import com.intro.utils.GlobalVariables.SERVICE_TAG;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.intro.webservice.JSONListener;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableJsonOperationCallback;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

/**
 * This class is for the Login via !ntro / fb / twitter /navigate to forget
 * password /Register
 * <p/>
 * By Ankit Kumar and Mirza Faisal
 */

public class LoginActivity extends BaseActivity {

    protected static final String TAG = "LoginActivity";
    private Context context;
    private ConnectionDetector cd;
    private EditText userEmail, password;

    private ImageView logo_Image;
    private CustomTextView login;
    private Button register;
    private ImageButton btn_Facebook, btn_Twitter;
    private String fName = "", lName = "", fbName = "", email_address = "",
            facebookID = "", birthday = "", gender = "";
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    private LoginButton facebookButton;
    private TwitterLoginButton twitterButton;
    String message;
    private SharedPreferences userData;
    private RelativeLayout relativeLayout;
    String email = "";
    String loginAction = "";
    ImageLoader im;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        setContentView(R.layout.activity_login);
        Appsee.start(getString(R.string.appsee_apikey));
        context = this;
        im = new ImageLoader(
                context);
        cd = new ConnectionDetector(getApplicationContext());
        facebookButton = (LoginButton) findViewById(R.id.facebook_button);
        facebookButton.setReadPermissions(Arrays.asList("email",
                "user_birthday", "public_profile", "user_photos"));

        try {
            twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_button);
            twitterButton.setCallback(new Callback<TwitterSession>() {
                @SuppressWarnings("unchecked")
                @Override
                public void success(Result<TwitterSession> result) {

                    try {
                        progressDia = CustomDialog.show(context,
                                GlobalVariables.loading);

                        System.out.println(result.data + " response="
                                + result.data.getUserId()
                                + result.data.getUserName());
                        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                        parameters.add(new Pair<String, String>("twitterid",
                                String.valueOf(result.data.getUserId())));
                        parameters
                                .add(new Pair<String, String>("email", ""));
                        parameters.add(new Pair<String, String>("bio", ""));
                        parameters.add(new Pair<String, String>("gender", "3"));
                        parameters.add(new Pair<String, String>("name", result.data.getUserName()));
                        parameters.add(new Pair<String, String>("birthday", ""));
                        parameters.add(new Pair<String, String>("device", ""));
                        mAuthService.loginUserWithTwitter(parameters,
                                new ApiJsonOperationCallback() {

                                    @Override
                                    public void onCompleted(
                                            JsonElement jsonElement,
                                            Exception exception,
                                            ServiceFilterResponse response) {

                                        if (progressDia != null
                                                && progressDia.isShowing())
                                            progressDia.dismiss();
                                        if (exception == null) {
                                            System.out.println("TwitterLoginResult " + jsonElement.toString());
                                            try {
                                                JSONObject jsonObject = new JSONObject(jsonElement.toString());
                                                mAuthService.saveTwitterData(jsonObject, context, false);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                System.out.println(jsonElement.toString());
                                            }
                                        } else {
                                            System.out.println("TwitterLoginException " + exception.toString());
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (progressDia != null
                                        && progressDia.isShowing())
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

        try {
            relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
            logo_Image = (ImageView) findViewById(R.id.login_logo);
            login = (CustomTextView) findViewById(R.id.login);
            register = (Button) findViewById(R.id.register);
            btn_Facebook = (ImageButton) findViewById(R.id.facebook);
            btn_Twitter = (ImageButton) findViewById(R.id.btn_Twitter);
            userEmail = (EditText) findViewById(R.id.email);
            password = (EditText) findViewById(R.id.password);
            password.setTransformationMethod(new PasswordTransformationMethod());

        } catch (Exception e) {
            e.printStackTrace();
        }
        addlistener();
    }


    private void callLogin() {
        try {
            progressDia = CustomDialog.show(context, GlobalVariables.loading);

            mAuthService.login(userEmail.getText()
                            .toString(), password.getText().toString(),
                    new TableJsonOperationCallback() {
                        @Override
                        public void onCompleted(
                                final JsonObject jsonObject,
                                Exception exception,
                                ServiceFilterResponse response) {
                            if (exception == null) {

                                if (progressDia != null && progressDia.isShowing())
                                    progressDia.dismiss();
                                // If that was successful, set and
                                // save
                                // the user data
                                System.out
                                        .println("resultjsonObject"
                                                + jsonObject
                                                .toString());
                                try {
                                    mAuthService.setUserAndSaveData(jsonObject, userEmail.getText().toString());

                                    final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                                    parameters.add(new Pair<String, String>("email", jsonObject.get("username").getAsString()));

                                    mAuthService.loginWithEmail(parameters,
                                            new ApiJsonOperationCallback() {

                                                @Override
                                                public void onCompleted(final JsonElement jsonElement,
                                                                        Exception exception, ServiceFilterResponse arg2) {

                                                    if (exception == null) {
                                                        System.out.println("result "
                                                                + jsonElement.toString());
                                                        if (jsonElement.toString().contains(
                                                                "no account found")) {
                                                            alart = new CustomAlartDialog(context,
                                                                    GlobalVariables.oh_snap,
                                                                    GlobalVariables.no_account_found);
                                                            alart.show();
                                                        }
                                                        try {
                                                            JSONObject jsonObject = new JSONObject(
                                                                    jsonElement.toString());
                                                            mAuthService.saveUserData(jsonObject,
                                                                    birthday, "", false, context, false);// ""
                                                            // is
                                                            // accestoken
                                                            // only
                                                            // provided
                                                            // in
                                                            // fb
                                                            // login
                                                            // case,false
                                                            // is
                                                            // for
                                                            // myprofile
                                                            // paramter

                                                        } catch (Exception e) {
                                                            e.printStackTrace();

                                                            runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    alart = new CustomAlartDialog(
                                                                            context,
                                                                            GlobalVariables.oh_snap,
                                                                            GlobalVariables.invalid_login);
                                                                    alart.show();
                                                                }
                                                            });

                                                        }
                                                    } else {
                                                        System.out.println("exception "
                                                                + exception.toString());
                                                    }
                                                }
                                            });


                                } catch (Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {

                                            Utility.validationByToastMessage(
                                                    context,
                                                    jsonObject
                                                            .toString());
                                        }
                                    });
                                }

                            } else {
                                if (progressDia != null && progressDia.isShowing())
                                    progressDia.dismiss();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        alart = new CustomAlartDialog(
                                                context,
                                                GlobalVariables.oh_snap,
                                                GlobalVariables.invalid_login);
                                        alart.show();
                                    }
                                });
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
    }

    private void addlistener() {

        btn_Twitter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loginAction = "twitter";
                im.ClearImage();
                twitterButton.performClick();
            }
        });

        btn_Facebook.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loginAction = "facebook";
                im.ClearImage();
                facebookButton.performClick();
            }

        });
        login.setOnClickListener(new OnClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {

                if (!userEmail.getText().toString().equalsIgnoreCase("")) {
                    if (!password.getText().toString().equalsIgnoreCase("")) {

                        if (password.length() >= 7) {
                            if (cd.isConnectingToInternet()) {
                                try {

                                    im.ClearImage();
                                    callLogin();

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                Utility.validationByToastMessage(
                                                        context,
                                                        GlobalVariables.request_not_done);
                                            } catch (Exception e) {

                                            }
                                        }
                                    });
                                }
                            } else {
                                alart = new CustomAlartDialog(context,
                                        GlobalVariables.oh_snap,
                                        GlobalVariables.network_error);
                                alart.show();
                            }
                        } else {
                            alart = new CustomAlartDialog(context,
                                    GlobalVariables.oh_snap,
                                    GlobalVariables.password_count_error);
                            alart.show();
                        }
                    } else {
                        alart = new CustomAlartDialog(context,
                                GlobalVariables.oh_snap,
                                GlobalVariables.password_error);
                        alart.show();
                    }
                } else {
                    alart = new CustomAlartDialog(context,
                            GlobalVariables.oh_snap, GlobalVariables.ValidEmail);
                    alart.show();
                }
            }

        });
        register.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cd.isConnectingToInternet()) {
                    Intent intent = new Intent(context, SignUpActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, R.anim.exit_slide_right);
                } else {
                    message = GlobalVariables.network_error;
                    Utility.validationByToastMessage(context, message);
                }
            }
        });
    }

    private class SessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.e("FB", "Logged statusCallback...");
            onSessionStateChange(session, state, exception);
        }
    }

    // same for every class For log In
    JSONListener getResponse = new JSONListener() {

        @Override
        public void onRemoteCallComplete(JSONObject jsonFromNet, SERVICE_TAG tag) {
            if (jsonFromNet != null) {
                switch (tag) {
                    case LOGIN:
                        System.out.println("jsonFromNet" + jsonFromNet.toString());
                        break;
                }
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.e("FB", "Logged in...");
            progressDia = CustomDialog.show(context,
                    GlobalVariables.loading);
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    Log.i("FB", "onCompleted");
                    if (user != null) {
                        session.getAccessToken();
                        facebookID = user.getId();
                        String accessToken = session.getAccessToken();
                        getFbDatatoLogin(user, accessToken);
                    }
                }
            }).executeAsync();
        } else if (state.isClosed()) {
            Log.e("FB", "Logged out...");
        }
    }

    public void getFbDatatoLogin(GraphUser user, final String accessToken) {
        facebookID = user.getId();
        fName = user.getFirstName();
        birthday = user.getBirthday();
        gender = user.asMap().get("gender").toString();
        try {
            email_address = user.asMap().get("email").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (email_address != null) {
            try {
                Log.e("FB", "Logged in.." + facebookID + " -- " + fName
                        + " -- " + birthday + " -- " + " -- "
                        + email_address + " -- " + gender);

                int genderBit = 0;
                if (gender.equalsIgnoreCase("male")) {
                    genderBit = 1;
                }
                final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                parameters.add(new Pair<String, String>("facebookid", facebookID));
                parameters.add(new Pair<String, String>("email", email_address));
                parameters.add(new Pair<String, String>("bio", ""));
                parameters.add(new Pair<String, String>("gender", genderBit + ""));
                parameters.add(new Pair<String, String>("name", fName));
                if (birthday == null)
                    birthday = "";
                parameters.add(new Pair<String, String>("birthday", birthday));
                parameters.add(new Pair<String, String>("device", ""));
                mAuthService.loginUserWithFb(parameters,
                        new ApiJsonOperationCallback() {

                            @Override
                            public void onCompleted(
                                    final JsonElement jsonElement,
                                    Exception exception,
                                    ServiceFilterResponse response) {
                                if (exception == null) {
                                    System.out.println("result " + jsonElement.toString());
                                    try {
                                        JSONArray jarray = new JSONArray(jsonElement.toString());
                                        JSONArray jarray1 = jarray.getJSONArray(0);
                                        JSONObject jsonObject = jarray1.getJSONObject(0);
                                        mAuthService.saveUserData(jsonObject, birthday, accessToken, false, context, false);
                                        // "" is accestoken only provided in fb login case,false is for myprofile paramter

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        mAuthService.registerUserWithFb(parameters,
                                                new ApiJsonOperationCallback() {

                                                    @Override
                                                    public void onCompleted(
                                                            final JsonElement jsonElement,
                                                            Exception exception,
                                                            ServiceFilterResponse response) {

                                                        if (progressDia != null && progressDia.isShowing())
                                                            progressDia.dismiss();
                                                        if (exception == null) {
                                                            System.out.println("result " + jsonElement.toString());
                                                            try {
                                                                JSONArray jarray = new JSONArray(jsonElement.toString());
                                                                JSONArray jarray1 = jarray.getJSONArray(0);
                                                                JSONObject jsonObject = jarray1.getJSONObject(0);
                                                                mAuthService.saveUserData(
                                                                        jsonObject, birthday, accessToken, false, context, true);
                                                                // "" is accestoken only provided in fb login case,
                                                                // false is for myprofile paramter
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else {
                                                            System.out.println("exception " + exception.toString());
                                                        }
                                                    }

                                                });
                                    }
                                } else {
                                    if (progressDia != null && progressDia.isShowing())
                                        progressDia.dismiss();
                                    alart = new CustomAlartDialog(context,
                                            GlobalVariables.oh_snap, exception.toString());
                                    alart.show();
                                }
                            }
                        });
                if (progressDia != null && progressDia.isShowing())
                    progressDia.dismiss();
            } catch (Exception e) {
                if (progressDia != null && progressDia.isShowing())
                    progressDia.dismiss();
                e.printStackTrace();
            }
        } else {
            // Clear all session info & ask user to login again
            Session session = Session.getActiveSession();
            if (session != null) {
                session.closeAndClearTokenInformation();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        try {
            if (loginAction.equalsIgnoreCase("twitter")) {
                twitterButton.onActivityResult(requestCode, responseCode,
                        intent);
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

}
