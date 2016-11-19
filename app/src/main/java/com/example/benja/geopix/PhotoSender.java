package com.example.benja.geopix;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

            HttpURLConnection httpUrlConnection = null;
            URL url = new URL("http://192.168.42.127:8080/images");
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);

            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty(
                    "Content-Type", "multipart/form-data;boundary=" + boundary);

            byte[] imgData = (byte[])params[0];
            Bitmap bm = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);

            OutputStream os = httpUrlConnection.getOutputStream();

            bm.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            /*
            DataOutputStream request = new DataOutputStream(
                    httpUrlConnection.getOutputStream());

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" +
                    attachmentName + "\";filename=\"" +
                    attachmentFileName + "\"" + crlf);
            request.writeBytes(crlf);
            request.write((byte[]) params[0]);


            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary +
                    twoHyphens + crlf);

            Log.d("Request being sent", request.toString());
            request.flush();
            request.close();
            */
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

            Log.d("PhotoSender Http Response", response);

            responseStream.close();
            httpUrlConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
