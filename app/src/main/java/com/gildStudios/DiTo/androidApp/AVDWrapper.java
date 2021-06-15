package com.gildStudios.DiTo.androidApp;

import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.support.annotation.Nullable;

public class AVDWrapper {
    private Handler mHandler;
    private Animatable mDrawable;
    private Callback mCallback;
    private Runnable mAnimationDoneRunnable = new Runnable() {
        @Override
        public void run() {
            if(mCallback != null)
                mCallback.onAnimationDone();
        }
    };

    public interface Callback {
        public void onAnimationDone();
        public void onAnimationStopped();
    }

    public AVDWrapper(Animatable animDrawable,
                      @Nullable Handler animHandler, Callback animCallback) {
        mDrawable = animDrawable;
        mHandler = animHandler != null ? animHandler : new Handler();
        mCallback = animCallback;
    }

    // Duration of the Animation
    public void start(long durationMillis) {
        mDrawable.start();
        mHandler.postDelayed(mAnimationDoneRunnable, durationMillis);
    }

    public void stop() {
        mDrawable.stop();
        mHandler.removeCallbacks(mAnimationDoneRunnable);

        if(mCallback != null)
            mCallback.onAnimationStopped();
    }
}