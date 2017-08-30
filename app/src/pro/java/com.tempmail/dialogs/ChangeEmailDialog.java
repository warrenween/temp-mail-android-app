package com.tempmail.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tempmail.MainActivity;
import com.tempmail.R;
import com.tempmail.utils.EmailValidator;
import com.tempmail.utils.Prefs;

import java.util.ArrayList;
import java.util.List;


public class ChangeEmailDialog extends DialogFragment {
    public static final String DOMAINS_LIST= "domains_list";
    MainActivity mainActivity;
    Button btnSave;
    EditText edtLogin;
    Spinner spDomain;
    List<String> domains;


    public ChangeEmailDialog() {
        // Required empty public constructor
    }

    public static ChangeEmailDialog newInstance(List<String> domains) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(DOMAINS_LIST, (ArrayList<String>) domains);
        ChangeEmailDialog changeEmailDialog= new ChangeEmailDialog();
        changeEmailDialog.setArguments(bundle);
        return changeEmailDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        domains = bundle.getStringArrayList(DOMAINS_LIST);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View rootView = inflater.inflate(R.layout.fragment_change_email, container, false);
        btnSave = (Button) rootView.findViewById(R.id.btnSave);
        edtLogin = (EditText) rootView.findViewById(R.id.edtLogin);
        spDomain = (Spinner) rootView.findViewById(R.id.spDomain);
        setSpinnerAdapter();
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = edtLogin.getText().toString();
                String newEmail = login + spDomain.getSelectedItem();
                if(login.length()==0)
                    Toast.makeText(mainActivity, R.string.wrong_login, Toast.LENGTH_LONG).show();
                else if(EmailValidator.isEmailValid(newEmail)) {
                    if(mainActivity!=null) {
                        SharedPreferences sp = mainActivity.getSharedPreferences(Prefs.PREF_APP, Context.MODE_PRIVATE);
                        sp.edit().putString(Prefs.PREF_SAVED_LOGIN, login).apply();
                        sp.edit().putString(Prefs.PREF_SAVED_DOMAIN, (String) spDomain.getSelectedItem()).apply();
                        mainActivity.onEmailChanged(newEmail);
                    }
                    dismiss();
                }else {
                    Toast.makeText(mainActivity, R.string.wrong_email, Toast.LENGTH_LONG).show();
                }
            }
        });
        return rootView;
    }


    private void setSpinnerAdapter(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainActivity,
                android.R.layout.simple_spinner_item, domains);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDomain.setAdapter(adapter);
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

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return new AlertDialog.Builder(getActivity())
//                .setTitle("Change email")
//                .setView(R.layout.fragment_change_email)
//                .setPositiveButton(getString(R.string.try_again),
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//
//                                dismiss();
//                            }
//                        }
//                )
////                .setNegativeButton(android.R.string.cancel,
////                        new DialogInterface.OnClickListener() {
////                            public void onClick(DialogInterface dialog, int whichButton) {
////                                dismiss();
////                            }
////                        }
////                )
//                .create();
//    }
}
