package com.yash.chatterbox.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yash.chatterbox.R;
import com.yash.chatterbox.activities.MessageActivity;
import com.yash.chatterbox.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder>
{
    private Context mContext;
    private List<User> userList;
    private boolean isChat;

    public UserAdapter(Context mContext, List<User> userList,boolean isChat) {
        this.mContext = mContext;
        this.userList = userList;
        this.isChat=isChat;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        Log.e("onCreateViewHolder","called");
        LayoutInflater layoutInflater= LayoutInflater.from(mContext);
        View view= layoutInflater.inflate(R.layout.user_item,parent,false);
        return new UserAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        Log.e("onBindViewHolder","called");
        final User user= userList.get(position);
        Log.e("User",""+user.getId()+user.getUserName());
        holder.tv_userName.setText(user.getUserName());
        if (user.getImageUrl().equals("default"))
        {
            holder.profile_image.setImageResource(R.drawable.userphoto);
        }
        else {
            Glide.with(mContext).load(user.getImageUrl()).into(holder.profile_image);
        }

        if (isChat)
        {
            if (user.getStatus().equals("online"))
            {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }
            else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId",user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {

        public TextView tv_userName;
        public CircleImageView profile_image,img_on,img_off;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            Log.e("inside my view holder","called");
            tv_userName= itemView.findViewById(R.id.tv_userName);
            profile_image=itemView.findViewById(R.id.profile_image);
            img_on=itemView.findViewById(R.id.img_on);
            img_off=itemView.findViewById(R.id.img_off);
        }
    }
}
