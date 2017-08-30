package com.tempmail.api;

import android.util.Base64;

import com.jakewharton.retrofit.Ok3Client;
import com.tempmail.BuildConfig;
import com.tempmail.api.models.answers.Mails;
import com.tempmail.utils.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Lotar on 25.12.2016.
 */

public class ApiClient {
    public static final String ENDPOINT = "http://api2.temp-mail.org/request";
    public static final String FORMAT = "format/json";
    private static final RestAdapter.LogLevel LOG_LEVEL = BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();


    private static RestAdapter.Builder builder = new RestAdapter.Builder()
            .setEndpoint(ENDPOINT)
            .setLogLevel(LOG_LEVEL)
            .setClient(new Ok3Client(okHttpClient));


    public static RestApiClient getClient(String username, String password) {
        if (username != null && password != null) {
            // concatenate username and password with colon for authentication
            String credentials = username + ":" + password;
            // create Base64 encodet string
            final String basic =
                    "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Authorization", basic);
                    request.addHeader("Accept", "application/json");
                }
            });
        }

        RestAdapter adapter = builder.build();
        return adapter.create(RestApiClient.class);
    }


    public interface RestApiClient {


        @GET("/domains/" + FORMAT)
        void getDomainsList(CancelableCallback<List<String>> callback);

        @GET("/mail/id/{email}/" + FORMAT)
        void getEmails(@Path("email") String email, CancelableCallback<List<Mails>> callback);
    }
}
