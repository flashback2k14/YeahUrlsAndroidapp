package com.yeahdev.yeahurls.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import com.yeahdev.yeahurls.R;
import com.yeahdev.yeahurls.interfaces.ICommunication;
import com.yeahdev.yeahurls.model.User;
import com.yeahdev.yeahurls.model.UserCreds;
import com.yeahdev.yeahurls.util.Utilities;

public class LoginFragment extends Fragment {
    private ICommunication passUserToMainActivity;
    private EditText etEmailAddress;
    private EditText etPassword;

    public LoginFragment() {}
    public static LoginFragment newInstance() { return new LoginFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        etEmailAddress = (EditText) v.findViewById(R.id.edit_text_email);
        etPassword = (EditText) v.findViewById(R.id.edit_text_password);

        Button btnLogin = (Button) v.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Firebase ref = new Firebase("https://yeah-url-extension.firebaseio.com/");
                    ref.authWithPassword(etEmailAddress.getText().toString(), etPassword.getText().toString(),
                            new Firebase.AuthResultHandler() {
                                @Override
                                public void onAuthenticated(AuthData authData) {
                                    UserCreds userCreds = new UserCreds();
                                    userCreds.setUserId(authData.getUid());
                                    userCreds.setExpireDate(authData.getExpires());

                                    User user = new User();
                                    user.setEmailAddress(etEmailAddress.getText().toString());
                                    user.setProvider(authData.getProvider());
                                    user.setProfileImage(authData.getProviderData().get("profileImageURL").toString());

                                    etEmailAddress.setText("");
                                    etPassword.setText("");

                                    passUserToMainActivity.passUserFromFirebase(userCreds, user);
                                }
                                @Override
                                public void onAuthenticationError(FirebaseError firebaseError) {
                                    Utilities.buildSnackbar(getActivity(), "Error Code:" + firebaseError.getCode() + ", Msg: " + firebaseError.getMessage());
                                }
                            }
                    );
                } catch (Exception e) {
                    Utilities.buildSnackbar(getActivity(), "authWithPassword failed! Error: " + e.getMessage());
                }
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            passUserToMainActivity = (ICommunication) getActivity();
        } catch (ClassCastException e) {
            Utilities.buildSnackbar(getActivity(), getActivity().toString() + " must implement ICommunication!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        passUserToMainActivity = null;
    }
}
