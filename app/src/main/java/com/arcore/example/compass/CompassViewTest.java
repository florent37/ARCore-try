package com.arcore.example.compass;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class CompassViewTest extends AppCompatImageView implements OnAzimuthListener, Animation.AnimationListener {
    private float mCurrentAzimuth;
    private boolean mIsProcessing = false;


    public CompassViewTest(Context context) {
        super(context);
    }

    public CompassViewTest(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassViewTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void onAzimuthChanged(float newAzimuth) {
        rotateTo(newAzimuth);
    }

    private void rotateTo(float newAzimuth) {
        if (!mIsProcessing) {
            mIsProcessing = true;
            RotateAnimation rotateAnimation = new RotateAnimation(
                    mCurrentAzimuth,
                    -newAzimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            mCurrentAzimuth = -newAzimuth;

            rotateAnimation.setDuration(1300);
            rotateAnimation.setRepeatCount(0);
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setAnimationListener(this);

            this.startAnimation(rotateAnimation);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mIsProcessing = false;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
