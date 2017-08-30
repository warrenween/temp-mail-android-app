package com.tempmail.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.tempmail.services.EmailAlarmService;
import com.tempmail.utils.Log;
import com.tempmail.utils.Prefs;

/**
 * Created by Lotar on 05.08.2016.
 */
public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.e(TAG, "onReceive");
            SharedPreferences sp = context.getSharedPreferences(Prefs.PREF_APP, Context.MODE_PRIVATE);
            String emailAddress = sp.getString(Prefs.PREF_SAVED_EMAIL, "");
            if (!emailAddress.isEmpty())
                context.startService(new Intent(context, EmailAlarmService.class));
        }
    }
}
