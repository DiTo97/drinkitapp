package com.gildStudios.DiTo.androidApp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.Shaker;
import com.github.alexjlockwood.kyrie.KyrieDrawable;

public class GlassListenerAdapter extends KyrieDrawable.ListenerAdapter {

    long stoptime;
    private final double tax;
    private final float limit;
    private final Context context;
    private final TextView textView;
    private final int stomachStatus;
    private final TextView warningView;

    public GlassListenerAdapter(TextView textView,TextView warningView, double tax, int stomachStatus, float limit, Context context){
        this.textView=textView;
        this.tax=tax;
        this.stomachStatus=stomachStatus;
        this.warningView=warningView;
        this.limit=limit;
        this.context=context;

    }

    @Override
    public void onAnimationStart(@NonNull KyrieDrawable drawable){
        textView.setVisibility(View.VISIBLE);
        stoptime = Drink.setAlcholicTaxTime(tax);
    }

    @Override
    public void onAnimationUpdate(@NonNull KyrieDrawable drawable){
        stoptime = Drink.setAlcholicTaxTime(tax);
        Log.d("massimo",Double.toString(drawable.getCurrentPlayTime()));
        if(tax<=1.00) {
            textView.setText(Double.toString((drawable.getCurrentPlayTime() / 1000.00)));
        }else textView.setText(Double.toString((drawable.getCurrentPlayTime() / 500.00)));
        long current = drawable.getCurrentPlayTime();
        if (stoptime <= current) {
            drawable.pause();
        }
    }

    @Override
    public void onAnimationEnd(@NonNull KyrieDrawable drawable){
        Shaker shaker = new Shaker(textView,-15,15,Color.GRAY, Color.RED);
        shaker.shake();
        warningView.setTextSize(18);
        warningView.setText(context.getString(R.string.warning1_5));
        textView.setText("4.00+");
    }

    @Override
    public void onAnimationPause(@NonNull KyrieDrawable drawable){
        Shaker shaker = new Shaker(textView,-15,15,Color.GRAY, Color.RED);
        shaker.shake();
        if(tax<=0.5) {
            warningView.setTextSize(18);
            warningView.setText(context.getString(R.string.notWarning));
        }else if(tax> 0.5 && tax <= 0.8){
            warningView.setTextSize(18);
            warningView.setText(context.getString(R.string.warning0_5));
        }else if(tax> 0.8 && tax <= 1.5){
            warningView.setTextSize(18);
            warningView.setText(context.getString(R.string.warning0_8));
        }else if(tax> 1.5){
            warningView.setTextSize(18);
            warningView.setText(context.getString(R.string.warning1_5));
        }
        if(tax > 1 && tax <= 2){

        }
        textView.setText(Double.toString(tax));
    }
}