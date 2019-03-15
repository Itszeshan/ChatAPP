package com.quickblox.sample.chatapp.Activites;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quickblox.sample.chatapp.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText mPhone;
    private Button mSendBtn;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallBacks;

    String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_main);

        FirebaseApp.initializeApp(this);

        mPhone = findViewById(R.id.phoneNumber_edit);
        mSendBtn = findViewById(R.id.sendCode_btn);

        userLoggedIn();

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mPhone.getText().equals(""))
                {
                    Intent intent = new Intent(LoginActivity.this, VerificationActivity.class);
                    intent.putExtra("phone",mPhone.getText().toString().trim());
                    startActivity(intent);
                    finish();
                }
                else
                {
                    mPhone.setError("Enter Phone Number");
                }
            }
        });

    }


        private void userLoggedIn () {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                startActivity(intent);
                finish();
            }
        }


    }

