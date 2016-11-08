package com.guam.museumentry;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.SystemRequirementsChecker;
import com.guam.museumentry.beans.SingleLocation;
import com.guam.museumentry.custom.DImageView;
import com.guam.museumentry.global.AndroidUtilities;
import com.guam.museumentry.global.BuildVars;
import com.guam.museumentry.global.DatabaseUtils;
import com.guam.museumentry.global.Storage;
import com.guam.museumentry.v4Style.DragFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static com.guam.museumentry.global.GlobalApplication.applicationContext;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    DragFrameLayout cropImageView;
    Drawable redTint, blueTint;
    Button btnScan;
    Button btnDeleteAll;
    boolean doubleBackToExitPressedOnce = false;
    ArrayList<SingleLocation> apiLocations = new ArrayList<>();
    AtomicInteger counter = new AtomicInteger(0);
    private Realm realm;
    private int VersionCount = 0;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cropImageView = (DragFrameLayout) findViewById(R.id.CropImageView);
        cropImageView.setDragFrameController(new DragFrameLayout.DragFrameLayoutController() {

            @Override
            public void onDragDrop(View floatingShape, boolean captured) {
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
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floor_1_new, opts);
        bitmap = Bitmap.createScaledBitmap(bitmap, AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y - AndroidUtilities.dp(24), false);
        Log.d(TAG, "onCreate Bitmap Size: " + bitmap.getHeight() + ":Width:" + bitmap.getWidth());
        setupEstimote();

        realm = DatabaseUtils.getInstance().realm;
        (findViewById(R.id.btnSavePin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmQuery<SingleLocation> realmQuery = realm.where(SingleLocation.class);
                        if (cropImageView.returnIdOfLastView() > -1) {
                            int idToQuery = cropImageView.returnIdOfLastView();
                            realmQuery.equalTo("vIndex", idToQuery);
                            Log.d(TAG, "execute CALLED: " + idToQuery);
                            SingleLocation singleLocation;
                            singleLocation = realmQuery.findFirst();
                            if (singleLocation == null)
                                singleLocation = realm.createObject(SingleLocation.class, idToQuery);
                            cropImageView.requestForFillData(singleLocation);
                            realm.copyToRealm(singleLocation);
                            Intent intent = new Intent(MainActivity.this, BeaconListActivity.class);
                            intent.putExtra("id_to_pass", singleLocation.getvIndex());
                            startActivityForResult(intent, 4646);
                        }
                    }
                });
            }
        });
        (findViewById(R.id.btnDeleteAll)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                realm.executeTransaction(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        realm.delete(SingleLocation.class);
//                    }
//                });
//                cropImageView.removeAllViews();
                RealmQuery<SingleLocation> realmQuery = realm.where(SingleLocation.class);
                if (cropImageView.returnIdOfLastView() > -1) {
                    int idToQuery = cropImageView.returnIdOfLastView();
                    cropImageView.removeLastView();
                    realmQuery.equalTo("vIndex", idToQuery);
                    Log.d(TAG, "execute CALLED: " + idToQuery);
                    SingleLocation singleLocation = realmQuery.findFirst();
                    if (singleLocation != null && singleLocation.isSaved()) {
                        deleteSingleBeacon(singleLocation.getAssignedIndex());
                    }
                } else {
                    Log.w(TAG, "onClick: No LAST VIEW TO DELETE FOUND");
                }
            }
        });
        (findViewById(R.id.btnAddPin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DImageView iv_sticker = new DImageView(MainActivity.this);
                iv_sticker.setImageDrawable(redTint);
                iv_sticker.setScaleType(ImageView.ScaleType.FIT_END);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(AndroidUtilities.dp(32), AndroidUtilities.dp(32));
                iv_sticker.setId(counter.incrementAndGet());
                cropImageView.addView(iv_sticker, layoutParams);
                cropImageView.addDragView(iv_sticker);//,layoutParams);
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
        cropImageView.setDrawableForUnSavedLocation(redTint);
        cropImageView.setDrawableForSavedLocation(blueTint);

        cropImageView.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        requestQueue = Volley.newRequestQueue(this);
        String checkUrl = BuildVars.API_POINT + "?check_data=true";
        JsonObjectRequest checkRequest = new JsonObjectRequest(Request.Method.GET, checkUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    VersionCount = response.getJSONArray("museum_data").getJSONObject(0).optInt("count", -1);
                    if (Storage.getInteger(getApplicationContext(), "versionCount") == VersionCount) {
                        readyToAddFromDb();
                    } else {
                        Storage.putIneger(getApplicationContext(), "versionCount", VersionCount);
                        setUpData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    readyToAddFromDb();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                readyToAddFromDb();
            }
        });
        requestQueue.add(checkRequest);
    }

    private void setUpData() {
        String url = BuildVars.API_POINT + "?beacon_data=true";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("museum_data")) {
                    try {
                        JSONArray museumData = response.getJSONArray("museum_data");
                        SingleLocation singleLocation;
                        for (int i = 0; i < museumData.length(); i++) {
                            JSONObject jsonObject = museumData.getJSONObject(i);
                            singleLocation = new SingleLocation();//  realm.createObject(SingleLocation.class);
                            singleLocation.setSaved(true);
                            singleLocation.setAssignedIndex(Integer.parseInt(jsonObject.optString("Id")));
                            singleLocation.setUserName(jsonObject.optString("beaconName"));
                            singleLocation.setBeaconID(jsonObject.optString("beaconId"));
                            singleLocation.setRightPercentage((float) jsonObject.optDouble("xPercentage"));
                            singleLocation.setBottomPercentage((float) jsonObject.optDouble("yPercentage"));
                            singleLocation.setFloorNumber(jsonObject.optInt("FloorName"));
                            singleLocation.setvIndex(counter.incrementAndGet());
                            apiLocations.add(singleLocation);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (apiLocations.size() > 0) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                SingleLocation dbobject;
                                //delete all current record and update new record
                                realm.delete(SingleLocation.class);
                                //End
                                for (int i = 0; i < apiLocations.size(); i++) {
                                    SingleLocation singleLocation = apiLocations.get(i);
                                    RealmQuery<SingleLocation> singleLocationRealmQuery = realm.where(SingleLocation.class);
                                    singleLocationRealmQuery.equalTo("assignedIndex", singleLocation.getAssignedIndex());
                                    dbobject = singleLocationRealmQuery.findFirst();
                                    if (dbobject != null) {
                                        dbobject.setSaved(true);
                                        dbobject.setAssignedIndex(singleLocation.getAssignedIndex());
                                        dbobject.setUserName(singleLocation.getUserName());
                                        dbobject.setBeaconID(singleLocation.getBeaconID());
                                        dbobject.setRightPercentage(singleLocation.getRightPercentage());
                                        dbobject.setBottomPercentage(singleLocation.getBottomPercentage());
                                        dbobject.setFloorNumber(singleLocation.getFloorNumber());
                                        realm.insertOrUpdate(dbobject);
                                    } else {
                                        realm.insert(singleLocation);
                                    }
                                }
                            }
                        });
                    }
                    readyToAddFromDb();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                readyToAddFromDb();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void deleteSingleBeacon(final int id) {
        String deleteUrl = BuildVars.API_POINT + "?delete_beacon=true&id=" + id;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, deleteUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "onResponse: " + response.toString());
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<SingleLocation> result = realm.where(SingleLocation.class).equalTo("assignedIndex", id).findAll();
                        if (result.deleteAllFromRealm()) {
                            Log.d(TAG, "execute: Delete SuccessFully");
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void readyToAddFromDb() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<SingleLocation> singleLocationRealmQuery = realm.where(SingleLocation.class);
                RealmResults<SingleLocation> allPins = singleLocationRealmQuery.findAll();
                cropImageView.addViewDirectly(allPins);
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
        if (!AndroidUtilities.isInternet()) {
            displayError(getString(R.string.error_internet_connection));
        }
    }

    private void displayError(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton(
                R.string.alert_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(i);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
