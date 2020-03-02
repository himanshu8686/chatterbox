package com.yash.chatterbox.fragments;

import com.yash.chatterbox.Notifications.MyResponse;
import com.yash.chatterbox.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService
{
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAApPoN_O0:APA91bFN7INUA9F6SsaYWEV7XJDVUagYm9iivmHOKDV9cAmiGMCn2277fgsJCELt5u2ht78xZR8U0sxOBm5gyS5ABheVzDkBr3bh-b1XlsQYBvOM4BlPWOkkp2f2HmyXieCsajb2Jb0a"

            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
