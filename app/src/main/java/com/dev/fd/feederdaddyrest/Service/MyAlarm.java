package com.dev.fd.feederdaddyrest.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dev.fd.feederdaddyrest.Orders;
import com.dev.fd.feederdaddyrest.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


//class extending the Broadcast Receiver
public class MyAlarm extends BroadcastReceiver {

    //the method will be fired when the alarm is triggerred
    @Override
    public void onReceive(final Context context, Intent intent) {

        //you can check the log that it is fired
        //Here we are actually not doing anything
        //but you can do any task here that you want to be done at a specific time everyday

        String key =  intent.getStringExtra("currentrequest");

                    Intent inte = new Intent(context, Orders.class);
                    inte.putExtra("currentorder","1");
                    inte.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context,0,inte, PendingIntent.FLAG_ONE_SHOT);


                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText("Start Cooking order #"+key.substring(1,key.length())+" which is to be delivered 45 min later!"))
                            .setContentText("Start Cooking order #"+key.substring(1,key.length())+" which is to be delivered 45 min later!")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.feeder_daddy_logo)
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                    R.drawable.feeder_daddy_logo))
                            .setContentTitle("45 min remaining in order #"+key.substring(1,key.length())+" delivery!")
                            .setSound(defaultSoundUri);

                    NotificationManager noti = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    noti.notify(0,builder.build());
                }

}
