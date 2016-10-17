package com.intro.model;

/**
 * Created by vinove on 10/15/2015.
 */
public class Chat {

    private String text,gender,senderId,status,senderName,sendTime,receiveTime;

    public Chat(String text, String gender, String senderId, String status, String senderName, String sendTime, String receiveTime){
        this.text = text;
        this.gender = gender;
        this.senderId = senderId;
        this.status = status;
        this.senderName = senderName;
        this.sendTime = sendTime;
        this.receiveTime = receiveTime;
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

    public String getSendTime() {
        return sendTime;
    }

    public String getReceiveTime() {
        return receiveTime;
    }
}
