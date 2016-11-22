package com.example.benja.geopix;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.benja.geopix.DisplayImageActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Context mContext;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        new PhotoGetter().execute(new Object[]{30, 40});
        //httpget from server (GET /images)
        //map JSON objects to points on map, no big deal yall


        mMap.setOnMarkerClickListener(new PhotoMarkerClickListener());
        // Add a marker in Sydney and move the camera
        LatLng slc = new LatLng(43.471991, -80.544769);
        LatLng e2 = new LatLng(43.471423, -80.538965);
        LatLng pac = new LatLng(43.471532, -80.546883);

        makeMarker(slc, "squirtle");
        makeMarker(e2, "charmander");
        makeMarker(pac, "bulbasaur");
    }

    private void makeMarker(LatLng latLng, String title){
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private class PhotoMarkerClickListener implements GoogleMap.OnMarkerClickListener {
        public boolean onMarkerClick(Marker marker){
            Log.d("MARKOTAG", "SOMEONE CLICKED ON A MARKER! THE MESSAGE IS: " + marker.getTitle());
            Intent startIntent = new Intent(mContext, DisplayImageActivity.class);
            startIntent.putExtra("ImageUri", Uri.parse("android.resource://" + mContext.getPackageName() + "/drawable/" + marker.getTitle()));
            mContext.startActivity(startIntent);
            return true;
        }
    }


    public class PhotoGetter extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                String boundary = "*****";

                HttpURLConnection httpUrlConnection = null;

                URL url = new URL("http://192.168.42.127:3002/images?latitude=" + params[0] + "&longitude=" + params[1]);
//            URL url = new URL("http://geopix-bengineering.rhcloud.com/ratings/101010101010101010101010");

                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");
                httpUrlConnection.setRequestProperty("Host", "android.schoolportal.gr");
                httpUrlConnection.connect();

                InputStream responseStream = new
                        BufferedInputStream(httpUrlConnection.getInputStream());

                BufferedReader responseStreamReader =
                        new BufferedReader(new InputStreamReader(responseStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                String response = stringBuilder.toString();

                JSONObject jsonObj = new JSONObject(response);

                Log.d("PhotoSender Http Response", response);

                responseStream.close();

                httpUrlConnection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}