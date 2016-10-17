package com.intro.adapter;

import java.util.ArrayList;

import com.intro.android.R;
import com.intro.customviews.CustomTextView;
import com.intro.model.ChatResponse;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * ChatAdapter this is adapter for Chat Screen it set messages left & right and also color change according to gender.
 * By Mirza Faisal
*/

public class ChatAdapter extends BaseAdapter {

    private Context context;
    LayoutInflater inflater;
    private ArrayList<ChatResponse> messages;
    SharedPreferences userData;

    public ChatAdapter(Context context, ArrayList<ChatResponse> messages) {
        this.context = context;
        this.messages = messages;
        userData = context.getSharedPreferences("UserData", 0);
    }

    @Override
    public int getCount() {
        return messages.size();
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
        inflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.chat, parent,
                    false);
            holder.messageTxt = (CustomTextView) convertView.findViewById(R.id.message_txt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChatResponse response = messages.get(position);
        //System.out.println("Chat Time: "+response.getTime());

        if (userData.getString("gender", "").equalsIgnoreCase("0") && response.getSenderId().equalsIgnoreCase(userData.getString("userid", ""))) {
            holder.messageTxt.setTextColor(context.getResources().getColor(R.color.pink));
        } else {
            holder.messageTxt.setTextColor(context.getResources().getColor(R.color.blue));
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)holder.messageTxt.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        holder.messageTxt.setLayoutParams(params);
        if (response.getSenderId().equalsIgnoreCase(userData.getString("userid", ""))) {
//            holder.messageTxt.setGravity(Gravity.RIGHT);
            params.gravity = Gravity.RIGHT;
            holder.messageTxt.setLayoutParams(params);
            holder.messageTxt.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.in_message_bg));
        }else{
//            holder.messageTxt.setGravity(Gravity.LEFT);
            params.gravity = Gravity.LEFT;
            holder.messageTxt.setLayoutParams(params);
            holder.messageTxt.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.out_message_bg));
            if (response.getGender().equalsIgnoreCase("0")) {
                holder.messageTxt.setTextColor(context.getResources().getColor(R.color.pink));
            } else {
                holder.messageTxt.setTextColor(context.getResources().getColor(R.color.blue));
            }
        }
        if(userData.getString("gender", "").equalsIgnoreCase(response.getGender())){
            if (userData.getString("gender", "").equalsIgnoreCase("0") && response.getSenderId().equalsIgnoreCase(userData.getString("userid", ""))) {
                holder.messageTxt.setTextColor(context.getResources().getColor(R.color.pink));
            } else {
                holder.messageTxt.setTextColor(context.getResources().getColor(R.color.blue));
            }
            if (response.getSenderId().equalsIgnoreCase(userData.getString("userid", ""))) {
                holder.messageTxt.setGravity(Gravity.RIGHT);
            }else{
                holder.messageTxt.setGravity(Gravity.LEFT);
                  //  holder.messageTxt.setTextColor(context.getResources().getColor(R.color.white));
                holder.messageTxt.setTextColor(context.getResources().getColor(R.color.black));
            }
        }

        holder.messageTxt.setText(response.getText());
        return convertView;
    }

    class ViewHolder {
        CustomTextView messageTxt;
    }

}
