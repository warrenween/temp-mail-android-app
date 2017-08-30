package com.tempmail.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.tempmail.R;

import java.io.InputStream;

/**
 * Created by Lotar on 05.10.2016.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    public static final String TAG = DownloadImageTask.class.getSimpleName();
    private ImageView bmImage;
    private String linkToGo;
    private Context mContext;

    public DownloadImageTask(Context context, ImageView bmImage, String linkToGo) {
        this.bmImage = bmImage;
        this.linkToGo = linkToGo;
        this.mContext = context;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.e(TAG, "onPreExecute");
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Log.e(TAG, "doInBackground");
        String urlDisplay = urls[0];
        Log.e(TAG, "url " + urlDisplay);
        Bitmap mIcon = null;
        try {
            InputStream in = new java.net.URL(urlDisplay).openStream();
            mIcon = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            if (e.getMessage() != null)
                Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        Log.e(TAG, "onPostExecute");
        bmImage.setVisibility(View.VISIBLE);
        bmImage.setImageBitmap(result);
        bmImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebURL(linkToGo);
            }
        });
    }


    private void openWebURL(String inURL) {
        if (mContext != null) {
            try {
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
                mContext.startActivity(browse);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, mContext.getString(R.string.cannot_found_browser), Toast.LENGTH_LONG).show();
            }

        }
    }
}
