package com.gildStudios.DiTo.androidApp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.gildStudios.DiTo.androidApp.adapters.SliderAdapter;

public class TutorialFragment extends Fragment {
    private ViewPager mSlideViewPager;
    private LinearLayout mDotsLayout;

    private Button nextButton;
    private Button backButton;

    private int mCurrentPage;

    private TextView[] mDots;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Haza", "Tutorial opened");
        ((HomeActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View tutView = inflater.inflate(R.layout.tutorial, container, false);

        mDotsLayout = tutView.findViewById(R.id.dotsLayout);
        mSlideViewPager = tutView.findViewById(R.id.slideViewPager);

        nextButton = tutView.findViewById(R.id.nextButton);
        backButton = tutView.findViewById(R.id.backButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentPage == mDots.length - 1) {
                    assert getActivity() != null;

                    // Fragments' back-Stack is cleared
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.popBackStackImmediate(null, FragmentManager
                            .POP_BACK_STACK_INCLUSIVE);

                    HomeFragment homeFragment = new HomeFragment();

                    FragmentTransaction homeTransact = fragmentManager.beginTransaction();
                    homeTransact.setCustomAnimations(R.anim.transition_from_right,
                            R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                    homeTransact.replace(R.id.homeWrapper, homeFragment)
                        .commit();
                } else {
                    mSlideViewPager.setCurrentItem(mCurrentPage + 1);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlideViewPager.setCurrentItem(mCurrentPage - 1);
            }
        });

        SliderAdapter sliderAdapter = new SliderAdapter(getActivity());
        mSlideViewPager.setAdapter(sliderAdapter);

        mSlideViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) { }

            @Override
            public void onPageSelected(int i) {
                addDotsIndicator(i);
                mCurrentPage = i;

                if(i == 0) {
                    nextButton.setEnabled(true);

                    backButton.setEnabled(false);
                    backButton.setVisibility(View.INVISIBLE);
                } else if(i == mDots.length - 1) {
                    nextButton.setEnabled(true);

                    backButton.setEnabled(true);
                    backButton.setVisibility(View.VISIBLE);

                    nextButton.setText(getResources().getString(R.string.btn_finishPage));
                } else {
                    nextButton.setEnabled(true);

                    backButton.setEnabled(true);
                    backButton.setVisibility(View.VISIBLE);

                    nextButton.setText(getResources().getString(R.string.btn_nextPage));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) { }
        });

        addDotsIndicator(0);

        return tutView;
    }


    public void addDotsIndicator(int pagePosition) {
        mDotsLayout.removeAllViews();
        mDots = new TextView[4];

        String bulletUnicode = "&#8226";

        for(int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(getContext());
            mDots[i].setText(Html.fromHtml(bulletUnicode));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.grayClean));

            mDotsLayout.addView(mDots[i]);
        }

        if(mDots.length > 0) {
            mDots[pagePosition].setTextColor(getResources().getColor(R.color.chiantiLight));
        }
    }
}

