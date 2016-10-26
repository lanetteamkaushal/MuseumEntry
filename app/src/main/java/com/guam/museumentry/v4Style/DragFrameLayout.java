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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.guam.museumentry.R;
import com.guam.museumentry.beans.SingleLocation;
import com.guam.museumentry.custom.DImageView;
import com.guam.museumentry.global.AndroidUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A {@link FrameLayout} that allows the user to drag and reposition child views.
 */
public class DragFrameLayout extends FrameLayout {

    private static final String TAG = "DragFrameLayout";
    AtomicInteger counter = new AtomicInteger(0);
    SparseArray<Rect> beacon = new SparseArray<>();
    //    ImageView imageView;
    boolean settle = true;
    View lastChangedView = null;
    Rect rect;
    Rect originalRect = new Rect();
    int image_size = 0;
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
    private boolean sizeChanged = false;
    private Drawable drawableForSavedLocation;
    private RealmResults<SingleLocation> allPins;

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
        counter.set(1);
        image_size = AndroidUtilities.dp(32);
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
                Log.d(TAG, "onViewPositionChanged() called with: " +
                        "changedView = [" + changedView.getId() + "], left = [" + left + "], top = [" + top + "], " +
                        "dx = [" + dx + "], dy = [" + dy + "]");
                if (beacon.indexOfKey(changedView.getId()) > -1) {
                    Rect rect = beacon.get(changedView.getId());
                    rect.left = left;
                    rect.top = top;
                    beacon.put(changedView.getId(), rect);
                    Log.w(TAG, "onViewPositionChanged: Pin found and updated");
                } else {
                    Log.e(TAG, "onViewPositionChanged: No Pin found");
                }
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
                Log.d(TAG, "onViewCaptured() called with: capturedChild = [" + capturedChild.getId() + "], activePointerId = [" + activePointerId + "]");
                settle = true;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                if (mDragFrameLayoutController != null) {
                    mDragFrameLayoutController.onDragDrop(releasedChild, false);
                }
                Log.d(TAG, "onViewReleased() called with: releasedChild = [" + releasedChild.getId() + "], xvel = [" + xvel + "], yvel = [" + yvel + "]");
                if (beacon.indexOfKey(releasedChild.getId()) > -1) {
                    Rect rect = beacon.get(releasedChild.getId());
                    mDragHelper.settleCapturedViewAt(rect.left, rect.top);
//                    if (mDragHelper.settleCapturedViewAt(rect.left, rect.top)) {
//                        if (!settle)
//                            settle = mDragHelper.continueSettling(true);
//                        Log.d(TAG, "onViewReleased: Settled" + settle);
//                    } else {
//                        Log.w(TAG, "onViewReleased: Settle captureView failed");
//                    }
                    lastChangedView = releasedChild;
                }
            }
        });
//        imageView = new ImageView(getContext());
//        FrameLayout.LayoutParams layoutParams = new LayoutParams(image_size, image_size);
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
        sizeChanged = true;
        dragView.setId(counter.incrementAndGet());
        Log.d(TAG, "addDragView: " + dragView.getId());
        beacon.append(dragView.getId(), new Rect());
        mDragViews.add(dragView);
        lastChangedView = dragView;
    }

    private void addDragView(View dragView, Rect rect, int index) {
        Log.d(TAG, "addDragView: " + index);
        beacon.append(dragView.getId(), rect);
        mDragViews.add(dragView);
        lastChangedView = dragView;
        dragView.setId(index);
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

    public void requestForFillData(SingleLocation singleLocation) {
        if (lastChangedView != null) {
            Log.d(TAG, "requestForFillData: We got view with id To save :" + lastChangedView);
            if (beacon.indexOfKey(lastChangedView.getId()) > -1) {
                Rect rect = beacon.get(lastChangedView.getId());
                rect.bottom = rect.top + image_size;
                rect.right = rect.left + image_size;
                singleLocation.setvIndex(lastChangedView.getId());
                singleLocation.setRightPercentage(rect.right / ((float) originalRect.width() / 100));
                singleLocation.setBottomPercentage(rect.bottom / ((float) originalRect.height() / 100));
                singleLocation.setSaved(true);
                ((DImageView) lastChangedView).setImageDrawable(drawableForSavedLocation);
            } else {
                Log.w(TAG, "requestForFillData: No ID found for last modified item");
            }
        }
    }

    public int totalViewCount() {
        return beacon.size();
    }

    public void setDrawableForSavedLocation(Drawable drawableForSavedLocation) {
        this.drawableForSavedLocation = drawableForSavedLocation;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (sizeChanged) {
//        if (changed) {
            if (beacon.size() > 0 && getChildCount() > 0) {
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    if (view != null && view.getVisibility() != GONE) {
                        if (beacon.indexOfKey(view.getId()) > -1) {
                            rect = beacon.get(view.getId());
                            view.measure(MeasureSpec.makeMeasureSpec(image_size, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(image_size, MeasureSpec.EXACTLY));
                            view.layout(rect.left, rect.top, rect.left + image_size, rect.top + image_size);
                        }
                    }
                }
            }
            sizeChanged = false;
        } else {
            super.onLayout(changed, left, top, right, bottom);
        }
        if (changed) {
            originalRect.set(left, top, right, bottom);
            Log.d(TAG, "onLayout() called with: changed = [" + changed + "], left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "]");
            if (allPins.size() > 0) {
                addViews();
            }
        }
    }

    public void addViews() {
        for (int i = 0; i < allPins.size(); i++) {
            SingleLocation singleLocation = allPins.get(i);
            DImageView iv_sticker = new DImageView(getContext());
            iv_sticker.setId(singleLocation.getvIndex());
            iv_sticker.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_location));
            iv_sticker.setScaleType(ImageView.ScaleType.FIT_END);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(AndroidUtilities.dp(32), AndroidUtilities.dp(32));

            Rect rect = new Rect();
            rect.right = Math.round(originalRect.width() * (singleLocation.getRightPercentage() / 100));
            rect.bottom = Math.round(originalRect.height() * (singleLocation.getBottomPercentage() / 100));
            rect.left = rect.right - image_size;
            rect.top = rect.bottom - image_size;
            Log.d(TAG, "addViews: " + rect.toString());
//            iv_sticker.setTranslationX(rect.left);
//            iv_sticker.setTranslationY(rect.top);
            addView(iv_sticker, layoutParams);
            addDragView(iv_sticker, rect, singleLocation.getvIndex());
        }
        int lastIndex = allPins.sort("vIndex", Sort.ASCENDING).last().getvIndex();
        counter.set(lastIndex);
        sizeChanged = true;
        invalidate();
    }

    public void addViewDirectly(RealmResults<SingleLocation> allPins) {
        this.allPins = allPins;
        if (originalRect.height() > 0) {
            addViews();
        }
    }

    /**
     * A controller that will receive the drag events.
     */
    public interface DragFrameLayoutController {
        public void onDragDrop(View view, boolean captured);
    }
}
