package com.yash.chatterbox.fragments;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.yash.chatterbox.R;
import com.yash.chatterbox.model.User;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private CircleImageView profile_image;
    private TextView tv_userName;
    private ProgressBar progress_circular;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private static final int IMAGE_REQUEST=1;
    private Uri imageUri;
    private StorageTask uploadTask;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        profile_image=view.findViewById(R.id.profile_image);
        tv_userName=view.findViewById(R.id.tv_userName);
        progress_circular=view.findViewById(R.id.progress_circular);
        storageReference= FirebaseStorage.getInstance().getReference("uploads");

        putUserDetailsIntoProfileFragment();

        profile_image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        return view;
    }

    private void putUserDetailsIntoProfileFragment() {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user= dataSnapshot.getValue(User.class);
                tv_userName.setText(user.getUserName());
                if (user.getImageUrl().equals("default"))
                {
                    profile_image.setImageResource(R.drawable.userphoto);
                }
                else {
                    Glide.with(getContext())
                            .load(user.getImageUrl())
                            .placeholder(R.drawable.userphoto).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openImage()
    {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage()
    {
        //progress bar will be showing
        profile_image.setVisibility(View.GONE);
        progress_circular.setVisibility(View.VISIBLE);

        if (imageUri!=null)
        {
            final StorageReference fileReference =storageReference.child(firebaseUser.getUid()
            +"."+getFileExtension(imageUri));

            uploadTask=fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri= (Uri) task.getResult();
                        String mUri=downloadUri.toString();

                        databaseReference=FirebaseDatabase.getInstance().getReference("Users")
                                .child(firebaseUser.getUid());
                        HashMap<String, Object> map=new HashMap<>();
                        map.put("imageUrl",mUri);
                        databaseReference.updateChildren(map);

                        //prograss bar dismiss
                        profile_image.setVisibility(View.VISIBLE);
                        progress_circular.setVisibility(View.GONE);
                    }
                    else {
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        //prograss bar dismiss
                        profile_image.setVisibility(View.VISIBLE);
                        progress_circular.setVisibility(View.GONE);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    //prograss bar dismiss
                    profile_image.setVisibility(View.VISIBLE);
                    progress_circular.setVisibility(View.GONE);
                }
            });
        }else {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {
            imageUri=data.getData();
            if (uploadTask!=null && uploadTask.isInProgress())
            {
                Toast.makeText(getContext(), "Upload in Progress", Toast.LENGTH_SHORT).show();
            }
            else {
                uploadImage();
            }
        }
    }
}
