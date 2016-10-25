package com.guam.museumentry;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.connection.scanner.ConfigurableDevicesScanner;
import com.guam.museumentry.adapter.BeaconListAdapter;
import com.guam.museumentry.beans.Beacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BeaconListActivity extends AppCompatActivity {

    public static final String EXTRA_SCAN_RESULT_ITEM_DEVICE = "com.Ldeveloperl1985GmailCom.MuseumEntry4Nk.SCAN_RESULT_ITEM_DEVICE";
    public static final Integer RSSI_THRESHOLD = -50;
    RecyclerView rlBeacons;
    ArrayList<Beacon> beacons;
    Beacon beaconItem;
    BeaconListAdapter beaconListAdapter;
    private ConfigurableDevicesScanner devicesScanner;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_list);
        rlBeacons = (RecyclerView) findViewById(R.id.rlBeacons);
        devicesScanner = new ConfigurableDevicesScanner(this);
        beacons = new ArrayList<>();
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            devicesScanner.scanForDevices(new ConfigurableDevicesScanner.ScannerCallback() {
                @Override
                public void onDevicesFound(List<ConfigurableDevicesScanner.ScanResultItem> list) {
//                    devicesCountTextView.setText(getString(R.string.detected_devices) + ": " + String.valueOf(list.size()));
                    if (!list.isEmpty()) {
                        for (int i = 0; i < list.size(); i++) {
                            ConfigurableDevicesScanner.ScanResultItem item = list.get(i);
                            if (item.rssi > RSSI_THRESHOLD) {
                                beaconItem = new Beacon();
                                beaconItem.setBeaconId(item.device.deviceId.toString());
                                beaconItem.setBeaconFreq(item.rssi);
                                beacons.add(beaconItem);
                            }
                        }
                        if (beacons.size() > 0) {
                            devicesScanner.stopScanning();
                            Collections.sort(beacons, new Comparator<Beacon>() {
                                @Override
                                public int compare(Beacon beacon, Beacon t1) {
                                    if (beacon.getBeaconFreq() > t1.getBeaconFreq()) {
                                        return 1;
                                    } else if (beacon.getBeaconFreq() < t1.getBeaconFreq()) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            });
                            beaconListAdapter = new BeaconListAdapter(BeaconListActivity.this, beacons);
                            rlBeacons.setLayoutManager(new LinearLayoutManager(BeaconListActivity.this, LinearLayoutManager.VERTICAL, false));
                            rlBeacons.setAdapter(beaconListAdapter);
                        }

                    }
                }
            });
        }
    }
}
