package com.guam.museumentry.service;

import android.util.Log;

import com.estimote.sdk.DeviceId;
import com.estimote.sdk.cloud.CloudCallback;
import com.estimote.sdk.cloud.EstimoteCloud;
import com.estimote.sdk.cloud.model.BeaconInfo;
import com.estimote.sdk.exception.EstimoteServerException;
import com.guam.museumentry.global.DatabaseUtils;

import java.util.ArrayList;

/**
 * Created by lcom75 on 26/10/16.
 */

public class FetchDetails {
    private static final String TAG = "FetchDetails";
    private static DispatchQueue cacheOutQueue = new DispatchQueue("cacheOutQueue");
    private static ArrayList<String> inQueue = new ArrayList<>();

    public static void fetchBeaconDetailsByDeviceID(final DeviceId identifier) {
        if (!inQueue.contains(identifier.toHexString())) {
            cacheOutQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
//                    EstimoteCloud.getInstance().fetchDeviceDetails(identifier, new CloudCallback<Device>() {
//                        @Override
//                        public void success(Device device) {
//                            DatabaseUtils.getInstance().saveDeviceInfo(device);
//                        }
//
//                        @Override
//                        public void failure(EstimoteServerException e) {
//
//                        }
//                    });
                    EstimoteCloud.getInstance().fetchBeaconDetails(identifier, new CloudCallback<BeaconInfo>() {
                        @Override
                        public void success(BeaconInfo beaconInfo) {
                            DatabaseUtils.getInstance().saveDeviceInfo(identifier, beaconInfo);
                        }

                        @Override
                        public void failure(EstimoteServerException e) {

                        }
                    });
                }
            });

        } else {
            Log.w(TAG, "Already in queue : " + identifier.toString());
        }
    }
}
