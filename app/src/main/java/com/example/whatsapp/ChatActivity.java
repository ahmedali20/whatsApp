package com.example.whatsapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    public static final String PRIVATE_MESSAGE = " Message";
    public static final String MESSAGES = "Messages";
    public static final String MESSAGE_TYPE = " Type";
    public static final String TEXT = "Text";
    public static final String IMAGE = "Image";
    public static final String From = "From";
    public static final String NAME = "Name";
    public static final String TO = "To";
    public static final String MESSAGEID = "Message ID";
    public static final String IMAGES = "Images Files";
    public static final String PDFFILES = "PDF Files";
    public static final String MSFILES = "MS Word Files";

    private static final int GalleryPick2 = 438;
    private ProgressDialog loadingBar;
    private Uri fileUri;
    private String cheacker = "", myUrL = "", saveCurrentTime, saveCurrentDate;
    private List<Messages> messagesList;
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar privateChatToolbar;
    private ImageButton sendPrivateMessageButton, attachFilesButton;
    private EditText userPrivateMessageInput;
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference RootRef;
    private StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messagesList = new ArrayList<>();


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        RootRef = mDatabase.getReference();


        messageReceiverID = getIntent().getExtras().get(ChatsFragment.USER_ID).toString();
        messageReceiverName = getIntent().getExtras().get(ChatsFragment.USER_NAME).toString();
        messageReceiverImage = getIntent().getExtras().get(ChatsFragment.USER_IMAGE).toString();


        Toast.makeText(ChatActivity.this, messageReceiverName, Toast.LENGTH_SHORT).show();


        InitializeControllers();


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);


        sendPrivateMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendMessage();

            }
        });


        DisplayLastSeen();


        attachFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence[] options = new CharSequence[]{

                        IMAGE,
                        PDFFILES,
                        MSFILES
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select The File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {

                        if (position == 0) {

                            cheacker = IMAGE;

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent, "select Image"), GalleryPick2);
                        }
                        if (position == 1) {
                            cheacker = "pdf";
                        }
                        if (position == 2) {
                            cheacker = "docx";
                        }

                    }
                });
                builder.show();
            }
        });
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
        attachFilesButton = findViewById(R.id.attach_private_files_button);

        userMessagesList = findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,  yyyy");
        saveCurrentDate = currentDateFormat.format(calendar.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTimeFormat.format(calendar.getTime());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick2 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("please wait, you`re file is sending...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            Log.e("kjnk,l", fileUri + " ");

            if (!cheacker.equals(IMAGE)) {


            } else if (cheacker.equals(IMAGE)) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child(IMAGES);

                final String messsageSenderRef = MESSAGES + "/" + messageSenderID + "/" + messageReceiverID;
                final String messsageRecevierRef = MESSAGES + "/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child(MESSAGES)
                        .child(messageSenderID).child(messageReceiverID)
                        .push();
                Log.e("kjnk,2", userMessageKeyRef + " ");


                final String messagePushID = userMessageKeyRef.getKey();
                Log.e("kjnk,3", messagePushID + " ");


                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                Log.e("kjnk,4", filePath + " ");


                uploadTask = filePath.putFile(fileUri);
                Log.e("kjnk,5", uploadTask + " ");


                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {

                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Log.e("kjnk,6", filePath + " ");


                        if (task.isSuccessful()) {

                            Uri downloadUrl = task.getResult();

                            myUrL = downloadUrl.toString();
                            Log.e("kjnk,8", myUrL + " ");

                            Map messageImageBody = new HashMap();

                            messageImageBody.put(PRIVATE_MESSAGE, myUrL);
                            messageImageBody.put(NAME, fileUri.getLastPathSegment());
                            messageImageBody.put(MESSAGE_TYPE, cheacker);
                            messageImageBody.put(From, messageSenderID);
                            messageImageBody.put(TO, messageReceiverID);
                            messageImageBody.put(MESSAGEID, messagePushID);
                            messageImageBody.put(GroupChatActivity.TIME, saveCurrentTime);
                            messageImageBody.put(GroupChatActivity.DATE, saveCurrentDate);


                            Map messageBodyDetails = new HashMap();

                            messageBodyDetails.put(messsageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messsageRecevierRef + "/" + messagePushID, messageImageBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();

                                        Toast.makeText(ChatActivity.this, "File Sent Successfully...", Toast.LENGTH_SHORT).show();

                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();

                                    }

                                    userPrivateMessageInput.setText("");

                                }
                            });
                        }
                    }
                });

            } else {

                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen() {

        RootRef.child(MainActivity.USERS).child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.child(MainActivity.USER_STATE).hasChild(MainActivity.STATE)) {

                            String state = dataSnapshot.child(MainActivity.USER_STATE).child(MainActivity.STATE).getValue().toString();
                            String date = dataSnapshot.child(MainActivity.USER_STATE).child(GroupChatActivity.DATE).getValue().toString();
                            String time = dataSnapshot.child(MainActivity.USER_STATE).child(GroupChatActivity.TIME).getValue().toString();

                            if (state.equals(MainActivity.ONLINE)) {

                                userLastSeen.setText(MainActivity.ONLINE);

                            } else if (state.equals(MainActivity.OFFLINE)) {

                                userLastSeen.setText(ChatsFragment.LAST_SEEN + date + " " + time);

                            }

                        } else {


                            userLastSeen.setText(MainActivity.OFFLINE);

                        }


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
            messageTextBody.put(TO, messageReceiverID);
            messageTextBody.put(MESSAGEID, messagePushID);
            messageTextBody.put(GroupChatActivity.TIME, saveCurrentTime);
            messageTextBody.put(GroupChatActivity.DATE, saveCurrentDate);
            messageTextBody.put(NAME, "");


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
                    userPrivateMessageInput.setText(" ");
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child(MESSAGES).child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Log.e("11111111111111", dataSnapshot + ": ");
                        Messages messages = new Messages(dataSnapshot.child(From).getValue().toString()
                                , dataSnapshot.child(PRIVATE_MESSAGE).getValue().toString()
                                , dataSnapshot.child(MESSAGE_TYPE).getValue().toString(),
                                dataSnapshot.child(TO).getValue().toString(),
                                dataSnapshot.child(MESSAGEID).getValue().toString(),
                                dataSnapshot.child(GroupChatActivity.TIME).getValue().toString(),
                                dataSnapshot.child(GroupChatActivity.DATE).getValue().toString(),
                                dataSnapshot.child(NAME).getValue().toString());


                        messagesList.add(messages);
                        Log.e("22222222222", dataSnapshot + ": ");


                        messageAdapter.notifyDataSetChanged();
                        Log.e("33333333333333333", dataSnapshot + ": ");

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

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
}