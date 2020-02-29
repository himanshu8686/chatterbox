package com.yash.chatterbox.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yash.chatterbox.R;
import com.yash.chatterbox.adapters.MessageAdapter;
import com.yash.chatterbox.model.Chat;
import com.yash.chatterbox.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener
{
    private Toolbar toolbar_layout;
    private CircleImageView profile_image;
    private TextView tv_userName;
    private EditText text_send;
    private ImageButton btn_send;
    private String userId;
    Intent intent;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private MessageAdapter messageAdapter;
    private List<Chat> chatList;
    private RecyclerView message_recycler_view;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        toolbar_layout=findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar_layout);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // enable back arrow on toolbar
        toolbar_layout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        message_recycler_view=findViewById(R.id.message_recycler_view);
        message_recycler_view.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        message_recycler_view.setLayoutManager(linearLayoutManager);



        profile_image= findViewById(R.id.profile_image);
        tv_userName=findViewById(R.id.tv_userName);
        text_send=findViewById(R.id.text_send);

        intent=getIntent();
        userId=intent.getStringExtra("userId");

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userId);

        btn_send=findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        showUserDetailOnToolbar();
    }

    private void showUserDetailOnToolbar() {


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                tv_userName.setText(user.getUserName());
                if (user.getImageUrl().equals("default"))
                {
                    profile_image.setImageResource(R.drawable.userphoto);
                }
                else {
                    Glide.with(MessageActivity.this).load(user.getImageUrl()).placeholder(R.drawable.userphoto).into(profile_image);
                }

                readMessage(firebaseUser.getUid(),userId,user.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender,String receiver,String message)
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);

        reference.child("Chats").push().setValue(hashMap);
    }

    private void readMessage(final String myId, final String userId, final String imageUrl){
        System.out.println("myId :"+myId+"---"+"userId :"+userId);
        chatList=new ArrayList<>();
        databaseReference=FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Chat chat=snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId)
                    || chat.getReceiver().equals(userId) && chat.getSender().equals(myId))
                    {
                        chatList.add(chat);
                    }
                    messageAdapter=new MessageAdapter(MessageActivity.this,chatList,imageUrl);
                    message_recycler_view.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onClick(View v) {
        if (v==btn_send)
        {
            String message=text_send.getText().toString();
            if (!message.equalsIgnoreCase(""))
            {
                sendMessage(firebaseUser.getUid(),userId,message);
            }else {
                Toast.makeText(this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            text_send.setText("");
        }
    }
}
