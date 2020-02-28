package com.yash.chatterbox.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yash.chatterbox.R;
import com.yash.chatterbox.adapters.UserAdapter;
import com.yash.chatterbox.model.User;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment
{
    private RecyclerView user_recycler_view;
    private List<User> userList;
    private UserAdapter userAdapter;

    public UsersFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_users, container, false);
        user_recycler_view =view.findViewById(R.id.user_recycler_view);
        user_recycler_view.setHasFixedSize(true);
        user_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));

        userList=new ArrayList<>();
        readUsers();
        return view;
    }

    private void readUsers()
    {
        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //userList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    User user=snapshot.getValue(User.class);
                    assert firebaseUser != null;
                    String id=firebaseUser.getUid();
                    Log.e("user id ",id);
                    assert user != null;
                    if (!user.getId().equals(id))
                    {
                        userList.add(user);
                    }
                    else if (userList.size()==0){
                        Toast.makeText(getContext(), "No users", Toast.LENGTH_SHORT).show();
                    }
                }
                // set in adapter
                Log.e("setting adapter","done");
                System.out.println(userList);
                userAdapter=new UserAdapter(getContext(),userList);
                user_recycler_view.setAdapter(userAdapter);
                Log.e("adapter ","setted");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
