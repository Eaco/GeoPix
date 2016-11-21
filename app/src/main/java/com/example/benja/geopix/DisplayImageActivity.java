package com.example.benja.geopix;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;

public class DisplayImageActivity extends Activity {

    DisplayImageActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_display_image);
        self = this;

        ImageView displayImageView = (ImageView)findViewById(R.id.display_activity_image);

        RatingBar ratingBar = (RatingBar) findViewById(R.id.rate_image);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Object[] params = {};
                new RatingSender().execute(params);
            }
        });

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
