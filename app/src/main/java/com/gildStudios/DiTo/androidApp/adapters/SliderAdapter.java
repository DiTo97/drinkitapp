package com.gildStudios.DiTo.androidApp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.gildStudios.DiTo.androidApp.R;


public class SliderAdapter extends PagerAdapter {
    private Context mContext;

    private int[] sliderImages = {
            R.drawable.intro_profile,
            R.drawable.intro_beer,
            R.drawable.intro_features,
            R.drawable.intro_offline
    };

    private String[] sliderHeadings = {
            "Sign Up",
            "Alcoholic Test",
            "Utilities",
            "Offline Mode"
    };

    private String[] sliderDescs;

    public SliderAdapter(Context context) {
        mContext = context;

        sliderDescs = new String[] {
            mContext.getString(R.string.tutorial_enter),
            mContext.getString(R.string.tutorial_test),
            mContext.getString(R.string.tutorial_utilities),
            mContext.getString(R.string.tutorial_offline)
        };
    }

    @Override
    public int getCount() {
        return sliderHeadings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View currentView, @NonNull Object o) {
        return currentView == (RelativeLayout) o;
    }

    @SuppressLint("WrongConstant")
    @Override @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService("layout_inflater");

        View aView = null;
        if(layoutInflater != null) {
            aView = layoutInflater.inflate(R.layout.slider_tutorial, container, false);

            ImageView slideImageView = aView.findViewById(R.id.slideImage);

            TextView slideHeading = aView.findViewById(R.id.slideHeading);
            TextView slideDesc = aView.findViewById(R.id.slideDescription);

            slideImageView.setImageResource(sliderImages[position]);
            slideHeading.setText(sliderHeadings[position]);
            slideDesc.setText(sliderDescs[position]);

            container.addView(aView);
        }

        return aView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }

}
