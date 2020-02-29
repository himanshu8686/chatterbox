package com.yash.chatterbox.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yash.chatterbox.R;
import com.yash.chatterbox.model.User;

import java.util.concurrent.TimeUnit;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_userName,et_phoneNumber;
    private Button next_btn;
    private LinearLayout firstLayout,secondOTPLayout;
    private TextView tv_topLine,tv_otp_msg,tv_resend_otp;
    private ProgressBar progress_circular;
    private PinView pinView;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String verificationID,codeFromServer,phoneNumber,userName;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initializeViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth=firebaseAuth.getInstance();
    }

    private void initializeViews()
    {
        tv_topLine= findViewById(R.id.tv_topLine);
        et_userName= findViewById(R.id.et_userName);
        et_phoneNumber= findViewById(R.id.et_phoneNumber);
        pinView=findViewById(R.id.pinView);
        tv_otp_msg= findViewById(R.id.tv_otp_msg);
        tv_resend_otp=findViewById(R.id.tv_resend_otp);
        tv_resend_otp.setOnClickListener(this);
        progress_circular=findViewById(R.id.progress_circular);

        next_btn= findViewById(R.id.next_btn);
        next_btn.setOnClickListener(this);

        firstLayout= findViewById(R.id.firstLayout);
        secondOTPLayout=findViewById(R.id.secondOTPLayout);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Intent intent =new Intent(this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
    @Override
    public void onClick(View v)
    {
        if (next_btn.getText().equals("Let's go!"))
        {
            firstFormProcessing();
        }
        else if (next_btn.getText().equals("Verify"))
        {
            try {
                secondOTPFormProcessing();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                progress_circular.setVisibility(View.GONE);
                next_btn.setVisibility(View.VISIBLE);
            }
        }
        else if (v==tv_resend_otp)
        {
            Log.e("resend otp","clicked");
            sendVerificationCode(phoneNumber);
        }
    }

    /**
     *  First form processing
     */
    private void firstFormProcessing()
    {
       userName=et_userName.getText().toString();
        phoneNumber="+91"+et_phoneNumber.getText().toString();

        if (TextUtils.isEmpty(userName))
        {
            et_userName.setError("Name can't be empty");
            et_userName.requestFocus();
            return;
        }
        if (phoneNumber.length()!=13)
        {
            et_phoneNumber.setError("Please enter Mobile no of 10 digits");
            et_phoneNumber.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(userName) && phoneNumber.length()==13)
        {

            next_btn.setText("Verify");
            firstLayout.setVisibility(View.GONE);
            secondOTPLayout.setVisibility(View.VISIBLE);
            tv_topLine.setText("I Still don't trust you.\nTell me something that only two of us know.");
            sendVerificationCode(phoneNumber);
        }
    }


    /**
     * Second form processing
     */
    private void secondOTPFormProcessing()
    {
        String OTP=pinView.getText().toString();

        if (codeFromServer==null)
        {
            Toast.makeText(this, "No auto verification enter manually", Toast.LENGTH_SHORT).show();
            PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationID,OTP);
            signInWithPhoneCredentials(credential,userName);

            pinView.setLineColor(Color.GREEN);
            tv_otp_msg.setText("OTP Verified");
            tv_otp_msg.setTextColor(Color.GREEN);
            next_btn.setText("Next");
        }
        else
        {
            pinView.setLineColor(Color.RED);
            tv_otp_msg.setText("OTP Verification Failed!!");
            tv_otp_msg.setTextColor(Color.RED);
        }
    }


    private void sendVerificationCode(String phoneNumber)
    {
        progress_circular.setVisibility(View.VISIBLE);
        next_btn.setVisibility(View.GONE);

        // This callback method is responsible for sending OTP to the phone no
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,60, TimeUnit.SECONDS,TaskExecutors.MAIN_THREAD, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
                    {
                        Log.e("method1 called ","onVerificationCompleted");
                        codeFromServer=  phoneAuthCredential.getSmsCode();
                        System.out.println("code from server " +codeFromServer);

                        // auto detecting code
                        if (codeFromServer!=null)
                        {
                            pinView.setText(codeFromServer);
                            pinView.setLineColor(Color.GREEN);
                            tv_otp_msg.setText("OTP Verified");
                            tv_otp_msg.setTextColor(Color.GREEN);
                            next_btn.setText("Next");

                            PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationID,codeFromServer);
                            signInWithPhoneCredentials(credential, userName);
                        }
                        else {
                            Toast.makeText(StartActivity.this, "No otp received ", Toast.LENGTH_SHORT).show();
                            progress_circular.setVisibility(View.GONE);
                            next_btn.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e)
                    {
                        Log.e("method2 called ","onVerificationFailed");
                        Toast.makeText(StartActivity.this, "verification failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        System.out.println("ERRR :"+e.getMessage());
                        progress_circular.setVisibility(View.GONE);
                        next_btn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        Log.e("method called ","onCodeAutoRetrievalTimeOut");
                        progress_circular.setVisibility(View.GONE);
                        next_btn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken)
                    {
                        Log.e("method3 called ","onCodeSent");
                        super.onCodeSent(s, forceResendingToken);
                        verificationID=s;
                        progress_circular.setVisibility(View.GONE);
                        next_btn.setVisibility(View.VISIBLE);
                    }
                });
    }


    private void signInWithPhoneCredentials(PhoneAuthCredential credential, final String userName)
    {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                Log.e("method X","onComplete called of fb");
                if (task.isSuccessful())
                {
                    FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
                    String userId=firebaseUser.getUid();
                    //we will store additional fields in firebase database
                    User user=new User(userId,userName,"default");
                    databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userId);
                    databaseReference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(StartActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(StartActivity.this,MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
                else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException)
                    {
                        Toast.makeText(StartActivity.this, "User already registered!!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(StartActivity.this, "Exception while sign in"+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
