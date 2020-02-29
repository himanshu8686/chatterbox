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
import com.yash.chatterbox.R;
import com.yash.chatterbox.adapters.UserAdapter;
import com.yash.chatterbox.model.Chat;
import com.yash.chatterbox.model.User;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView chats_recycler_view;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<String> usersList;

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

        databaseReference=FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Chat chat=snapshot.getValue(Chat.class);
                    if (chat.getSender().equals(firebaseUser.getUid()))
                    {
                        usersList.add(chat.getReceiver());
                    }
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private void readChats()
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

                    // Display 1 user from chats
                    for (String id:usersList)
                    {
                        if (user.getId().equals(id))
                        {
                            if (mUsers.size()!=0)
                            {
                                for (User user1:mUsers)
                                {
                                    if (!user.getId().equals(user1.getId()))
                                    {
                                        mUsers.add(user);
                                    }
                                }
                            }
                            else {
                                mUsers.add(user);
                            }
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(),mUsers,true);
                chats_recycler_view.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
