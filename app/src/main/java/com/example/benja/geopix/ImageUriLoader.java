package com.example.benja.geopix;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Marko on 2016-10-16.
 */
public class ImageUriLoader {
    ImageView mImageView;

    public ImageUriLoader(ImageView _imageView){
        mImageView = _imageView;
    }

    public void loadFromUri(Uri loadFrom){
        Log.d("MARKOTAG", "Loading from Uri now!");
        if(loadFrom == null){
            return;
        }
        Log.d("MARKOTAG", "Uri is: " + loadFrom.toString());
        if(loadFrom.getScheme() != null && loadFrom.getScheme().equals("android.resource")){
            mImageView.setImageURI(loadFrom);
        } else {
            new LoadBitmapTask().execute(loadFrom);
        }
    }

    private class LoadBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Uri... params){
            if(params.length < 1 ) {
                return null;
            } else {
                Bitmap returnBitmap = null;
                Uri uri = params[0];
                InputStream inputStream = null;
                BufferedInputStream bufferedInputStream = null;
                try{
                    URLConnection urlConnection = new URL(uri.toString()).openConnection();
                    urlConnection.connect();
                    inputStream = urlConnection.getInputStream();
                    bufferedInputStream = new BufferedInputStream(inputStream, 8192);
                    returnBitmap = BitmapFactory.decodeStream(bufferedInputStream);
                } catch (IOException e){

                } finally {
                    if(bufferedInputStream != null){
                        try{
                            bufferedInputStream.close();
                        } catch(IOException e){}
                    }
                    if(inputStream != null){
                        try{
                            inputStream.close();
                        } catch(IOException e){}
                    }
                }
                return returnBitmap;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result){
            mImageView.setImageBitmap(result);
        }
    }
}
