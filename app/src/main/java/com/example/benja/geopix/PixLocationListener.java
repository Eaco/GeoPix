package com.example.benja.geopix;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by benja on 11/19/2016.
 */
public class PixLocationListener implements LocationListener {
    double lat, lon = 0;

    @Override
    public void onLocationChanged(Location location) {
        //Hey, a non null location! Sweet!

        //open the map:
        lat = location.getLatitude();
        lon = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
