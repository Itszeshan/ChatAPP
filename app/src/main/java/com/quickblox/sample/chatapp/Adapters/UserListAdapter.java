package com.quickblox.sample.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.quickblox.sample.chatapp.Activites.ChatMessageActivity;
import com.quickblox.sample.chatapp.Cache.ChatKeysCache;
import com.quickblox.sample.chatapp.Cache.ContactsCache;
import com.quickblox.sample.chatapp.Model.Chat;
import com.quickblox.sample.chatapp.R;
import com.quickblox.sample.chatapp.Model.User;
import com.quickblox.sample.chatapp.Model.Key;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private ArrayList<User> userList;
    private ArrayList<Chat> chatList;

    String currentUserName,otherUserName = "";
    String currentUserPhone,otherUserPhone = "";
    public UserListAdapter(ArrayList<User> userList) {
        this.userList = userList;
    }
    ArrayList<String> chats1 = new ArrayList<>();
    ArrayList<String> chats2 = new ArrayList<>();
    String common = null;
    ChatKeysCache keysCache;

    public void setChatList(ArrayList<Chat> chatList)
    {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_user, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        keysCache = ChatKeysCache.getInstance();

        UserListViewHolder rcv = new UserListViewHolder(view);

        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListViewHolder holder, final int position) {
        holder.mName.setText(userList.get(position).getName());
        holder.mPhone.setText(userList.get(position).getStatus());
        if(!userList.get(position).getProfileImage().toString().isEmpty())
        {
            Glide.with(holder.context).load(userList.get(position).getProfileImage()).into(holder.mProfileImage);
        }
         getUserChatKeys(userList.get(position).getuId());
         getUserChatKeys(FirebaseAuth.getInstance().getCurrentUser().getUid());

        holder.userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createChat(position,view);
            }
            });

        }

    private void createChat(int position, View view) {

        chats1 = keysCache.getKeysOfUser(userList.get(position).getuId());
        chats2 = keysCache.getKeysOfUser(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Log.d("Dekh_size", chats1.size() + " " + chats2.size());

        for(String key: chats1)
        {
            Log.d("chat_keys_1", key);
        }

        for(String key: chats2)
        {
            Log.d("chat_keys_2", key);
        }

        chats1.retainAll(chats2);
        if(chats1.size() > 0)
        {
            common = chats1.get(0);
        }
        if(common!=null)
        Log.d("Dekh_common", common);

        if (common == null) {

            getCurrentUserNameFromOtherUserContacts(userList.get(position).getuId(), FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());


            getUserChatKeys(userList.get(position).getuId());
            getUserChatKeys(FirebaseAuth.getInstance().getCurrentUser().getUid());

//            Intent intent = new Intent(view.getContext(), ChatMessageActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            Bundle bundle = new Bundle();
//            bundle.putString("chatId", key);
//            otherUserName = ContactsCache.getInstance().getNameByPhone(userList.get(position).getPhone());
//            Log.d("DEKho", " " + otherUserName);
//            bundle.putString("chatName",otherUserName);
//            intent.putExtras(bundle);
//            view.getContext().startActivity(intent);
            Toast.makeText(view.getContext(), "Chat Created!", Toast.LENGTH_SHORT).show();
        } else {

//            Intent intent = new Intent(view.getContext(), ChatMessageActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            Bundle bundle = new Bundle();
//            bundle.putString("chatId", common);
//            otherUserName = ContactsCache.getInstance().getNameByPhone(userList.get(position).getPhone());
//            bundle.putString("chatName",otherUserName);
//            intent.putExtras(bundle);
//            view.getContext().startActivity(intent);

            Toast.makeText(view.getContext(), "Chat Already exist!", Toast.LENGTH_SHORT).show();
        }

    }

    private void getCurrentUserNameFromOtherUserContacts(final String uId, final String phoneNumber) {
        Log.d("USER_CAME_IP", uId + " " + phoneNumber);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("contacts")
                .child(uId);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("USER_CAME_IN", uId + " " + phoneNumber);
                String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
                Map<String,Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                for(String key1: map.keySet())
                {
                    Log.d("KL1", key);
                    if(key1.equals(phoneNumber))
                    {
                        Log.d("USER_CAME_IN", "yes");
                        currentUserName = (String) map.get(phoneNumber);
                        Log.d("USER_CAME_IN", currentUserName);
                    }
                }

                setCurrentUserChatDetails(key, uId);
                setOtherUserChatDetails(key, uId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cacheKeys(Key key)
    {
        keysCache.putKey(key);
    }


    private void setCurrentUserChatDetails(String key, final String uid)
    {
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("user")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("chat")
                .child(key);

        DatabaseReference otherUserDB = FirebaseDatabase.getInstance().getReference().child("user")
                .child(uid);

        otherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                String profileImageUrl = null;
                otherUserPhone = (String) map.get("phone");

                if(map.get("profileImageUrl") != null)
                {
                    profileImageUrl = (String) map.get("profileImageUrl");
                }

                otherUserName = ContactsCache.getInstance().getNameByPhone(otherUserPhone);

                Map<String, Object> updateMap = new HashMap<>();

                if(otherUserName == null)
                {
                    updateMap.put("chatName", otherUserPhone);
                }
                else
                {
                    updateMap.put("chatName", otherUserName);
                }

                if(profileImageUrl != null)
                {
                    updateMap.put("chatImage", profileImageUrl);
                }

                updateMap.put("creator", FirebaseAuth.getInstance().getCurrentUser().getUid());
                updateMap.put("receiver", uid);
                updateMap.put("lastMsg", "Chat Created!");

                Time time = new Time();
                time.setToNow();

                String messageTime = time.hour + ":" + time.minute + ":" + time.second;


                try {
                    SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm:ss");
                    SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm:ss a");
                    Date _24HourDt = _24HourSDF.parse(messageTime);
                    messageTime = _12HourSDF.format(_24HourDt);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                updateMap.put("lastMsgTime",messageTime);

                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
                String date = df.format(c);

                updateMap.put("lastMsgDate", date);

                chatRef.updateChildren(updateMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void setOtherUserChatDetails(String key, final String uId)
    {
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("user").child(uId)
                .child("chat")
                .child(key);

        final DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("user")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        currentUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                    currentUserPhone = (String) map.get("phone");
                    String profileImageUrl = (String) map.get("profileImageUrl");

                    Map<String, Object> updateMap = new HashMap<>();

                    Log.d("SET_CAME_IN", "yes");

                    if(currentUserName != null )
                    {
                        updateMap.put("chatName", currentUserName);
                    }

                    if(currentUserName == null)
                    {
                        updateMap.put("chatName", currentUserPhone);
                    }

                    if(profileImageUrl != null)
                    {
                        updateMap.put("chatImage", profileImageUrl);
                    }

                    updateMap.put("creator", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    updateMap.put("receiver", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    updateMap.put("lastMsg", "Chat Created!");

                    Time time = new Time();
                    time.setToNow();

                    String messageTime = time.hour + ":" + time.minute + ":" + time.second;


                    try {
                        SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm:ss");
                        SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm:ss a");
                        Date _24HourDt = _24HourSDF.parse(messageTime);
                        messageTime = _12HourSDF.format(_24HourDt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    updateMap.put("lastMsgTime",messageTime);

                    Date c = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
                    String date = df.format(c);

                    updateMap.put("lastMsgDate", date);

                    chatRef.updateChildren(updateMap);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String checkExistingChat()
    {
        chats1.retainAll(chats2);
        if(chats1.size() > 0)
        {
            common = chats1.get(0);
        }

        return common;
    }

    private void getUserChatKeys(final String uId)
    {
        Log.d("MERA_TAG",uId);
        final ArrayList<String> chats = new ArrayList<>();

        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user")
                .child(uId).child("chat");

        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                    Log.d("ISKO_Cached", uId);
                    for(String key: map.keySet())
                    {
                        chats.add(key);
                    }

                    cacheKeys(new Key(uId,chats));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public class UserListViewHolder extends RecyclerView.ViewHolder {

        public TextView mName,mPhone;
        public CircleImageView mProfileImage;
        public LinearLayout userContainer;
        public Context context;

        public UserListViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.user_name);
            mPhone = itemView.findViewById(R.id.user_phone);
            mProfileImage = itemView.findViewById(R.id.profile_image);
            userContainer = itemView.findViewById(R.id.whole_user_container);
            context = itemView.getContext();
        }
    }
}
