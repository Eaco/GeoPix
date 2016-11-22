package com.example.benja.geopix;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
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

import org.json.JSONArray;
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
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, new PixLocationListener());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        new PhotoGetter().execute(new Object[]{lon, lat});


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
            startIntent.putExtra("ImageUri", Uri.parse("https://geopix-bengineering.rhcloud.com/images/" + marker.getTitle()));
//            startIntent.putExtra("uniqueID", marker.getTitle());
            mContext.startActivity(startIntent);
            return true;
        }
    }


    public class PhotoGetter extends AsyncTask<Object, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Object... params) {
            try {
                String boundary = "*****";

                HttpURLConnection httpUrlConnection = null;

//                URL url = new URL("http://192.168.42.127:3002/images?latitude=" + params[0] + "&longitude=" + params[1]);
                URL url = new URL("http://geopix-bengineering.rhcloud.com/images?latitude=" + params[0] + "&longitude=" + params[1]);
//            URL url = new URL("http://geopix-bengineering.rhcloud.com/ratings/101010101010101010101010");

                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");
//                httpUrlConnection.setRequestProperty("Host", "android.schoolportal.gr");
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

                JSONArray jsonArray = new JSONArray(response);

                responseStream.close();
                httpUrlConnection.disconnect();

                Log.d("PhotoSender Http Response", response);

                return jsonArray;
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

        @Override
        protected void onPostExecute(JSONArray images) {
            try {
                for (int i = 0; i < images.length(); i++) {
                    JSONObject j = images.getJSONObject(i);
                    JSONObject loc = j.getJSONObject("location");
                    JSONArray coordArray = (JSONArray) loc.get("coordinates");

                    LatLng coords = new LatLng(Double.parseDouble(coordArray.getString(0)), Double.parseDouble(coordArray.getString(1)));
                    String id = j.getString("_id");
                    makeMarker(coords, id);
                    Log.d("photofetcher", coordArray.get(0) + " " + coordArray.get(1));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}