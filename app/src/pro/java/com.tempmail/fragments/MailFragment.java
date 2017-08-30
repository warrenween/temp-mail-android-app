package com.tempmail.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.tempmail.MainActivity;
import com.tempmail.R;
import com.tempmail.adapters.MailListAdapter;
import com.tempmail.api.ApiClient;
import com.tempmail.api.CancelableCallback;
import com.tempmail.api.models.answers.Mail;
import com.tempmail.api.models.answers.Mails;
import com.tempmail.utils.Log;
import com.tempmail.utils.OnFragmentInteractionListener;
import com.tempmail.utils.Utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MailFragment extends Fragment {
    private static final String MAIL = "mail";
    private static final String TAG = MailFragment.class.getSimpleName();
    MainActivity mainActivity;
    Context context;
    OnFragmentInteractionListener listener;
    WebView webView;
    TextView tvSubjectText, tvFromText, tvTime;
    CancelableCallback<List<Mails>> cancelableCallback;
    Mails mails;

    public MailFragment() {
        // Required empty public constructor
    }

    public static MailFragment newInstance(Mails mail) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MAIL, mail);
        MailFragment mailFragment = new MailFragment();
        mailFragment.setArguments(bundle);
        return mailFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mails = (Mails) getArguments().getSerializable(MAIL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_mail, container, false);

        Toolbar myToolbar = (Toolbar) rootView.findViewById(R.id.my_toolbar);
        mainActivity.setSupportActionBar(myToolbar);
        ActionBar actionBar = mainActivity.getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(mails.getMailSubject());
        }


        webView = (WebView) rootView.findViewById(R.id.webView);
        tvSubjectText = (TextView) rootView.findViewById(R.id.tvSubjectText);
        tvFromText = (TextView) rootView.findViewById(R.id.tvFromText);
        tvTime = (TextView) rootView.findViewById(R.id.tvTime);

        tvSubjectText.setText(mails.getMailSubject());
        tvFromText.setText(mails.getMailFrom());
        Date date = new Date(Double.valueOf(mails.getMailTimestamp()).longValue()*1000);
        String dateLocal =  DateFormat.getDateTimeInstance().format(date);
        tvTime.setText(dateLocal);



        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        //settings.setJavaScriptEnabled(true);
        //webView.setWebViewClient(new WebViewClient());
        settings.setDefaultTextEncodingName("utf-8");
        String changedHtml = changedHeaderHtml(mails.getMailHtml());
        webView.loadDataWithBaseURL("fake://not/needed", changedHtml, "text/html", "utf-8", "");
        //webView.loadData(changeFontHtml, "text/html", "UTF-8");

        return rootView;
    }


    public static String changedHeaderHtml(String htmlText) {
        String head = "<head><meta name=\"viewport\" content=\"width=device-width, user-scalable=yes\" charset='UTF-8' /></head>";

        String closedTag = "</body></html>";
        return head + htmlText + closedTag;
    }


//    private int getScale(){
//        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        int width = display.getWidth();
//        Double val = new Double(width)/new Double(PIC_WIDTH);
//        val = val * 100d;
//        return val.intValue();
//    }



    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            mainActivity.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
        MailFragment.this.context = activity;
        if (activity != null) {
            listener = (OnFragmentInteractionListener) activity;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        MailFragment.this.context = context;
        if (context != null) {
            listener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        mainActivity = null;
    }


    private class MyWebViewClient extends WebViewClient{


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return true;
        }
    }

}
