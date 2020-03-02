package com.yash.chatterbox.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yash.chatterbox.Notifications.Token;
import com.yash.chatterbox.R;
import com.yash.chatterbox.adapters.UserAdapter;
import com.yash.chatterbox.model.ChatList;
import com.yash.chatterbox.model.User;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView chats_recycler_view;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<ChatList> usersList;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_chats, container, false);

        chats_recycler_view=view.findViewById(R.id.chats_recycler_view);
        chats_recycler_view.setHasFixedSize(true);
        chats_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        usersList=new ArrayList<>();
        databaseReference=FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    ChatList chatList=snapshot.getValue(ChatList.class);
                    usersList.add(chatList);
                }

                displayChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken());
        return view;
    }

    private void displayChatList()
    {
        mUsers=new ArrayList<>();
        databaseReference=FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    User user=snapshot.getValue(User.class);
                    for (ChatList chatList:usersList)
                    {
                        if (user.getId().equals(chatList.getId()))
                        {
                            mUsers.add(user);
                        }
                    }
                }
                userAdapter =new UserAdapter(getContext(),mUsers,true);
                chats_recycler_view.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateToken(String token)
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1=new Token(token);
        databaseReference.child(firebaseUser.getUid()).setValue(token1);

    }
}
