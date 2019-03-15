package com.quickblox.sample.chatapp.Cache;

import android.util.Log;

import com.quickblox.sample.chatapp.Model.Contact;

import java.util.ArrayList;

public class ContactsCache {

    private static ContactsCache contactsCache;
    private ArrayList<Contact> contactsArray;

    public ContactsCache() {
        this.contactsArray  = new ArrayList<Contact>();
    }

    public static synchronized ContactsCache getInstance()
    {
        if(contactsCache == null)
            contactsCache = new ContactsCache();
        return contactsCache;
    }

    public void putContactsArrayList(ArrayList<Contact> contacts)
    {
        this.contactsArray = contacts;
    }

    public void putContact(Contact contact)
    {
        if(contactsArray.size() > 0)
        {
            for(int i = 0; i < contactsArray.size(); i++)
            {
                if(!contactsArray.get(i).getPhone().equals(contact.getPhone()))
                {
                    contactsArray.add(contact);
                    return;
                }
            }
        }
        else
        {
            contactsArray.add(contact);
        }
    }

    public String getNameByPhone(String phone)
    {
        if(contactsArray.size() > 0 )
        {
            for(int i = 0 ; i < contactsArray.size(); i++)
            {
                if(contactsArray.get(i).getPhone().equals(phone))
                {
                    return contactsArray.get(i).getName();
                }
            }
        }
        return null;
    }

    public ArrayList<Contact> getContactsArrayList()
    {
        return contactsArray;
    }

    public void clearCache()
    {
        this.contactsArray.clear();
    }

}
