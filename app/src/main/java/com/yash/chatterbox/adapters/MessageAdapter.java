package com.yash.chatterbox.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yash.chatterbox.R;
import com.yash.chatterbox.activities.MessageActivity;
import com.yash.chatterbox.model.Chat;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>
{
    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;

    private Context mContext;
    private List<Chat> chatList;
    private String imageUrl;
    private FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> chatList,String imageUrl)
    {
        this.mContext = mContext;
        this.chatList = chatList;
        this.imageUrl=imageUrl;
    }

    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType==MSG_TYPE_RIGHT)
        {
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.MyViewHolder(view);
        }
        else {
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MyViewHolder holder, int position)
    {
       Chat chat=chatList.get(position);
       holder.show_message.setText(chat.getMessage());
        if (imageUrl.equals("default"))
        {
            holder.profile_image.setImageResource(R.drawable.userphoto);
        }
        else {
            Glide.with(mContext).load(imageUrl).into(holder.profile_image);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(firebaseUser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else return MSG_TYPE_LEFT;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {

        public TextView show_message;
        public CircleImageView profile_image;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            show_message=itemView.findViewById(R.id.show_message);
            profile_image=itemView.findViewById(R.id.profile_image);
        }
    }
}

