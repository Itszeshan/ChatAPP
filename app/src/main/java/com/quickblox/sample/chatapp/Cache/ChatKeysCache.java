package com.quickblox.sample.chatapp.Cache;

import com.quickblox.sample.chatapp.Model.Key;

import java.util.ArrayList;

public class ChatKeysCache {

    private static ChatKeysCache chatKeysCache;
    private ArrayList<Key> keys;

    private ChatKeysCache()
    {
        this.keys = new ArrayList<Key>();
    }

    public static ChatKeysCache getInstance()
    {
        if(chatKeysCache == null)
            chatKeysCache = new ChatKeysCache();
        return chatKeysCache;
    }

    public void putKey(Key key)
    {
        if(!checkUid(key.getUid()))
            this.keys.add(key);
    }

    public void putKeys(ArrayList<Key> keys)
    {
        this.keys = keys;
    }

    private boolean checkUid(String uid)
    {
        if(keys.size() > 0)
        {
            for(int i = 0 ; i < keys.size(); i++)
            {
                if(keys.get(i).getUid().equals(uid))
                {
                    return true;
                }
            }
        }
        return false;
    }
    public ArrayList<String> getKeysOfUser(String uid)
    {
        ArrayList<String> keyList = new ArrayList<>();
        if(keys.size() > 0)
        {
            for(int i = 0 ; i < keys.size(); i++)
            {
                if(keys.get(i).getUid().equals(uid))
                {
                    return keys.get(i).getKeys();
                }
            }
        }
        return keyList;
    }

    public void clearCache()
    {
        this.keys.clear();
    }

}
