package com.guam.museumentry;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.SystemRequirementsChecker;
import com.guam.museumentry.beans.SingleLocation;
import com.guam.museumentry.custom.DImageView;
import com.guam.museumentry.global.AndroidUtilities;
import com.guam.museumentry.global.BuildVars;
import com.guam.museumentry.v4Style.DragFrameLayout;

import java.io.FileNotFoundException;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.guam.museumentry.global.GlobalApplication.applicationContext;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    DragFrameLayout cropImageView;
    Drawable redTint, blueTint;
    private Realm realm;

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
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        try {
            Realm.migrateRealm(config, new Migration());
        } catch (FileNotFoundException ignored) {
            // If the Realm file doesn't exist, just ignore.
        }
        realm = Realm.getInstance(config);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(SingleLocation.class);
            }
        });
        (findViewById(R.id.btnSavePin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                cropImageView.getCroppedImage();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        SingleLocation singleLocation = realm.createObject(SingleLocation.class);
                        cropImageView.requestForFillData(singleLocation);
                        Intent intent = new Intent(MainActivity.this, BeaconListActivity.class);
                        startActivityForResult(intent, 4646);
                    }
                });
            }
        });
        Drawable normalDrawable = getResources().getDrawable(R.drawable.ic_action_location);
        redTint = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(redTint, Color.RED);
        redTint = DrawableCompat.unwrap(redTint);
        Drawable normalDrawable1 = getResources().getDrawable(R.drawable.ic_action_location);
        blueTint = DrawableCompat.wrap(normalDrawable1);
        DrawableCompat.setTint(blueTint, Color.BLUE);
        blueTint = DrawableCompat.unwrap(blueTint);
        cropImageView.setDrawableForSavedLocation(blueTint);
        (findViewById(R.id.btnAddPin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DImageView iv_sticker = new DImageView(MainActivity.this);
                iv_sticker.setImageDrawable(redTint);
                iv_sticker.setScaleType(ImageView.ScaleType.FIT_END);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(AndroidUtilities.dp(32), AndroidUtilities.dp(32));
                cropImageView.addView(iv_sticker, layoutParams);
                cropImageView.addDragView(iv_sticker);//,layoutParams);
            }
        });
        cropImageView.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
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
