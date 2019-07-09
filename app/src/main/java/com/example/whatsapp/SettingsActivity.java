package com.example.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    public static final String STATUS = "Status";
    public static final String IMAGE = "Image";
    public static final String UID = "UserID";
    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private FirebaseDatabase mDatabase;
    private DatabaseReference RootRif;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mDatabase = FirebaseDatabase.getInstance();
        RootRif = mDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        InitializeFields();


        userName.setVisibility(View.INVISIBLE);


        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        RetrieveUserInfo();
    }


    private void InitializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
    }


    private void updateSettings() {


        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();


        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(SettingsActivity.this, "please Write Your User name First ....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(SettingsActivity.this, "please Write Your Status..", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put(UID, currentUserID);
            profileMap.put(MainActivity.NAME, setUserName);
            profileMap.put(STATUS, setUserStatus);
            RootRif.child(MainActivity.USERS).child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "profile updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }


    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // msh fahmha
        startActivity(mainIntent);
        finish();
    }

    private void RetrieveUserInfo() {

        RootRif.child(MainActivity.USERS).child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(MainActivity.NAME) && (dataSnapshot.hasChild(IMAGE)))) {
                            String retrieveUserName = dataSnapshot.child(MainActivity.NAME).getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child(STATUS).getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child(IMAGE).getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(MainActivity.NAME))) {
                            String retrieveUserName = dataSnapshot.child(MainActivity.NAME).getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child(STATUS).getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                        } else {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "please set & update your profile info", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}