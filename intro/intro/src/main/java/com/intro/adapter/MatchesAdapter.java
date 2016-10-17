package com.intro.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ParseException;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.intro.android.ChatActivity;
import com.intro.android.NewMatchesActivity;
import com.intro.android.ProfileActivity;
import com.intro.android.R;
import com.intro.customviews.CustomTextView;
import com.intro.customviews.ImageViewRounded;
import com.intro.imageUtils.ImageLoader;
import com.intro.utils.GlobalVariables;
import com.intro.webservice.BaseActivity;

/**
 * MatchesAdapter this is adapter for set currently matched user profile and also handle timer for each item in this list.
 * It also handle color for new unread messages.
 * By Mirza Faisal
 */

public class MatchesAdapter extends BaseAdapter {

    LayoutInflater inflater;
    Context con;
    int position;
    ArrayList<JSONObject> matchesList;
    public static long baseTime;
    private Date matchedate = null, currentDate = null;
    public long difference;
    public static Timer timer = null;
    private int i = 0;
    private long diffMinutes, diffHours;
    LocalBroadcastManager broadcaster;
    public static boolean isRunningService = false;
    public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 seconds
    public static final int NOTIFICATION_ID = 1;
    private Handler mHandler = new Handler();
    private SharedPreferences userData;
    private Firebase mfirebase;
    private ArrayList<String> firebaseId = new ArrayList<String>();
    public ArrayList<Long> differcrList;
    private List<String> senderidList, messageCountList;
    TimeDisplayTimerTask timedisplatTaxkObj;
    private String listOfId = "", comeFrom;

    public MatchesAdapter(Context con, ArrayList<JSONObject> matchesList, String comeFrom) {
        this.con = con;
        this.comeFrom = comeFrom;
        this.matchesList = matchesList;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (comeFrom.equalsIgnoreCase("matches")) {
            timer = new Timer();
            timedisplatTaxkObj = new TimeDisplayTimerTask();
            timedisplatTaxkObj.textViewList = new ArrayList<TextView>();
            differcrList = new ArrayList<Long>();
            timer.scheduleAtFixedRate(timedisplatTaxkObj, 0, NOTIFY_INTERVAL);
        }
    }

