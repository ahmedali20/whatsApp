package com.example.whatsapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {


    private static final String GROUP_MESSAGE = "Group Message";
    private static final String DATE = "Date";
    private static final String TIME = "Time";

    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        // get the group name from the group fragment & store it in currentGroupName
        currentGroupName = getIntent().getExtras().get(GroupsFragment.GROUPNAME).toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        UsersRef = mDatabase.getReference().child(MainActivity.USERS);
        GroupNameRef = mDatabase.getReference().child(MainActivity.GROUPS).child(currentGroupName);


        InitializeFields();

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageInfoToDatabase();


                userMessageInput.setText("");


                // atumotic make scroll to display the new message
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }

        });
    }


    @Override
    protected void onStart() {
        super.onStart();


        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);

                }
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


    private void InitializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);


        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        mScrollView = findViewById(R.id.myscroll_view);
        displayTextMessage = findViewById(R.id.group_chat_text_display);
    }


    private void GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child(MainActivity.NAME).getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendMessageInfoToDatabase() {


        String message = userMessageInput.getText().toString();
        //give key id to each group
        String messageKEY = GroupNameRef.push().getKey();


        if (TextUtils.isEmpty(message)) {
            Toast.makeText(GroupChatActivity.this, "write your message here... ", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,  yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());


            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());


            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);


            GroupMessageKeyRef = GroupNameRef.child(messageKEY);


            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put(MainActivity.NAME, currentUserName);
            messageInfoMap.put(GROUP_MESSAGE, message);
            messageInfoMap.put(DATE, currentDate);
            messageInfoMap.put(TIME, currentTime);
            messageInfoMap.put(SettingsActivity.UID, currentUserID);


            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }


    private void DisplayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();


        while (iterator.hasNext()) {

            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            String UID = (String) ((DataSnapshot) iterator.next()).getValue();


            displayTextMessage.append(chatName + " :\n" + chatMessage + " \n" + chatTime + "   " + chatDate + "\n\n\n");


            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);


        }
    }

}