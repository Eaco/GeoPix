package com.example.benja.geopix;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
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
public class PhotoSender extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] params) {
        try {
            String attachmentName = "jpeg";
            String attachmentFileName = "jpeg.jpeg";
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            HttpURLConnection httpUrlConnection;

//            URL url = new URL("http://192.168.42.127:3002/images");
            URL url = new URL("http://geopix-bengineering.rhcloud.com/images");

            httpUrlConnection = (HttpURLConnection) url.openConnection();
            String basicAuth = "Bearer: " + params[3];
            httpUrlConnection.setRequestProperty ("Authorization", basicAuth);

            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.connect();

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("latitude", params[1]);
            jsonParam.put("longitude", params[2]);

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

            JSONObject jsonObj = new JSONObject(response);

            String id = (String) jsonObj.get("_id");

            Log.d("PhotoSender Http Response", response);

            responseStream.close();

            httpUrlConnection.disconnect();

            Log.d("PhotoSender", "doInBackground: Uploading image" + id);
//            url= new URL("http://192.168.42.127:3002/images/" + id);
            url = new URL("http://geopix-bengineering.rhcloud.com/images/" + id);

            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);

            httpUrlConnection.setRequestProperty ("Authorization", basicAuth);
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty(
                    "Content-Type", "multipart/form-data;boundary=" + boundary);


            byte[] imgData = (byte[])params[0];
            Bitmap bm = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);


            OutputStream os = httpUrlConnection.getOutputStream();

            bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            responseStream = new
                    BufferedInputStream(httpUrlConnection.getInputStream());

            responseStreamReader =
                    new BufferedReader(new InputStreamReader(responseStream));

            stringBuilder = new StringBuilder();

            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();

            response = stringBuilder.toString();

            Log.d("Photosender", "doInBackground: " + response
            );

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
