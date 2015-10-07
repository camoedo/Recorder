package com.camoedo.recorder;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;


public class App extends Application {

    private static App singleton;

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                BuildConfig.CONSUMER_KEY, BuildConfig.CONSUMER_SECRET);
        Fabric.with(this, new Crashlytics(), new TwitterCore(authConfig), new Digits());
    }

    public static App getInstance() {
        return singleton;
    }

    public static DigitsSession getSession() {
        return Digits.getSessionManager().getActiveSession();
    }

    public static void clearSession() {
        Digits.getSessionManager().clearActiveSession();
    }

    public static boolean isLoggedIn() {
        return getSession() != null;
    }

    public static void logException(Throwable throwable) {
        Crashlytics.logException(throwable);
    }
}
