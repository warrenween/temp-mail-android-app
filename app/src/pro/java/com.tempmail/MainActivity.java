package com.tempmail;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.org.apache.commons.lang3.RandomStringUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.Toast;

import com.orm.util.NamingHelper;
import com.tempmail.api.ApiClient;
import com.tempmail.api.CancelableCallback;
import com.tempmail.api.models.answers.Mails;
import com.tempmail.dialogs.SimpleDialog;
import com.tempmail.dialogs.TellWhatThinkDialog;
import com.tempmail.fragments.MailsListFragment;
import com.tempmail.fragments.MainFragment;
import com.tempmail.models.Email;
import com.tempmail.services.CheckNewEmailService;
import com.tempmail.services.EmailAlarmService;
import com.tempmail.utils.Constants;
import com.tempmail.utils.Log;
import com.tempmail.utils.OnFragmentInteractionListener;
import com.tempmail.utils.Prefs;
import com.tempmail.utils.Utils;
import com.tempmail.utils.interfaces.OnEmailsCountListener;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements OnEmailsCountListener, OnFragmentInteractionListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    private String generatedEmail;
    List<String> mDomains;
    SharedPreferences sp;
    ProgressDialog progressDialog;
    SimpleDialog simpleDialog;
    private CheckNewEmailService checkEmailAlarmService;
    int count=0;
    private ServiceConnection mCheckMailServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            checkEmailAlarmService = ((CheckNewEmailService.LocalBinder) service).getService();
            checkEmailAlarmService.addListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            checkEmailAlarmService = null;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate " + this.hashCode());
        Intent intent = getIntent();
        boolean isShouldStop = openUrlFromNotification(intent, true);
        super.onCreate(savedInstanceState);
        if (isShouldStop) {
            return;
        }

        setContentView(R.layout.activity_main);



        sp = getSharedPreferences(Prefs.PREF_APP, Context.MODE_PRIVATE);
        generatedEmail = sp.getString(Prefs.PREF_SAVED_EMAIL, "");
        navigateToFragment(MainFragment.newInstance(), false);

        Intent startAlarmServiceIntent = new Intent(this, EmailAlarmService.class);
        startService(startAlarmServiceIntent);

        Intent startEmailServiceIntent = new Intent(this, CheckNewEmailService.class);
        bindService(startEmailServiceIntent, mCheckMailServiceConnection, Context.BIND_AUTO_CREATE);


        versionControl();
        checkIfNeedReviewDialog();


        //Log.d(TAG, "SHA1 " + Utils.getCertificateSHA1Fingerprint(this));
        //Log.d(TAG, "SHA256 " + Utils.getCertificateSHA256Fingerprint(this));
    }


    public boolean openUrlFromNotification(Intent intent, boolean isFromOnCreate) {
        if (intent != null) {
            Log.d(TAG, "intent!=null");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Log.d(TAG, "bundle!=null");
                String url = intent.getExtras().getString("url");
                if (!TextUtils.isEmpty(url) && isUrlValid(url)) {
                    Log.d(TAG, "url not empty");
                    openWebURL(url);
                    if (isFromOnCreate)
                        finish();
                    return true;
                }
            }
        }
        return false;
    }


    public boolean isUrlValid(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        openUrlFromNotification(intent, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }


    private void versionControl() {
        int oldVersionCode;
        int versionCode = BuildConfig.VERSION_CODE;
        oldVersionCode = sp.getInt(Prefs.VERSION_KEY, 0);
        if (versionCode != oldVersionCode)
            changeVersion(versionCode);
    }

    private void changeVersion(int newVersionCode) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Prefs.VERSION_KEY, newVersionCode);
        editor.putInt(Prefs.COUNTER_KEY, 0);
        editor.putBoolean(Prefs.NEED_SHOW_KEY, true);
        editor.putLong(Prefs.TIME_KEY, System.currentTimeMillis());
        editor.apply();
    }


    public void checkIfNeedReviewDialog() {
        Log.e(TAG, "checkIfNeedReviewDialog");
        //Log.d("ratingDialog", "check");
        long currentData = Calendar.getInstance().getTimeInMillis();
        //Log.d("ratingDialog", "cd: " + currentData);
        long oldData = sp.getLong(Prefs.TIME_KEY, 0);
        //Log.d("ratingDialog", "od: " + oldData);
        long timeDifference = currentData - oldData;
        if (timeDifference < Constants.ONE_DAY) {
            //Log.d("ratingDialog", "dif: " + timeDifference);
            return;
        }
        boolean needShow = sp.getBoolean(Prefs.NEED_SHOW_KEY, true);
        Log.e(TAG, "need show review dialog");
        if (needShow) {
            //Log.d("ratingDialog", "show");
            TellWhatThinkDialog tellWhatThinkDialog = TellWhatThinkDialog.newInstance();
            try {
                tellWhatThinkDialog.show(getFragmentManager(), TellWhatThinkDialog.class.getSimpleName());
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void getDomainsList(final boolean isShouldGenerate) {
        showProgressDialog();
        ApiClient.getClient(getString(R.string.login), getString(R.string.password)).getDomainsList(new CancelableCallback<List<String>>(new Callback<List<String>>() {
            @Override
            public void success(List<String> domains, Response response) {
                dismissProgressDialog();
                mDomains = domains;
                String savedDomain = sp.getString(Prefs.PREF_SAVED_DOMAIN, "");
                boolean isDomainChanged = !TextUtils.isEmpty(savedDomain) && !mDomains.contains(savedDomain);

                if (isShouldGenerate || isDomainChanged) {
                    if(isDomainChanged) {
                        String savedLogin = sp.getString(Prefs.PREF_SAVED_LOGIN, "");
                        generateEmail(savedLogin);
                    }else
                        generateEmail(null);
                    getEmails(generatedEmail, false);
                }
            }

            @Override
            public void failure(final RetrofitError error) {
                //error.printStackTrace();
                dismissProgressDialog();
                Log.e(TAG, "get Domains error kind " + error.getKind());
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (error.getKind() == RetrofitError.Kind.NETWORK) {
                            showSimpleDialog(getString(R.string.network_error), getString(R.string.check_network_connection));
                        } else {
                            showSimpleDialog(getString(R.string.get_domains_error_title), getString(R.string.get_domains_error_message));
                        }
                    }
                });
            }
        }));
    }


    public void showSimpleDialog(String title, String message) {
        Log.e(TAG, "show Simple Dialog ");
        if (simpleDialog != null)
            simpleDialog.dismissAllowingStateLoss();
        simpleDialog = SimpleDialog.newInstance(title, message);
        Log.e(TAG, "simpleDialog " + simpleDialog.hashCode());
        simpleDialog.setCancelable(false);
        try {
            simpleDialog.show(getSupportFragmentManager(), SimpleDialog.class.getSimpleName());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void generateEmail(String preFilledLogin) {
        if (mDomains == null || mDomains.size() == 0) {
            Toast.makeText(this, R.string.no_available_domains, Toast.LENGTH_LONG).show();
            return;
        }
        Email.deleteAll(Email.class);
        String generatedLogin;
        if(preFilledLogin==null)
            generatedLogin = RandomStringUtils.randomAlphabetic(6, 10).toLowerCase();
        else
            generatedLogin= preFilledLogin;
        int randomDomainValue = generateRandom(mDomains.size(), 0);
        String randomDomain = mDomains.get(randomDomainValue);
        generatedEmail = generatedLogin + randomDomain;
        Fragment f= getCurrentFragment();
        if(f instanceof MainFragment)
            ((MainFragment) f).setEmailAddressText(generatedEmail);
        sp.edit().putString(Prefs.PREF_SAVED_EMAIL, generatedEmail).apply();
        sp.edit().putInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, 0).apply();
        sp.edit().putString(Prefs.PREF_SAVED_LOGIN, generatedLogin).apply();
        sp.edit().putString(Prefs.PREF_SAVED_DOMAIN, randomDomain).apply();
    }


    public void onEmailChanged(String email){
        generatedEmail= email;
        getEmails(generatedEmail, true);
        sp.edit().putString(Prefs.PREF_SAVED_EMAIL, generatedEmail).apply();
        sp.edit().putInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, 0).apply();
        Fragment f= getCurrentFragment();
        if(f instanceof MainFragment) {
            ((MainFragment) f).setEmailAddressText(generatedEmail);
        }
    }


    public void getEmails(final String emailAddress, final boolean isSaveNewAddress) {
        Log.e(TAG, "Get email for: " + emailAddress);
        ApiClient.getClient(getString(R.string.login), getString(R.string.password)).getEmails(Utils.getMd5(emailAddress), new CancelableCallback<List<Mails>>(new Callback<List<Mails>>() {

            @Override
            public void success(List<Mails> mails, Response response) {
                //Log.e(TAG, "list size "+ mails.size());
//                if(isSaveNewAddress){
//
//                }
                saveNewEmails(mails);
                showEmailsCount();
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                Response response = error.getResponse();
                if (response != null && response.getStatus() == 404) {
                    removeShortcutBadger();
                    Fragment f= getCurrentFragment();
                    if(f instanceof MainFragment)
                    ((MainFragment) f).setEmailCountText("0");
//                    if(isSaveNewAddress){
//                        Fragment f= getCurrentFragment();
//                        if(f instanceof MainFragment)
//                            ((MainFragment) f).setEmailAddressText(emailAddress);
//                        //tv_email.setText(emailAddress);
//                        sp.edit().putString(Prefs.PREF_SAVED_EMAIL, emailAddress).apply();
//                        sp.edit().putInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, 0).apply();
//                    }
                }
                //tv_email.setText(emailAddress);
            }
        }));
    }


    public void saveNewEmails(List<Mails> mails) {
        if (mails == null)
            return;
        for (Mails mail : mails) {
            List<Email> emails = Email.find(Email.class, NamingHelper.toSQLNameDefault("eid") + "=" + "\"" + mail.getMailId() + "\"");
            if (emails.isEmpty()) {
                Email email = new Email(mail.getMailId(), false);
                email.save();
            }
        }
    }


    public Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.container);
    }


    public void showEmailsCount() {
        List<Email> emails = Email.find(Email.class, "checked" + "=" + "0");
        int newEmailsCount = emails.size();
        if (emails.isEmpty()) {
            sp.edit().putInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, newEmailsCount).apply();
            removeShortcutBadger();
            Fragment f= getCurrentFragment();
            if(f instanceof MainFragment)
                ((MainFragment) f).setEmailCountText("0");
        } else {
            try {
                ShortcutBadger.applyCount(MainActivity.this, newEmailsCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Fragment f= getCurrentFragment();
            if(f instanceof MainFragment)
                ((MainFragment) f).setEmailCountText(String.valueOf(newEmailsCount));
            //btn_check_mail.setText(String.valueOf(newEmailsCount));
        }
    }


    public void removeShortcutBadger() {
        try {
            ShortcutBadger.removeCount(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int generateRandom(int maxValue, int minValue) {
        Random r = new Random();
        return r.nextInt(maxValue - minValue) + minValue;
    }


    public void openWebURL(String inURL) {
        try {
            Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
            if (browse.resolveActivity(getPackageManager()) != null)
                startActivity(browse);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (!isFinishing())
                    Toast.makeText(this, R.string.cannot_found_browser, Toast.LENGTH_LONG).show();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

    }


    public void makeEmailsRead() {
        removeShortcutBadger();
        List<Email> emails = Email.listAll(Email.class);
        for (Email email : emails) {
            email.setChecked(true);
            email.save();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy ");
        dismissProgress();
        if (checkEmailAlarmService != null) {
            unbindService(mCheckMailServiceConnection);
            checkEmailAlarmService.removeListener(this);
        }
    }

    @Override
    public void onEmailsCountChange(int count) {
        Log.e(TAG, "onEmailsCountChange " + count);
        Fragment f= getCurrentFragment();
        if(f instanceof MainFragment)
            ((MainFragment) f).setEmailCountText(String.valueOf(String.valueOf(count)));
        if(f instanceof MailsListFragment && this.count != count) {
            ((MailsListFragment) f).getEmails(generatedEmail);
        }
        this.count= count;
        //btn_check_mail.setText(String.valueOf(count));
    }


    public void showProgressDialog() {
        Log.d(TAG, "showProgressDialog " + " isFinishing " + isFinishing());
        if (!isFinishing()) {
            try {
                progressDialog = ProgressDialog.show(this, getString(R.string.progress_dialog_title),
                        getString(R.string.progress_dialog_message));
                progressDialog.setCancelable(false);
                Log.d(TAG, "showProgressDialog " + progressDialog.hashCode());
            } catch (IllegalArgumentException | WindowManager.BadTokenException e) {
                e.printStackTrace();
            }
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null)
            Log.d(TAG, "dismissProgressDialog " + progressDialog.hashCode() + " isFinishing " + isFinishing());
        else
            Log.d(TAG, "dismissProgressDialog null");
        if (!isFinishing())
            dismissProgress();

    }


    public void dismissProgress() {
        if (progressDialog != null)
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
    }


    public String getGeneratedEmail() {
        return generatedEmail;
    }


    public List<String> getmDomains() {
        return mDomains;
    }

    @SuppressLint("CommitTransaction")
    @Override
    public void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction()
                .replace(R.id.container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(fragment.getClass().getSimpleName());
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }



    public void addFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction()
                .add(R.id.container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(fragment.getClass().getSimpleName());
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }


}
