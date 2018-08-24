package com.mazeit.mypushnotification;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by User on 5/16/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        sendMessageToActivity(remoteMessage.getData().get("message"), remoteMessage.getData().get("phone"));
    }

    private void sendMessageToActivity(String msg, String phone) {
        Intent intent = new Intent("PushNotificationMessages");
        intent.putExtra("Message", msg);
        intent.putExtra("Phone", phone);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}