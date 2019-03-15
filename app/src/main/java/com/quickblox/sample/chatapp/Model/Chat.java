package com.quickblox.sample.chatapp.Model;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;

public class Chat implements Serializable {

    private String chatId;
    private String chatName;
    private String lastMsg;
    private String lastMsgTime;
    private String chatImage;
    private String date;
    private ArrayList<User> users = new ArrayList<>();

    public Chat(String chatId, String chatName, String lastMsg,String lastMsgTime, String chatImage, String date) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.lastMsg = lastMsg;
        this.lastMsgTime = lastMsgTime;
        this.chatImage = chatImage;
        this.date = date;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    public String getDate() {
        return date;
    }

    public Uri getChatImage() {
        if(chatImage == null)
            return null;
        return Uri.parse(chatImage);
    }
    public String getLastMsg() {
        return lastMsg;
    }

    public String getLastMsgTime() {
        return lastMsgTime;
    }

    public String getChatId() {
        return chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public ArrayList<User> getUsers()
    {
        return this.users;
    }

    public void addUser(User user)
    {
        users.add(user);
    }


}
