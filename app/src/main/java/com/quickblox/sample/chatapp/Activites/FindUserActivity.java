package com.quickblox.sample.chatapp.Activites;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.quickblox.sample.chatapp.Adapters.UserListAdapter;
import com.quickblox.sample.chatapp.Cache.AppUsersCache;
import com.quickblox.sample.chatapp.Cache.ContactsCache;
import com.quickblox.sample.chatapp.Model.Chat;
import com.quickblox.sample.chatapp.Model.Contact;
import com.quickblox.sample.chatapp.Model.User;
import com.quickblox.sample.chatapp.R;
import com.quickblox.sample.chatapp.Utils.CountryToPhonePrefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FindUserActivity extends AppCompatActivity {

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;
    private ContactsCache cache;
    private AppUsersCache usersCache;
    static ArrayList<User> userList, contactList;
    private Toolbar toolbar;
    ArrayList<Chat> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);


        toolbar = (Toolbar) findViewById(R.id.toolbar_create_chat);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Select Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        chatList = (ArrayList<Chat>) args.getSerializable("chatList");

        contactList = new ArrayList<>();
        userList = new ArrayList<>();
        cache = ContactsCache.getInstance();
        usersCache = AppUsersCache.getInstance();

        initRecyclerView();

        if(cache.getContactsArrayList().size() != 0)
        {
            userList = usersCache.getAppUsersArrayList();
            mUserListAdapter = new UserListAdapter(userList);
            mUserList.setAdapter(mUserListAdapter);
            updateList();
        }
        else
        {
            getContactList();
        }

        usersCache.putAppUsersArrayList(userList);


    }

    private void updateList() {
        updateContactList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(FindUserActivity.this, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity(intent);
                finish();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FindUserActivity.this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(intent);
        finish();
    }

    private void updateContactList()
    {
        String ISOPREFIX = countryISO();
        Cursor phones = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        String phone;

        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

//            long contactId = getContactIDFromNumber(phone, getApplicationContext());
//            Bitmap photo = openPhoto(contactId);

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(!String.valueOf(phone.charAt(0)).equals("+"))
            {
                if(String.valueOf(phone.charAt(0)).equals("0"))
                {
                    phone =phone.substring(1,phone.length());
                }
                phone = ISOPREFIX + phone;

            }



            User user = new User(name, phone, null,null,null);

            if(!checkNum(user))
            {
                contactList.add(user);
                updateUserDetails(user);
            }


        }

    }

    private void updateUserDetails(User user) {
        DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = userDBRef.orderByChild("phone").equalTo(user.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                    String phone = "";
                    String name = "";
                    String uId = "";
                    String profileImage = "";
                    String status = "";

                    for(DataSnapshot childSnapShot: dataSnapshot.getChildren())
                    {
                        if(childSnapShot.child("phone").getValue() != null)
                            phone = childSnapShot.child("phone").getValue().toString();

                        if(childSnapShot.child("profileImageUrl").getValue() != null)
                            profileImage = childSnapShot.child("profileImageUrl").getValue().toString();

                        if(childSnapShot.child("status").getValue() != null)
                            status = childSnapShot.child("status").getValue().toString();


                        name = findUserName(phone);
                        uId = childSnapShot.getKey();
                        if(phone.equals(myPhone))
                            continue;
                        User user  = new User(name, phone, profileImage,uId,status);
                        if(!checkName(user))
                        {
                            saveContactsToDatabase(name,phone);
                            userList.add(user);
                            mUserListAdapter = new UserListAdapter(userList);
                            mUserList.setAdapter(mUserListAdapter);
                            mUserListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getContactList() {
        String ISOPREFIX = countryISO();
        Cursor phones = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        String phone;

        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

//            long contactId = getContactIDFromNumber(phone, getApplicationContext());
//            Bitmap photo = openPhoto(contactId);

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if(!String.valueOf(phone.charAt(0)).equals("+"))
            {
                if(String.valueOf(phone.charAt(0)).equals("0"))
                {
                    phone =phone.substring(1,phone.length());
                }
                phone = ISOPREFIX + phone;

            }



            User user = new User(name, phone, null,null,null);
            cache.putContact(new Contact(phone,name));
            if(!checkNum(user))
            {
                contactList.add(user);
                getUserDetails(user);
            }


        }
    }

    private void saveContactsToDatabase(final String name, final String phone)
    {
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("contacts")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = new HashMap<>();

                if(dataSnapshot.exists())
                {
                    map = (Map<String, Object>) dataSnapshot.getValue();
                }

                map.put(phone, name);
                db.updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private ArrayList<Contact> convertUserToContact(ArrayList<User> users)
    {
        ArrayList<Contact> contacts = new ArrayList<>();

        for(int i = 0; i < users.size(); i++)
        {
            String name = users.get(i).getName();
            String phone = users.get(i).getPhone();
            Log.d("User_Name", name);
            contacts.add(new Contact(phone, name));
        }

        return contacts;
    }

    private void getUserDetails(User user)
    {
        DatabaseReference userDBRef = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = userDBRef.orderByChild("phone").equalTo(user.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                    String phone = "";
                    String name = "";
                    String uId = "";
                    String profileImage = "";
                    String status = "";

                    for(DataSnapshot childSnapShot: dataSnapshot.getChildren())
                    {
                        if(childSnapShot.child("phone").getValue() != null)
                            phone = childSnapShot.child("phone").getValue().toString();
                        if(childSnapShot.child("profileImageUrl").getValue() != null)
                            profileImage = childSnapShot.child("profileImageUrl").getValue().toString();
                        Log.d("Dekh 1", profileImage);
                        if(childSnapShot.child("status").getValue() != null)
                            status = childSnapShot.child("status").getValue().toString();

                        name = findUserName(phone);
                        uId = childSnapShot.getKey();
                        if(phone.equals(myPhone))
                            continue;
                        User user  = new User(name, phone, profileImage,uId,status);
                        if(!checkName(user))
                        {
                            saveContactsToDatabase(name,phone);
                            Log.d("YAHA_User", user.getName());
                            userList.add(user);
                            mUserListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void saveUserName(final String name, String uId) {
        final DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user").child(uId);

        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    HashMap<String, Object> getData = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if(getData.containsKey("name"))
                        {
                            getData.replace("name", name);
                        }
                        else
                        {
                            getData.put("name",name);
                        }

                        userDB.updateChildren(getData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String findUserName(String phone){
        String name = null;
        for(int i = 0 ; i < contactList.size(); i++)
        {
            if(contactList.get(i).getPhone().equals(phone))
            {
                name = contactList.get(i).getName();
                return name;
            }
        }
        return null;
    }

    private boolean checkName(User user)
    {
        for(int i = 0; i < userList.size(); i++)
        {
            if(userList.get(i).getPhone().equals(user.getPhone()))
            {
                return true;
            }

        }
        return false;
    }

    private String countryISO()
    {
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso() != null)
        {
            if(!telephonyManager.getNetworkCountryIso().toString().equals(""))
            {
                iso = telephonyManager.getNetworkCountryIso().toString();
            }
        }

        if(iso != null)
        {

            return CountryToPhonePrefix.getPhone(iso);
        }

        return iso;
    }

    private boolean checkNum (User user)
    {
        for(int i = 0; i < contactList.size(); i++)
        {
            if(contactList.get(i).getName().equals(user.getName()))
            {
                return true;
            }

        }
        return false;
    }

    private void initRecyclerView() {
        mUserList = (RecyclerView) findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled(false);
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager =  new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(userList);
        ((UserListAdapter) mUserListAdapter).setChatList(chatList);
        mUserList.setAdapter(mUserListAdapter);

    }
}
