package com.yash.chatterbox.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yash.chatterbox.R;
import com.yash.chatterbox.adapters.ViewPagerAdapter;
import com.yash.chatterbox.fragments.ChatsFragment;
import com.yash.chatterbox.fragments.ProfileFragment;
import com.yash.chatterbox.fragments.UsersFragment;
import com.yash.chatterbox.model.Chat;
import com.yash.chatterbox.model.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView profile_image;
    private TextView tv_userName;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private String userId;

    private Toolbar toolbar_layout;
    private TabLayout tab_layout;
    private ViewPager view_pager;
    private  ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViewsForMainActivity();

        setSupportActionBar(toolbar_layout);
        getSupportActionBar().setTitle("");
        showUserOnToolbar();

        databaseReference= FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
                int unRead=0;
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Chat chat=snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsSeen())
                    {
                        unRead++;
                    }
                }
                if (unRead==0)
                {
                    viewPagerAdapter.addFragment(new ChatsFragment(),"Chats");
                }
                else {

                    viewPagerAdapter.addFragment(new ChatsFragment(),"(" + unRead + ")Chats");
                }
                viewPagerAdapter.addFragment(new UsersFragment(),"Users");
                viewPagerAdapter.addFragment(new ProfileFragment(),"Profile");
                view_pager.setAdapter(viewPagerAdapter);
                tab_layout.setupWithViewPager(view_pager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       // setUpViewPager();
    }

//    /**
//     * This setUpViewPager() method is user defined method for placing view pager adapter in view
//     */
//    private void setUpViewPager() {
//        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        viewPagerAdapter.addFragment(new ChatsFragment(),"Chats");
//        viewPagerAdapter.addFragment(new UsersFragment(),"Users");
//        viewPagerAdapter.addFragment(new ProfileFragment(),"Profile");
//        view_pager.setAdapter(viewPagerAdapter);
//        tab_layout.setupWithViewPager(view_pager);
//    }

    /**
     * This showUserOnToolbar() method is user defined method and is used for customizing on the toolbar
     */
    private void showUserOnToolbar() {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        userId= firebaseUser.getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                User user=dataSnapshot.getValue(User.class);
                tv_userName.setText(user.getUserName());
                if (user.getImageUrl().equals("default"))
                {
                    profile_image.setImageResource(R.drawable.userphoto);
                }
                else {

                    Glide.with(getApplicationContext()).load(user.getImageUrl()).placeholder(R.drawable.userphoto).into(profile_image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * All the views are going to be initialized in this method
     */
    private void initializeViewsForMainActivity()
    {
        profile_image= findViewById(R.id.profile_image);
        tv_userName=findViewById(R.id.tv_userName);

        toolbar_layout=findViewById(R.id.toolbar_layout);
        tab_layout=findViewById(R.id.tab_layout);
        view_pager  = findViewById(R.id.view_pager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout_menu:
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(MainActivity.this,StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v)
    {

    }

    private void status(String status)
    {
        databaseReference=FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
