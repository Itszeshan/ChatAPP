package com.quickblox.sample.chatapp.Model;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class User implements Serializable {

    private String name;
    private String phone;
    private String uId;
    private String mProfileImage;
    private String status;
    private String notificationKey;

    public User(String uId)
    {
        this.uId = uId;
    }

    public User(String name, String phone, String mProfileImage, String uId, String status) {
        this.name = name;
        this.phone = phone;
        this.mProfileImage = mProfileImage;
        this.uId = uId;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getuId() {
        return uId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Uri getProfileImage() {

        return Uri.parse(mProfileImage);
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public String getNotificationKey() {
        return notificationKey;
    }
}
