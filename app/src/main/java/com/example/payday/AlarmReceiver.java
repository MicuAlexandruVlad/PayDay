package com.example.payday;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "Receiver";
    private Reserve currentReserve;

    @Override
    public void onReceive(Context context, Intent intent){


        String action = intent.getAction();
        Log.i(TAG, "onReceive: action -> " + action);
        assert action != null;
        if (action.equals("send.reserve")) {
            currentReserve = new Reserve();

            String reserveName = intent.getExtras().getString("reserve_name");
            String when = intent.getExtras().getString("when");
            String at = intent.getExtras().getString("at");
            int goalAmount = intent.getExtras().getInt("reserve_goal_amount");
            int actualAmount = intent.getExtras().getInt("reserve_actual_amount");
            String sameDayReminderDate = intent.getExtras().getString("same_day_reminder_date");
            String fullScheduledDate = intent.getExtras().getString("full_scheduled_date");
            String referencedUsername = intent.getExtras().getString("referenced_user_name");
            boolean hasSameDayReminder = intent.getExtras().getBoolean("has_same_day_reminder");
            boolean hasSecondaryReminder = intent.getExtras().getBoolean("has_secondary_reminder");
            boolean finalised = intent.getExtras().getBoolean("is_finalised");
            String secondaryReminderDate = intent.getExtras().getString("secondary_reminder_date");
            String note = intent.getExtras().getString("note");
            int priority = intent.getExtras().getInt("priority");

            createNotification(context, intent, reserveName, when, at, goalAmount, actualAmount);
        }
    }

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Payday";
            String description = "test";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification(Context context, Intent intent, String reserveName,
                                    String when, String at, int goalAmount, int actualAmount) {
        createNotificationChannel(context);
        Intent intent1 = new Intent(context, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, 0);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(reserveName + " Reserve")
                .setContentText("Payment")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Payment inbound " + when + " at " + at + ". Goal amount: " + goalAmount
                        + ". Reserved amount: " + actualAmount))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = 12;
        notificationManager.notify(notificationId, mBuilder.build());
    }


}
