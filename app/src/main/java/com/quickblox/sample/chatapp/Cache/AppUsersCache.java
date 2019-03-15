package com.quickblox.sample.chatapp.Cache;

import com.quickblox.sample.chatapp.Model.User;

import java.util.ArrayList;

public class AppUsersCache {

    private static AppUsersCache appUsersCache;
    private ArrayList<User> appUsersArray;

    public AppUsersCache() {
        this.appUsersArray  = new ArrayList<User>();
    }

    public static synchronized AppUsersCache getInstance()
    {
        if(appUsersCache == null)
            appUsersCache = new AppUsersCache();
        return appUsersCache;
    }

    public void putAppUsersArrayList(ArrayList<User> appUsers)
    {
        this.appUsersArray = appUsers;
    }

    public void putUser(User User)
    {
        appUsersArray.add(User);
    }

    public String getNameByPhone(String phone)
    {
        if(appUsersArray.size() > 0 )
        {
            for(int i = 0 ; i < appUsersArray.size(); i++)
            {
                if(appUsersArray.get(i).getPhone().equals(phone))
                {
                    return appUsersArray.get(i).getName();
                }
            }
        }

        return null;
    }

    public ArrayList<User> getAppUsersArrayList()
    {
        return appUsersArray;
    }

    public void clearCache()
    {
        this.appUsersArray.clear();
    }
    
}
