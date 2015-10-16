package com.camoedo.recorder.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.camoedo.recorder.Camera;
import com.camoedo.recorder.R;
import com.camoedo.recorder.activity.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class RecorderService extends Service {

    private static final String TAG = "RecorderService";
    private static final int NOTIFICATION_ID = 1;

    private final IBinder mBinder = new ServiceBinder();

    private final Handler mHandler = new Handler();

    private Camera mCamera;
    private Timer mTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        mCamera = new Camera(this);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        mCamera.takePicture();
                    }
                });
            }
        }, 0, 2000);

        startForeground(NOTIFICATION_ID, getNotification());

        Log.i(TAG, "Service Started.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        stopForeground(true);

        Log.i(TAG, "Service Stopped.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Recorder")
                .setContentText("14 package were sent")
                .setTicker("Recording...");

        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        return builder.build();
    }


    public Camera getCamera() {
        return mCamera;
    }

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class ServiceBinder extends Binder {
        public RecorderService getService() {
            // Return this instance of CameraService so clients can call public methods
            return RecorderService.this;
        }
    }
}