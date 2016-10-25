/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guam.museumentry.v4Style;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FrameLayout} that allows the user to drag and reposition child views.
 */
public class DragFrameLayout extends FrameLayout {

    private static final String TAG = "DragFrameLayout";
    /**
     * The list of {@link View}s that will be draggable.
     */
    private List<View> mDragViews;
    /**
     * The {@link DragFrameLayoutController} that will be notify on drag.
     */
    private DragFrameLayoutController mDragFrameLayoutController;
    private ViewDragHelper mDragHelper;
    private Drawable imageDrawable;
//    ImageView imageView;

    public DragFrameLayout(Context context) {
        super(context);
        init();
    }

    public DragFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mDragViews = new ArrayList<View>();
        /**
         * Create the {@link ViewDragHelper} and set its callback.
         */
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return mDragViews.contains(child);
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                Log.d(TAG, "onViewPositionChanged() called with: changedView = [" + changedView + "], left = [" + left + "], top = [" + top + "], dx = [" + dx + "], dy = [" + dy + "]");
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }

            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
                if (mDragFrameLayoutController != null) {
                    mDragFrameLayoutController.onDragDrop(capturedChild, true);
                }
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                if (mDragFrameLayoutController != null) {
                    mDragFrameLayoutController.onDragDrop(releasedChild, false);
                }
                Log.d(TAG, "onViewReleased() called with: releasedChild = [" + releasedChild + "], xvel = [" + xvel + "], yvel = [" + yvel + "]");
            }
        });
//        imageView = new ImageView(getContext());
//        FrameLayout.LayoutParams layoutParams = new LayoutParams(AndroidUtilities.dp(32), AndroidUtilities.dp(32));
//        layoutParams.gravity = Gravity.CENTER;
//        imageView.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return false;
//            }
//        });
//        addView(imageView, layoutParams);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDragHelper.processTouchEvent(ev);
        return true;
    }

    /**
     * Adds a new {@link View} to the list of views that are draggable within the container.
     *
     * @param dragView the {@link View} to make draggable
     */
    public void addDragView(View dragView) {
        mDragViews.add(dragView);
    }

    /**
     * Sets the {@link DragFrameLayoutController} that will receive the drag events.
     *
     * @param dragFrameLayoutController a {@link DragFrameLayoutController}
     */
    public void setDragFrameController(DragFrameLayoutController dragFrameLayoutController) {
        mDragFrameLayoutController = dragFrameLayoutController;
    }

    public void setImageDrawable(Drawable imageDrawable) {
        this.imageDrawable = imageDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(imageDrawable);
        } else {
            setBackgroundDrawable(imageDrawable);
        }
    }

    /**
     * A controller that will receive the drag events.
     */
    public interface DragFrameLayoutController {

        public void onDragDrop(View view, boolean captured);
    }
}
