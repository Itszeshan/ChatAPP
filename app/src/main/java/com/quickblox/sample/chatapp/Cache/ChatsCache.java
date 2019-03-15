package com.quickblox.sample.chatapp.Cache;

import com.quickblox.sample.chatapp.Model.Chat;

import java.util.ArrayList;

public class ChatsCache {

    private static ChatsCache chatsCache;
    private ArrayList<Chat> chatsArray;

    public ChatsCache() {
        this.chatsArray  = new ArrayList<>();
    }

    public static synchronized ChatsCache getInstance()
    {
        if(chatsCache == null)
            chatsCache = new ChatsCache();
        return chatsCache;
    }

    public void putChatsArrayList(ArrayList<Chat> chats)
    {
        this.chatsArray = chats;
    }

    public void putChat(Chat chat)
    {
        if(chatsArray.size() > 0)
        {
            for(int i = 0; i < chatsArray.size(); i++)
            {
                if(!chatsArray.get(i).getChatId().equals(chat.getChatId()))
                {
                    chatsArray.add(chat);
                    return;
                }
            }
        }
        else
        {
            chatsArray.add(chat);
        }
    }

    public ArrayList<Chat> getChatsArrayList()
    {
        return chatsArray;
    }

    public void clearCache()
    {
        this.chatsArray.clear();
    }
    
}
