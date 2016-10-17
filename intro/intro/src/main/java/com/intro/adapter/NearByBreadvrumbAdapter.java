package com.intro.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.intro.android.ProfileActivity;
import com.intro.android.R;
import com.intro.customviews.CustomTextView;
import com.intro.imageUtils.ImageLoader;
import com.intro.utils.GlobalVariables;
import com.intro.webservice.BaseActivity;
import com.shaded.fasterxml.jackson.databind.deser.Deserializers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by vinove on 14/4/16.
 */
public class NearByBreadvrumbAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<JSONObject> breadcrumbList;
    private SharedPreferences userData;
    private Date matchedate = null, currentDate = null;
    TimeDisplayTimerTask timedisplatTaxkObj;
    public static Timer timer = null;
    public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 seconds
    public ArrayList<Long> differcrList;
    private Handler mHandler = new Handler();
    private int i = 0, position;
    private long diffMinutes, diffHours;
    public static boolean isRunningService = false;

    public NearByBreadvrumbAdapter(Context context, ArrayList<JSONObject> breadcrumbList) {
        this.context = context;
        this.breadcrumbList = breadcrumbList;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timedisplatTaxkObj = new TimeDisplayTimerTask();
        timedisplatTaxkObj.textViewList = new ArrayList<TextView>();
        differcrList = new ArrayList<Long>();
        timer.scheduleAtFixedRate(timedisplatTaxkObj, 0, NOTIFY_INTERVAL);
    }

    @Override
    public int getCount() {
        return breadcrumbList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;
        convertView = inflater.inflate(R.layout.near_breadcrumb_item, parent,
                false);
        holder = new ViewHolder();
        holder.profileImage = (ImageView) convertView.findViewById(R.id.profileImg);
        holder.name = (CustomTextView) convertView.findViewById(R.id.personNameTextView);
        holder.time = (CustomTextView) convertView.findViewById(R.id.timeTextView);
        // convertView.setTag(holder);
        try {
            userData = context.getSharedPreferences("UserData", 0);
            JSONObject jsonObject = breadcrumbList.get(position);
            holder.name.setText(BaseActivity.decodeUTF(jsonObject.getString("Name")));
            ImageLoader iL = new ImageLoader(context);
            iL.DisplayImage(GlobalVariables.Image_URL + jsonObject.getString("firebaseid").toString() + "-1", holder.profileImage);

            long difference = 0, currentDiff = 0, matchedDiff = 0;
            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'000'Z'"); // yyyy-MM-dd
            // HH 'hrs'
            // mm 'min'
            format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            System.out.println("Date : "
                    + jsonObject.getString("expires").toString());


            System.out.println("current expire: " + userData.getString("expires", ""));
            matchedate = format.parse(jsonObject.getString("expires")
                    .toString());
            currentDate = format.parse(userData.getString("expires", ""));

            Date date = new Date();

            matchedDiff = matchedate.getTime() - date.getTime();
            currentDiff = currentDate.getTime() - date.getTime();

            if (matchedDiff < currentDiff) {
                difference = matchedDiff;
                System.out
                        .println("matched : " + matchedDiff);
            } else {
                difference = currentDiff;
                System.out.println("current : " + currentDiff);
            }

            diffMinutes = difference / (60 * 1000) % 60;
            diffHours = difference / (60 * 60 * 1000) % 24;
            holder.time.setText(diffHours + " hrs " + (diffMinutes + 1)
                    + " min");
            if (differcrList.size() < (position + 1)) {
                differcrList.add(difference);
                timedisplatTaxkObj.textViewList.add(holder.time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

                                        breadcrumbList.remove(position);
                                        if (differcrList != null && differcrList.size() > position)
                                            differcrList.remove(position);
                                        notifyDataSetChanged();
                                        if (breadcrumbList.size() < 1) {
                                            timer.cancel();
                                            timer.purge();
                                            timer = null;
                                        }
                                    }
                                }
                        }
                        i++;
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                }

            });
        }

    }

    public class ViewHolder {
        ImageView profileImage;
        CustomTextView name, time;
    }
}
