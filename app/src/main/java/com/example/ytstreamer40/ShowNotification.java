package com.example.ytstreamer40;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class ShowNotification {
    NotificationCompat.Builder builder;
    String CHANNEL_ID,title,msg;
    int REQUEST_CODE = 100;
    Context context;
    NotificationManager manager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    ShowNotification(Context context,String title, String msg,int channel_id)
    {
        this.title = title;
        this.context = context;
        this.msg = msg;
        this.CHANNEL_ID = String.valueOf(channel_id);
        Activity act = (Activity) context;

        PendingIntent intent = PendingIntent.getActivity(context,REQUEST_CODE,new Intent(context,act.getClass()),PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(intent)
        .setAutoCancel(true);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.createNotificationChannel(channel);
    }

    public void show()
    {
        manager.notify(REQUEST_CODE,builder.build());
    }

}
