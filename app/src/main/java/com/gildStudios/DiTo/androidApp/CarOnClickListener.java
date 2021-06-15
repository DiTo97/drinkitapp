package com.gildStudios.DiTo.androidApp;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.github.alexjlockwood.kyrie.KyrieDrawable;

import java.util.concurrent.ThreadLocalRandom;

final public class CarOnClickListener implements View.OnClickListener {
    @NonNull
    private final KyrieDrawable drawable;
    @NonNull
    private final ImageView imgview;
    @NonNull
    private final Button button;
    private Handler handler = new Handler();
    private final int min = 1000;
    private final int max = 3000;

    public CarOnClickListener(@NonNull KyrieDrawable drawable, @NonNull ImageView imgview, @NonNull Button button) {
        this.drawable = drawable;
        this.imgview = imgview;
        this.button = button;
    }

    private Runnable mAnimationDoneRunnable = new Runnable() {
        @Override
        public void run() {
            button.setEnabled(true);
            drawable.start();
        }
    };

    public void start() {
        int randomDuration = ThreadLocalRandom.current().nextInt(min, max + 1);
        Log.d("random", Integer.toString(randomDuration));
        button.setEnabled(false);
        handler.postDelayed(mAnimationDoneRunnable, randomDuration);
    }

    @Override
    public void onClick(View v) {
        imgview.setVisibility(View.VISIBLE);
        if (drawable.isStarted()) {
            drawable.pause();
        } else {
            drawable.setCurrentPlayTime(0);
            button.setText(v.getContext().getString(R.string.be_ready_reflex));
            start();
        }
    }


}
