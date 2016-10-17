package com.intro.webservice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.gson.JsonObject;


/*
import com.microsoft.windowsazure.mobileservices.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponseCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;*/
import com.intro.android.LoginActivity;
import com.intro.android.ProfileActivity;
import com.intro.android.R;
import com.intro.android.SetProfileActivity;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.table.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.TableJsonQueryCallback;

@SuppressWarnings("deprecation")
public class AuthService extends BaseActivity {
    public static MobileServiceClient mClient;
    private MobileServiceJsonTable mTableUsers;
    private MobileServiceJsonTable mTableAuthData;
    private MobileServiceJsonTable mTableBadAuth;
    private MobileServiceJsonTable mTableBlobBlobs;
    private Context mContext;
    private final String TAG = "AuthService";

    public AuthService(Context context) {
        mContext = context;
        try {
            initialize();
            mTableUsers = mClient.getTable("Users");
            mTableAuthData = mClient.getTable("AuthData");
            mTableBadAuth = mClient.getTable("BadAuth");
            mTableBlobBlobs = mClient.getTable("BlobBlobs");
        } catch (Exception e) {
            Log.e(TAG,"There was an error creating the Mobile Service.  Verify the URL");
        }
    }

    public void setContext(Context context) {
        mClient.setContext(context);
    }

    public String getUserId() {
        return mClient.getCurrentUser().getUserId();
    }

