package com.gildStudios.DiTo.androidApp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.gildStudios.DiTo.androidApp.ActivityUtils;
import com.gildStudios.DiTo.androidApp.activities.AccessActivity;
import com.github.alexjlockwood.kyrie.KyrieDrawable;

public class SplashListenerAdapter extends KyrieDrawable.ListenerAdapter {

    private ImageView imgview;
    private Context context;

    public SplashListenerAdapter(ImageView imgview, Context context){
        this.imgview=imgview;
        this.context=context;
    }

    @Override
    public void onAnimationEnd(@NonNull KyrieDrawable drawable){
        imgview.setVisibility(View.VISIBLE);
        ActivityUtils.showActivity((AppCompatActivity)context, AccessActivity.class);
    }
}
