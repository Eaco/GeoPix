package com.example.benja.geopix;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class DisplayImageActivity extends Activity {

    DisplayImageActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_display_image);
        self = this;

        ImageView displayImageView = (ImageView)findViewById(R.id.display_activity_image);

        Intent intent = getIntent();
        Uri imageUri = intent.getParcelableExtra("ImageUri");
        new ImageUriLoader(displayImageView).loadFromUri(imageUri);

        RelativeLayout layout = (RelativeLayout)findViewById(R.id.display_activity_layout);
        layout.setOnClickListener(new CloseOnClickListener());
    }

    private class CloseOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            self.finish();
        }
    }
}
