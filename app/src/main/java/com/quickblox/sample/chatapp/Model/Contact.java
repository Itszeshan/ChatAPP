package com.quickblox.sample.chatapp.Model;

/*

Displaying the field name as phone but it actually is the status.

 */

import android.net.Uri;

public class Contact {

    private String phone;
    private String name;

    public Contact(String phone, String name) {
        this.phone = phone;
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

}
