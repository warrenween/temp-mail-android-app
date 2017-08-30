package com.tempmail.dialogs;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.tempmail.MainActivity;
import com.tempmail.R;
import com.tempmail.utils.Prefs;
import com.tempmail.utils.Utils;
import com.zendesk.sdk.feedback.BaseZendeskFeedbackConfiguration;
import com.zendesk.sdk.feedback.ui.ContactZendeskActivity;
import com.zendesk.sdk.model.access.AnonymousIdentity;
import com.zendesk.sdk.model.access.Identity;
import com.zendesk.sdk.network.impl.ZendeskConfig;

import java.util.Arrays;
import java.util.List;


public class HelpImproveDialog extends DialogFragment implements View.OnClickListener {
    public static final String TAG = HelpImproveDialog.class.getSimpleName();
    Context mContext;
    View rootView;
    TextView tvSure;
    TextView tvNotNow;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String additionalData;


    public HelpImproveDialog() {
        // Required empty public constructor
    }


    public static HelpImproveDialog newInstance() {
        return new HelpImproveDialog();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_help_improve_dialog, container, false);
        tvSure = (TextView) rootView.findViewById(R.id.sure);
        tvNotNow = (TextView) rootView.findViewById(R.id.not_now);
        tvSure.setOnClickListener(this);
        tvNotNow.setOnClickListener(this);
        sharedPreferences = getActivity().getSharedPreferences(Prefs.PREF_APP, Context.MODE_PRIVATE);

        return rootView;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onClick(View v) {
        editor = sharedPreferences.edit();
        if (v.getId() == R.id.not_now) {
            int newCounter = sharedPreferences.getInt(Prefs.COUNTER_KEY, 0);
            //Log.e(TAG, "new Counter " + newCounter);
            if (mContext instanceof MainActivity) {
                //Log.e(TAG, "Context instanceof MainActivity ");
                editor.putInt(Prefs.COUNTER_KEY, ++newCounter);
                if (newCounter > 2)
                    editor.putBoolean(Prefs.NEED_SHOW_KEY, false);
            }
        } else {
            editor.putBoolean(Prefs.NEED_SHOW_KEY, false);
            startZendeskTicket();
        }
        editor.apply();
        dismissAllowingStateLoss();
    }


    public void startZendeskTicket() {
        AnonymousIdentity.Builder builder = new AnonymousIdentity.Builder();
        Identity anonymousIdentity = builder.build();


        additionalData = "Android " + "Version: " + Build.VERSION.RELEASE + ";" + " Device model:" + Build.MODEL + ";";
        additionalData += " App Version: " + Utils.getAppVersion(getActivity()) + ";";

        ZendeskConfig.INSTANCE.setIdentity(anonymousIdentity);
        //ZendeskConfig.INSTANCE.setContactConfiguration(new SampleFeedbackConfiguration());

        ContactZendeskActivity.startActivity(getActivity(), new SampleFeedbackConfiguration());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Log.e(TAG, "onAttach");
        mContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //Log.e(TAG, "onDetach");
        mContext = null;
    }

    // Configures the Contact Zendesk component
    private class SampleFeedbackConfiguration extends BaseZendeskFeedbackConfiguration {

        @Override
        public String getRequestSubject() {
            return "Feedback from TempMail Android app";
        }

        @Override
        public String getAdditionalInfo() {
            return additionalData;
        }


        @Override
        public List<String> getTags() {
            return Arrays.asList("Android", "TempMail", Build.VERSION.RELEASE, Build.MODEL);
        }
    }


}
