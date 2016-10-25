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

/**
 * Created by lcom75 on 25/10/16.
 */

public class DImageView extends ImageView {
    Paint debugPaint = null;

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
        debugPaint.setColor(Color.YELLOW);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeWidth(AndroidUtilities.dp(1));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getHeight() > 0) {
            canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), debugPaint);
        }
        super.onDraw(canvas);
    }
}
