package com.yash.chatterbox.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yash.chatterbox.activities.MessageActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyFirebaseMessaging extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String sented=remoteMessage.getData().get("sented");
        String user=remoteMessage.getData().get("user");

        SharedPreferences preferences=getSharedPreferences("PREFS",MODE_PRIVATE);
        String currentUser=preferences.getString("currentuser","none");

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser !=null && sented.equals(firebaseUser.getUid()))
        {
            if (!currentUser.equals(user))
            {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                {
                    sendOreoNotification(remoteMessage);
                }
                else {

                    sendNotification(remoteMessage);
                }
            }
        }
    }

    /**
     *
     * @param remoteMessage
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoNotification(RemoteMessage remoteMessage)
    {

        String user=remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");

        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int requestCode=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, MessageActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("userId",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,requestCode,intent,PendingIntent.FLAG_ONE_SHOT);

        try {
            Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone=RingtoneManager.getRingtone(getApplicationContext(), defaultSound);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        OreoNotification oreoNotification=new OreoNotification(this);
        Notification.Builder builder=oreoNotification.getOreoNotification(title,body,pendingIntent,icon);
        int i=0;
        if (requestCode>0)
        {
            i=requestCode;
        }
        oreoNotification.getManager().notify(i,builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage)
    {
        String user=remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");

        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int requestCode=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, MessageActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("userId",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,requestCode,intent,PendingIntent.FLAG_ONE_SHOT);

        try {
            Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone=RingtoneManager.getRingtone(getApplicationContext(), defaultSound);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Notification.Builder builder=new Notification.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setLights(Color.BLUE, 500, 500)
                .setVibrate(new long[] {0,500})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i=0;
        if (requestCode>0)
        {
            i=requestCode;
        }
        notificationManager.notify(i,builder.build());
    }
}
