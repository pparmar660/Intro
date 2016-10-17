package com.intro.model;

/**
 * Created by vinove on 10/15/2015.
 */
public class ChatResponse  {

    private String text, gender, senderId, status, senderName, time;

    public ChatResponse() {
    }

    public ChatResponse(String text, String gender, String senderId, String status, String senderName, String time) {
        this.text = text;
        this.gender = gender;
        this.senderId = senderId;
        this.status = status;
        this.senderName = senderName;
        this.time = time;
    }


    public void setText(String text) {
        this.text = text;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public String getGender() {
        return gender;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }
//
//    @Override
//    public int compareTo(ChatResponse another) {
//        return Integer.parseInt(time)
//                - Integer.parseInt(another.time);
//    }
}
