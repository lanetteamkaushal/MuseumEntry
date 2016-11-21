package com.guam.museumentry;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.sdk.connection.DeviceConnection;
import com.estimote.sdk.connection.DeviceConnectionCallback;
import com.estimote.sdk.connection.DeviceConnectionProvider;
import com.estimote.sdk.connection.exceptions.DeviceConnectionException;
import com.estimote.sdk.connection.exceptions.TimeoutOperationException;
import com.estimote.sdk.connection.scanner.ConfigurableDevice;
import com.estimote.sdk.connection.settings.CallbackHandler;
import com.estimote.sdk.connection.settings.SettingCallback;
import com.estimote.sdk.connection.settings.SettingsEditor;
import com.guam.museumentry.beans.Beacon;
import com.guam.museumentry.beans.Power;
import com.guam.museumentry.beans.SingleLocation;
import com.guam.museumentry.global.BuildVars;
import com.guam.museumentry.global.DatabaseUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmQuery;

public class BeaconDetailEntry extends AppCompatActivity implements View.OnClickListener {

    public static final ArrayList<Integer> tagsFloorsIds = new ArrayList<Integer>(3) {{
        add(1);
        add(2);
        add(3);
    }};
    private static final String TAG = "BeaconDetailEntry";
    public ArrayList<String> tagsFloors = new ArrayList<String>(3) {{
        add("Ground Floor");
        add("First Floor");
        add("Second Floor");
    }};
    public RelativeLayout content_beacon_detail_entry;
    public RelativeLayout rlEditSection;
    public TextView tvUserNameTitle;
    public EditText etUserName;
    public TextView tvStickerNo;
    public EditText etStickerNo;
    public Button btnSave;
    public TextView tvBeaconID;
    public TextView tvColorTitle;
    public TextView tvBeaconColor;
    public TextView tvNameTitle;
    public TextView tvName;
    public TextView tvStatus;
    ImageView ivStatus;
    int LEVEL_DISCONNECTED = 1;
    int LEVEL_CONNECTED = 2;
    Realm realm;
    String beaconID;
    int point_index;
    Spinner spFloor;
    String rightPercent = "", bottomPercent = "";
    ArrayList<com.guam.museumentry.beans.Power> powerArrayList = new ArrayList<>();
    SparseIntArray relationPower = new SparseIntArray();
    TextView tvRange;
    SharedPreferences preferenceManager;
    int isLocationEntry = 0;
    //    SeekBar sbPower;
    private ConfigurableDevice configurableDevice;
    private DeviceConnection connection;
    private DeviceConnectionProvider connectionProvider;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> adapterTags;
    private SingleLocation location = null;
    private CallbackHandler handler;
    private RequestQueue queue;
    private int doubleIt = -4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_detail_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        bindViews();
        queue = Volley.newRequestQueue(BeaconDetailEntry.this);
        realm = DatabaseUtils.getInstance().realm;
        Intent intent = getIntent();
        configurableDevice = intent.getParcelableExtra(BeaconListActivity.EXTRA_SCAN_RESULT_ITEM_DEVICE);
        beaconID = intent.getStringExtra(BeaconListActivity.EXTRA_BEACON_ID);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<Beacon> query = realm.where(Beacon.class);
                query.equalTo("beaconId", beaconID);
                Beacon beacon = query.findFirst();
                setData(beacon);
            }
        });
        point_index = intent.getIntExtra(BeaconListActivity.EXTRA_POINT_ID, -1);
        if (point_index < 0) {
            Log.e(TAG, "onCreate: POINTER ID NOT FOUND");
            setResult(RESULT_CANCELED);
            finish();
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<SingleLocation> locationRealmQuery = realm.where(SingleLocation.class);
                locationRealmQuery.equalTo("vIndex", point_index);
                location = locationRealmQuery.findFirst();
                if (location != null) {
                    if (location.isSaved()) {
                        int floorNo = location.getFloorNumber();
                        if (tagsFloorsIds.contains(floorNo) && floorNo > 0) {
                            if (tagsFloorsIds.size() > (floorNo - 1)) {
                                spFloor.setSelection((floorNo - 1), true);
                            }
                            etUserName.setText(location.getUserName());
                            etStickerNo.setText(location.getBeaconID());
                        }
                    }
                    rightPercent = String.valueOf(location.getRightPercentage());
                    bottomPercent = String.valueOf(location.getBottomPercentage());
                } else {
                    Log.e(TAG, "execute: No POINTER FOUND TO SAVE");
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
        connectionProvider = new DeviceConnectionProvider(this);
        connectToDevice();
    }

    private void bindViews() {
        tvBeaconID = (TextView) findViewById(R.id.tvBeaconID);
        tvColorTitle = (TextView) findViewById(R.id.tvColorTitle);
        tvBeaconColor = (TextView) findViewById(R.id.tvBeaconColor);
        tvNameTitle = (TextView) findViewById(R.id.tvNameTitle);
        tvName = (TextView) findViewById(R.id.tvName);
        content_beacon_detail_entry = (RelativeLayout) findViewById(R.id.content_beacon_detail_entry);
        rlEditSection = (RelativeLayout) findViewById(R.id.rlEditSection);
        tvUserNameTitle = (TextView) findViewById(R.id.tvUserNameTitle);
        etUserName = (EditText) findViewById(R.id.etUserName);
        tvStickerNo = (TextView) findViewById(R.id.tvStickerNo);
        etStickerNo = (EditText) findViewById(R.id.evStickerNo);
        btnSave = (Button) findViewById(R.id.btnSave);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        ivStatus = (ImageView) findViewById(R.id.ivStatus);
        spFloor = (Spinner) findViewById(R.id.spFloor);
        adapterTags = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                tagsFloors);
        adapterTags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFloor.setAdapter(adapterTags);
        tvStatus.setText(getResources().getString(R.string.disconnect));
        ivStatus.setImageLevel(LEVEL_DISCONNECTED);
        btnSave.setOnClickListener(this);
        preparePowerData();
    }

    private void setData(Beacon beacon) {
        tvBeaconID.setText(beacon.getBeaconId());
        tvBeaconColor.setText(beacon.getBeaconColor());
        tvName.setText(beacon.getBeaconName());
        findViewById(R.id.tvEstimateTitle).setVisibility(View.GONE);
        findViewById(R.id.tvDistance).setVisibility(View.GONE);
        if (beacon.getBeaconPower() != 200) {
            if (relationPower.indexOfKey(beacon.getBeaconPower()) > -1) {
                int position = relationPower.get(beacon.getBeaconPower());
//                sbPower.setProgress(position);
                Power singlePower = powerArrayList.get(position);
                tvRange.setText(String.format(Locale.getDefault(), "~%1$2.1fm/%2$2.1fft", singlePower.rangeMeter, singlePower.rangeFt));
            }
        }
    }

    private void connectToDevice() {
        if (connection == null || !connection.isConnected()) {
            connectionProvider.connectToService(new DeviceConnectionProvider.ConnectionProviderCallback() {
                @Override
                public void onConnectedToService() {
                    connection = connectionProvider.getConnection(configurableDevice);
                    connection.connect(new DeviceConnectionCallback() {
                        @Override
                        public void onConnected() {
                            connection.settings.beacon.transmitPower().get(new SettingCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer integer) {
                                    Log.d(TAG, "Connect Device onSuccess() called with: integer = [" + integer + "]");
                                    if (relationPower.indexOfKey(integer) > -1) {
                                        int position = relationPower.get(integer);
//                                        sbPower.setProgress(position);
                                        Power singlePower = powerArrayList.get(position);
                                        tvRange.setText(String.format(Locale.getDefault(), "~%1$2.1fm/%2$2.1fft", singlePower.rangeMeter, singlePower.rangeFt));
                                    }
                                }

                                @Override
                                public void onFailure(DeviceConnectionException e) {

                                }
                            });
                            updateStatus(true);
                        }

                        @Override
                        public void onDisconnected() {
                            updateStatus(false);
                        }

                        @Override
                        public void onConnectionFailed(DeviceConnectionException e) {
                            if (e instanceof TimeoutOperationException) {
                                displayError(getString(R.string.error_timeout), true);
                            } else if (e instanceof DeviceConnectionException) {
                                displayError(getString(R.string.error_device_disconnected), true);
                            }
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
            });
        }
    }

    private void preparePowerData() {
        com.guam.museumentry.beans.Power power = new com.guam.museumentry.beans.Power();
        power.setPowerMode(com.guam.museumentry.beans.Power.WEAK);
        power.power = -40;
        power.rangeMeter = 1.5f;
        power.rangeFt = 5;
        relationPower.put(-40, 0);
        powerArrayList.add(power);
        power = new com.guam.museumentry.beans.Power();
        power.setPowerMode(com.guam.museumentry.beans.Power.WEAK);
        power.power = -20;
        power.rangeMeter = 3.5f;
        power.rangeFt = 12;
        relationPower.put(-20, 1);
        powerArrayList.add(power);
        power = new com.guam.museumentry.beans.Power();
        power.setPowerMode(com.guam.museumentry.beans.Power.WEAK);
        power.power = -16;
        power.rangeMeter = 7f;
        power.rangeFt = 22;
        relationPower.put(-16, 2);
        powerArrayList.add(power);
        power = new com.guam.museumentry.beans.Power();
        power.setPowerMode(com.guam.museumentry.beans.Power.WEAK);
        power.power = -12;
        power.rangeMeter = 15f;
        power.rangeFt = 50;
        relationPower.put(-12, 3);
        powerArrayList.add(power);
        power = new com.guam.museumentry.beans.Power();
        power.setPowerMode(com.guam.museumentry.beans.Power.WEAK);
        power.power = -8;
        power.rangeMeter = 30f;
        power.rangeFt = 100;
        relationPower.put(-8, 4);
        powerArrayList.add(power);
        power = new com.guam.museumentry.beans.Power();
        power.setPowerMode(com.guam.museumentry.beans.Power.WEAK);
        power.power = -4;
        power.rangeMeter = 40f;
        power.rangeFt = 130;
        relationPower.put(-4, 5);
        powerArrayList.add(power);
        power = new com.guam.museumentry.beans.Power();
        power.setPowerMode(com.guam.museumentry.beans.Power.WEAK);
        power.power = 0;
        power.rangeMeter = 50f;
        power.rangeFt = 160;
        relationPower.put(-0, 6);
        powerArrayList.add(power);
        power = new com.guam.museumentry.beans.Power();
        power.setPowerMode(com.guam.museumentry.beans.Power.WEAK);
        power.power = 4;
        power.rangeMeter = 70f;
        power.rangeFt = 230;
        relationPower.put(4, 7);
        powerArrayList.add(power);
        tvRange = (TextView) findViewById(R.id.tvRange);
        String powerFromPref = preferenceManager.getString("power_preference", "-4");
        doubleIt = Integer.parseInt(powerFromPref);
        if (relationPower.indexOfKey(doubleIt) > -1) {
            tvRange.setText(String.format(Locale.getDefault()
                    , "~%1$2.1fm/%2$2.1fft"
                    , powerArrayList.get(relationPower.get(doubleIt)).rangeMeter
                    , powerArrayList.get(relationPower.get(doubleIt)).rangeFt));
        } else {
            doubleIt = -4;
            tvRange.setText(String.format(Locale.getDefault()
                    , "~%1$2.1fm/%2$2.1fft"
                    , 40
                    , 130));
        }
        isLocationEntry = preferenceManager.getBoolean("entry_for", false) ? 1 : 0;

//        sbPower = (SeekBar) findViewById(R.id.sbPower);
//        sbPower.setMax(7);
//        sbPower.incrementProgressBy(1);
//        sbPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                if (i < powerArrayList.size()) {
//                    Power singlePower = powerArrayList.get(i);
//                    tvRange.setText(String.format(Locale.getDefault(), "~%1$2.1fm/%2$2.1fft", singlePower.rangeMeter, singlePower.rangeFt));
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
    }

    private void updateStatus(boolean isConnected) {
        if (isConnected) {
//            tvStatus.setText(getResources().getString(R.string.connect));
            ivStatus.setImageLevel(LEVEL_CONNECTED);
        } else {
//            tvStatus.setText(getResources().getString(R.string.disconnect));
            ivStatus.setImageLevel(LEVEL_DISCONNECTED);
        }
    }

    private void displayError(String msg, final boolean b) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton(
                R.string.alert_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (b) finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connection != null && connection.isConnected())
            connection.close();
        if (handler != null) {
            handler.drop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectToDevice();
    }

    @Override
    public void onClick(View view) {
        if (view == btnSave) {
            if (dataIsOkay())
                saveAction();
        }
    }

    private boolean dataIsOkay() {
        etUserName.setError(null);
        etStickerNo.setError(null);
        if (TextUtils.isEmpty(etUserName.getText().toString())) {
            etUserName.setError("necessary");
            return false;
        }
        if (TextUtils.isEmpty(etStickerNo.getText().toString())) {
            etStickerNo.setError("necessary");
            return false;
        }
        if (!TextUtils.isEmpty(etStickerNo.getText().toString())) {
            int minorID = Integer.parseInt(etStickerNo.getText().toString());
            if (minorID > 65536 || minorID < 1) {
                etStickerNo.setError("Sticker Number must between 1 - 655536");
                return false;
            }
        }
        return true;
    }

    private void saveAction() {
        if (!connection.isConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.wait_until_beacon_connected);
            builder.setCancelable(true);
            builder.setPositiveButton(
                    R.string.alert_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            progressDialog = ProgressDialog.show(this, ".", ".");
            progressDialog.setCanceledOnTouchOutside(true);
            checkForTrue();
        }
    }

    private void checkForTrue() {
        progressDialog.setTitle(R.string.writing_settings);
        progressDialog.setMessage(getString(R.string.please_wait));
        String urlToCall = String.format(BuildVars.API_POINT_OLD + "?check_insert=true&beaconId=%1$s&floorName=%2$s", etStickerNo.getText().toString(), String.valueOf(tagsFloorsIds.get(spFloor.getSelectedItemPosition())));
        if (location != null && location.isSaved() && location.getAssignedIndex() > 0) {
            urlToCall = String.format(BuildVars.API_POINT_OLD + "?check_update=true&beaconId=%1$s&floorName=%2$s&id=%3$s",
                    etStickerNo.getText().toString(), String.valueOf(tagsFloorsIds.get(spFloor.getSelectedItemPosition())), location.getAssignedIndex());
        }
        Log.d(TAG, "checkForTrue: URL TO CHECK :" + urlToCall);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlToCall, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                if (response.optString("status", "false").equalsIgnoreCase("true")) {
                    writeSettings();
                } else {
                    displayError(getString(R.string.error_sticker_exist), false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                error.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);
    }

    /*
    Prepare set of settings based on the default or your custom UI.
    Here is also a place to fetch apropriate settings for your device from your custom CMS
    or to save those that were be saved in the onSuccess block before calling displaySuccess.
    */
    private void writeSettings() {
        SettingsEditor edit = connection.edit();
        edit.set(connection.settings.beacon.enable(), true);
        edit.set(connection.settings.beacon.minor(), Integer.valueOf(etStickerNo.getText().toString()));
        edit.set(connection.settings.beacon.major(), tagsFloorsIds.get(spFloor.getSelectedItemPosition()));
        edit.set(connection.settings.beacon.transmitPower(), doubleIt);
        progressDialog.setTitle(R.string.writing_settings);
        progressDialog.setMessage(getString(R.string.please_wait));
        handler = edit.commit(new SettingCallback() {
            @Override
            public void onSuccess(Object o) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displaySuccess();
                    }
                });
            }

            @Override
            public void onFailure(DeviceConnectionException e) {
                final DeviceConnectionException eF = e;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displayError(eF);
                    }
                });
            }
        });
    }

    private void displaySuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.configuration_succeeded);
        builder.setCancelable(true);
        builder.setPositiveButton(
                R.string.configure_next_beacon,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
//                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        String url = String.format(BuildVars.API_POINT_OLD + "?add_beacon=true" +
                        "&beaconName=%1$s&beaconId=%2$s&xPercentage=%3$s&yPercentage=%4$s&floorName=%5$s&isLocation=%6$s",
                Uri.encode(etUserName.getText().toString()), etStickerNo.getText().toString(), rightPercent, bottomPercent
                , String.valueOf(tagsFloorsIds.get(spFloor.getSelectedItemPosition()))
                , isLocationEntry);
        if (location != null && location.isSaved() && location.getAssignedIndex() > 0) {
            url = String.format(BuildVars.API_POINT_OLD + "?update_beacon=true"
                            + "&id=%1$s"
                            + "&beaconName=%2$s&beaconId=%3$s&xPercentage=%4$s&yPercentage=%5$s&floorName=%6$s",
                    location.getAssignedIndex(),
                    Uri.encode(etUserName.getText().toString()), etStickerNo.getText().toString(), rightPercent, bottomPercent, String.valueOf(tagsFloorsIds.get(spFloor.getSelectedItemPosition())));
        }
        Log.d(TAG, "displaySuccess URL TO CALL: " + url);
        StringRequest myReq = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int assignedId = -1;
                            if (jsonObject.has("id")) {
                                assignedId = jsonObject.optInt("id", -1);
                                final int finalAssignedId = assignedId;
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        location.setSaved(true);
                                        location.setFloorNumber(tagsFloorsIds.get(spFloor.getSelectedItemPosition()));
                                        location.setUserName(etUserName.getText().toString());
                                        location.setBeaconID(etStickerNo.getText().toString());
                                        if (finalAssignedId > -1) {
                                            location.setAssignedIndex(finalAssignedId);
                                        }
                                        realm.copyToRealm(location);
                                    }
                                });
                            }
                            connectionProvider.destroy();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setResult(RESULT_OK);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse() called with: error = [" + error.getMessage() + "]");
                    }
                });
        queue.add(myReq);
    }

    private void displayError(DeviceConnectionException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(e.getLocalizedMessage());
        builder.setCancelable(true);
        builder.setPositiveButton(
                R.string.alert_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
