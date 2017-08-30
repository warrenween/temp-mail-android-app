package com.tempmail.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tempmail.utils.CheckEmailAlarmUtils;
import com.tempmail.utils.Constants;
import com.tempmail.utils.Log;
import com.tempmail.utils.Prefs;

/**
 * Created by Lotar on 25.12.2016.
 */

public class EmailAlarmService extends Service {
    public static final String TAG = EmailAlarmService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    Handler handler;
    Runnable runnable;
    int timeCount = 0;
    SharedPreferences sp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Prefs.PREF_APP, MODE_PRIVATE);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //Log.e(TAG, "time count " + timeCount);
                if (timeCount < Constants.TEN_MINUTES) {
                    try {
                        startService(new Intent(EmailAlarmService.this, CheckNewEmailService.class));
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    cancelAlarm();
                    startAlarm();
                    timeCount += Constants.CHECK_TIME_BEFORE;
                    handler.postDelayed(runnable, Constants.CHECK_TIME_BEFORE);
                } else {
                    Log.e(TAG, "should be stopped");
                    cancelAlarm();
                    startAlarm();
                    stopSelf();
                }
            }
        };
    }


    public void startAlarm() {
        CheckEmailAlarmUtils checkEmailAlarmUtils = new CheckEmailAlarmUtils(this);
        checkEmailAlarmUtils.setCheckEmailAlarm(false);
    }


    public void cancelAlarm() {
        CheckEmailAlarmUtils checkEmailAlarmUtils = new CheckEmailAlarmUtils(this);
        checkEmailAlarmUtils.cancelAlarm();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timeCount = 0;
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, Constants.CHECK_TIME_BEFORE);

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }


    public class LocalBinder extends Binder {
        public EmailAlarmService getService() {
            // Return this instance of LocalService so clients can call public methods
            return EmailAlarmService.this;
        }
    }
}
