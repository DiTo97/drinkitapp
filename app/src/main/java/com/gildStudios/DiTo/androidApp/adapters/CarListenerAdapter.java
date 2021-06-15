package com.gildStudios.DiTo.androidApp.adapters;

import android.app.AlertDialog;
import android.app.Dialog;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gildStudios.DiTo.androidApp.CarOnClickListener;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.Shaker;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.gildStudios.DiTo.androidApp.fragments.HomeFragment;
import com.gildStudios.DiTo.androidApp.fragments.ReflexTestFragment;
import com.github.alexjlockwood.kyrie.KyrieDrawable;

final public class CarListenerAdapter extends KyrieDrawable.ListenerAdapter {
    @NonNull
    private final Button button;
    private Context context;
    private final ViewGroup container;
    private final ImageView imgview;
    private final LayoutInflater inflater;
    public static final String coso = "MARIO";
    private long endtime1;

    private boolean isBtnClicked;

    public CarListenerAdapter(@NonNull Button button, Context context, ImageView imgview, ViewGroup container, LayoutInflater inflater) {
        this.button = button;
        this.context = context;
        this.imgview = imgview;
        this.container=container;
        this.inflater=inflater;
    }

    @Override
    public void onAnimationStart(@NonNull KyrieDrawable drawable) {
        isBtnClicked = false;

        button.setText(context.getString(R.string.test_stop));
        endtime1 = System.currentTimeMillis();
    }

    @Override
    public void onAnimationPause(@NonNull KyrieDrawable drawable) {
        isBtnClicked = true;

        button.setText(context.getString(R.string.test_play));
        long pauseTime = drawable.getCurrentPlayTime();

        TextView title = new TextView(context);
        title.setText(context.getString(R.string.test_won));
        title.setPadding(0,15,0,0);
        title.setTextColor(ContextCompat.getColor(context, R.color.okGreen));
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);

        AlertDialog ad = new AlertDialog.Builder(context)
                                    .create();
                            ad.setCancelable(false);
                            ad.setCustomTitle(title);
                            ad.setMessage( context.getString(R.string.test_took)+ pauseTime + " ms");
                            ad.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.test_play), new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    /*Fragment frg = null;
                                    frg =((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("ReflexTestFragment");
                                    final FragmentTransaction ft = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
                                    Fragment RefreshFragment = new ReflexTestFragment();
                                    Bundle refreshArgs = new Bundle();
                                    refreshArgs.putString("refresh",coso);
                                    RefreshFragment.setArguments(refreshArgs);
                                    ft.replace(R.id.homeWrapper,RefreshFragment);
                                    ft.commit();*/


                                /*    FragmentTransaction ft = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
                                    if (Build.VERSION.SDK_INT >= 26) {
                                        ft.setReorderingAllowed(false);
                                    }
                                    //TODO: Prova a passare il fragment in un Bundle
                                    Fragment currentFragment = new ReflexTestFragment();
                                    Bundle refreshArgs = new Bundle();
                                    refreshArgs.putString("refresh", coso);
                                    currentFragment.setArguments(refreshArgs);
                                    ft.replace(R.id.homeWrapper, currentFragment);

                                    ft.commit(); */

                                    //Soluzione al bug del menu
                                    Fragment frg = null;
                                    frg = ((AppCompatActivity)context).getSupportFragmentManager().findFragmentByTag("fragment_reflexGame");
                                    final FragmentTransaction ft = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
                                    ft.detach(frg);
                                    ft.attach(frg);
                                    ft.commit();
                                }
                            });
                            ad.setButton(Dialog.BUTTON_NEGATIVE, context.getString(R.string.test_back), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    ActivityUtils.showActivity((Activity) context, HomeActivity.class);
                                    assert context != null;

                                    // Fragments' back-Stack is cleared
                                    FragmentManager fragmentManager = ((HomeActivity) context).getSupportFragmentManager();
                                    fragmentManager.popBackStackImmediate(null, FragmentManager
                                            .POP_BACK_STACK_INCLUSIVE);

                                    HomeFragment homeFragment = new HomeFragment();

                                    FragmentTransaction homeTransact = fragmentManager.beginTransaction();
                                    homeTransact.setCustomAnimations(R.anim.transition_from_right,
                                            R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                                    homeTransact.replace(R.id.homeWrapper, homeFragment)
                                            .commit();
                                }
                            });

                            ad.show();


        TextView messageView = (TextView)ad.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
        messageView.setTextColor(ContextCompat.getColor(context, R.color.uber_black));
        Button btnPositive = ad.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = ad.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 20;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);

        ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.okGreen));
        ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.okGreen));

    }

    @Override
    public void onAnimationEnd(@NonNull final KyrieDrawable drawable) {
            Shaker shaker = new Shaker(imgview,-15,15,Color.GRAY, Color.RED);
            shaker.shake();

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isBtnClicked = true;

                    long a = endtime1;
                    double finaltime = (System.currentTimeMillis() - endtime1 ) / 1000.00;

                    View alertView = inflater.inflate(R.layout.alert2, container, false);
                    final AlertDialog ad = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                            .setView(alertView)
                            .create();

                    TextView continueBtn = alertView.findViewById(R.id.continueAnimation2);
                    continueBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ad.dismiss();
                            imgview.setVisibility(View.VISIBLE);
                        }
                    });

                    ad.show();
                    String retryString = context.getString(R.string.textView_retryText, finaltime);
                    ((TextView) alertView.findViewById(R.id.textViewText)).setText(retryString);
                    button.setOnClickListener(null);
                    button.setText(context.getString(R.string.test_again));
                    button.setOnClickListener(new CarOnClickListener(drawable, imgview, button));
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Context to be checked
                    assert context != null;

                    ReflexTestFragment reflexGame = (ReflexTestFragment) ((HomeActivity) context).getSupportFragmentManager()
                            .findFragmentByTag("fragment_reflexGame");

                    if(reflexGame != null && reflexGame.isVisible()
                            && reflexGame.getUserVisibleHint()) {
                        if (!isBtnClicked)
                            button.performClick();
                    }
                }
            }, 1000);
    }
}
