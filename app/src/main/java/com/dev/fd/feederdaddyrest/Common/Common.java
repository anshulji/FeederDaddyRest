package com.dev.fd.feederdaddyrest.Common;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.dev.fd.feederdaddyrest.Remote.APIService;
import com.dev.fd.feederdaddyrest.Remote.FCMRetrofitClient;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Common {

    public static String currentfragment="ordermeal";
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String SETASBANNER = "Set As Banner";
    public static final String HIDEORSHOW = "Out of Stock OR Available";
    private static final String  fcmUrl = "https://fcm.googleapis.com/";

    public static String getCity(Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("MyData",Context.MODE_PRIVATE);
        return sharedPreferences.getString("city","N/A");
    }
    public static String getIsopen(Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("MyData",Context.MODE_PRIVATE);
        return sharedPreferences.getString("isopen","N/A");
    }

    public static String getRestaurantId(Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("MyData",Context.MODE_PRIVATE);
        return sharedPreferences.getString("restaurantid","N/A");
    }


    public static APIService getFCMClient()
    {
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On my way";
        else
            return "shipped";

    }

    public static boolean isConnectedToInternet(Context context)
    {ConnectivityManager cm =
            (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static String getDate(long time)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(android.text.format.DateFormat.format("dd MMM yyyy, hh:mm a",calendar).toString());
        return date.toString();
    }

    public static String getRestauranttype(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences("MyData",Context.MODE_PRIVATE);
        return sharedPreferences.getString("restauranttype","N/A");
    }
}
