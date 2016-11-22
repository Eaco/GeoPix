package com.example.benja.geopix;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by benja on 11/20/2016.
 */
public class RatingSender extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] params) {
        try {
            HttpURLConnection urlConn;
            DataOutputStream printout;
            DataInputStream input;
            URL url = new URL("http://192.168.42.127:3002/ratings/101010101010101010101010");
//            URL url = new URL("http://geopix-bengineering.rhcloud.com/ratings/101010101010101010101010");

            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setUseCaches(false);
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Connection", "Keep-Alive");
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.setRequestProperty("Host", "android.schoolportal.gr");
            urlConn.connect();

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("rating", params[0]);

            OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
            out.write(jsonParam.toString());
            out.flush();
            out.close();

            InputStream responseStream = new
                    BufferedInputStream(urlConn.getInputStream());

            BufferedReader responseStreamReader =
                    new BufferedReader(new InputStreamReader(responseStream));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();

            String response = stringBuilder.toString();

            Log.d("PhotoSender Http Response", response);

            responseStream.close();
            urlConn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
