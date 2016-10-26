package com.guam.museumentry;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.estimote.sdk.connection.DeviceConnection;
import com.estimote.sdk.connection.DeviceConnectionCallback;
import com.estimote.sdk.connection.DeviceConnectionProvider;
import com.estimote.sdk.connection.exceptions.DeviceConnectionException;
import com.estimote.sdk.connection.scanner.ConfigurableDevice;
import com.estimote.sdk.connection.settings.SettingCallback;
import com.estimote.sdk.connection.settings.SettingsEditor;
import com.guam.museumentry.beans.Beacon;
import com.guam.museumentry.global.DatabaseUtils;

import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmQuery;

public class BeaconDetailEntry extends AppCompatActivity implements View.OnClickListener {

    public static final HashMap<String, Integer> tagsMajorsMapping = new HashMap<String, Integer>() {{
        put("Ground Floor", 0);
        put("First Floor", 1);
        put("Second Floor", 2);
    }};
    private static final String TAG = "BeaconDetailEntry";
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
    Spinner spFloor;
    private ConfigurableDevice configurableDevice;
    private DeviceConnection connection;
    private DeviceConnectionProvider connectionProvider;
    private ProgressDialog progressDialog;

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
        ArrayAdapter<String> adapterTags = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                tagsMajorsMapping.keySet().toArray(new String[tagsMajorsMapping.keySet().size()]));
        adapterTags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFloor.setAdapter(adapterTags);
        tvStatus.setText(getResources().getString(R.string.disconnect));
        ivStatus.setImageLevel(LEVEL_DISCONNECTED);
        btnSave.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_detail_entry);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bindViews();
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
        connectionProvider = new DeviceConnectionProvider(this);
        connectToDevice();
    }

    private void setData(Beacon beacon) {
        tvBeaconID.setText(beacon.getBeaconId());
        tvBeaconColor.setText(beacon.getBeaconColor());
        tvName.setText(beacon.getBeaconName());
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
                            updateStatus(true);
                        }

                        @Override
                        public void onDisconnected() {
                            updateStatus(false);
                        }

                        @Override
                        public void onConnectionFailed(DeviceConnectionException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    });
                }
            });
        }
    }

    private void updateStatus(boolean isConnected) {
        if (isConnected) {
            tvStatus.setText(getResources().getString(R.string.connect));
            ivStatus.setImageLevel(LEVEL_CONNECTED);
        } else {
            tvStatus.setText(getResources().getString(R.string.disconnect));
            ivStatus.setImageLevel(LEVEL_DISCONNECTED);
        }
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
            writeSettings();
        }
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
        edit.set(connection.settings.beacon.major(), tagsMajorsMapping.get(spFloor.getSelectedItem()));
        progressDialog.setTitle(R.string.writing_settings);
        progressDialog.setMessage(getString(R.string.please_wait));
        edit.commit(new SettingCallback() {
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
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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

    @Override
    protected void onResume() {
        super.onResume();
        connectToDevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connection != null && connection.isConnected())
            connection.close();
        try {
            connectionProvider.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        if (view == btnSave) {
            saveAction();
        }
    }
}
