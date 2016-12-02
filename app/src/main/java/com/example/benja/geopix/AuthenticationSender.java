package com.example.benja.geopix;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by benja on 11/22/2016.
 */
public class    AuthenticationSender extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] params) {
        try {
            HttpURLConnection httpUrlConnection = null;

//            URL url = new URL("http://192.168.42.127:3002/users");
            URL url = new URL("http://geopix-bengineering.rhcloud.com/users");

            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
//            httpUrlConnection.setRequestProperty("Host", "android.schoolportal.gr");
            httpUrlConnection.connect();

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("name", params[0]);
            Log.d("Auth", "Name: " + params[0]);
            jsonParam.put("email", params[1]);
            Log.d("Auth", "email: " + params[1]);
            jsonParam.put("token", params[2]);
            Log.d("Auth", "token: " + params[2]);

            OutputStreamWriter out = new OutputStreamWriter(httpUrlConnection.getOutputStream());
            out.write(jsonParam.toString());
            out.flush();
            out.close();

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

            Log.d("AuthenticationSender Http Response", response);

            responseStream.close();

            httpUrlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
