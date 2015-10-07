package com.camoedo.recorder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.camoedo.recorder.App;
import com.camoedo.recorder.R;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DigitsAuthButton phoneButton = (DigitsAuthButton) findViewById(R.id.phone_button);

        if (App.isLoggedIn()) {
            phoneButton.setVisibility(View.GONE);
            new Thread() {
                public void run() {
                    try {
                        sleep(1000);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } catch (InterruptedException e) {
                        App.logException(e);
                    } finally {
                        finish();
                    }
                }
            }.start();
        } else {
            phoneButton.setVisibility(View.VISIBLE);
            phoneButton.setCallback(new AuthCallback() {
                @Override
                public void success(DigitsSession digitsSession, String phoneNumber) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void failure(DigitsException e) {
                    App.logException(e);
                }
            });
        }
    }
}
