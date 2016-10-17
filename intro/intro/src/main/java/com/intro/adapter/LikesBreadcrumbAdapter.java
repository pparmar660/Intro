package com.intro.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.intro.android.ChatActivity;
import com.intro.android.NewMatchesActivity;
import com.intro.android.ProfileActivity;
import com.intro.android.R;
import com.intro.customviews.CustomTextView;
import com.intro.customviews.ImageViewRounded;
import com.intro.imageUtils.ImageLoader;
import com.intro.utils.GlobalVariables;
import com.intro.webservice.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ankit on 29/8/16 and the adapter used for inflating and set views for breadcrumbs.
 */
public class LikesBreadcrumbAdapter extends BaseAdapter {

    LayoutInflater inflater;
    Context con;
    int position;
    ArrayList<JSONObject> matchesList;

    public static Timer timer = null;
    private int i = 0;
    private long diffMinutes, diffHours;
    public static boolean isRunningService = false;
    public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 seconds
    private Handler mHandler = new Handler();

    public ArrayList<Long> differcrList;
    private List<String> senderidList, messageCountList;
    TimeDisplayTimerTask timedisplatTaxkObj;
    private String listOfId = "";
    int likedays;

    public LikesBreadcrumbAdapter(Context con, ArrayList<JSONObject> matchesList) {
        this.con = con;
        this.matchesList = matchesList;
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
        inflater = (LayoutInflater) con.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        holder = new ViewHolder();

        convertView = inflater.inflate(R.layout.breadcrumb_list_item, parent, false);

        holder.breadcrumbLikeLayout = (RelativeLayout) convertView.findViewById(R.id.breadcrumbLikeLayout);
        holder.breadcrumbImage = (ImageViewRounded) convertView.findViewById(R.id.breadcrumbProfileImg);
        holder.timeTxt = (CustomTextView) convertView.findViewById(R.id.breadcrumbTimeTextView);
        holder.messageCount = (CustomTextView) convertView.findViewById(R.id.messageCount);
        convertView.setTag(holder);

        if (position != matchesList.size() - 1) {
            final JSONObject obj = matchesList.get(position);
            try {
                ImageLoader iL = new ImageLoader(con);

                Bitmap bitmap = iL.getImage(GlobalVariables.Image_URL + obj.getString("firebaseid") + "-1");
//                iL.DisplayImage(GlobalVariables.Image_URL + obj.getString("firebaseid").toString() + "-1", holder.breadcrumbImage);

                SharedPreferences userData = con.getSharedPreferences("UserData", 0);
                senderidList = new LinkedList<>(Arrays.asList(userData.getString("senderid", "").split(",")));
                messageCountList = new LinkedList<>(Arrays.asList(userData.getString("messageCount", "").split(",")));
                for (int i = 0; i < senderidList.size(); i++) {
                    if (senderidList.get(i).equalsIgnoreCase(obj.getString("firebaseid"))) {
                        // holder.breadcrumbLikeLayout.setBackgroundColor(con.getResources().getColor(R.color.blue));
                        holder.messageCount.setVisibility(View.VISIBLE);
                        if (userData.getString("gender", "").equalsIgnoreCase("0")) {
                            holder.messageCount.setBackground(con.getResources().getDrawable(R.drawable.message_count_pink));
                        } else {
                            holder.messageCount.setBackground(con.getResources().getDrawable(R.drawable.message_count_blue));
                        }
                        holder.messageCount.setText(messageCountList.get(i));
                    }
                }

                likedays = obj.getInt("likeDays");


                long difference = 0;
                try {
                    Date matchDate = null;
                    SimpleDateFormat format = new SimpleDateFormat(
                            "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'"); // yyyy-MM-dd HH 'hrs' mm 'min'
                    format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                    System.out.println("Date : " + obj.getString("liketime"));
                    try {
                        matchDate = format.parse(obj.getString("liketime"));
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_YEAR, 6);
                        Date date = new Date(cal.getTimeInMillis());
                        difference = date.getTime() - matchDate.getTime();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                diffMinutes = difference / (60 * 1000) % 60;
                diffHours = difference / (60 * 60 * 1000) % 24;
                //  holder.breadcrumbImage.setImageBitmap(bitmap);
                iL.DisplayImage(GlobalVariables.Image_URL + obj.getString("firebaseid") + "-1", holder.breadcrumbImage);
                if (likedays == 7) {
                    holder.timeTxt.setTextColor(con.getResources().getColor(R.color.pink));
                    if (diffHours < 1) {
                        holder.timeTxt.setText(diffMinutes + " mins");
                    } else {
                        holder.timeTxt.setText(diffHours + " hrs");
                    }
                    if (differcrList.size() < (position + 1)) {
                        differcrList.add(difference);
                        timedisplatTaxkObj.textViewList.add(holder.timeTxt);
                    }
                } else if (likedays < 7) {

                    holder.timeTxt.setTextColor(con.getResources().getColor(R.color.black));
                    holder.timeTxt.setText((7 - obj.getInt("likeDays")) + " days");
                } else {
                    try {
                        holder.breadcrumbImage.setImageBitmap(toGrayscale(bitmap));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                holder.breadcrumbLikeLayout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            holder.breadcrumbLikeLayout.setBackgroundColor(Color.TRANSPARENT);

                            for (int i = 0; i < senderidList.size(); i++) {
                                if (senderidList.get(i).equalsIgnoreCase(obj.getString("firebaseid"))) {
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
                            intent.putExtra(GlobalVariables.comeFrom, GlobalVariables.breadcrumbAdapter);
                            con.startActivity(intent);
                            ((Activity) con).finish();
                            ((Activity) con).overridePendingTransition(0, R.anim.exit_slide_right);
                            SharedPreferences settings = con.getSharedPreferences("UserData", 0);
                            SharedPreferences.Editor preferencesEditor = settings.edit();

                            preferencesEditor.putString("matchUserId", obj.getString("firebaseid"));
                            preferencesEditor.putString("matchUserName", BaseActivity.decodeUTF(obj.getString("Name")));
                            preferencesEditor.putString("matchUserExpires", "");
                            preferencesEditor.putString("matchUserAge", obj.getString("age"));
                            preferencesEditor.putString("matchUserGender", obj.getString("gender"));
                            preferencesEditor.putString("senderid", listOfId);
                            preferencesEditor.apply();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        } else {
            holder.breadcrumbImage.setBackground(con.getResources().getDrawable(R.drawable.breadcrumb_item_bg));
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
                                    if (likedays == 7) {
                                        textViewList.get(j).setTextColor(con.getResources().getColor(R.color.pink));
                                        if (diffHours < 1) {
                                            textViewList.get(j).setText(diffMinutes + " mins");
                                        } else {
                                            textViewList.get(j).setText(diffHours + " hrs");
                                        }
                                    } else {
                                        textViewList.get(j).setTextColor(con.getResources().getColor(R.color.pink));
                                        textViewList.get(j).setText((7 - likedays) + " days");
                                    }
                                    if (diff < 0) {
                                        matchesList.remove(position);
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
                        // e.printStackTrace();
                    }
                }

            });
        }

    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public class ViewHolder {
        ImageViewRounded breadcrumbImage;
        CustomTextView timeTxt, messageCount;
        RelativeLayout breadcrumbLikeLayout;
    }
}

