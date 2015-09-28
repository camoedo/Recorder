package com.camoedo.recorder.service;

import com.camoedo.recorder.MainActivity;
import com.camoedo.recorder.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CameraService extends AbstractService implements SurfaceHolder.Callback {

    private static final String TAG = "CameraService";
    private static final int NOTIFICATION_ID = 1;

    private int mCameraId;
    private Camera mCamera;

    Camera.PictureCallback cameraPictureCallback = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            if (bitmap != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                String date = dateFormat.format(new Date());
                String path = Environment.getExternalStorageDirectory() + File.separator + date + ".jpg";
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private final Handler handler = new Handler();

    Timer timer;
    TimerTask timerTask = new TimerTask() {
        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    mCamera.takePicture(null, null, cameraPictureCallback);
                }
            });
        }
    };

    @Override
    public void onStartService() {
        SurfaceView surface = new SurfaceView(getApplicationContext());
        surface.getHolder().addCallback(this);

        try {
            mCameraId = findFrontFacingCamera();
            mCamera = Camera.open(mCameraId);
            Camera.Parameters params = mCamera.getParameters();
            params.setSceneMode(Camera.Parameters.SCENE_MODE_PARTY);
            params.setPictureFormat(ImageFormat.JPEG);
            params.setJpegQuality(100);
            mCamera.setParameters(params);

            timer = new Timer();
            timer.schedule(timerTask, 0, 1000);
        } catch (Exception e) {
            Log.d(TAG, "Cannot instantiate camera");
        }

        startForeground(NOTIFICATION_ID, getNotification());
        Log.i(TAG, "Service Started.");

    }

    @Override
    public void onStopService() {
        if (timer != null) {
            timer.cancel();
            timer = null;
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.setDisplayOrientation(180);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null)
            mCamera.stopPreview();
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.v(TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
}
