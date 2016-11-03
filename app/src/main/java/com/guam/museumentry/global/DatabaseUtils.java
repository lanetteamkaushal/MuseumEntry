package com.guam.museumentry.global;

import android.util.Log;

import com.estimote.sdk.DeviceId;
import com.estimote.sdk.cloud.model.BeaconInfo;
import com.estimote.sdk.cloud.model.Device;
import com.guam.museumentry.Migration;
import com.guam.museumentry.beans.Beacon;

import java.io.FileNotFoundException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;

/**
 * Created by lcom75 on 26/10/16.
 */

public class DatabaseUtils {
    private static final String TAG = "DatabaseUtils";
    public static volatile DatabaseUtils singleInstance;
    public Realm realm;

    public DatabaseUtils() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        try {
            Realm.migrateRealm(config, new Migration());
        } catch (FileNotFoundException ignored) {
            // If the Realm file doesn't exist, just ignore.
        }
        realm = Realm.getInstance(config);
    }

    public static DatabaseUtils getInstance() {
        if (singleInstance == null) {
            singleInstance = new DatabaseUtils();
        }
        return singleInstance;
    }

    public void saveDeviceInfo(final Device device) {

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmQuery<Beacon> query = realm.where(Beacon.class);
                query.equalTo("beaconId", device.identifier.toString());
                Beacon result = query.findFirst();
                result.setBeaconColor(device.color);
                result.setBeaconName(device.settings.advertisers.iBeacon.get(0).name);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: ");
            }
        });
    }

    public void saveDeviceInfo(final DeviceId identifier, final BeaconInfo beaconInfo) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(final Realm realm) {
                RealmQuery<Beacon> query = realm.where(Beacon.class);
                query.equalTo("beaconId", AndroidUtilities.escapeBracket(identifier.toString()));
                final Beacon result = query.findFirst();
                result.setMajorID(beaconInfo.major);
                result.setMinorID(beaconInfo.minor);
                result.setBeaconColor(beaconInfo.color.text);
                result.setBeaconName(beaconInfo.name);
                realm.copyToRealm(result);

            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: ");
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.beaconDetailUpdate, AndroidUtilities.escapeBracket(identifier.toString()));
                    }
                });

            }
        });
    }
}
