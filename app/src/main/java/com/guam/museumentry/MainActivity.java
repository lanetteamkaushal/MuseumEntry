package com.guam.museumentry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.SystemRequirementsChecker;
import com.guam.museumentry.Stickers.StickerImageView;
import com.guam.museumentry.global.AndroidUtilities;
import com.guam.museumentry.global.BuildVars;

import static com.guam.museumentry.global.GlobalApplication.applicationContext;

public class MainActivity extends AppCompatActivity {

    FrameLayout cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cropImageView = (FrameLayout) findViewById(R.id.CropImageView);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outWidth = AndroidUtilities.displaySize.x;
        opts.outHeight = AndroidUtilities.displaySize.y - AndroidUtilities.dp(72);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_1, opts);
        bitmap = Bitmap.createScaledBitmap(bitmap, AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y - AndroidUtilities.dp(24), false);
//        cropImageView.setImageBitmap(bitmap);
        setupEstimote();
        (findViewById(R.id.btnSavePin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                cropImageView.getCroppedImage();
            }
        });
        (findViewById(R.id.btnAddPin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StickerImageView iv_sticker = new StickerImageView(MainActivity.this);
                iv_sticker.setImageDrawable(getResources().getDrawable(R.drawable.c10));
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(AndroidUtilities.dp(32),AndroidUtilities.dp(32));
                cropImageView.addView(iv_sticker);//,layoutParams);
            }
        });
        cropImageView.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        StickerImageView iv_sticker = new StickerImageView(MainActivity.this);
        iv_sticker.setImageDrawable(getResources().getDrawable(R.drawable.c10));
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(AndroidUtilities.dp(32),AndroidUtilities.dp(32));
        cropImageView.addView(iv_sticker);//,layoutParams);
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