    public void getSasUrlForNewBlob(String blobName, String containerName,TableJsonOperationCallback callback) {

        JsonObject blob = new JsonObject();
        blob.addProperty("id", 0);
        // Create parameters to pass in the blob details. We do this with params
        // because it would be stripped out if we put it on the blob object
        List<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("containerName", containerName));
        parameters.add(new Pair<String, String>("blobName", blobName));
        mTableBlobBlobs.insert(blob, parameters, callback);
    }

    /**
     * Handles logging in with custom auth
     * @param username
     * @param password
     * @param callback
     */
    public void login(String username, String password,TableJsonOperationCallback callback) {
        JsonObject customUser = new JsonObject();
        customUser.addProperty("username", username);
        customUser.addProperty("password", password);
        List<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("login", "true"));
        mTableUsers.insert(customUser, parameters, callback);
    }

    public void loginWithEmail(ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("login", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void callUserCheckins(ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("usercheckin", null, "GET", parameters, callback);//usercheckin
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void callUserCheckins2(ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("usercheckin2", null, "GET", parameters, callback);//usercheckin
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void getPrefetchImages(ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("prefetch_images", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void getAuthData(TableJsonQueryCallback callback) {
        mTableAuthData.where().execute(callback);
    }

    public void getUserData(ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("get_user", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void addTime(ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("addtime", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void getNextExpiration(ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        // TODO Auto-generated method stub
        try {
            initialize();
            mClient.invokeApi("getnextexpiration", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void getCurrentExpiration(
            ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("get_current_expiration", null, "GET",parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    /**
     * Checks to see if we have userId and token stored on the device and sets
     * them if so
     * @return
     */
    public boolean isUserAuthenticated() {
        SharedPreferences settings = mContext.getSharedPreferences("UserData",0);
        if (settings != null) {
            String userId = settings.getString("userid", null);
            String token = settings.getString("token", null);
            if (userId != null && !userId.equals("")) {
                setUserData(userId, token);
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a nwe MobileServiceUser using a userId and token passed in. Also
     * sets the current provider
     * @param userId
     * @param token
     */
    public void setUserData(String userId, String token) {
        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        mClient.setCurrentUser(user);
    }

    /***
     * Pulls the user ID and token out of a json object from the server
     * @param jsonObject
     */
    public void setUserAndSaveData(JsonObject jsonObject, String email) {
        String userId;
        if (jsonObject.has("id")) {
            userId = jsonObject.get("id").getAsString();
        } else {
            String id = jsonObject.get("userId").getAsString();
            String[] separated = id.split(":");
            userId = separated[1];
        }
        String token = jsonObject.get("token").getAsString();
        String eMail = "";
        try {
            eMail = jsonObject.get("email").getAsString();
        } catch (Exception e) {
            eMail = email;
        }
        setUserData(userId, token);
        saveUserData(email);
    }

    /**
     * Saves userId and token to SharedPreferences. NOTE: This is not secure and
     * is just used as a storage mechanism. In reality, you would want to come
     * up with a more secure way of storing this information.
     */

    public void saveUserData(String email) {
        SharedPreferences settings = mContext.getSharedPreferences("UserData",0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putString("userid", mClient.getCurrentUser().getUserId());
        preferencesEditor.putString("token", mClient.getCurrentUser().getAuthenticationToken());
        preferencesEditor.putString("email", email);
        preferencesEditor.commit();
    }

    @SuppressLint("SimpleDateFormat")
    public void saveUserData(JSONObject jsonObject, String birthday,String accessToken, Boolean myProfile, Context context, Boolean signup) {
        SharedPreferences settings = context.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        String age = "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            Date date = format.parse(birthday);
            System.out.println(date);
            age = "" + Utility.getAge(date);
        } catch (Exception e) {
        }
        try {
            if (jsonObject.getString("age") == null || jsonObject.getString("age").equalsIgnoreCase("null")) {
                preferencesEditor.putString("age", age);
            } else {
                preferencesEditor.putString("age", jsonObject.getString("age"));
            }
        } catch (Exception e) {
            preferencesEditor.putString("age", age + "");
        }

        try {
            if (jsonObject.getString("gender").equals("1") || jsonObject.getString("gender").equals("0")) {
                preferencesEditor.putString("gender", jsonObject.getString("gender"));
            } else {
                String gender = "1";
                if (!jsonObject.getBoolean("gender")) {
                    gender = "0";
                }
                preferencesEditor.putString("gender", gender);
            }
        } catch (JSONException e1) {
            preferencesEditor.putString("gender", "");
        }
        try {
            preferencesEditor.putString("userid", jsonObject.getString("userID"));
        } catch (Exception e) {}
        try {
            preferencesEditor.putString("updatedAt", jsonObject.getString("__updatedAt"));
        } catch (Exception e) {
            preferencesEditor.putString("updatedAt", "");
        }
        try {
            preferencesEditor.putString("name", decodeUTF(jsonObject.getString("name")));
        } catch (Exception e) {
            preferencesEditor.putString("name", "");
        }
        try {
            preferencesEditor.putString("bio", decodeUTF(jsonObject.getString("bio")));
        } catch (Exception e) {
            preferencesEditor.putString("bio", "");
        }
        try {
            preferencesEditor.putString("haiku1",jsonObject.getString("haiku1"));
        } catch (Exception e) {
            preferencesEditor.putString("haiku1", "");
        }
        try {
            preferencesEditor.putString("haiku2",jsonObject.getString("haiku2"));
        } catch (Exception e) {
            preferencesEditor.putString("haiku2", "");
        }
        try {
            preferencesEditor.putString("haiku3",jsonObject.getString("haiku3"));
        } catch (Exception e) {
            preferencesEditor.putString("haiku3", "");
        }
        try {
            if (jsonObject.getBoolean("wantsFemales") == true) {
                preferencesEditor.putString("wantsFemales", "1");
            } else {
                preferencesEditor.putString("wantsFemales", "0");
            }
        } catch (Exception e) {
            preferencesEditor.putString("wantsFemales", "1");
        }
        try {
            if (jsonObject.getBoolean("wantsMales") == true) {
                preferencesEditor.putString("wantsMales", "1");
            } else {
                preferencesEditor.putString("wantsMales", "0");
            }
        } catch (Exception e) {
            preferencesEditor.putString("wantsMales", "1");
        }
        try {
            preferencesEditor.putString("high", jsonObject.getInt("high") + "");
        } catch (Exception e) {
            preferencesEditor.putString("high", "");
        }
        try {
            preferencesEditor.putString("low", jsonObject.getInt("low") + "");
        } catch (Exception e) {
            preferencesEditor.putString("low", "");
        }
        try {
            preferencesEditor.putString("expires2",jsonObject.getString("expires2"));
        } catch (Exception e) {
            preferencesEditor.putString("expires2", "");
        }

        if (!myProfile) { // if condition for check this method is used for
            // LoginActivity or ProfileActivity. parameters in if condition is only for
            // LoginActivity not for ProfileActivity.
            try {
                preferencesEditor.putString("firebaseid",jsonObject.getString("firebaseid"));
            } catch (Exception e) {
                preferencesEditor.putString("firebaseid", "");
            }
            try {
                preferencesEditor.putString("accessToken",accessToken);
            } catch (Exception e) {
                preferencesEditor.putString("accessToken", "");
            }
            try {
                System.out.println("fbID saving" + jsonObject.getString("facebookid"));
                if (jsonObject.getString("facebookid").equalsIgnoreCase("null")|| jsonObject.getString("facebookid") == null) {
                    preferencesEditor.putString("facebookid", "");
                } else {
                    preferencesEditor.putString("facebookid",jsonObject.getString("facebookid"));
                }
            } catch (Exception e) {
                preferencesEditor.putString("facebookid", "");
            }

            try {
                preferencesEditor.putString("userCID",jsonObject.getString("userCID"));
            } catch (Exception e) {
                preferencesEditor.putString("userCID", "");
            }
            try {
                preferencesEditor.putString("email",jsonObject.getString("useremail"));
            } catch (Exception e) {
                preferencesEditor.putString("email", "");
            }
            try {
                preferencesEditor.putString("deviceID", jsonObject.getString("deviceID"));
            } catch (Exception e) {
                preferencesEditor.putString("deviceID", "");
            }
            try {
                preferencesEditor.putString("Image1",jsonObject.getString("Image1"));
            } catch (Exception e) {
                preferencesEditor.putString("Image1", "");
            }
            try {
                preferencesEditor.putString("Image2",jsonObject.getString("Image2"));
            } catch (Exception e) {
                preferencesEditor.putString("Image2", "");
            }
            try {
                preferencesEditor.putString("Image3",jsonObject.getString("Image3"));
            } catch (Exception e) {
                preferencesEditor.putString("Image3", "");
            }
            try {
                preferencesEditor.putString("Image1Uploaded",jsonObject.getString("image1_uploaded"));
            } catch (Exception e) {
                preferencesEditor.putString("Image1Uploaded", "0");
            }
            try {
                preferencesEditor.putString("Image2Uploaded",jsonObject.getString("image2_uploaded"));
            } catch (Exception e) {
                preferencesEditor.putString("Image2Uploaded", "0");
            }
            try {
                preferencesEditor.putString("Image3Uploaded",jsonObject.getString("image3_uploaded"));
            } catch (Exception e) {
                preferencesEditor.putString("Image3Uploaded", "0");
            }
            try {
                preferencesEditor.putString("expires",jsonObject.getString("expires"));
            } catch (Exception e) {
                preferencesEditor.putString("expires", "");
            }
            preferencesEditor.commit();

            try {
                if (jsonObject.getString("age") == null|| jsonObject.getString("age").equalsIgnoreCase("null")) {
                    Intent loggedInIntent = new Intent(context,SetProfileActivity.class);
                    loggedInIntent.putExtra(GlobalVariables.comeFrom, "Auth");
                    loggedInIntent.putExtra("signUp", signup);
                    loggedInIntent.putExtra("cancel", true);
                    loggedInIntent.putExtra(GlobalVariables.comeFrom, "");
                    context.startActivity(loggedInIntent);
                    ((Activity) context).overridePendingTransition(0, R.anim.exit_slide_right);
                    ((Activity) context).finish();
                } else {
                    Intent loggedInIntent = new Intent(context,ProfileActivity.class);
                    if(jsonObject.getString("Image1").equalsIgnoreCase("null") && jsonObject.getString("Image2").equalsIgnoreCase("null")
                            && jsonObject.getString("Image3").equalsIgnoreCase("null")){
                        GlobalVariables.randomImagesList.add(String.valueOf(getRandomMale()));
                        GlobalVariables.randomImagesList.add(String.valueOf(getRandomMale()));
                        GlobalVariables.randomImagesList.add(String.valueOf(getRandomMale()));
                    }else{
                        GlobalVariables.randomImagesList.clear();
                    }
                    context.startActivity(loggedInIntent);
                    ((Activity) context).overridePendingTransition(0, R.anim.exit_slide_right);
                    ((Activity) context).finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                preferencesEditor.putString("start_time",jsonObject.getString("start_time"));
            } catch (Exception e) {
                preferencesEditor.putString("start_time", "");
            }
            try {
                preferencesEditor.putString("end_time",jsonObject.getString("end_time"));
            } catch (Exception e) {
                preferencesEditor.putString("end_time", "");
            }
            preferencesEditor.commit();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public void saveTwitterData(JSONObject jsonObject, Context context, Boolean signup) {
        SharedPreferences settings = context.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        String age = "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            Date date = format.parse(jsonObject.getString("birthday"));
            System.out.println(date);
            age = "" + Utility.getAge(date);
        } catch (Exception e) {
        }
        try {
            if (jsonObject.getString("age") == null|| jsonObject.getString("age").equalsIgnoreCase("null")) {
                preferencesEditor.putString("age", age);
            } else {
                preferencesEditor.putString("age", jsonObject.getString("age"));
            }
        } catch (Exception e) {
            preferencesEditor.putString("age", age + "");
        }

        try {
            if (jsonObject.getString("gender").equals("1")|| jsonObject.getString("gender").equals("0")) {
                preferencesEditor.putString("gender",jsonObject.getString("gender"));
            } else {
                String gender = "1";
                if (!jsonObject.getBoolean("gender")) {
                    gender = "0";
                }
                preferencesEditor.putString("gender", gender);
            }
        } catch (JSONException e1) {
            preferencesEditor.putString("gender", "");
        }
        try {
            preferencesEditor.putString("userid", jsonObject.getString("id"));
        } catch (Exception e) {
        }
        try {
            preferencesEditor.putString("name", decodeUTF(jsonObject.getString("name")));
        } catch (Exception e) {
            preferencesEditor.putString("name", "");
        }
        try {
            preferencesEditor.putString("bio", decodeUTF(jsonObject.getString("bio")));
        } catch (Exception e) {
            preferencesEditor.putString("bio", "");
        }
        try {
            preferencesEditor.putString("Image1",jsonObject.getString("Image1"));
        } catch (Exception e) {
            preferencesEditor.putString("Image1", "");
        }
        try {
            preferencesEditor.putString("Image2",jsonObject.getString("Image2"));
        } catch (Exception e) {
            preferencesEditor.putString("Image2", "");
        }
        try {
            preferencesEditor.putString("Image3",jsonObject.getString("Image3"));
        } catch (Exception e) {
            preferencesEditor.putString("Image3", "");
        }
        try {
            preferencesEditor.putString("Image1Uploaded",jsonObject.getString("image1_uploaded"));
        } catch (Exception e) {
            preferencesEditor.putString("Image1Uploaded", "0");
        }
        try {
            preferencesEditor.putString("Image2Uploaded", jsonObject.getString("image2_uploaded"));
        } catch (Exception e) {
            preferencesEditor.putString("Image2Uploaded", "0");
        }
        try {
            preferencesEditor.putString("Image3Uploaded",jsonObject.getString("image3_uploaded"));
        } catch (Exception e) {
            preferencesEditor.putString("Image3Uploaded", "0");
        }
        try {
            preferencesEditor.putString("updatedAt", jsonObject.getString("__updatedAt"));
        } catch (Exception e) {
            preferencesEditor.putString("updatedAt", "");
        }
        try {
            preferencesEditor.putString("expires2",jsonObject.getString("expires2"));
        } catch (Exception e) {
            preferencesEditor.putString("expires2", "");
        }
        try {
            preferencesEditor.putString("twitter_username",jsonObject.getString("twitter_username"));
        } catch (Exception e) {
            preferencesEditor.putString("twitter_username", "");
        }
        try {
            if (jsonObject.getBoolean("wantsFemales") == true) {
                preferencesEditor.putString("wantsFemales", "1");
            } else {
                preferencesEditor.putString("wantsFemales", "0");
            }
        } catch (Exception e) {
            preferencesEditor.putString("wantsFemales", "1");
        }
        try {
            if (jsonObject.getBoolean("wantsMales") == true) {
                preferencesEditor.putString("wantsMales", "1");
            } else {
                preferencesEditor.putString("wantsMales", "0");
            }
        } catch (Exception e) {
            preferencesEditor.putString("wantsMales", "1");
        }
        try {
            preferencesEditor.putString("high", jsonObject.getInt("high") + "");
        } catch (Exception e) {
            preferencesEditor.putString("high", "");
        }
        try {
            preferencesEditor.putString("low", jsonObject.getInt("low") + "");
        } catch (Exception e) {
            preferencesEditor.putString("low", "");
        }
        try {
            preferencesEditor.putString("firebaseid",jsonObject.getString("firebaseid"));
        } catch (Exception e) {
            preferencesEditor.putString("firebaseid", "");
        }
        try {
            preferencesEditor.putString("twitterid",jsonObject.getString("twitterid"));
        } catch (Exception e) {
            preferencesEditor.putString("twitterid", "");
        }
        try {
            if (jsonObject.getString("facebookid").equalsIgnoreCase("null")|| jsonObject.getString("facebookid") == null) {
                preferencesEditor.putString("facebookid", "");
            } else {
                preferencesEditor.putString("facebookid",jsonObject.getString("facebookid"));
            }
        } catch (Exception e) {
            preferencesEditor.putString("facebookid", "");
        }
        try {
            preferencesEditor.putString("email", jsonObject.getString("email"));
        } catch (Exception e) {
            preferencesEditor.putString("email", "");
        }
        try {
            preferencesEditor.putString("deviceID",jsonObject.getString("deviceid"));
        } catch (Exception e) {
            preferencesEditor.putString("deviceID", "");
        }
        preferencesEditor.commit();
        try {
            if (jsonObject.has("age")) {
                if (jsonObject.getString("age") == null|| jsonObject.getString("age").equalsIgnoreCase("null")
                        || jsonObject.getString("name").equalsIgnoreCase("null")|| jsonObject.getString("name") == null) {
                    try {
                        preferencesEditor.putString("twitter", GlobalVariables.twitterLogin);
                    } catch (Exception e) {
                    }
                    Intent loggedInIntent = new Intent(context,SetProfileActivity.class);
                    loggedInIntent.putExtra(GlobalVariables.comeFrom, "Auth");
                    loggedInIntent.putExtra("signUp", signup);
                    loggedInIntent.putExtra("cancel", true);
                    context.startActivity(loggedInIntent);
                    ((Activity) context).overridePendingTransition(0, R.anim.exit_slide_right);
                    ((Activity) context).finish();
                } else {
                    Intent loggedInIntent = new Intent(context,ProfileActivity.class);
                    if(jsonObject.getString("Image1").equalsIgnoreCase("null") && jsonObject.getString("Image2").equalsIgnoreCase("null")
                            && jsonObject.getString("Image3").equalsIgnoreCase("null")){
                        GlobalVariables.randomImagesList.add(String.valueOf(getRandomMale()));
                        GlobalVariables.randomImagesList.add(String.valueOf(getRandomMale()));
                        GlobalVariables.randomImagesList.add(String.valueOf(getRandomMale()));
                    }else{
                        GlobalVariables.randomImagesList.clear();
                    }
                    context.startActivity(loggedInIntent);
                    ((Activity) context).overridePendingTransition(0, R.anim.exit_slide_right);
                    ((Activity) context).finish();
                }
            } else {
                Intent loggedInIntent = new Intent(context,SetProfileActivity.class);
                loggedInIntent.putExtra(GlobalVariables.comeFrom, "Auth");
                loggedInIntent.putExtra("signUp", signup);
                loggedInIntent.putExtra("cancel", true);
                loggedInIntent.putExtra(GlobalVariables.comeFrom, "");
                context.startActivity(loggedInIntent);
                ((Activity) context).overridePendingTransition(0, R.anim.exit_slide_right);
                ((Activity) context).finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        preferencesEditor.commit();
    }

    /**
     * Register the user if they're creating a custom auth account
     * @param username
     * @param password
     * @param email
     * @param callback
     */
    public void registerUser(String username, String password, String email, TableJsonOperationCallback callback) {
        JsonObject newUser = new JsonObject();
        newUser.addProperty("username", username);
        newUser.addProperty("password", password);
        newUser.addProperty("email", email);
        mTableUsers.insert(newUser, callback);
    }

    public void registerUserWithFb(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("login_facebook2", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void loginUserWithFb(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("login_facebook", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void setUserProfile(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("update_user", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void setUserImage(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("create_images", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void initialize() {
        try {
            mClient = new MobileServiceClient(GlobalVariables.ServerUrl,GlobalVariables.key, mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loginUserWithTwitter(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("create_user_twitter", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void getNearMatches(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("matches_near_me", null, "GET", parameters,callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLikesBreadcrumb(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("get_likes_breadcrumb", null, "GET", parameters,callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getFavMatches(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("get_favourate_user", null, "GET", parameters,callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearEvent(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("clear_data_after_event_expire", null, "GET", parameters,callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void logout(boolean shouldRedirectToLogin, Context context) {
        // Clear the cookies so they won't auto login to a provider again
        CookieSyncManager.createInstance(mContext);
        System.out.println("4");
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        // Clear the user id and token from the shared preferences
        SharedPreferences settings = mContext.getSharedPreferences("UserData",0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.clear();
        preferencesEditor.commit();
        // Clear the user and return to the auth activity
        mClient.logout();
        if (shouldRedirectToLogin) {
            Intent intent = new Intent(context, LoginActivity.class);
            /*
             * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
			 * Intent.FLAG_ACTIVITY_NEW_TASK);
			 */
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }

    public void blockUser(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("joaniehateschachi", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void deleteUser(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("delete_user", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void reportInappropriate(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("report_user", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }

    }

    public void dropBreadcrumb(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("drop_breadcrumb", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    /**
     * Calls a method on the server that will auto trigger a 401 result
     * @param shouldRetry
     * @param callback
     */
    public void testForced401(boolean shouldRetry, TableJsonOperationCallback callback) {
        JsonObject data = new JsonObject();
        data.addProperty("data", "data");
        mTableBadAuth.insert(data, callback);
    }

    public void getQueueData(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("findmesomeone3", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void getBreacrumb(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("find_me_breadcrumbs", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void breacrumbLikedUser(ArrayList<Pair<String, String>> parameters,ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("breadcrumb_liked_users", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void likeUser(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("joanieloveschachi", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void dislikeUser(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("joaniedoesnotlikechachi", null, "GET",parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void breadcrumbLike(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("like_breadcrumb", null, "GET", parameters,callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void breadcrumbDislike(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("dislike_breadcrumb", null, "GET",parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void setPing(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("ping", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void addConversation(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("add_conversation", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void clearConversation(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("clear_conversation", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void addFav(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("make_favourite", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void updateDeviceId(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("update_deviceid", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public void nearByBreadcrumb(ArrayList<Pair<String, String>> parameters, ApiJsonOperationCallback callback) {
        try {
            initialize();
            mClient.invokeApi("getnearbyactivebreadcrumb", null, "GET", parameters, callback);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}
