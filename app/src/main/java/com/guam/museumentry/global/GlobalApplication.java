package com.guam.museumentry.global;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.estimote.sdk.BeaconManager;
import com.guam.museumentry.MainActivity;

import io.realm.Realm;

public class GlobalApplication extends Application {

    public static volatile Context applicationContext;
    public static Handler applicationHandler;
    public static int cacheSize;
    private String TAG = "Global Application";
    private BeaconManager beaconManager;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this.getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        cacheSize = maxMemory / 8;
        Realm.init(this);
//        beaconManager = new BeaconManager(getApplicationContext());
//        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//            @Override
//            public void onServiceReady() {
//                beaconManager.startMonitoring(new Region(
//                        "monitored region 1",
//                        UUID.fromString("BC10B920-6595-5897-5D69-4728950C2C91"),
//                        36887, 10290));
//            }
//        });
//        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
//            @Override
//            public void onEnteredRegion(Region region, List<Beacon> list) {
//                showNotification(
//                        "Your gate closes in 47 minutes.",
//                        "Current security wait time is 15 minutes, "
//                                + "and it's a 5 minute walk from security to the gate. "
//                                + "Looks like you've got plenty of time!");
//                Log.d(TAG, "onEnteredRegion: We got :" + list.size());
//                for (Beacon beacon :
//                        list) {
//                    Log.d(TAG, "onEnteredRegion MAC: " + beacon.getMacAddress());
//                    Log.d(TAG, "onEnteredRegion UUID: " + beacon.getProximityUUID());
//                    Log.d(TAG, "onEnteredRegion MAJOR: " + beacon.getMajor());
//                    Log.d(TAG, "onEnteredRegion MINOR: " + beacon.getMinor());
//                    EstimoteCloud.getInstance().fetchBeaconDetails(beacon.getMacAddress(), new CloudCallback<BeaconInfo>() {
//                        @Override
//                        public void success(BeaconInfo beaconInfo) {
//                            Log.d(TAG, "success Name:  " + beaconInfo.name);
//                            Log.d(TAG, "success Eddystone Instance: " + beaconInfo.settings.eddystoneInstance);
//                            Log.d(TAG, "success Eddystone NameSpace: " + beaconInfo.settings.eddystoneNamespace);
//                            Log.d(TAG, "success Eddystone URL: " + beaconInfo.settings.eddystoneUrl);
//                            Log.d(TAG, "success Advertise Interval: " + beaconInfo.settings.advertisingIntervalMillis);
//                            Log.d(TAG, "success isSecure: " + beaconInfo.settings.secure);
//                        }
//
//                        @Override
//                        public void failure(EstimoteServerException e) {
//                            if (e != null) e.printStackTrace();
//                        }
//                    });
//                    EstimoteCloud.getInstance().fetchBeaconDetails(beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor(), new CloudCallback<BeaconInfo>() {
//                        @Override
//                        public void success(BeaconInfo beaconInfo) {
//                            Log.d(TAG, "success Name Fetch:  " + beaconInfo.name);
//                            Log.d(TAG, "success Eddystone Instance: " + beaconInfo.settings.eddystoneInstance);
//                            Log.d(TAG, "success Eddystone NameSpace: " + beaconInfo.settings.eddystoneNamespace);
//                            Log.d(TAG, "success Eddystone URL: " + beaconInfo.settings.eddystoneUrl);
//                            Log.d(TAG, "success Advertise Interval: " + beaconInfo.settings.advertisingIntervalMillis);
//                            Log.d(TAG, "success isSecure: " + beaconInfo.settings.secure);
//                        }
//
//                        @Override
//                        public void failure(EstimoteServerException e) {
//                            if (e != null) e.printStackTrace();
//                        }
//                    });
//                    break;
//                }
//            }
//
//            @Override
//            public void onExitedRegion(Region region) {
//                // could add an "exit" notification too if you want (-:
//                showNotification("Your are exiting", "You are out of Range");
//            }
//        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build();
        }
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        System.gc();
    }
}
