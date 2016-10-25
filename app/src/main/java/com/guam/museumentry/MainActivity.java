package com.guam.museumentry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.SystemRequirementsChecker;
import com.guam.museumentry.global.AndroidUtilities;
import com.guam.museumentry.global.BuildVars;
import com.guam.museumentry.v4Style.DragFrameLayout;

import static com.guam.museumentry.global.GlobalApplication.applicationContext;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    DragFrameLayout cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cropImageView = (DragFrameLayout) findViewById(R.id.CropImageView);
        cropImageView.setDragFrameController(new DragFrameLayout.DragFrameLayoutController() {

            @Override
            public void onDragDrop(View floatingShape, boolean captured) {
                /* Animate the translation of the {@link View}. Note that the translation
                 is being modified, not the elevation. */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    floatingShape.animate()
                            .translationZ(captured ? 50 : 0)
                            .setDuration(100);
                }
                Log.d(TAG, captured ? "Drag" : "Drop");
            }
        });
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outWidth = AndroidUtilities.displaySize.x;
        opts.outHeight = AndroidUtilities.displaySize.y - AndroidUtilities.dp(72);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_1, opts);
        bitmap = Bitmap.createScaledBitmap(bitmap, AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y - AndroidUtilities.dp(24), false);
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
                ImageView iv_sticker = new ImageView(MainActivity.this);
                iv_sticker.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_location));
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(AndroidUtilities.dp(32), AndroidUtilities.dp(32));
                cropImageView.addView(iv_sticker, layoutParams);
                cropImageView.addDragView(iv_sticker);//,layoutParams);
            }
        });
        cropImageView.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        ImageView iv_sticker = new ImageView(MainActivity.this);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_location);
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, AndroidUtilities.dp(32), AndroidUtilities.dp(32), false);
        iv_sticker.setImageDrawable(new BitmapDrawable(getResources(), bitmap1));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(AndroidUtilities.dp(32), AndroidUtilities.dp(32));
        cropImageView.addView(iv_sticker, layoutParams);
        cropImageView.addDragView(iv_sticker);//,layoutParams);
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
