package com.guam.museumentry.beans;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by lcom75 on 25/10/16.
 */

public class SingleLocation extends RealmObject implements Parcelable {

    public static final Parcelable.Creator<SingleLocation> CREATOR = new Parcelable.Creator<SingleLocation>() {
        @Override
        public SingleLocation createFromParcel(Parcel source) {
            return new SingleLocation(source);
        }

        @Override
        public SingleLocation[] newArray(int size) {
            return new SingleLocation[size];
        }
    };
    int vIndex;
    String beaconID;
    String userName;
    float rightPercentage;
    float bottomPercentage;
    boolean isSaved;

    public SingleLocation() {
    }

    protected SingleLocation(Parcel in) {
        this.beaconID = in.readString();
        this.userName = in.readString();
        this.rightPercentage = in.readFloat();
        this.bottomPercentage = in.readFloat();
    }

    public int getvIndex() {
        return vIndex;
    }

    public void setvIndex(int vIndex) {
        this.vIndex = vIndex;
    }

    public String getBeaconID() {
        return beaconID;
    }

    public void setBeaconID(String beaconID) {
        this.beaconID = beaconID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getRightPercentage() {
        return rightPercentage;
    }

    public void setRightPercentage(float rightPercentage) {
        this.rightPercentage = rightPercentage;
    }

    public float getBottomPercentage() {
        return bottomPercentage;
    }

    public void setBottomPercentage(float bottomPercentage) {
        this.bottomPercentage = bottomPercentage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.beaconID);
        dest.writeString(this.userName);
        dest.writeFloat(this.rightPercentage);
        dest.writeFloat(this.bottomPercentage);
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}
