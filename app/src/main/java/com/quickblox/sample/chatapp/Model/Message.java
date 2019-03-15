package com.quickblox.sample.chatapp.Model;

import java.util.ArrayList;

public class Message {

    public static final int SENT = 0;
    public static final int RECEIVED = 1;

    private String messageId;
    private String messageText;
    private String messageTime;
    private String messageDate;
    private ArrayList<String> mediaUrlList;
    private int type;

    public Message(String messageId, String messageText, String messageTime, int type, String messageDate,ArrayList<String> mediaUrlList) {
        this.messageText = messageText;
        this.messageTime = messageTime;
        this.type = type;
        this.messageId = messageId;
        this.messageDate = messageDate;
        this.mediaUrlList = mediaUrlList;
    }

    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public String getMessageDate() {
        return messageDate;
    }

    public String getMessageText() {

        return messageText;
    }

    public int getType() {
        return type;
    }

    public String getMessageId() {
        return messageId;
    }
}
