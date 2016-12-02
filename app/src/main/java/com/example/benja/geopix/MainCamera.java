package com.example.benja.geopix;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainCamera extends AppCompatActivity {

    LocationManager locationManager;
    PixLocationListener locationListener;
    private double lat;
    private double lon;
    private boolean selfieCam = false;
    private String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Intent intent = getIntent();
        idToken = intent.getStringExtra("signintoken");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, new PixLocationListener());
        //Remove title bar and initialize view

        setContentView(R.layout.activity_camera);

        Context context = getApplicationContext();

        getPermissions(context);

        Camera backCamera = getCameraInstance(true);
        Camera frontCamera = getCameraInstance(false);
        CameraPreview backPreView = new CameraPreview(this, backCamera, false);
        CameraPreview frontPreView = new CameraPreview(this, frontCamera, true
        );
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(backPreView);

        locationListener = new PixLocationListener();

        setupCaptureButton(backCamera, frontCamera);
        setupMapButton(context);
        setupSelfieButton(preview, backPreView, frontPreView);
    }

    private void setupSelfieButton(final FrameLayout preview, final CameraPreview back, final CameraPreview front) {
        FloatingActionButton selfie = (FloatingActionButton)findViewById(R.id.fab_selfie);
        selfie.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!selfieCam){
                            preview.removeView(back);
                            preview.addView(front);
                        }
                        else {
                            preview.removeView(front);
                            preview.addView(back);
                        }
                        selfieCam = !selfieCam;
                    }
                });
    }

    private void getPermissions(Context context) {
        if (!checkCameraHardware(context)) {
            System.exit(0);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int permissionCheckWrite = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheckWrite != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void setupCaptureButton(final Camera backCamera, final Camera frontCamera) {
        FloatingActionButton captureButton = (FloatingActionButton) findViewById(R.id.fab_pic);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        try {
                            Log.d("sertupCaptureButton", "Taking Photo");
                            if(!selfieCam) {
                                backCamera.takePicture(null, null, mPicture);
                            }
                            else {
                                frontCamera.takePicture(null, null, mPicture);
                            }
                        } catch (Exception e) {
                            Log.d("something", "else");
                        }
                    }
                });
    }

    private void setupMapButton(final Context mContext) {
        FloatingActionButton mapButton = (FloatingActionButton) findViewById(R.id.fab_map);
        mapButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        try {
                            Intent startIntent = new Intent(mContext, MapsActivity.class);
                            startIntent.putExtra("idToken", idToken);
                            mContext.startActivity(startIntent);
                        } catch (Exception e) {
                            Log.d("something", "else");
                        }
                    }
                });
    }

    private void postPic() {

    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("cam", "OnPictureTaken has been called ");

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d("cam", "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                //TODO make this async
                getLocation();
                uploadPicture(data);
                Log.d("LocationManager", "lat: " + lat + ", lon: " + lon);
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.flush();
//                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("cam", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("cam", "Error accessing file: " + e.getMessage());
            }
            camera.stopPreview();
            camera.startPreview();
        }
    };

    private void uploadPicture(byte[] pixels) throws IOException {
        Object[] params = {pixels, lat, lon, idToken};
        new PhotoSender().execute(params);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Criteria criteria = new Criteria();
        String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
        locationManager.requestLocationUpdates(bestProvider, 1000, 0, locationListener);

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        } 
    }


    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(boolean backFacing) {
        Camera c = null;

        try {
            if (backFacing){
                c = Camera.open(); // attempt to get a Camera instance
            }
            else {
                int id = findFrontFacingCamera();
                c = Camera.open(id);
            }
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d("Main", "CAMERA GONE BAE");
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * This should help us find the front facing camera
     */
    private static int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d("Camera", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

//        File mediaStorageDir = new File(Environment.DIRECTORY_PICTURES, "MyCameraApp");

        final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsolutePath(), "/MyCameraApp");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_DANK_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }
}


