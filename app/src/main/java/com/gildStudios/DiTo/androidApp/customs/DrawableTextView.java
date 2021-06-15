package com.gildStudios.DiTo.androidApp.customs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.graphics.Rect;

import com.gildStudios.DiTo.androidApp.R;

public class DrawableTextView extends AppCompatTextView {

    public static final String TAG = "ClickableButtonEditText";

    private Drawable mDrawableRight;
    private Drawable mDrawableLeft;
    private Drawable mDrawableTop;
    private Drawable mDrawableBottom;

    private OnDrawableClickListener mClickListener;

    private boolean mConsumeEvent  = false;
    private boolean mDownTouch     = false;
    private boolean mInDevelopment = false;
    private int mExtraArea = 0;

    public DrawableTextView(Context activeContext, AttributeSet attrSet, int defStyle) {
        super(activeContext, attrSet, defStyle);
    }

    public DrawableTextView(Context activeContext, AttributeSet attrSet) {
        super(activeContext, attrSet);
    }

    public DrawableTextView(Context activeContext) {
        super(activeContext);
    }

    public void consumeEvent() {
        this.setConsumeEvent(true);
    }

    public void setConsumeEvent(boolean eventConsumed) {
        this.mConsumeEvent = eventConsumed;
    }

    public void developmentMode() {
        this.setDevelopmentMode(true);
    }

    public void setDevelopmentMode(boolean inDevelopment) {
        this.mInDevelopment = inDevelopment;
    }

    public void setExtraArea(int areaInDP) {
        int areaInPixel = 0;

        if(areaInDP < 0) {
            areaInPixel = getResources().getDimensionPixelSize(R.dimen.tv_fuzzArea);
        } else {
            areaInPixel = Math.round(areaInDP * getResources()
                    .getDisplayMetrics().density);
        }

        this.mExtraArea = areaInPixel;
    }

    public int getExtraArea() {
        return mExtraArea;
    }

    @Override
    public void setCompoundDrawables(Drawable drawableLeft, Drawable drawableTop,
                                                Drawable drawableRight, Drawable drawableBottom) {
        if(drawableRight != null) {
            mDrawableRight = drawableRight;
        }

        if(drawableLeft != null) {
            mDrawableLeft  = drawableLeft;
        }
        super.setCompoundDrawables(drawableLeft, drawableTop,
                drawableRight, drawableBottom);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if(!mInDevelopment) {
            switch(motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownTouch = true;
                    return true;

                case MotionEvent.ACTION_UP:
                    if (mDownTouch) {
                        mDownTouch = false;

                        Rect drawableBounds;

                        int x = (int) motionEvent.getX();
                        int y = (int) motionEvent.getY();

                        if (mDrawableLeft != null) {
                            drawableBounds = mDrawableLeft.getBounds();

                            if (drawableBounds.contains(x - mExtraArea, y - mExtraArea)) {
                                mClickListener.onClick(OnDrawableClickListener
                                        .DrawablePosition.Left);

                                if (mConsumeEvent) {
                                    cancelEvent(motionEvent);
                                    return false;
                                }
                            }
                        } else if (mDrawableRight != null) {
                            drawableBounds = mDrawableRight.getBounds();
                            if (x >= (this.getRight() - drawableBounds.width() - mExtraArea) && x <= (this.getRight() - this.getPaddingRight() + mExtraArea)
                                    && y >= (this.getPaddingTop() - mExtraArea) && y <= (this.getHeight() - this.getPaddingBottom()) + mExtraArea) {

                                mClickListener.onClick(OnDrawableClickListener
                                        .DrawablePosition.Right);

                                if (mConsumeEvent) {
                                    cancelEvent(motionEvent);
                                    return false;
                                }
                            }
                        } else if (mDrawableTop != null) {

                        } else if (mDrawableBottom != null) {

                        }
                    }
            }
        }

        return super.onTouchEvent(motionEvent);
    }

    @Override
    protected void finalize() throws Throwable {
        mDrawableRight  = null;
        mDrawableBottom = null;
        mDrawableLeft   = null;
        mDrawableTop    = null;

        super.finalize();
    }

    private void cancelEvent(MotionEvent motionEvent) {
        motionEvent.setAction(MotionEvent.ACTION_CANCEL);
    }

    public void setOnDrawableClickListener(OnDrawableClickListener drawableListener) {
        this.mClickListener = drawableListener;
    }

    public void setDefaults() {
        setConsumeEvent(true);
        setExtraArea(-1);
    }

    public interface OnDrawableClickListener {

        public static enum DrawablePosition {
            Top, Bottom, Left, Right
        };

        public void onClick(DrawablePosition drawableTarget);

    }

}
