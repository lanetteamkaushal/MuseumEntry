package com.guam.museumentry.beans;

import com.estimote.sdk.connection.scanner.ConfigurableDevice;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by lcom75 on 25/10/16.
 */

public class Beacon extends RealmObject {
    @Ignore
    public ConfigurableDevice device;
    @PrimaryKey
    String beaconId;
    String beaconColor;
    String beaconName;
    @Ignore
    Integer beaconFreq;
    @Ignore
    Double distance;
    private Integer majorID;
    private Integer minorID;
    private int beaconPower = 200;

    public int getBeaconPower() {
        return beaconPower;
    }

    public void setBeaconPower(int beaconPower) {
        this.beaconPower = beaconPower;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getBeaconName() {
        return beaconName;
    }

    public void setBeaconName(String beaconName) {
        this.beaconName = beaconName;
    }

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

    public void setDevice(ConfigurableDevice device) {
        this.device = device;
    }

    public Integer getMajorID() {
        return majorID;
    }

    public void setMajorID(Integer majorID) {
        this.majorID = majorID;
    }

    public Integer getMinorID() {
        return minorID;
    }

    public void setMinorID(Integer minorID) {
        this.minorID = minorID;
    }
}
