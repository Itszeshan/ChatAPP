package com.quickblox.sample.chatapp.Activites;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class VerificationActivity extends AppCompatActivity {

    private EditText mCode;
    private Button mVerifyBtn;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallBacks;
    String phoneNumber = "";
    String mVerificationId;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mCode = (EditText) findViewById(R.id.code_et);
        code = mCode.getText().toString();

        mVerifyBtn = (Button) findViewById(R.id.verifyCode_btn);

        phoneNumber = getIntent().getExtras().getString("phone");



        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!mCode.getText().toString().isEmpty())
                {
                    mVerifyBtn.setEnabled(false);
                    verifyPhoneNumberWithCode();
                }
                else
                {
                    mCode.setError("Please enter code");
                }
            }
        });

        mcallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCred(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationId = s;
                Toast.makeText(VerificationActivity.this, "Code Sent!", Toast.LENGTH_SHORT).show();
            }
        };

        startPhoneNumberVerification();
    }


    private void verifyPhoneNumberWithCode()
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mCode.getText().toString().trim());
        signInWithPhoneAuthCred(credential);
    }



    private void signInWithPhoneAuthCred(PhoneAuthCredential phoneAuthCredential) {

        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists())
                            {
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("phone", user.getPhoneNumber());
                                userMap.put("name", user.getDisplayName());

                                userRef.updateChildren(userMap);
                            }

                            Intent intent = new Intent(VerificationActivity.this, ChatActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
                else
                {
                    Toast.makeText(VerificationActivity.this, "Invalid Code!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void startPhoneNumberVerification() {


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mcallBacks );       // OnVerificationStateChangedCallbacks
    }
}
