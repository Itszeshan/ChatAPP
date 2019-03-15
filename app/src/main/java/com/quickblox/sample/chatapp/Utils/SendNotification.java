package com.quickblox.sample.chatapp.Utils;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class SendNotification {

    public SendNotification(String message, String heading, String notificationKey)
    {
        try {
            JSONObject object = new JSONObject("{'contents': {'en':'"+ message +"'}, " +
                    "'include_player_ids': ['" + notificationKey + "']," +
                    "'headings':{'en': '" + heading + "'}}");

            OneSignal.postNotification(object, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
