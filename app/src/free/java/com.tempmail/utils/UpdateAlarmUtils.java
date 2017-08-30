package com.tempmail.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tempmail.services.UpdateAdService;

/**
 * Created by Lotar on 10.07.2016.
 */
public class UpdateAlarmUtils {
    public static final String ALARM_ACTION = "com.tempmail.update_ad_service";
    public static final Integer ALARM_REQUEST_CODE = 222;
    Context mContext;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    public UpdateAlarmUtils(Context context) {
        mContext = context;
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setUpdateAlarm(boolean isNeedUpdate) {
        alarmIntent = getUpdateServiceIntent(isNeedUpdate);
        if (alarmIntent != null)
            createAlarm(alarmIntent);
    }


    public PendingIntent getUpdateServiceIntent(boolean isNeedUpdate) {
        Intent intent = new Intent(mContext, UpdateAdService.class);
        intent.setAction(ALARM_ACTION);
        PendingIntent alarmIntent = PendingIntent.getService(mContext, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE);
        if (alarmIntent != null && !isNeedUpdate) {
            Log.d("UpdateAlarmUtils", "alarm not null");
            return null;
        }
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void createAlarm(PendingIntent alarmIntentUpdateCurrent) {
        long startTime = System.currentTimeMillis() + Constants.ONE_DAY;
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime,
                AlarmManager.INTERVAL_DAY, alarmIntentUpdateCurrent);
    }


    public void cancelAlarm() {
        alarmIntent = getUpdateServiceIntent(false);
        if (alarmIntent != null)
            alarmMgr.cancel(alarmIntent);
    }
}
