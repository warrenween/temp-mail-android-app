package com.tempmail;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.org.apache.commons.lang3.RandomStringUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.InterstitialCallbacks;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.orm.util.NamingHelper;
import com.tempmail.api.ApiClient;
import com.tempmail.api.CancelableCallback;
import com.tempmail.api.models.answers.Mails;
import com.tempmail.dialogs.SimpleDialog;
import com.tempmail.dialogs.TellWhatThinkDialog;
import com.tempmail.models.Ads;
import com.tempmail.models.Email;
import com.tempmail.services.CheckNewEmailService;
import com.tempmail.services.EmailAlarmService;
import com.tempmail.utils.Constants;
import com.tempmail.utils.DownloadAdSettingsFile;
import com.tempmail.utils.DownloadImageTask;
import com.tempmail.utils.Log;
import com.tempmail.utils.Prefs;
import com.tempmail.utils.UpdateAlarmUtils;
import com.tempmail.utils.Utils;
import com.tempmail.utils.interfaces.OnEmailsCountListener;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnEmailsCountListener, InterstitialCallbacks {
    private static final String TAG = MainActivity.class.getSimpleName();
    private String generatedEmail;
    Button btn_check_mail, btn_copy, btn_change;
    List<String> mDomains;
    SharedPreferences sp;
    TextView tv_email;
    boolean isJustStart = true;
    int bannerPeriod = -1, interstitialPeriod = -1, bannerUrlPeriod = -1;
    String urlBannerLink, urlBannerImageLink;
    ImageView iv_url_banner;
    boolean startAdChecked = false;
    ProgressDialog progressDialog;
    boolean isBannerAdShouldBe = false;
    SimpleDialog simpleDialog;
    private CheckNewEmailService checkEmailAlarmService;
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
        initView();
        sp = getSharedPreferences(Prefs.PREF_APP, Context.MODE_PRIVATE);
        generatedEmail = sp.getString(Prefs.PREF_SAVED_EMAIL, "");
        if (generatedEmail.isEmpty()) {
            getDomainsList(true);
            tv_email.setText(R.string.your_email_will_be_here);
        } else {
            getDomainsList(false);
            tv_email.setText(generatedEmail);
            getEmails(generatedEmail);
        }

        Intent startEmailServiceIntent = new Intent(this, CheckNewEmailService.class);
        bindService(startEmailServiceIntent, mCheckMailServiceConnection, BIND_AUTO_CREATE);

        Intent startAlarmServiceIntent = new Intent(this, EmailAlarmService.class);
        startService(startAlarmServiceIntent);

        int savedCount = sp.getInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, 0);
        btn_check_mail.setText(String.valueOf(savedCount));

        initializeAds();
        versionControl();
        checkIfNeedReviewDialog();

        String link = getString(R.string.ad_settings_link) + getString(R.string.ad_settings_file_name);
        new DownloadAdSettingsFile(this).execute(link);

        getInterstitialData();
        getBannerData();
        getUrlAdData();

        //showInterstitialAd();
        showUrlBanner();
        showAdNetworkBanner();

        startAlarmUpdateAd();

        //Utils.showNotificationUrl(this, "url");
        makeButtonsWidth();

        //Log.d("Firebase", "token "+ FirebaseInstanceId.getInstance().getToken());
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth = outMetrics.widthPixels / density;
//
        Log.d(TAG, "dpWidth " + dpWidth);
    }


    public void makeButtonsWidth() {
        btn_change.post(new Runnable() {
            @Override
            public void run() {
                int btnChangeWidth = btn_change.getMeasuredWidth();
                int btnCopyWidth = btn_copy.getMeasuredWidth();
                Log.e(TAG, "btnCopyWidth " + btnCopyWidth);
                Log.e(TAG, "btnChangeWidth " + btnChangeWidth);
                if (btnChangeWidth > btnCopyWidth) {
                    btn_copy.setWidth(btnChangeWidth);
                } else if (btnCopyWidth > btnChangeWidth) {
                    btn_change.setWidth(btnCopyWidth);
                }
            }
        });
    }


    public void startAlarmUpdateAd() {
        UpdateAlarmUtils updateAlarmUtils = new UpdateAlarmUtils(this);
        updateAlarmUtils.setUpdateAlarm(false);
    }


    public void getInterstitialData() {
        List<Ads> adsList = Select.from(Ads.class)
                .where(Condition.prop("type").eq(getString(R.string.interstial_type))).list();
        if (adsList.size() > 0) {
            Ads ad = adsList.get(0);
            interstitialPeriod = Integer.valueOf(ad.getPeriod());
        }
    }


    public void getBannerData() {
        List<Ads> adsList = Select.from(Ads.class)
                .where(Condition.prop("type").eq(getString(R.string.banner_type))).list();
        if (adsList.size() > 0) {
            Ads ad = adsList.get(0);
            bannerPeriod = Integer.valueOf(ad.getPeriod());
        }
    }


    public void getUrlAdData() {
        List<Ads> adsList = Select.from(Ads.class)
                .where(Condition.prop("type").eq(getString(R.string.url_type))).list();
        if (adsList.size() > 0) {
            Ads ad = adsList.get(0);
            bannerUrlPeriod = Integer.valueOf(ad.getPeriod());
            urlBannerImageLink = ad.getImage_url();
            //Log.e(TAG, "url banner "+ urlBannerImageLink);
            urlBannerLink = ad.getLink();
        }
    }


    public void showAd(Activity activity, int adType) {
        try {
            Appodeal.show(activity, adType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showInterstitialAd() {
        if (!startAdChecked) {
            startAdChecked = true;
            int countStartAd = sp.getInt(Prefs.START_AD_COUNT, 0);
            //Log.d(TAG, "countStartAd=" + countStartAd + " interstitialPeriod =" + interstitialPeriod);
            //Log.d(TAG, "countStartAd% interstitialPeriod=" + countStartAd % interstitialPeriod);
            if (interstitialPeriod != -1 && interstitialPeriod != 0) {
                if (countStartAd % interstitialPeriod == 0) {
                    //Log.d(TAG, "show interstitial Ad");
                    showAd(this, Appodeal.INTERSTITIAL);
                }
                sp.edit().putInt(Prefs.START_AD_COUNT, ++countStartAd).apply();
            }
        }
    }


    public void showUrlBanner() {
        int countUrlBanner = sp.getInt(Prefs.URL_AD_COUNT, 0);
        //Log.d(TAG, "countUrlBanner=" + countUrlBanner + " bannerUrlPeriod =" + bannerUrlPeriod);
        //Log.d(TAG, "countUrlBanner% bannerUrlPeriod=" + countUrlBanner % bannerUrlPeriod);
        if (bannerUrlPeriod != -1 && bannerUrlPeriod != 0) {
            if (countUrlBanner % bannerUrlPeriod == 0) {
                //Log.d(TAG, "show url banner");
                new DownloadImageTask(MainActivity.this, iv_url_banner, urlBannerLink)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlBannerImageLink);
            }
            sp.edit().putInt(Prefs.URL_AD_COUNT, ++countUrlBanner).apply();
        }
    }


    public void showAdNetworkBanner() {
        int countAdNetworkTextAd = sp.getInt(Prefs.BANNER_AD_COUNT, 0);
        //Log.d(TAG, "countAdNetworkTextAd=" + countAdNetworkTextAd + " bannerPeriod =" + bannerPeriod);
        //Log.d(TAG, "countAdNetworkTextAd% bannerPeriod=" + countAdNetworkTextAd % bannerPeriod);
        if (bannerPeriod != -1 && bannerPeriod != 0) {
            if (countAdNetworkTextAd % bannerPeriod == 0) {
                //Log.d(TAG, "show AdNetwork Ad");
                isBannerAdShouldBe = true;
                showAd(this, Appodeal.BANNER_BOTTOM);
            }
            sp.edit().putInt(Prefs.BANNER_AD_COUNT, ++countAdNetworkTextAd).apply();
        }
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
        if (isBannerAdShouldBe) {
            try {
                Appodeal.onResume(this, Appodeal.BANNER_BOTTOM);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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


    public void initializeAds() {
        try {
            Appodeal.disableLocationPermissionCheck();
            Appodeal.setTesting(false);
            Appodeal.setAutoCache(Appodeal.INTERSTITIAL, true);
            Appodeal.setAutoCache(Appodeal.BANNER_BOTTOM, true);
            //Appodeal.setOnLoadedTriggerBoth(Appodeal.INTERSTITIAL, false);
            Appodeal.setBannerCallbacks(new BannerCallbacks() {
                @Override
                public void onBannerLoaded(int i, boolean b) {
                    Log.e(TAG, "onBannerLoaded");
                }

                @Override
                public void onBannerFailedToLoad() {
                    Log.e(TAG, "onBannerFailedToLoad");
                }

                @Override
                public void onBannerShown() {
                    Log.e(TAG, "onBannerShown");
                }

                @Override
                public void onBannerClicked() {
                    Log.e(TAG, "onBannerClicked");
                }
            });
            Appodeal.initialize(this, getString(R.string.appodeal_key), Appodeal.INTERSTITIAL | Appodeal.BANNER_BOTTOM);
            Appodeal.setInterstitialCallbacks(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDomainsList(final boolean isShouldGenerate) {
        showProgressDialog();
        ApiClient.getClient(getString(R.string.login), getString(R.string.password)).getDomainsList(new CancelableCallback<List<String>>(new Callback<List<String>>() {
            @Override
            public void success(List<String> domains, Response response) {
                dismissProgressDialog();
                mDomains = domains;
//                for(String domain: domains){
//                    Log.e(TAG, domain);
//                }
                String savedDomain = sp.getString(Prefs.PREF_SAVED_DOMAIN, "");
                boolean isDomainChanged = !TextUtils.isEmpty(savedDomain) && !mDomains.contains(savedDomain);
                if (isShouldGenerate || isDomainChanged) {
                    if(isDomainChanged) {
                        String savedLogin = sp.getString(Prefs.PREF_SAVED_LOGIN, "");
                        generateEmail(savedLogin);
                    }else
                        generateEmail(null);
                    getEmails(generatedEmail);
                }
                showInterstitialAd();
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
        tv_email.setText(generatedEmail);
        sp.edit().putString(Prefs.PREF_SAVED_EMAIL, generatedEmail).apply();
        sp.edit().putInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, 0).apply();
        sp.edit().putString(Prefs.PREF_SAVED_LOGIN, generatedLogin).apply();
        sp.edit().putString(Prefs.PREF_SAVED_DOMAIN, randomDomain).apply();
    }


    public void getEmails(final String emailAddress) {
        Log.e(TAG, "Get email for: " + emailAddress);
        ApiClient.getClient(getString(R.string.login), getString(R.string.password)).getEmails(Utils.getMd5(emailAddress), new CancelableCallback<List<Mails>>(new Callback<List<Mails>>() {

            @Override
            public void success(List<Mails> mails, Response response) {
                //Log.e(TAG, "list size "+ mails.size());
                saveNewEmails(mails);
                showEmailsCount();
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                Response response = error.getResponse();
                if (response != null && response.getStatus() == 404) {
                    removeShortcutBadger();
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


    public void showEmailsCount() {
        List<Email> emails = Email.find(Email.class, "checked" + "=" + "0");
        int newEmailsCount = emails.size();
        if (emails.isEmpty()) {
            sp.edit().putInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, newEmailsCount).apply();
            removeShortcutBadger();
            btn_check_mail.setText("0");
        } else {
            try {
                ShortcutBadger.applyCount(MainActivity.this, newEmailsCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
            btn_check_mail.setText(String.valueOf(newEmailsCount));
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


    public void initView() {
        btn_copy = (Button) findViewById(R.id.btn_copy);
        btn_change = (Button) findViewById(R.id.btn_change);
        btn_check_mail = (Button) findViewById(R.id.bnt_check_mail);
        tv_email = (TextView) findViewById(R.id.tv_email);
        iv_url_banner = (ImageView) findViewById(R.id.iv_url_banner);

        btn_copy.setOnClickListener(this);
        btn_change.setOnClickListener(this);
        btn_check_mail.setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_copy:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("email", generatedEmail);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.email_copied, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_change:
                btn_check_mail.setText("0");
                generateEmail(null);
                getEmails(generatedEmail);
                break;
            case R.id.bnt_check_mail:
                openWebURL(getString(R.string.temp_email_link, Utils.getNeededLanguage(this), generatedEmail));
                makeEmailsRead();
                btn_check_mail.setText("0");
                break;
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
        btn_check_mail.setText(String.valueOf(count));
    }

    @Override
    public void onInterstitialLoaded(boolean isPrecache) {
        Log.e(TAG, "onInterstitialLoaded " + isPrecache);
        showInterstitialAd();
    }

    @Override
    public void onInterstitialFailedToLoad() {
        Log.e(TAG, "onInterstitialFailedToLoad");
    }

    @Override
    public void onInterstitialShown() {
        Log.e(TAG, "onInterstitialShown");
    }

    @Override
    public void onInterstitialClicked() {
        Log.e(TAG, "onInterstitialClicked");
    }

    @Override
    public void onInterstitialClosed() {
        Log.e(TAG, "onInterstitialClosed");
    }


    private void showProgressDialog() {
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

    private void dismissProgressDialog() {
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
}
