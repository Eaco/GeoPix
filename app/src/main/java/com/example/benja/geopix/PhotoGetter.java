package com.example.benja.geopix;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by benja on 11/18/2016.
 */
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
