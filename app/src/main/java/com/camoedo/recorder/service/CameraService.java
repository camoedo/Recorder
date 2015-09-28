package com.camoedo.recorder.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.camoedo.recorder.Camera;
import com.camoedo.recorder.MainActivity;
import com.camoedo.recorder.R;

import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends AbstractService {

    private static final String TAG = "CameraService";
    private static final int NOTIFICATION_ID = 1;

    private final Handler mHandler = new Handler();

    private Camera mCamera;
    private Timer mTimer;

    @Override
    public void onStartService() {

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
    public void onStopService() {

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

    @Override
    public void onReceiveMessage(Message msg) {

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
}