    @Override
    public int getCount() {
        return matchesList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"NewApi", "SimpleDateFormat"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            inflater = (LayoutInflater) con
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.matches_items, parent,
                    false);
            holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.matchesLayout);
            holder.personalLayout = (RelativeLayout) convertView.findViewById(R.id.personLayout);
            holder.nameTxt = (TextView) convertView
                    .findViewById(R.id.personNameTextView);
            holder.ageTxt = (TextView) convertView
                    .findViewById(R.id.personAgeTextView);
            holder.distanceTxt = (TextView) convertView
                    .findViewById(R.id.diastanceTextView);
            holder.timeTxt = (TextView) convertView
                    .findViewById(R.id.timeTextView);
            holder.profileImage = (ImageViewRounded) convertView
                    .findViewById(R.id.profileImg);
            holder.clockImage = (ImageView) convertView
                    .findViewById(R.id.clockImageView);
            holder.messageCount = (CustomTextView) convertView.findViewById(R.id.messageCount);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final JSONObject obj = matchesList.get(position);

        try {
            holder.nameTxt.setText(BaseActivity.decodeUTF(obj.getString("Name").toString()));
            holder.ageTxt.setText(" | " + obj.getString("age").toString());
            ImageLoader iL = new ImageLoader(con);
            iL.DisplayImage(GlobalVariables.Image_URL + obj.getString("firebaseid").toString() + "-1", holder.profileImage);

            userData = con.getSharedPreferences("UserData", 0);
            senderidList = new LinkedList<>(Arrays.asList(userData.getString("senderid", "").split(",")));
            messageCountList = new LinkedList<>(Arrays.asList(userData.getString("messageCount", "").split(",")));
            if (!userData.getString("senderid", "").equalsIgnoreCase("")) {
                if (userData.getString("gender", "").equalsIgnoreCase("1")) {
                    ProfileActivity.toolbarMatchesImageView.setBackgroundDrawable(con.getResources().getDrawable(R.drawable.chat_blue));
                } else {
                    ProfileActivity.toolbarMatchesImageView.setBackgroundDrawable(con.getResources().getDrawable(R.drawable.pink_chat));
                }
            }
            for (int i = 0; i < senderidList.size(); i++) {
                if (senderidList.get(i).equalsIgnoreCase(obj.getString("firebaseid").toString())) {
                    //  holder.relativeLayout.setBackgroundColor(con.getResources().getColor(R.color.blue));
                    holder.messageCount.setVisibility(View.VISIBLE);
                    if (userData.getString("gender", "").equalsIgnoreCase("0")) {
                        holder.messageCount.setBackground(con.getResources().getDrawable(R.drawable.message_count_pink));
                    } else {
                        holder.messageCount.setBackground(con.getResources().getDrawable(R.drawable.message_count_blue));
                    }
                    holder.messageCount.setText(messageCountList.get(i));
                }
            }

            if (comeFrom.equalsIgnoreCase("matches")) {
                holder.clockImage.setVisibility(View.VISIBLE);
                if (Float.parseFloat(obj.getString("distance")) < 0.25) {
                    holder.distanceTxt.setText(con.getString(R.string.less_then_25_miles));
                } else {
                    holder.distanceTxt.setText(obj.getString("distance").toString() + con.getString(R.string.miles_away));
                }

                long difference = 0, currentDiff = 0, matchedDiff = 0;
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'000'Z'"); // yyyy-MM-dd HH 'hrs' mm 'min'
                    format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                    System.out.println("Date : " + obj.getString("expires").toString());
                    try {
                        System.out.println("current expire: " + userData.getString("expires", ""));
                        matchedate = format.parse(obj.getString("expires")
                                .toString());
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
                diffMinutes = difference / (60 * 1000) % 60;
                diffHours = difference / (60 * 60 * 1000) % 24;
                holder.timeTxt.setText(diffHours + " hrs " + (diffMinutes + 1)
                        + " min");
                if (differcrList.size() < (position + 1)) {
                    differcrList.add(difference);
                    timedisplatTaxkObj.textViewList.add(holder.timeTxt);
                }
            } else {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.personalLayout.getLayoutParams();
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                holder.personalLayout.setLayoutParams(lp);
                holder.clockImage.setVisibility(View.GONE);
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    holder.relativeLayout.setBackgroundColor(Color.TRANSPARENT);

                    for (int i = 0; i < senderidList.size(); i++) {
                        if (senderidList.get(i).equalsIgnoreCase(obj.getString("firebaseid").toString())) {
                            senderidList.remove(i);
                            messageCountList.remove(i);
                        }
                    }
                    for (int i = 0; i < senderidList.size(); i++)
                        if (i == 0)
                            listOfId = senderidList.get(i);
                        else listOfId = listOfId + "," + senderidList.get(i);

                    System.out.println("ListId : " + listOfId);

                    if (listOfId.equalsIgnoreCase("")) {
                        ProfileActivity.toolbarMatchesImageView.setBackgroundDrawable(con.getResources().getDrawable(R.drawable.top_chat));
                    }

                    Intent intent = new Intent(con, ChatActivity.class);
                    if (comeFrom.equalsIgnoreCase("matches")) {
                        intent.putExtra(GlobalVariables.comeFrom, GlobalVariables.matchAdapter);
                    } else {
                        intent.putExtra(GlobalVariables.comeFrom, GlobalVariables.favAdapter);
                    }
                    con.startActivity(intent);
//                    ((Activity) con).finish();
                    ((Activity) con).overridePendingTransition(0, R.anim.exit_slide_right);
                    SharedPreferences settings = con.getSharedPreferences("UserData", 0);
                    SharedPreferences.Editor preferencesEditor = settings.edit();

                    preferencesEditor.putString("matchUserId", obj.getString("firebaseid").toString());
                    preferencesEditor.putString("matchUserName", BaseActivity.decodeUTF(obj.getString("Name").toString()));
                    if (comeFrom.equalsIgnoreCase("matches")) {
                        preferencesEditor.putString("matchUserExpires", obj.getString("expires").toString());
                    } else {
                        preferencesEditor.putString("matchUserExpires", "");
                    }
                    preferencesEditor.putString("matchUserAge", obj.getString("age").toString());
                    preferencesEditor.putString("matchUserGender", obj.getString("gender").toString());
                    preferencesEditor.putString("senderid", listOfId);
                    preferencesEditor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return convertView;
    }

    class TimeDisplayTimerTask extends TimerTask {

        public ArrayList<TextView> textViewList;


        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        isRunningService = true;

                        for (int j = 0; j < textViewList.size(); j++) {

                            if (!differcrList.isEmpty())
                                if (differcrList.size() > j) {
                                    long diff = differcrList.get(j) - (i * 10000);
                                    System.out.println("mtime" + diff);
                                    diffMinutes = diff / (60 * 1000) % 60;
                                    diffHours = diff / (60 * 60 * 1000) % 24;
                                    textViewList.get(j).setText(diffHours + " hrs " + (diffMinutes + 1)
                                            + " min");
                                    if (diff < 0) {
                                        for (int i = 0; i < GlobalVariables.bothArrayList.size(); i++) {
                                            JSONObject breadcrumbObject = GlobalVariables.bothArrayList.get(i);
                                            JSONObject matchObject = matchesList.get(position);
                                            if (breadcrumbObject.getString("firebaseid").equalsIgnoreCase(matchObject.getString("firebaseid"))) {
                                                ((NewMatchesActivity) con).breadcrumbArrayList.remove(((NewMatchesActivity) con).breadcrumbArrayList.size() - 1);
                                                ((NewMatchesActivity) con).breadcrumbArrayList.add(breadcrumbObject);
                                                Collections.sort(((NewMatchesActivity) con).breadcrumbArrayList, new Comparator<JSONObject>() {
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
                                                ((NewMatchesActivity) con).breadcrumbArrayList.add(new JSONObject());
                                                ((NewMatchesActivity) con).breadcrumbAdapter.notifyDataSetChanged();

                                                if (((NewMatchesActivity) con).breadcrumbArrayList.size() > 1) {
                                                    ((NewMatchesActivity) con).breadcrumbHeader.setText("BREADCRUMBS (" + (((NewMatchesActivity) con).breadcrumbArrayList.size() - 1) + ")");
                                                }
                                                break;
                                            }
                                        }
                                        matchesList.remove(position);
                                        if (matchesList.size() > 0) {
                                            ((NewMatchesActivity) con).liveBtn.setText("LIVE (" + matchesList.size() + ")");
                                        }
                                        if (differcrList != null && differcrList.size() > position)
                                            differcrList.remove(position);
                                        notifyDataSetChanged();
                                        if (matchesList.size() < 1) {
                                            timer.cancel();
                                            timer.purge();
                                            timer = null;
                                            listOfId = "";
                                            ProfileActivity.toolbarMatchesImageView.setBackgroundDrawable(con.getResources().getDrawable(R.drawable.top_chat));
                                        }
                                    }
                                }
                        }
                        i++;
                    } catch (Exception e) {
                    }
                }

            });
        }

    }

    public class ViewHolder {
        TextView nameTxt, ageTxt, distanceTxt, timeTxt, messageCount;
        ImageViewRounded profileImage;
        ImageView clockImage;
        RelativeLayout relativeLayout, personalLayout;
    }
}
