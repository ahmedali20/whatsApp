package com.example.whatsapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    public static final String LAST_SEEN = "Last Seen : ";
    public static final String LAST_SEEN_DATE = "Date : ";
    public static final String LAST_SEEN_TIME = "Time : ";
    public static final String USER_ID = "Visit User ID : ";
    public static final String USER_NAME = "Visit User Name: ";
    public static final String USER_IMAGE = "Visit User Image";


    private View privateChatsView;
    private RecyclerView privateChatsList;
    private String currentUserID;


    private FirebaseDatabase mDatabase;
    private DatabaseReference ChatsRef, UserRef;
    private FirebaseAuth mAuth;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);


        InitializeFields();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        ChatsRef = mDatabase.getReference().child(ProfileActivity.CONTACTS).child(currentUserID);
        UserRef = mDatabase.getReference().child(MainActivity.USERS);


        return privateChatsView;
    }

    private void InitializeFields() {

        privateChatsList = privateChatsView.findViewById(R.id.private_chats_list);
        privateChatsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    @Override
    public void onStart() {

        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatsRef, Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {


                        final String usersIDs = getRef(position).getKey();
                        final String[] userProfileImage = {"default image"};


                        UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    if (dataSnapshot.hasChild(SettingsActivity.IMAGE)) {

                                        userProfileImage[0] = dataSnapshot.child(SettingsActivity.IMAGE).getValue().toString();


                                        Picasso.get().load(userProfileImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                    }


                                    final String profileName = dataSnapshot.child(MainActivity.NAME).getValue().toString();
                                    final String profileStatus = dataSnapshot.child(SettingsActivity.STATUS).getValue().toString();

                                    holder.userName.setText(profileName);


                                    if (dataSnapshot.child(MainActivity.USER_STATE).hasChild(MainActivity.STATE)) {

                                        String state = dataSnapshot.child(MainActivity.USER_STATE).child(MainActivity.STATE).getValue().toString();
                                        String date = dataSnapshot.child(MainActivity.USER_STATE).child(GroupChatActivity.DATE).getValue().toString();
                                        String time = dataSnapshot.child(MainActivity.USER_STATE).child(GroupChatActivity.TIME).getValue().toString();

                                        if (state.equals(MainActivity.ONLINE)) {

                                            holder.userStatus.setText(MainActivity.ONLINE);

                                        } else if (state.equals(MainActivity.OFFLINE)) {

                                            holder.userStatus.setText(LAST_SEEN + date + " " + time);

                                        }

                                    } else {


                                        holder.userStatus.setText(MainActivity.OFFLINE);

                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra(USER_ID, usersIDs);
                                            chatIntent.putExtra(USER_NAME, profileName);
                                            chatIntent.putExtra(USER_IMAGE, userProfileImage[0]);

                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                        return viewHolder;
                    }
                };

        privateChatsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
