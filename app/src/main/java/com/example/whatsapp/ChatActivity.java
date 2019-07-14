package com.example.whatsapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static final String PRIVATE_MESSAGE = "Private Message";
    public static final String MESSAGES = "Messages";
    public static final String MESSAGE_TYPE = "Messages Type";
    public static final String TEXT = "Text";
    public static final String From = "From";
    public static final String TO = "To";

    private final List<Messages> messagesList = new ArrayList<>();
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar privateChatToolbar;
    private ImageButton sendPrivateMessageButton;
    private EditText userPrivateMessageInput;
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        RootRef = mDatabase.getReference();


        messageReceiverID = getIntent().getExtras().get(ChatsFragment.USER_ID).toString();
        messageReceiverName = getIntent().getExtras().get(ChatsFragment.USER_NAME).toString();
        messageReceiverImage = getIntent().getExtras().get(ChatsFragment.USER_IMAGE).toString();


        Toast.makeText(ChatActivity.this, messageReceiverName, Toast.LENGTH_SHORT).show();


        InitializeControllers();


        sendPrivateMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendMessage();

            }
        });


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

    }


    private void InitializeControllers() {

        privateChatToolbar = findViewById(R.id.private_chat_bar_layout);
        setSupportActionBar(privateChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userImage = findViewById(R.id.custom_profile_image);

        sendPrivateMessageButton = findViewById(R.id.send_private_message_button);
        userPrivateMessageInput = findViewById(R.id.input_private_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


    }


    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child(MESSAGES).child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();
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

    private void SendMessage() {

        String messageText = userPrivateMessageInput.getText().toString();

        if (TextUtils.isEmpty(messageText)) {

            Toast.makeText(ChatActivity.this, "write your message here... ", Toast.LENGTH_SHORT).show();
        } else {

            String messsageSenderRef = MESSAGES + "/" + messageSenderID + "/" + messageReceiverID;
            String messsageRecevierRef = MESSAGES + "/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child(MESSAGES)
                    .child(messageSenderID).child(messageReceiverID)
                    .push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();

            messageTextBody.put(PRIVATE_MESSAGE, messageText);
            messageTextBody.put(MESSAGE_TYPE, TEXT);
            messageTextBody.put(From, messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messsageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messsageRecevierRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {

                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();

                    }
                    userPrivateMessageInput.setText("");
                }
            });


        }

    }


}
