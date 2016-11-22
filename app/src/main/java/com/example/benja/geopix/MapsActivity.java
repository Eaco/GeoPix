package com.example.benja.geopix;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.net.HttpURLConnection;

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
}
