package com.tempmail.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.orm.util.NamingHelper;
import com.tempmail.R;
import com.tempmail.api.ApiClient;
import com.tempmail.api.CancelableCallback;
import com.tempmail.api.models.answers.Mails;
import com.tempmail.models.Email;
import com.tempmail.utils.Prefs;
import com.tempmail.utils.Utils;
import com.tempmail.utils.interfaces.OnEmailsCountListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Lotar on 25.12.2016.
 */

public class CheckNewEmailService extends Service {
    public static final String TAG = CheckNewEmailService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    SharedPreferences sp;
    List<OnEmailsCountListener> mOnEmailsCountListeners = new ArrayList<>();

    public static String getJsonFromResponse(Response response) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
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

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.e(TAG, "onCreate");
        sp = getSharedPreferences(Prefs.PREF_APP, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.e(TAG, "onStartCommand");
        String emailAddress = sp.getString(Prefs.PREF_SAVED_EMAIL, "");
        if (!emailAddress.isEmpty())
            getEmails(emailAddress);
        return START_NOT_STICKY;
    }

    public void addListener(OnEmailsCountListener onEmailsCountListener) {
        mOnEmailsCountListeners.add(onEmailsCountListener);
    }

    public void removeListener(OnEmailsCountListener onEmailsCountListener) {
        mOnEmailsCountListeners.remove(onEmailsCountListener);
    }

    public void notifyListeners(int count) {
        for (OnEmailsCountListener onEmailsCountListener : mOnEmailsCountListeners)
            onEmailsCountListener.onEmailsCountChange(count);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void getEmails(final String emailAddress) {
        //Log.e(TAG, "Get email for: "+ emailAddress);
        ApiClient.getClient(getString(R.string.login), getString(R.string.password)).getEmails(Utils.getMd5(emailAddress), new CancelableCallback<List<Mails>>(new Callback<List<Mails>>() {

            @Override
            public void success(List<Mails> mails, Response response) {
//                if(mails == null){
//                    String message = "Email: " + emailAddress + " Response " + getJsonFromResponse(response);
//                    Crashlytics.log(message);
//                    try {
//                        throw new NullPointerException("Mails list null");
//                    }catch (NullPointerException e){
//                        e.printStackTrace();
//                        Crashlytics.logException(e);
//                    }
//                }
                saveNewEmails(mails);
                showEmailsCount();
            }

            @Override
            public void failure(RetrofitError error) {
                //error.printStackTrace();
                Response response = error.getResponse();
                if (response != null && response.getStatus() == 404) {
                    removeShortcutBadger();
                }
            }
        }));
    }

    public void removeShortcutBadger() {
        try {
            ShortcutBadger.removeCount(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveNewEmails(List<Mails> mails) {
        if (mails == null)
            return;
        for (Mails mail : mails) {
            List<Email> emails = Email.find(Email.class, NamingHelper.toSQLNameDefault("eid") + "=" + "\"" + mail.getMailId() + "\"");
            if (emails.isEmpty()) {
                Utils.showNewEmailNotification(getApplicationContext(), getString(R.string.new_mail));
                Email email = new Email(mail.getMailId(), false);
                email.save();
            }
        }
    }


    public void showEmailsCount() {
        List<Email> emails = Email.find(Email.class, "checked" + "=" + "0");
        int newEmailsCount = emails.size();
        if (emails.isEmpty()) {
            removeShortcutBadger();
            notifyListeners(newEmailsCount);
            sp.edit().putInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, newEmailsCount).apply();
        } else {
            sp.edit().putInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, newEmailsCount).apply();
            try {
                ShortcutBadger.applyCount(getApplicationContext(), newEmailsCount);
            } catch (Exception e) {
                e.printStackTrace();
            }

            notifyListeners(newEmailsCount);
        }
    }


    public class LocalBinder extends Binder {
        public CheckNewEmailService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CheckNewEmailService.this;
        }
    }
}
