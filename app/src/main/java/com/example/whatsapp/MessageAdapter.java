package com.example.whatsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference UserRef;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {

        String senderMessageId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        Log.e("jhgfd", messages.getFrom());
        Log.e("jhgfd", messages.getType());

        String fromUserId = messages.getFrom();
        Log.e("dsaasd", fromUserId + "=  ");
        String fromMessagesType = messages.getType();
        Log.e("dsaasd", fromMessagesType + "=  ");
        String toUserId = messages.getTo();
        Log.e("dsaasd", toUserId + "=  ");


        mDatabase = FirebaseDatabase.getInstance();
        UserRef = mDatabase.getReference().child(MainActivity.USERS).child(fromUserId);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(SettingsActivity.IMAGE)) {

                    String receiverImage = dataSnapshot.child(SettingsActivity.IMAGE).getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);

        if (fromMessagesType.equals(ChatActivity.TEXT)) {

            if (fromUserId.equals(senderMessageId)) {

                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n " + messages.getTime() + " - " + messages.getDate());
            } else {
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText((messages.getMessage() + "\n" + messages.getTime() + " - " + messages.getDate()));
            }
        } else if (fromMessagesType.equals(ChatActivity.IMAGE)) {

            if (fromUserId.equals(senderMessageId)) {

                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);

            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        } else if (fromMessagesType.equals(ChatActivity.PDF) || fromMessagesType.equals(ChatActivity.DOCS)) {

            if (fromUserId.equals(senderMessageId)) {


                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);


                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/friendlychat-a20.appspot.com/o/Images%20Files%2Ffile.png?alt=media&token=874eff52-1e63-4b98-b5c8-d60acf382c43")
                        .into(messageViewHolder.messageSenderPicture);
               /* //download the file by the user
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });*/

            } else {


                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);


                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/friendlychat-a20.appspot.com/o/Images%20Files%2Ffile.png?alt=media&token=874eff52-1e63-4b98-b5c8-d60acf382c43")
                        .into(messageViewHolder.messageReceiverPicture);
               /* //download the file by the user
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });*/

            }
        }


        if (fromUserId.equals(senderMessageId)) {

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userMessagesList.get(position).getType().equals(ChatActivity.PDF) || userMessagesList.get(position).getType().equals(ChatActivity.DOCS)) {

                        CharSequence[] options = new CharSequence[]{

                                "Delete For Me",
                                "Download and View This Document",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    DeleteSentMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);


                                } else if (which == 1) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                } else if (which == 3) {


                                    DeleteMessageForEveryOne(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals(ChatActivity.TEXT)) {

                        CharSequence[] options = new CharSequence[]{

                                "Delete For Me",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    DeleteSentMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);


                                } else if (which == 2) {

                                    DeleteMessageForEveryOne(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals(ChatActivity.IMAGE)) {

                        CharSequence[] options = new CharSequence[]{

                                "Delete For Me",
                                "View This Image",
                                "Cancel",
                                "Delete For Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    DeleteSentMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);


                                } else if (which == 1) {

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("Url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                } else if (which == 3) {

                                    DeleteMessageForEveryOne(position, messageViewHolder);

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userMessagesList.get(position).getType().equals(ChatActivity.PDF) || userMessagesList.get(position).getType().equals(ChatActivity.DOCS)) {

                        CharSequence[] options = new CharSequence[]{

                                "Delete Message",
                                "Download and View This Document",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    DeleteReceivedMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);


                                } else if (which == 1) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals(ChatActivity.TEXT)) {

                        CharSequence[] options = new CharSequence[]{

                                "Delete Message",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    DeleteReceivedMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals(ChatActivity.IMAGE)) {

                        CharSequence[] options = new CharSequence[]{

                                "Delete Message",
                                "View This Image",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message ?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {
                                    DeleteReceivedMessage(position, messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                } else if (which == 1) {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("Url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (userMessagesList == null) {
            return 0;
        }
        return userMessagesList.size();
    }

    private void DeleteSentMessage(final int position, final MessageViewHolder holder) {

        DatabaseReference RootRef = mDatabase.getReference();
        RootRef.child(ChatActivity.MESSAGES)
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DeleteReceivedMessage(final int position, final MessageViewHolder holder) {

        DatabaseReference RootRef = mDatabase.getReference();
        RootRef.child(ChatActivity.MESSAGES)
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DeleteMessageForEveryOne(final int position, final MessageViewHolder holder) {

        final DatabaseReference RootRef = mDatabase.getReference();
        RootRef.child(ChatActivity.MESSAGES)
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    RootRef.child(ChatActivity.MESSAGES)
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.receiver_message_profile_image);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image);
        }
    }
}