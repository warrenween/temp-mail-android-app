package com.tempmail.fragments;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tempmail.MainActivity;
import com.tempmail.R;
import com.tempmail.dialogs.ChangeEmailDialog;
import com.tempmail.utils.Log;
import com.tempmail.utils.OnFragmentInteractionListener;
import com.tempmail.utils.Prefs;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = MainFragment.class.getSimpleName();
    Button btn_check_mail, btn_copy, btn_change;

    SharedPreferences sp;
    TextView tv_email;
    MainActivity mainActivity;
    Context context;
    OnFragmentInteractionListener listener;


    public MainFragment() {
        // Required empty public constructor
    }


    public static MainFragment newInstance() {
        return new MainFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initView(view);
        sp = context.getSharedPreferences(Prefs.PREF_APP, Context.MODE_PRIVATE);

        if (mainActivity.getGeneratedEmail().isEmpty()) {
            mainActivity.getDomainsList(true);
            tv_email.setText(R.string.your_email_will_be_here);
        } else {
            mainActivity.getDomainsList(false);
            tv_email.setText(mainActivity.getGeneratedEmail());
            mainActivity.getEmails(mainActivity.getGeneratedEmail(), false);
        }


        int savedCount = sp.getInt(Prefs.PREF_LAST_EMAIL_COUNT_SAVED, 0);
        btn_check_mail.setText(String.valueOf(savedCount));

        makeButtonsWidth();


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    public void initView(View rootView) {
        btn_copy = (Button) rootView.findViewById(R.id.btn_copy);
        btn_change = (Button) rootView.findViewById(R.id.btn_change);
        btn_check_mail = (Button) rootView.findViewById(R.id.bnt_check_mail);
        tv_email = (TextView) rootView.findViewById(R.id.tv_email);
//        ViewTreeObserver vto = tv_email.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//            @Override
//            public void onGlobalLayout() {
//                ViewTreeObserver obs = tv_email.getViewTreeObserver();
//                obs.removeGlobalOnLayoutListener(this);
//                if (tv_email.getLineCount() > 3) {
//                    int lineEndIndex = tv_email.getLayout().getLineEnd(2);
//                    String text = tv_email.getText().subSequence(0, lineEndIndex - 3) + "...";
//                    snippet.setText(text);
//                }
//            }
//        });

        btn_copy.setOnClickListener(this);
        btn_change.setOnClickListener(this);
        btn_check_mail.setOnClickListener(this);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_copy:
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("email", mainActivity.getGeneratedEmail());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, R.string.email_copied, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_change:
                ChangeEmailDialog changeEmailDialog = ChangeEmailDialog.newInstance(mainActivity.getmDomains());
                changeEmailDialog.show(mainActivity.getSupportFragmentManager(), ChangeEmailDialog.class.getSimpleName());
//                btn_check_mail.setText("0");
//                generateEmail();
//                getEmails(generatedEmail);
                break;
            case R.id.bnt_check_mail:
                if(mainActivity!=null)
                mainActivity.makeEmailsRead();
                btn_check_mail.setText("0");
                listener.navigateToFragment(MailsListFragment.newInstance(), true);
                break;
        }
    }


    public void setEmailCountText(String count){
        btn_check_mail.setText(count);
    }


    public void setEmailAddressText(String count){
        tv_email.setText(count);
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
        MainFragment.this.context = activity;
        if (activity != null) {
            listener = (OnFragmentInteractionListener) activity;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        MainFragment.this.context = context;
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
