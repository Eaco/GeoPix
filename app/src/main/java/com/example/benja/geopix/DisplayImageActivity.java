package com.example.benja.geopix;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;

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

public class DisplayImageActivity extends Activity {

    DisplayImageActivity self;
    private String idToken;
    static RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_display_image);
        self = this;

        ImageView displayImageView = (ImageView)findViewById(R.id.display_activity_image);

        ratingBar = (RatingBar) findViewById(R.id.rate_image);
        Intent intent = getIntent();

        final String id = intent.getStringExtra("imageId");

        Uri imageUri = intent.getParcelableExtra("imageUri");
        idToken = intent.getStringExtra("idToken");
        new ImageUriLoader(displayImageView).loadFromUri(imageUri);
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.display_activity_layout);
        layout.setOnClickListener(new CloseOnClickListener());

        new RatingGetter().execute(new Object[]{id, idToken});

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Object[] params = {rating, id, idToken};
                new RatingSender().execute(params);
            }
        });
    }

    private class CloseOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            self.finish();
        }
    }

    public static class RatingGetter extends AsyncTask<Object, Void, Integer> {
        @Override
        protected Integer doInBackground(Object... params) {
            try {
                String boundary = "*****";

                HttpURLConnection httpUrlConnection = null;

//                URL url = new URL("http://192.168.42.127:3002/ratings/" + params[0];
                URL url = new URL("http://geopix-bengineering.rhcloud.com/ratings/" + params[0]);

                httpUrlConnection = (HttpURLConnection) url.openConnection();
                String basicAuth = "Bearer: " + params[1];
                httpUrlConnection.setRequestProperty ("Authorization", basicAuth);

                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Content-Type", "application/json");
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
                Log.d("rating getter", response);

                JSONObject rating = new JSONObject(response);

                responseStream.close();
                httpUrlConnection.disconnect();

                Log.d("GetRating Response", response);
                Integer t = rating.getInt("rating");

                return t;
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
        protected void onPostExecute(Integer rating) {
            if(ratingBar != null && rating != null){
                ratingBar.setRating(rating.floatValue());
            }
        }
    }


}
