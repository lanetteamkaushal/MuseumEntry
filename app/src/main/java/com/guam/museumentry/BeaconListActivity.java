package com.guam.museumentry;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;

import com.estimote.sdk.DeviceId;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.estimote.sdk.connection.scanner.ConfigurableDevicesScanner;
import com.guam.museumentry.adapter.BeaconListAdapter;
import com.guam.museumentry.beans.Beacon;
import com.guam.museumentry.global.AndroidUtilities;
import com.guam.museumentry.global.DatabaseUtils;
import com.guam.museumentry.global.NotificationCenter;
import com.guam.museumentry.service.FetchDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class BeaconListActivity extends AppCompatActivity implements NotificationCenter.NotificationCenterDelegate, View.OnClickListener {

    public static final String EXTRA_SCAN_RESULT_ITEM_DEVICE = "com.Ldeveloperl1985GmailCom.MuseumEntry4Nk.SCAN_RESULT_ITEM_DEVICE";
    public static final String EXTRA_BEACON_ID = "com.Ldeveloperl1985GmailCom.MuseumEntry4Nk.BEACON_ID";
    public static final String EXTRA_POINT_ID = "com.Ldeveloperl1985GmailCom.MuseumEntry4Nk.POINT_ID";
    public static final Integer RSSI_THRESHOLD = -55;
    private static final String TAG = "BeaconListActivity";
    public LinearLayoutManager llm;
    RecyclerView rlBeacons;
    ArrayList<Beacon> beacons;
    Beacon beaconItem;
    BeaconListAdapter beaconListAdapter;
    HashMap<String, Integer> queueForFetchDetails = new HashMap<>();
    ProgressBar pbBluetoothSearch;
    Set<String> beaconIds = new HashSet<>();
    int id_to_pass = 0;
    private ConfigurableDevicesScanner devicesScanner;
    private Realm realm;
    private Button btnScan;
    private ObjectAnimator animation;
    private DividerItemDecoration mDividerItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_list);
        rlBeacons = (RecyclerView) findViewById(R.id.rlBeacons);
        devicesScanner = new ConfigurableDevicesScanner(this);
        beacons = new ArrayList<>();
        realm = DatabaseUtils.getInstance().realm;
        pbBluetoothSearch = (ProgressBar) findViewById(R.id.pbBluetoothSearch);
        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setVisibility(View.GONE);
        btnScan.setOnClickListener(this);
        animation = ObjectAnimator.ofInt(pbBluetoothSearch, "progress", 0, 500); // see this max value coming back here, we animale towards that value
        animation.setDuration(5000); //in milliseconds
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
        id_to_pass = getIntent().getIntExtra("id_to_pass", -1);
        if (id_to_pass < 0) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            if (beacons.size() == 0) scanForDevices();
        }
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.beaconDetailUpdate);
    }

    private void scanForDevices() {
        devicesScanner.scanForDevices(new ConfigurableDevicesScanner.ScannerCallback() {

            @Override
            public void onDevicesFound(List<ConfigurableDevicesScanner.ScanResultItem> list) {
//                    devicesCountTextView.setText(getString(R.string.detected_devices) + ": " + String.valueOf(list.size()));
                if (!list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        ConfigurableDevicesScanner.ScanResultItem item = list.get(i);
//                        if (item.rssi > RSSI_THRESHOLD) {
                        if (beaconIds.add(item.device.deviceId.toHexString())) {
                            beaconItem = new Beacon();
                            beaconItem.setBeaconId(AndroidUtilities.escapeBracket(item.device.deviceId.toString()));
                            beaconItem.setBeaconFreq(item.rssi);
                            beaconItem.setDevice(item.device);
                            beaconItem.setDistance(Utils.computeAccuracy(item));
                            beacons.add(beaconItem);

//                            }
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
//                        realm.executeTransaction(new Realm.Transaction() {
//                            @Override
//                            public void execute(Realm realm) {
//                                realm.copyToRealmOrUpdate(beacons);
//                            }
//                        });
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                for (int i = 0; i < beacons.size(); i++) {
                                    final int finalI = i;
                                    Beacon original = beacons.get(finalI);
                                    RealmQuery<Beacon> query = RealmQuery.createQuery(realm, Beacon.class);
                                    query.equalTo("beaconId", beacons.get(finalI).getBeaconId());
                                    Beacon beaconNew = query.findFirst();
                                    if (beaconNew != null) {
                                        original.setBeaconName(beaconNew.getBeaconName());
                                        original.setBeaconColor(beaconNew.getBeaconColor());
                                    } else {
                                        beaconNew = realm.createObject(Beacon.class, original.getBeaconId());
                                        realm.copyToRealm(beaconNew);
                                    }
                                }
                            }
                        });
                        beaconListAdapter = new BeaconListAdapter(BeaconListActivity.this, beacons, new BeaconListAdapter.detailListener() {
                            @Override
                            public void fetchDetails(int position, DeviceId deviceId) {
                                queueForFetchDetails.put(AndroidUtilities.escapeBracket(deviceId.toString()), position);
                                FetchDetails.fetchBeaconDetailsByDeviceID(deviceId);
                            }

                            @Override
                            public void onItemClick(int position, Beacon beacon) {
                                Intent intent = new Intent(BeaconListActivity.this, BeaconDetailEntry.class);
                                intent.putExtra(EXTRA_SCAN_RESULT_ITEM_DEVICE, beacon.device);
                                intent.putExtra(EXTRA_BEACON_ID, beacon.getBeaconId());
                                intent.putExtra(EXTRA_POINT_ID, id_to_pass);
                                startActivity(intent);
                            }
                        });
                        rlBeacons.setLayoutManager(llm = new LinearLayoutManager(BeaconListActivity.this, LinearLayoutManager.VERTICAL, false));
                        mDividerItemDecoration = new DividerItemDecoration(rlBeacons.getContext(), llm.getOrientation());
                        mDividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_rv));
                        rlBeacons.addItemDecoration(mDividerItemDecoration);
                        rlBeacons.setAdapter(beaconListAdapter);
                        pbBluetoothSearch.clearAnimation();
                        pbBluetoothSearch.setVisibility(View.GONE);
                        btnScan.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.beaconDetailUpdate);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.beaconDetailUpdate) {
            final String beacon = (String) args[0];

            if (queueForFetchDetails.containsKey(beacon)) {
                final int position = queueForFetchDetails.get(beacon);
                if (beacons.size() > 0 && position < beacons.size()) {
                    final Beacon needToUpdate = beacons.get(position);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(final Realm realm) {
                            RealmQuery<Beacon> query = realm.where(Beacon.class);
                            query.equalTo("beaconId", beacon);
                            final Beacon result = query.findFirst();
                            needToUpdate.setBeaconColor(result.getBeaconColor());
                            needToUpdate.setBeaconName(result.getBeaconName());
                            if (beaconListAdapter != null && beaconListAdapter.getItemCount() > position) {
                                beaconListAdapter.notifyItemChanged(position);
                            }
                        }
                    });
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmQuery<Beacon> query = realm.where(Beacon.class);
                            RealmResults<Beacon> allBeacons = query.findAll();
                            Log.d(TAG, "execute: " + allBeacons.size());
                        }
                    });

                }
                queueForFetchDetails.remove(beacon);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnScan) {
            btnScan.setVisibility(View.GONE);
            beacons.clear();
            beaconIds.clear();
            if (beaconListAdapter != null) {
                beaconListAdapter.notifyDataSetChanged();
            }
            animation.start();
            pbBluetoothSearch.setVisibility(View.VISIBLE);
            scanForDevices();
        }
    }
}
