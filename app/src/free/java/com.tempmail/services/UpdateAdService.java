package com.tempmail.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tempmail.R;
import com.tempmail.utils.DownloadAdSettingsFile;

/**
 * Created by Lotar on 30.12.2016.
 */

public class UpdateAdService extends Service {
    public static final String TAG = UpdateAdService.class.getSimpleName();
    private final IBinder mBinder = new UpdateAdService.LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.e(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.e(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.e(TAG, "onStartCommand");
        String link = getString(R.string.ad_settings_link) + getString(R.string.ad_settings_file_name);
        new DownloadAdSettingsFile(this).execute(link);
        stopSelf();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public class LocalBinder extends Binder {
        public UpdateAdService getService() {
            // Return this instance of LocalService so clients can call public methods
            return UpdateAdService.this;
        }
    }
}
