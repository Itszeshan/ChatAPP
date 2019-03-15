package com.quickblox.sample.chatapp.Activites;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.quickblox.sample.chatapp.Adapters.ChatListAdapter;
import com.quickblox.sample.chatapp.Adapters.UserListAdapter;
import com.quickblox.sample.chatapp.Cache.AppUsersCache;
import com.quickblox.sample.chatapp.Cache.ChatKeysCache;
import com.quickblox.sample.chatapp.Cache.ChatsCache;
import com.quickblox.sample.chatapp.Cache.ContactsCache;
import com.quickblox.sample.chatapp.Model.Chat;
import com.quickblox.sample.chatapp.Model.User;
import com.quickblox.sample.chatapp.R;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FloatingActionButton mDisplayContactsBtn;
    private RecyclerView mChatList;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;
    private ArrayList<Chat> chatList;
    private ChatsCache chatsCache;
    private android.support.v7.widget.Toolbar toolbar;
    String chatName,lastMsg,lastMsgTime,lastMsgDate,chatImage;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.logout_btn_menu:
                clearAllCache();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity(intent);
                finish();
                break;

            case R.id.settings_btn_menu:
                intent = new Intent(ChatActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void clearAllCache() {
        AppUsersCache.getInstance().clearCache();
        ChatKeysCache.getInstance().clearCache();
        ChatsCache.getInstance().clearCache();
        ContactsCache.getInstance().clearCache();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Fresco.initialize(this);
        mDisplayContactsBtn = (FloatingActionButton) findViewById(R.id.display_contacts_btn);
        chatList = new ArrayList<>();
        chatsCache = ChatsCache.getInstance();

        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("notificationKey")
                        .setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        mDisplayContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(ChatActivity.this, FindUserActivity.class);
                bundle.putSerializable("chatList", (Serializable)chatList);
                intent.putExtra("BUNDLE", bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_chat);
        }

        setSupportActionBar(toolbar);


        getPermissions();
        initRecyclerView();
        getUserChatList();
       //chatsCache.putChatsArrayList(chatList);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
    

    private void getUserChatList()
    {
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("user")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("chat");

        mUserChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot childSnapshot: dataSnapshot.getChildren())
                    {

                        chatName = (String) childSnapshot.child("chatName").getValue();
                        Log.d("Hun_uper", chatName);
                        lastMsg   = (String) childSnapshot.child("lastMsg").getValue();
                        lastMsgTime = (String) childSnapshot.child("lastMsgTime").getValue();
                        chatImage = (String) childSnapshot.child("chatImage").getValue();
                        lastMsgDate = (String) childSnapshot.child("lastMsgDate").getValue();
                        
                        String receiver = (String) childSnapshot.child("receiver").getValue();
                        Log.d("Hun_uper", receiver);

                        Chat chat = new Chat(childSnapshot.getKey(),chatName,lastMsg,lastMsgTime,chatImage,lastMsgDate);
                        //updatePic(receiver, chat);

                        if(!checkChatId(chat))
                        {
                            User user = new User(receiver);
                            chat.addUser(user);
                            chatList.add(chat);
                            getNotificationKey(receiver);
                        }
                        
                    }

                    if(chatList.size() > 1)
                    {
                        Collections.sort(chatList, new Comparator<Chat>() {

                            @Override
                            public int compare(Chat o1, Chat o2) {
                                try {
                                    return new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a").parse(o1.getDate())
                                            .compareTo(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a")
                                                    .parse(o2.getDate()));
                                } catch (ParseException e) {
                                    return 0;
                                }
                            }
                        });

                        Collections.reverse(chatList);
                    }

                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void updatePic(final String receiver, final Chat chat)
    {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("user")
                .child(receiver);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String newchatImage = "";
                String notificationKey = "";
                if(dataSnapshot.exists())
                {
                    Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                    newchatImage = (String) map.get("profileImageUrl");
                }

                chat.setChatImage(newchatImage);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        
    }

    private void getNotificationKey(final String receiver)
    {
        Log.d("CameID", receiver);

        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user")
                .child(receiver);

        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = new User(dataSnapshot.getKey());
                Log.d("CameID2", receiver);

                String notificationKey = "";
                String profileImage = "";
                if(dataSnapshot.child("notificationKey").getValue() != null)
                {
                    notificationKey = dataSnapshot.child("notificationKey").getValue().toString();
                    Log.d("Notifi_Key", notificationKey);
                    user.setNotificationKey(notificationKey);
                }

                for(Chat mChat: chatList)
                {
                    Log.d("ML", mChat.getChatName() + " " + mChat.getUsers().size());
                    for(User mUser: mChat.getUsers())
                    {
                        if(mUser.getuId().equals(receiver))
                        {
                            Log.d("notifi_Key", notificationKey);
                            mUser.setNotificationKey(notificationKey);
                            Log.d("ML2", "NO: " + " " + mUser.getNotificationKey());
                        }
                    }
                }

                mChatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean checkChatId(Chat chat)
    {
        for(int i = 0; i < chatList.size(); i++)
        {
            if(chatList.get(i).getChatId().equals(chat.getChatId()))
            {
                return true;
            }
        }
        return false;
    }

    private void initRecyclerView() {
        mChatList = (RecyclerView) findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled(false);
        mChatList.setHasFixedSize(false);
        mChatListLayoutManager =  new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(chatList);
        mChatList.setAdapter(mChatListAdapter);

    }
    

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }



}
