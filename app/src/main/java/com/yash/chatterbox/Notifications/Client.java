package com.yash.chatterbox.Notifications;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client
{
    private static Retrofit retrofit=null;

    public static Retrofit getClient(String url)
    {
        if (retrofit==null)
        {
            retrofit.newBuilder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
