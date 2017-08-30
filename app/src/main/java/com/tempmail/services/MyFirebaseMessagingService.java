package com.tempmail.services;

import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tempmail.R;
import com.tempmail.utils.Log;
import com.tempmail.utils.Utils;

/**
 * Created by Lotar on 12.01.2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String url = null;

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "To: " + remoteMessage.getTo());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            url = remoteMessage.getData().get("url");
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String text = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();
            Log.d(TAG, "Message Notification Body: " + text);
            Log.d(TAG, "Message Notification title: " + title);
            if (TextUtils.isEmpty(title))
                title = getString(R.string.app_name);
            if (!TextUtils.isEmpty(url))
                Utils.showNotificationUrl(this, title, text, url);
            else
                Utils.showDefaultNotification(this, title, text);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
