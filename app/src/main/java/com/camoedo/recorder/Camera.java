package com.camoedo.recorder;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;

import com.camoedo.recorder.view.CameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class Camera implements android.hardware.Camera.PictureCallback {

    private static final String TAG = "Camera";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Context mContext;

    private int mCameraId;
    private android.hardware.Camera mCamera;

    private CameraView mCameraView;
    private FrameLayout mPreviewHolder;

    public Camera(Context context) {
        mContext = context;

        try {
            mCameraId = findFrontFacingCamera();
            initCamera();
        } catch (Exception e) {
            Log.d(TAG, "Cannot instantiate camera");
        }
    }

    public Camera(Context context, FrameLayout previewHolder) {
        this(context);
        mPreviewHolder = previewHolder;
        mPreviewHolder.addView(mCameraView);
    }

    @Override
    public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    public void takePicture() {
        mCamera.takePicture(null, null, this);
    }

    public void release() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        if (mCameraView != null) {
            mCameraView = null;
        }
    }

    public void switchCamera() {
        if (mCameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK){
            mCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mPreviewHolder.removeView(mCameraView);
        initCamera();
        mPreviewHolder.addView(mCameraView);
    }

    public CameraView getCameraView() {
        return mCameraView;
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(i, info);
            if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.v(TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void initCamera() {
        release();
        mCamera = android.hardware.Camera.open(mCameraId);
//            android.hardware.Camera.Parameters params = mCamera.getParameters();
//            params.setSceneMode(android.hardware.Camera.Parameters.SCENE_MODE_PARTY);
//            params.setColorEffect(android.hardware.Camera.Parameters.EFFECT_MONO);
//            params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_AUTO);
//            params.setPictureFormat(ImageFormat.JPEG);
//            params.setJpegQuality(100);
//            mCamera.setParameters(params);

        mCameraView = new CameraView(mContext, mCamera);
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        // TODO: Change to Environment.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "RecorderService");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
