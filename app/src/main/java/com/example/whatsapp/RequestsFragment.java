package com.example.whatsapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class RequestsFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestsList;
    private String currentUserID;


    private FirebaseDatabase mDatabase;
    private DatabaseReference ChatRequestRef, UsersRef;
    private FirebaseAuth mAuth;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);


        InitializeFields();


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        ChatRequestRef = mDatabase.getReference().child(ProfileActivity.CHAT_REQUESTS);
        UsersRef = mDatabase.getReference().child(MainActivity.USERS);


        return RequestsFragmentView;
    }

    private void InitializeFields() {

        myRequestsList = RequestsFragmentView.findViewById(R.id.requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    @Override
    public void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestRef.child(currentUserID), Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {

                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference gatTypeRef = getRef(position).child(ProfileActivity.REQUEST_TYPE).getRef();

                        gatTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Log.e("11111111111111", dataSnapshot + ": ");

                                if (dataSnapshot.exists()) {
                                    Log.e("22222222222", dataSnapshot + ": ");

                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals(ProfileActivity.RECEIVED)) {
                                        Log.e("33333333333", dataSnapshot + ": ");

                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {

                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                Log.e("44444444444", dataSnapshot + ": ");

                                                if (dataSnapshot.hasChild(SettingsActivity.IMAGE)) {
                                                    Log.e("5555555555555", dataSnapshot + ": ");

                                                    final String requestUserProfileImage = dataSnapshot.child(SettingsActivity.IMAGE).getValue().toString();
                                                    final String requestProfileName = dataSnapshot.child(MainActivity.NAME).getValue().toString();
                                                    final String requestProfileStatus = dataSnapshot.child(SettingsActivity.STATUS).getValue().toString();

                                                    holder.userName.setText(requestProfileName);
                                                    holder.userStatus.setText(requestProfileStatus);
                                                    Picasso.get().load(requestUserProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                } else {
                                                    Log.e("6666666666666", dataSnapshot + ": ");

                                                    final String requestProfileName = dataSnapshot.child(MainActivity.NAME).getValue().toString();
                                                    final String requestProfileStatus = dataSnapshot.child(SettingsActivity.STATUS).getValue().toString();

                                                    holder.userName.setText(requestProfileName);
                                                    holder.userStatus.setText(requestProfileStatus);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                        return viewHolder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptRequest, CancelRequest;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptRequest = itemView.findViewById(R.id.request_accept_button);
            CancelRequest = itemView.findViewById(R.id.request_cancel_button);
        }
    }
}