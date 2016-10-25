package com.guam.museumentry.beans;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by lcom75 on 25/10/16.
 */

public class Beacon extends RealmObject {
    @PrimaryKey
    String beaconId;
    String beaconColor;
    @Ignore
    Integer beaconFreq;

    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    public String getBeaconColor() {
        return beaconColor;
    }

    public void setBeaconColor(String beaconColor) {
        this.beaconColor = beaconColor;
    }

    public Integer getBeaconFreq() {
        return beaconFreq;
    }

    public void setBeaconFreq(Integer beaconFreq) {
        this.beaconFreq = beaconFreq;
    }
}
