package com.quickblox.sample.chatapp.Model;

import java.util.ArrayList;

public class Key {

    private String uid;
    private ArrayList<String> keys;

    public Key(String uid, ArrayList<String> keys) {
        this.uid = uid;
        this.keys = keys;
    }

    public String getUid() {
        return uid;
    }

    public ArrayList<String> getKeys() {
        return keys;
    }
}
