package com.intro.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appsee.Appsee;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.JsonElement;
import com.intro.adapter.ChatAdapter;
import com.intro.customviews.CustomAlartDialog;
import com.intro.customviews.CustomDialog;
import com.intro.customviews.CustomEditText;
import com.intro.customviews.CustomTextView;
import com.intro.customviews.ImageViewRounded;
import com.intro.imageUtils.ImageLoader;
import com.intro.model.Chat;
import com.intro.model.ChatResponse;
import com.intro.utils.GlobalVariables;
import com.intro.utils.Utility;
import com.intro.webservice.BaseActivity;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

/**
 * This class is for the chat
 * <p/>
 * By Ankit Kumar and Mirza Faisal
 */

public class ChatActivity extends BaseActivity {

    Context context;
    private RelativeLayout rounded_img_lay;
    private ImageButton back_arrow, favImage;
    private CustomTextView time_tv, name, send;
    private CustomEditText message_et;
    private ListView chatList;
    private ImageViewRounded rounded_image_1, rounded_image_2, rounded_image_3;
    private SharedPreferences userData, toolTipPreferences;
    private ImageLoader imgL;
    private String matchUserId, matchUserName, changedStatus = "read", comeFrom;
    private ArrayList<ChatResponse> messages = new ArrayList<ChatResponse>();
    private ArrayList<String> userids = new ArrayList<>();
    private ChatAdapter adapter;
    private Firebase firebase;
    private ChatResponse chatResponse;
    public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 seconds
    private Handler mHandler = new Handler();
    public static Timer mTimer = null;
    private Date date = null;
    private int i = 0, counter = 0;
    private long diffMinutes, diffHours;
    private Date matchedate = null, currentDate = null;
    long difference = 0, currentDiff = 0, matchedDiff = 01;
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_ID_PERMISSIONS = 1;
    private RelativeLayout blackCover, mainLayout;
    private TextView okay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_chat);
        Appsee.start(getString(R.string.appsee_apikey));
        rounded_img_lay = (RelativeLayout) findViewById(R.id.rounded_img_layout);
        back_arrow = (ImageButton) findViewById(R.id.back_arrow);
        favImage = (ImageButton) findViewById(R.id.favImage);
        name = (CustomTextView) findViewById(R.id.name);
        time_tv = (CustomTextView) findViewById(R.id.time_tv);
        rounded_image_1 = (ImageViewRounded) findViewById(R.id.rounded_img_1);
        rounded_image_2 = (ImageViewRounded) findViewById(R.id.rounded_img_2);
        rounded_image_3 = (ImageViewRounded) findViewById(R.id.rounded_img_3);
        chatList = (ListView) findViewById(R.id.chatList);
        message_et = (CustomEditText) findViewById(R.id.message_et);
        send = (CustomTextView) findViewById(R.id.send_bt);
        blackCover = (RelativeLayout) findViewById(R.id.blackCover);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        okay = (TextView) findViewById(R.id.okay);

        chatList.post(new Runnable() {
            @Override
            public void run() {
                chatList.setSelection(chatList.getCount());
            }
        });

        userData = getSharedPreferences("UserData", MODE_PRIVATE);
        toolTipPreferences = getSharedPreferences("toolTipPreferences", MODE_PRIVATE);

        if(toolTipPreferences.getBoolean(GlobalVariables.first_time_chat, true)){
            enableDisableView(mainLayout,false);
            blackCover.setVisibility(View.VISIBLE);
        }else{
            blackCover.setVisibility(View.GONE);
            setData();
        }

        adlistner();
    }

    public void setData(){
        clearConversation();
        matchUserId = userData.getString("matchUserId", "");
        matchUserName = userData.getString("matchUserName", "");
        GlobalVariables.ChatId = matchUserId;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        imgL = new ImageLoader(context);
        userids.add(userData.getString("userid", ""));
        userids.add(userData.getString("userid", ""));
        userids.add(matchUserId);
        userids.add(matchUserId);

        Collections.sort(userids);

        name.setText(matchUserName);

        if (!Utility.hasPermissions(ChatActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(ChatActivity.this, PERMISSIONS, REQUEST_ID_PERMISSIONS);
        } else {
            imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-1", rounded_image_1);
            imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-2", rounded_image_2);
            imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-3", rounded_image_3);
        }
        System.out.println("Url: " + userids.get(0) + "-" + userids.get(1) + "-" + userids.get(2) + "-" + userids.get(3));
        firebase = new Firebase(GlobalVariables.FirebaseUrl + userids.get(0) + "-" + userids.get(1) + "-" + userids.get(2) + "-" + userids.get(3));

        comeFrom = getIntent().getStringExtra(GlobalVariables.comeFrom);
        if (comeFrom.equalsIgnoreCase(GlobalVariables.breadcrumbAdapter)) {
            favImage.setVisibility(View.GONE);
        } else {
            favImage.setVisibility(View.VISIBLE);
            if (comeFrom.equalsIgnoreCase(GlobalVariables.favAdapter)) {
                favImage.setBackground(getResources().getDrawable(R.drawable.star_2));
            } else {
                favImage.setBackground(getResources().getDrawable(R.drawable.star_1));
            }

            if (!userData.getString("matchUserExpires", "").equalsIgnoreCase("")) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(
                            "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'000'Z'"); // yyyy-MM-dd HH 'hrs' mm 'min'
                    format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                    System.out.println("Date : " + userData.getString("matchUserExpires", ""));
                    try {
                        matchedate = format.parse(userData.getString("matchUserExpires", ""));
                        currentDate = format.parse(userData.getString("expires", ""));
                        Date date = new Date();
                        matchedDiff = matchedate.getTime() - date.getTime();
                        currentDiff = currentDate.getTime() - date.getTime();

                        if (matchedDiff < currentDiff) {
                            difference = matchedDiff;
                            System.out.println("matched : " + matchedDiff);
                        } else {
                            difference = currentDiff;
                            System.out.println("current : " + currentDiff);
                        }
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (mTimer != null) {
                    mTimer = null;
                }
                if (matchedDiff > 0 && currentDiff > 0) {
                    mTimer = new Timer();
                    mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
                }
            }
        }
        progressDia = CustomDialog.show(ChatActivity.this, GlobalVariables.loading);
        firebase.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to the database
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                try {
                    System.out.println("chat data : " + snapshot.getValue());
                    DateFormat df = DateFormat.getTimeInstance();
                    df.setTimeZone(TimeZone.getTimeZone("gmt"));
                    Date date = new Date();
                    String gmtTime = df.format(date);
                    long data = date.getTime() * 1000;

                    try {
                        chatResponse = new ChatResponse();
                        System.out.println("chat text " + snapshot.child("text").getValue());
                        if (snapshot.child("status").getValue().toString().equalsIgnoreCase("unread") &&
                                !snapshot.child("senderId").getValue().toString().equalsIgnoreCase(userData.getString("userid", ""))) {
                            Map<String, Object> time = new HashMap<String, Object>();
                            snapshot.getRef().child("status").setValue("read");
                            snapshot.getRef().child("recieveTime").setValue(String.valueOf(data));
                            time.put("status", changedStatus);
                            time.put("receiveTime", String.valueOf(data));
                            snapshot.getRef().updateChildren(time);
                        }

                        if (snapshot.child("senderId").getValue().toString().equalsIgnoreCase(userData.getString("userid", ""))) {
                            chatResponse.setTime(snapshot.child("sendTime").getValue().toString());
                        } else {
                            if (snapshot.child("status").getValue().toString().equalsIgnoreCase("read")) {
                                chatResponse.setTime(snapshot.child("receiveTime").getValue().toString());
                            } else {
                                chatResponse.setTime(String.valueOf(data));
                            }
                        }
                        chatResponse.setText(snapshot.child("text").getValue().toString());
                        chatResponse.setGender(snapshot.child("gender").getValue().toString());
                        chatResponse.setSenderId(snapshot.child("senderId").getValue().toString());
                        chatResponse.setSenderName(snapshot.child("senderName").getValue().toString());
                        chatResponse.setStatus(snapshot.child("status").getValue().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        messages.add(chatResponse);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        firebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("There are " + snapshot.getChildrenCount() + " blog posts");
                System.out.println("new snapchat: " + snapshot);
                try {
                    if (progressDia != null && progressDia.isShowing())
                        progressDia.dismiss();
                    Collections.sort(messages, new Comparator<ChatResponse>() {
                        @Override
                        public int compare(ChatResponse lhs, ChatResponse rhs) {

                            Double l1 = Double.parseDouble(lhs.getTime());//new Long(lhs.getTime());
                            Double l2 = Double.parseDouble(rhs.getTime());//new Long(rhs.getTime());
                            int time = l1.compareTo(l2);
                            return time;
                        }
                    });
                    for (int i = 0; i < messages.size(); i++) {
                        System.out.println("Message: " + messages.get(i).getTime());
                    }
                    chatList.post(new Runnable() {
                        @Override
                        public void run() {
                            chatList.setSelection(chatList.getCount());
                        }
                    });
                    adapter = new ChatAdapter(context, messages);
                    chatList.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ID_PERMISSIONS:
                imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-1", rounded_image_1);
                imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-2", rounded_image_2);
                imgL.DisplayImage(GlobalVariables.Image_URL + matchUserId + "-3", rounded_image_3);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalVariables.chatActivity = GlobalVariables.ChatResume;
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalVariables.chatActivity = GlobalVariables.ChatPause;
        GlobalVariables.ChatId = "";
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void adlistner() {

        okay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableView(mainLayout, true);
                blackCover.setVisibility(View.GONE);
                SharedPreferences.Editor editor = toolTipPreferences.edit();
                editor.putBoolean(GlobalVariables.first_time_chat, false);
                editor.commit();
                setData();
            }
        });

        back_arrow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ChatActivity.this, NewMatchesActivity.class);
//                startActivity(intent);
                finish();
                overridePendingTransition(0, R.anim.exit_slide_left);
            }
        });

        favImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addFav();
            }
        });

        rounded_img_lay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MatchedUserProfileActivity.class);
                intent.putExtra("matchUserId", matchUserId);
                startActivity(intent);
                overridePendingTransition(0, R.anim.exit_slide_right);
            }
        });

        message_et.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                send.setTextColor(getResources().getColor(R.color.pink));
            }
        });

        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!message_et.getText().toString().equalsIgnoreCase("")) {
                    send.setTextColor(getResources().getColor(R.color.white));
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    DateFormat df = DateFormat.getTimeInstance();
                    df.setTimeZone(TimeZone.getTimeZone("gmt"));
                    Date date = new Date();
                    String gmtTime = df.format(date);
                    long data = date.getTime() * 1000;
                    addConversation();
                    Chat chat = new Chat(message_et.getText().toString(), userData.getString("gender", ""), userData.getString("userid", ""),
                            "unread", userData.getString("name", ""), String.valueOf(data), String.valueOf(data));
                    firebase.push().setValue(chat, String.valueOf(data));
                    message_et.setText("");
                }
            }
        });
    }

    public void addConversation() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("conversation", userData.getString("matchUserId", "")));
        parameters.add(new Pair<String, String>("SenderId", userData.getString("userid", "")));
        mAuthService.addConversation(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement, Exception e, ServiceFilterResponse serviceFilterResponse) {
                if (e == null) {
                    try {
                        System.out.println("Response: " + jsonElement.toString());
                        JSONArray jsonArray = new JSONArray(jsonElement.toString());
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        if (jsonObject.getString("code").equals("201")) {
                            final CustomAlartDialog alartDialog = new CustomAlartDialog(context, GlobalVariables.oh_snap,
                                    matchUserName + " has blocked you. You will not be able to send messages.");
                            alartDialog.show();

                            CustomAlartDialog.okBtn.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alartDialog.dismiss();
                                    Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(0, R.anim.exit_slide_left);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("Error: " + e.toString());
                }
            }
        });
    }

    public void clearConversation() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("ReciverId", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("SenderId", userData.getString("matchUserId", "")));
        mAuthService.clearConversation(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement, Exception e, ServiceFilterResponse serviceFilterResponse) {

            }
        });
    }

    public void addFav() {
        final ArrayList<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
        parameters.add(new Pair<String, String>("userid", userData.getString("userid", "")));
        parameters.add(new Pair<String, String>("favUserId", userData.getString("matchUserId", "")));
        mAuthService.addFav(parameters, new ApiJsonOperationCallback() {
            @Override
            public void onCompleted(JsonElement jsonElement, Exception e, ServiceFilterResponse serviceFilterResponse) {
                if (e == null) {
                    try {
                        System.out.println("AddFavResponse: " + jsonElement.toString());
                        if (comeFrom.equalsIgnoreCase(GlobalVariables.favAdapter)) {
                            finish();
                            overridePendingTransition(0, R.anim.exit_slide_left);
                        } else {
                            favImage.setBackground(getResources().getDrawable(R.drawable.star_2));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("AddFavError: " + e.toString());
                }
            }
        });
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        long time = difference - (i * 10000);
                        System.out.println("chat time" + time);
                        i++;
                        diffMinutes = time / (60 * 1000) % 60;
                        diffHours = time / (60 * 60 * 1000) % 24;

                        time_tv.setText(diffHours + " hrs " + (diffMinutes + 1) + " min");
                        if (time <= 0) {
                            System.out.println("Timeout");
                            mTimer.cancel();
                            mTimer = null;
                            SharedPreferences.Editor e = userData.edit();
                            e.putString("matchUserExpires", "");
                            e.commit();

                            Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0, R.anim.exit_slide_left);
                        }
                    } catch (Exception e) {
                    }
                }

            });
        }
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
//        Intent intent = new Intent(ChatActivity.this, NewMatchesActivity.class);
//        startActivity(intent);
//        finish();
        overridePendingTransition(0, R.anim.exit_slide_left);
    }
}
