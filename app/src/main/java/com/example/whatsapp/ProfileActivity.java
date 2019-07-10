package com.example.whatsapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    public static final String CHAT_REQUESTS = "Chat Requests";
    public static final String REQUEST_TYPE = "Request Type";
    public static final String REQUEST_SENT = "Request_Sent";
    public static final String SENT = "sent";
    public static final String RECEIVED = "Received";


    private String receiverUserID, currentState, senderUserID;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton;

    private FirebaseDatabase mDatabase;
    private DatabaseReference UserRef, ChatRequsetRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mDatabase = FirebaseDatabase.getInstance();
        UserRef = mDatabase.getReference().child(MainActivity.USERS);
        ChatRequsetRef = mDatabase.getReference().child(CHAT_REQUESTS);
        mAuth = FirebaseAuth.getInstance();


        receiverUserID = getIntent().getExtras().get("Visit_user_Id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();


        InitializeFields();


        RetriveUserInfo();
    }


    private void InitializeFields() {

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_profile_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        currentState = "new";
    }

    private void RetriveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(SettingsActivity.IMAGE))) {

                    String userImage = dataSnapshot.child(SettingsActivity.IMAGE).getValue().toString();
                    String userName = dataSnapshot.child(MainActivity.NAME).getValue().toString();
                    String userStatus = dataSnapshot.child(SettingsActivity.STATUS).getValue().toString();


                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    ManageChatRequest();


                } else {

                    String userName = dataSnapshot.child(MainActivity.NAME).getValue().toString();
                    String userStatus = dataSnapshot.child(SettingsActivity.STATUS).getValue().toString();


                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    ManageChatRequest();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {


        ChatRequsetRef.child(SENT).child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiverUserID)) {

                            String request_type = dataSnapshot.child(receiverUserID).child(REQUEST_TYPE).getValue().toString();

                            if (request_type.equals(SENT)) {

                                currentState = REQUEST_SENT;
                                sendMessageRequestButton.setText("Cancel Chat Request");

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserID.equals(receiverUserID)) {

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendMessageRequestButton.setEnabled(false);

                    if (currentState.equals("new")) {

                        sendChatRequest();

                    }

                    if (currentState.equals(REQUEST_SENT)) {
                        cancelChatRequest();

                    }
                }
            });

        } else {

            sendMessageRequestButton.setVisibility(View.INVISIBLE);

        }


    }


    private void sendChatRequest() {

        ChatRequsetRef.child(SENT).child(senderUserID).child(receiverUserID).child(REQUEST_TYPE)
                .setValue(SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    ChatRequsetRef.child(RECEIVED).child(receiverUserID).child(senderUserID).child(REQUEST_TYPE)
                            .setValue(RECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            sendMessageRequestButton.setEnabled(true);
                            currentState = REQUEST_SENT;
                            sendMessageRequestButton.setText("Cancel Chat Request");

                        }
                    });

                }
            }
        });

    }


    private void cancelChatRequest() {

        ChatRequsetRef.child(SENT).child(senderUserID).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    ChatRequsetRef.child(RECEIVED).child(receiverUserID).child(senderUserID)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {


                            if (task.isSuccessful()) {
                                sendMessageRequestButton.setEnabled(true);
                                currentState = "new";
                                sendMessageRequestButton.setText("Send Message");
                            }

                        }
                    });
                }


            }
        });
    }
}