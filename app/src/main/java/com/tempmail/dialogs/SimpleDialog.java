package com.tempmail.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.tempmail.MainActivity;
import com.tempmail.R;


public class SimpleDialog extends DialogFragment {
    public static final String DIALOG_TITLE = "dialog_title";
    public static final String DIALOG_MESSAGE = "dialog_message";
    String mTitle, mMessage;
    MainActivity mainActivity;


    public SimpleDialog() {
        // Required empty public constructor
    }

    public static SimpleDialog newInstance(String title, String message) {
        SimpleDialog fragment = new SimpleDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_TITLE, title);
        bundle.putString(DIALOG_MESSAGE, message);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTitle = bundle.getString(DIALOG_TITLE);
            mMessage = bundle.getString(DIALOG_MESSAGE);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setPositiveButton(getString(R.string.try_again),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                retryGetData();
                                dismiss();
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                (getActivity()).finish();
                                dismiss();
                            }
                        }
                )
                .create();
    }


    public void retryGetData() {
        if (mainActivity != null) {
            if (mainActivity.getGeneratedEmail().isEmpty())
                mainActivity.getDomainsList(true);
            else
                mainActivity.getDomainsList(false);
        }

    }
}
