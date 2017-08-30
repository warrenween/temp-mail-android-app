package com.tempmail.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.tempmail.R;
import com.tempmail.models.Ads;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Lotar on 12.10.2016.
 */

public class DownloadAdSettingsFile extends AsyncTask<String, Void, String> {
    public static final String TAG = DownloadAdSettingsFile.class.getSimpleName();
    private Context mContext;

    public DownloadAdSettingsFile(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String urlLink = params[0] + "?" + System.currentTimeMillis();
        Log.d(TAG, urlLink);
        try {
            URL url = new URL(urlLink);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            Log.d(TAG, "fileLength  " + fileLength);
            // download the file
            input = connection.getInputStream();
            output = mContext.openFileOutput(mContext.getString(R.string.ad_settings_file_name), Context.MODE_PRIVATE);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result != null)
            Log.e(TAG, "Download error: " + result);
        else {
            String jsonAd = getTextFromFile();
            Log.d(TAG, "File download successfully " + jsonAd);
            try {
                JSONObject jsonObject = new JSONObject(jsonAd);
                JSONArray jsonArray = jsonObject.getJSONArray("ad");
                Ads.deleteAll(Ads.class);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject currentJsonObject = (JSONObject) jsonArray.get(i);

                    Gson gson = new Gson();
                    List<Ads> adsList = Ads.listAll(Ads.class);
                    Log.e(TAG, "count Ad:" + adsList.size());
                    Ads ads;

                    //if (adsList.size() == 0) {
                    ads = gson.fromJson(currentJsonObject.toString(), Ads.class);
//                    } else {
//                        Ads adsTemp = gson.fromJson(jsonAd, Ads.class);
//                        ads = adsList.get(0);
//                        ads.setImage_url(adsTemp.getImage_url());
//                        ads.setLink(adsTemp.getLink());
//                        ads.setPeriod(adsTemp.getPeriod());
//                        ads.setType(adsTemp.getType());
//                    }
                    ads.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    private String getTextFromFile() {
        File file = new File(mContext.getFilesDir(), mContext.getString(R.string.ad_settings_file_name));

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}
