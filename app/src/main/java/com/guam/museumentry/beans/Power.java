package com.guam.museumentry.beans;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by lcom75 on 8/11/16.
 */

public class Power {
    public static final String WEAK = "WEAK";
    public static final String NORMAL = "NORMAL";
    public static final String STRONG = "STRONG";
    public int power;
    public float rangeMeter;
    public float rangeFt;

    @powerMode
    public String getPowerMode() {
        return null;
    }

    public void setPowerMode(@powerMode String mode) {

    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({WEAK, NORMAL, STRONG})
    public @interface powerMode {
    }
}
