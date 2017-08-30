package com.tempmail;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.orm.SugarApp;
import com.tempmail.models.Email;
import com.zendesk.sdk.network.impl.ZendeskConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Lotar on 25.12.2016.
 */

public class ApplicationClass extends SugarApp {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        initZendesk();

        //This is for fixing bug with recreating database. Don't delete this
        Email email = new Email();
        Email.findById(Email.class, (long) 1);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public void initZendesk() {
        ZendeskConfig.INSTANCE.init(this, "https://privatix.zendesk.com", "066132e8c639addad67bf202b57164105bc8eb85b6180938", "mobile_sdk_client_c690252b5974d75dac92");
    }
}
