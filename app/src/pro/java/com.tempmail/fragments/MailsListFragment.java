package com.tempmail.fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tempmail.MainActivity;
import com.tempmail.R;
import com.tempmail.adapters.MailListAdapter;
import com.tempmail.api.ApiClient;
import com.tempmail.api.CancelableCallback;
import com.tempmail.api.models.answers.Mails;
import com.tempmail.utils.Log;
import com.tempmail.utils.OnFragmentInteractionListener;
import com.tempmail.utils.Prefs;
import com.tempmail.utils.Utils;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MailsListFragment extends Fragment {
    private static final String TAG = MailsListFragment.class.getSimpleName();
    private RecyclerView rvMails;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    MainActivity mainActivity;
    Context context;
    OnFragmentInteractionListener listener;
    CancelableCallback<List<Mails>> cancelableCallback;
    TextView tvNoData;
    int mailSize = 0;

    public MailsListFragment() {
        // Required empty public constructor
    }

    public static MailsListFragment newInstance() {
        return new MailsListFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_mails_list, container, false);
        rvMails = (RecyclerView) rootView.findViewById(R.id.rvMails);
        tvNoData= (TextView) rootView.findViewById(R.id.tvNoData);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        rvMails.setLayoutManager(mLayoutManager);


        Toolbar myToolbar = (Toolbar) rootView.findViewById(R.id.my_toolbar);
        mainActivity.setSupportActionBar(myToolbar);
        ActionBar actionBar = mainActivity.getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.emails_list);
        }


        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getEmails(mainActivity.getGeneratedEmail());
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if(cancelableCallback!=null)
            cancelableCallback.cancel();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            mainActivity.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void getEmails(final String emailAddress) {
        mainActivity.showProgressDialog();
        ApiClient.getClient(getString(R.string.login), getString(R.string.password)).getEmails(Utils.getMd5(emailAddress), cancelableCallback = new CancelableCallback<>(new Callback<List<Mails>>() {

            @Override
            public void success(List<Mails> mails, Response response) {
                if(mails.size()>0) {
//                    if(mails.size()!=mailSize) {
//                        mailSize = mails.size();
                        mAdapter = new MailListAdapter(context, mails);
                        rvMails.setAdapter(mAdapter);
                        tvNoData.setVisibility(View.GONE);
                        rvMails.setVisibility(View.VISIBLE);
                    //}
                }else{
                    tvNoData.setVisibility(View.VISIBLE);
                    rvMails.setVisibility(View.GONE);
                }
                if (mainActivity != null)
                    mainActivity.dismissProgressDialog();
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                if (mainActivity != null)
                    mainActivity.dismissProgressDialog();
//                Response response = error.getResponse();
//                if (response != null && response.getStatus() == 404) {
//
//                }
                //tv_email.setText(emailAddress);
            }
        }));
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
        MailsListFragment.this.context = activity;
        if (activity != null) {
            listener = (OnFragmentInteractionListener) activity;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        MailsListFragment.this.context = context;
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
}
