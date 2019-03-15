package com.quickblox.sample.chatapp.Activites;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.quickblox.sample.chatapp.Adapters.MediaAdapter;
import com.quickblox.sample.chatapp.Adapters.MessageListAdapter;
import com.quickblox.sample.chatapp.Model.Chat;
import com.quickblox.sample.chatapp.Model.Message;
import com.quickblox.sample.chatapp.Model.User;
import com.quickblox.sample.chatapp.R;
import com.quickblox.sample.chatapp.Utils.SendNotification;
import com.wang.avi.AVLoadingIndicatorView;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatMessageActivity extends AppCompatActivity {

    private RecyclerView mMsgList,mMedia;
    private RecyclerView.Adapter mMsgListAdapter,mMediaAdapter;
    private RecyclerView.LayoutManager mMsgListLayoutManager,mMediaLayoutManager;
    private FloatingActionButton sendBtn;
    private EditText sendEdit;
    String chatId,chatName = "";
    private ArrayList<Message> msgList;
    DatabaseReference mChatDB;
    String lastMsg = "";
    String lastMsgTime = "";
    String lastMsgDate = "";
    String receiver = "";
    Chat mChat;
    private Toolbar toolbar;
    private FrameLayout progressBarHolder;
    private AVLoadingIndicatorView bar;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        progressBarHolder = (FrameLayout) findViewById(R.id.progressBar_holder);
        sendBtn = (FloatingActionButton) findViewById(R.id.send_message_btn);
        sendEdit = (EditText) findViewById(R.id.send_message_edit);
        bar = (AVLoadingIndicatorView) findViewById(R.id.avi);

        msgList = new ArrayList<>();

       sendEdit.setOnTouchListener(new View.OnTouchListener() {

           @Override
           public boolean onTouch(View v, MotionEvent event) {
               final int DRAWABLE_LEFT = 0;
               final int DRAWABLE_TOP = 1;
               final int DRAWABLE_RIGHT = 2;
               final int DRAWABLE_BOTTOM = 3;

               if(event.getAction() == MotionEvent.ACTION_UP) {
                   if(event.getRawX() >= (sendEdit.getRight() - sendEdit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                       openMedia();
                       return true;
                   }
               }

               return false;
           }
       });

       mChat = (Chat) getIntent().getSerializableExtra("chat");

        chatName = mChat.getChatName();
        chatId = mChat.getChatId();

        getReceiver();
        toolbar = (Toolbar) findViewById(R.id.toolbar_messages);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(chatName);

        mChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaUriList.isEmpty())
                {
                    sendTextMessage();
                }
                else
                {
                    sendMediaMessage();
                }
            }
        });

        initMessageRecyclerView();
        initMediaRecyclerView();
        getChatMessages();


    }

    final int[] totalMediaUploaded = {0};
    final ArrayList<String> mediaIdList = new ArrayList<>();

    ArrayList<String> mediaUriList = new ArrayList<>();
    private void openMedia() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Media"),1);

    }


    private void showProgressBar()
    {
        progressBarHolder.setVisibility(View.VISIBLE);
        bar.show();
    }

    private void hideProgressBar()
    {
        progressBarHolder.setVisibility(View.INVISIBLE);
        bar.hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 1)
            {
                /*
                Clip data tells whether data is just single Uri or
                it has multiple uri's(List).
                if clip data is not null then intent returned a list.
                 */
                if(data.getClipData() == null)
                {
                    mediaUriList.add(data.getData().toString());
                }
                else
                {
                    for(int i = 0;i < data.getClipData().getItemCount(); i++)
                    {
                        mediaUriList.add(data.getClipData()
                                .getItemAt(i)
                                .getUri()
                                .toString());
                    }
                }
            }

            mMediaAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case android.R.id.home:
                deleteChatIfNotMessaged(receiver);
                deleteChatIfNotMessaged(FirebaseAuth.getInstance().getCurrentUser().getUid());
                Intent intent = new Intent(ChatMessageActivity.this, ChatActivity.class);
                startActivity(intent);
                finish();
        }

        return true;
    }

    private void deleteChatIfNotMessaged(final String uid) {
        DatabaseReference checkChatDb = FirebaseDatabase.getInstance().getReference()
                .child("chat")
                .child(chatId);

        checkChatDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("IDR_DEKH", "me aa");

                if(!dataSnapshot.exists())
                {
                    Log.d("IDR_DEKH", "me andr aa");
                    final DatabaseReference userDb  = FirebaseDatabase.getInstance().getReference()
                            .child("user")
                            .child(uid)
                            .child("chat");

                    userDb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                                map.remove(chatId);
                                Map<String,Object> updateMap = new HashMap<>();
                                updateMap.putAll(map);
                                userDb.updateChildren(updateMap);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteChatIfNotMessaged(receiver);
        deleteChatIfNotMessaged(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Intent intent = new Intent(ChatMessageActivity.this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    private void getChatMessages() {

        mChatDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    String text = "";
                    String senderId = "";
                    String time = "";
                    String date = "";
                    int type;
                    ArrayList<String> mediaUriList = new ArrayList<>();



                    if(dataSnapshot.child("text").getValue() != null)
                    {
                        text = dataSnapshot.child("text").getValue().toString();
                        lastMsg = text;
                    }

                    if(dataSnapshot.child("time").getValue() != null)
                    {
                        time = dataSnapshot.child("time").getValue().toString();
                        lastMsgTime = time;
                    }

                    if(dataSnapshot.child("sender").getValue() != null)
                    {
                        senderId = dataSnapshot.child("sender").getValue().toString();
                    }

                    if(senderId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    {
                        type = 0;
                    }
                    else{
                        type = 1;
                    }

                    if(dataSnapshot.child("date").getValue() != null)
                    {
                        date = (String) dataSnapshot.child("date").getValue();
                        lastMsgDate = date;
                    }

                    if(dataSnapshot.child("media").getChildrenCount() > 0)
                    {
                        for(DataSnapshot media: dataSnapshot.child("media").getChildren())
                        {
                            mediaUriList.add(media.getValue().toString());
                        }
                    }


                    Message msg = new Message(dataSnapshot.getKey(),text, time,type, date, mediaUriList);

                    if(senderId != "")
                    {
                        msgList.add(msg);
                        mMsgListLayoutManager.scrollToPosition(msgList.size()-1);
                        mMsgListAdapter.notifyDataSetChanged();
                    }

                    updateLastMsg(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    updateLastMsg(receiver);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getReceiver()
    {
        final DatabaseReference userDB = FirebaseDatabase.getInstance().getReference()
        .child("user")
        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        .child("chat")
        .child(chatId);

        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                    receiver = (String) map.get("receiver");
                    Log.d("HERE_IT_IS", receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateLastMsg(String uid)
    {
        final DatabaseReference userDB = FirebaseDatabase.getInstance().getReference()
                .child("user")
                .child(uid)
                .child("chat")
                .child(chatId);

        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                    map.put("lastMsg", lastMsg);
                    map.put("lastMsgTime",lastMsgTime);
                    map.put("lastMsgDate", lastMsgDate);
                    userDB.updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendTextMessage()
    {
        String text = sendEdit.getText().toString();

        if(!text.isEmpty())
        {
            String messageId = mChatDB.push().getKey();
            final DatabaseReference newMessageDB = mChatDB.child(messageId);

            final Map<String, Object> newMessageMap = new HashMap<>();

            String messageTime = "";
            Time time = new Time();
            time.setToNow();

            messageTime = time.hour + ":" + time.minute + ":" + time.second;


            try {
                SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm:ss a");
                Date _24HourDt = _24HourSDF.parse(messageTime);
                messageTime = _12HourSDF.format(_24HourDt);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
            String date = df.format(c);

            newMessageMap.put("time",messageTime);
            newMessageMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
            newMessageMap.put("date", date);
            newMessageMap.put("text", text);
            updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
        }

        sendEdit.setText("");


    }

    private void sendMediaMessage()
    {
        String messageTime = "";
        Time time = new Time();
        time.setToNow();

        messageTime = time.hour + ":" + time.minute + ":" + time.second;


        try {
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm:ss a");
            Date _24HourDt = _24HourSDF.parse(messageTime);
            messageTime = _12HourSDF.format(_24HourDt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
        String date = df.format(c);

            String messageId = mChatDB.push().getKey();
            final DatabaseReference newMessageDB = mChatDB.child(messageId);

            final Map<String, Object> newMessageMap = new HashMap<>();

            newMessageMap.put("time",messageTime);
            newMessageMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
            newMessageMap.put("date", date);


            if(!mediaUriList.isEmpty())
            {
                for(String mediaUri: mediaUriList)
                {
                    String mediaId =  newMessageDB.child("media").push().getKey();
                    mediaIdList.add(mediaId);

                    final StorageReference filePath = FirebaseStorage.getInstance()
                            .getReference()
                            .child("chat")
                            .child(chatId)
                            .child(messageId)
                            .child(mediaId);

                    final UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

                     showProgressBar();

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded[0]) + "/", uri.toString());

                                    totalMediaUploaded[0]++;
                                    if(totalMediaUploaded[0] == mediaUriList.size())
                                    {
                                        String text = sendEdit.getText().toString();
                                        if(!text.isEmpty())
                                            newMessageMap.put("text", text);

                                        updateDatabaseWithNewMessage(newMessageDB,newMessageMap);

                                        hideProgressBar();
                                        sendEdit.setText("");

                                    }
                                }
                            });
                        }
                    });

                }
            }


    }


    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map<String,Object> updateMap)
    {
        newMessageDb.updateChildren(updateMap);
        sendEdit.setText("");
        mediaUriList.clear();
        mediaIdList.clear();
        mMediaAdapter.notifyDataSetChanged();

        String message;

        if(updateMap.get("text") != null)
        {
            message = updateMap.get("text").toString();
        }
        else
        {
            message = "Media received";
        }

        for(User mUser: mChat.getUsers())
        {
            Log.d("ML1", "NO: " + " " + mUser.getNotificationKey());
            new SendNotification(message, "New Message", mUser.getNotificationKey());
        }
    }

    private void initMediaRecyclerView() {
        mMedia = (RecyclerView) findViewById(R.id.media_recycler_view);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        mMediaLayoutManager =  new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(),mediaUriList);
        mMedia.setAdapter(mMediaAdapter);

    }


    private void initMessageRecyclerView() {
        mMsgList = (RecyclerView) findViewById(R.id.messagesList);
        mMsgList.setNestedScrollingEnabled(false);
        mMsgList.setHasFixedSize(false);
        mMsgListLayoutManager =  new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mMsgList.setLayoutManager(mMsgListLayoutManager);
        mMsgListAdapter = new MessageListAdapter(msgList);
        mMsgList.setAdapter(mMsgListAdapter);

    }
    
}
