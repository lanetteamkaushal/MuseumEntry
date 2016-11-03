package com.guam.museumentry.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.guam.museumentry.global.AndroidUtilities;
import com.guam.museumentry.global.NotificationCenter;

/**
 * Created by lcom75 on 25/10/16.
 */

public class DImageView extends ImageView implements NotificationCenter.NotificationCenterDelegate {
    Paint debugPaint = null;
    boolean isSelected = false;

    public DImageView(Context context) {
        super(context);
        init();
    }

    public DImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        debugPaint = new Paint();
        debugPaint.setColor(Color.RED);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeWidth(AndroidUtilities.dp(2));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getHeight() > 0) {
            if (isSelected) {
                canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), debugPaint);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.beaconPinSelected);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.beaconPinSelected);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.beaconPinSelected) {
            int idToSelect = (int) args[0];
            if (idToSelect == getId()) {
                isSelected = true;
            } else {
                isSelected = false;
            }
            invalidate();
        }
    }
}
