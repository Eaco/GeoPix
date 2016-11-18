package com.example.benja.geopix;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.support.v4.app.Fragment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainCamera extends AppCompatActivity {

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        setContentView(R.layout.activity_camera);
        Context context = getApplicationContext();
        if (!checkCameraHardware(context)){
            System.exit(0);
        }
        File file = new File(context.getFilesDir(), "testfile");

        String string = "test";
        try {
            FileOutputStream outputStream = openFileOutput(file.getName(), Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int permissionCheckWrite = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheckWrite != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} ,2);
        }

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA} ,1);
        }


        Camera mCamera = getCameraInstance();

        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        setupCaptureButton(mCamera);
        setupMapButton(context);
        //if it makes it this far the device has a camera


    }

    private void setupCaptureButton(final Camera mCamera){
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    try {
                        mCamera.takePicture(null, null, mPicture);
                        mCamera.stopPreview();
                        mCamera.startPreview();
                    }catch(Exception e){
                        Log.d("something","else");
                    }
                }
            });
    }

    private void setupMapButton(final Context mContext){
        Button mapButton = (Button) findViewById(R.id.button_map);
        mapButton.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get an image from the camera
                try {
                    Intent startIntent = new Intent(mContext, MapsActivity.class);
                    mContext.startActivity(startIntent);
                }catch(Exception e){
                    Log.d("something","else");
                }
            }
        });
    }

    private void postPic(){

    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("cam", "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                //TODO make this async
//                uploadPicture(data);
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("cam", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("cam", "Error accessing file: " + e.getMessage());
            }
        }
    };

    private void uploadPicture(byte[] pixels) throws IOException {
        String attachmentName = "jpeg";
        String attachmentFileName = "jpeg.jpeg";
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        HttpURLConnection httpUrlConnection = null;
        URL url = new URL("http://example.com/server.cgi");
        httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setUseCaches(false);
        httpUrlConnection.setDoOutput(true);

        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
        httpUrlConnection.setRequestProperty(
                "Content-Type", "multipart/form-data;boundary=" + boundary);

        DataOutputStream request = new DataOutputStream(
                httpUrlConnection.getOutputStream());

        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                attachmentName + "\";filename=\"" +
                attachmentFileName + "\"" + crlf);
        request.writeBytes(crlf);
        request.write(pixels);


        request.writeBytes(crlf);
        request.writeBytes(twoHyphens + boundary +
                twoHyphens + crlf);

        request.flush();
        request.close();
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;


        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d("Main", "CAMERA GONE BAE");
        }
        return c; // returns null if camera is unavailable
    }



    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_DANK_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}


