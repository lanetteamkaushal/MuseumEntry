package com.guam.museumentry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.SystemRequirementsChecker;
import com.guam.museumentry.crop.cropwindow.CropImageView;
import com.guam.museumentry.global.AndroidUtilities;
import com.guam.museumentry.global.BuildVars;

import static com.guam.museumentry.global.GlobalApplication.applicationContext;

public class MainActivity extends AppCompatActivity {

    CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cropImageView = (CropImageView) findViewById(R.id.CropImageView);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outWidth = AndroidUtilities.displaySize.x;
        opts.outHeight = AndroidUtilities.displaySize.y - AndroidUtilities.dp(72);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_1, opts);
        bitmap = Bitmap.createScaledBitmap(bitmap, AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y - AndroidUtilities.dp(24), false);
        cropImageView.setImageBitmap(bitmap);
        setupEstimote();
        (findViewById(R.id.btnSavePin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropImageView.getCroppedImage();
            }
        });
    }

    private void setupEstimote() {
        EstimoteSDK.initialize(applicationContext, BuildVars.APP_ID, BuildVars.APP_TOKEN);
        EstimoteSDK.enableDebugLogging(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }
}
