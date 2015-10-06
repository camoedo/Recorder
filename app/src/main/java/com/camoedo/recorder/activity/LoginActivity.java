package com.camoedo.recorder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.camoedo.recorder.R;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.Session;


public class LoginActivity extends AppCompatActivity {

    private DigitsAuthButton phoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneButton = (DigitsAuthButton) findViewById(R.id.phone_button);

        DigitsSession session = Digits.getSessionManager().getActiveSession();

        if (session != null) {
            phoneButton.setVisibility(View.GONE);
            new Thread() {
                public void run() {
                    try {
                        sleep(1000);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
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
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void failure(DigitsException e) {
                    Crashlytics.logException(e);
                }
            });
        }
    }
}
