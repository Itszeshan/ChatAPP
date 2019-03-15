package com.quickblox.sample.chatapp.Activites;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickblox.sample.chatapp.R;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class ShowProfileActivity extends AppCompatActivity {

    private ImageView profile_pic;
    private android.support.v7.widget.Toolbar toolbar;
    private TextView mStatus;
    private TextView mPhone;
    String chatId,chatName = "";
    String userId = "";
    String status = "Something";
    String phone = "Something";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        profile_pic = (ImageView) findViewById(R.id.profile_image_show);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.name_toolbar);
        }

        mStatus = (TextView) findViewById(R.id.status_show);
        mPhone = (TextView) findViewById(R.id.phoneNumber_show);

        chatId = getIntent().getExtras().getString("chatId");
        getUserId();
        chatName = getIntent().getExtras().getString("chatName");
        String profile_Uri = getIntent().getExtras().getString("profileImageUrl");
        if(profile_Uri != null)
        {
            Uri uri = Uri.parse(profile_Uri);
            Glide.with(this).load(uri).into(profile_pic);
        }
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setTitle(chatName);

    }

    private void getUserId() {
        Log.d("LL1", chatId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("chat")
                .child(chatId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                if(map.get("receiver") != null)
                {
                  userId = map.get("receiver").toString();
                }
                Log.d("LL2", "us" + userId);

                getStatus();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getStatus()
    {
        Log.d("ML1", "yes");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user")
                .child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                if(map.get("status") != null)
                {
                    status = map.get("status").toString();
                }

                if(map.get("phone") != null)
                {
                    phone = map.get("phone").toString();
                }
                Log.d("ML2", "yes" + phone);
                mStatus.setText(status);
                mPhone.setText(phone);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
