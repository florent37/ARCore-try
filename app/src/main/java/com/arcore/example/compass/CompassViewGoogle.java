package com.arcore.example.compass;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.arcore.example.R;
import com.arcore.example.compass.custom.MathUtils;

public class CompassViewGoogle extends View implements OnAzimuthListener {

    private static final double BITMAP_SCALE_FACTOR = 0.25;
    private static final double SCALE_FACTOR = 1.3;
    private float mAzimuth = 359f;
    private int mPrevAzimuth;
    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private Bitmap mArrowBitmap;
    private Matrix mMatrix;
    private int centerX;
    private int centerY;
    private float mHeading;
    private float mAnimatedHeading;

    private int mBitMapWidth;
    private static final float MIN_DISTANCE_TO_ANIMATE = 15.0f;

    private int mBitMapHeight;
    private final ValueAnimator mAnimator;

    private static final String LOG_TAG = CompassViewGoogle.class.getSimpleName();

    public CompassViewGoogle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAnimator = new ValueAnimator();
        setupAnimator();
        init();
    }

    public CompassViewGoogle(Context context) {
        super(context);

        mAnimator = new ValueAnimator();
        setupAnimator();

        init();
    }

    public CompassViewGoogle(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAnimator = new ValueAnimator();
        setupAnimator();
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mMatrix = new Matrix();

        prepareBitmap();
        updateMatrix();
    }

    private void prepareBitmap() {
        mArrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.red_arrow);
        mBitMapWidth = (int) (mArrowBitmap.getWidth() * BITMAP_SCALE_FACTOR);
        mBitMapHeight = (int) (mArrowBitmap.getHeight() * BITMAP_SCALE_FACTOR);
        mArrowBitmap = Bitmap.createScaledBitmap(mArrowBitmap, mBitMapWidth, mBitMapHeight, true);

        mWidth = (int) (mBitMapWidth * SCALE_FACTOR);
        mHeight = (int) (mBitMapHeight * SCALE_FACTOR);

        centerX = mWidth / 2 - mBitMapWidth / 2;
        centerY = mHeight / 2 - mBitMapHeight / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mArrowBitmap, mMatrix, mPaint);
    }

    private void setAzimuth(float newAzimuth) {
        int intAzimuth = (int) newAzimuth;
        if (mPrevAzimuth != intAzimuth) {
            mPrevAzimuth = intAzimuth;
            mAzimuth = newAzimuth;
            updateMatrix();

//            animateTo();


//            invalidate();
            Log.e(LOG_TAG, "azimuth changed " + newAzimuth);
        }
    }

    public void updateMatrix() {
        mMatrix.reset();
        mMatrix.postRotate(mAzimuth, mBitMapWidth / 2, mBitMapHeight / 2);
        mMatrix.postTranslate(centerX, centerY);
    }

    private void animateTo(float end) {
        // Only act if the animator is not currently running. If the user's orientation changes
        // while the animator is running, we wait until the end of the animation to update the
        // display again, to prevent jerkiness.
        if (!mAnimator.isRunning()) {
            float start = mAnimatedHeading;
            float distance = Math.abs(end - start);
            float reverseDistance = 360.0f - distance;
            float shortest = Math.min(distance, reverseDistance);

            if (Float.isNaN(mAnimatedHeading) || shortest < MIN_DISTANCE_TO_ANIMATE) {
                // If the distance to the destination angle is small enough (or if this is the
                // first time the compass is being displayed), it will be more fluid to just redraw
                // immediately instead of doing an animation.
                mAnimatedHeading = end;
                invalidate();
            } else {
                // For larger distances (i.e., if the compass "jumps" because of sensor calibration
                // issues), we animate the effect to provide a more fluid user experience. The
                // calculation below finds the shortest distance between the two angles, which may
                // involve crossing 0/360 degrees.
                float goal;

                if (distance < reverseDistance) {
                    goal = end;
                } else if (end < start) {
                    goal = end + 360.0f;
                } else {
                    goal = end - 360.0f;
                }

                mAnimator.setFloatValues(start, goal);
                mAnimator.start();
            }
        }
    }

    private void setupAnimator() {
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(250);

        // Notifies us at each frame of the animation so we can redraw the view.
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mAnimatedHeading = MathUtils.mod((Float) mAnimator.getAnimatedValue(), 360.0f);
                invalidate();
            }
        });

        // Notifies us when the animation is over. During an animation, the user's head may have
        // continued to move to a different orientation than the original destination angle of the
        // animation. Since we can't easily change the animation goal while it is running, we call
        // animateTo() again, which will either redraw at the new orientation (if the difference is
        // small enough), or start another animation to the new heading. This seems to produce
        // fluid results.
        mAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animator) {
                animateTo(mHeading);
            }
        });
    }

    @Override
    public void onAzimuthChanged(float newAzimuth) {
        mHeading = MathUtils.mod(newAzimuth, 360.0f);
        animateTo(mHeading);
//        setAzimuth(newAzimuth);
    }
//
//    @Override
//    public void onAzimuthChanged(GoogleCompassSensor orientationManager) {
//        onAzimuthChanged(orientationManager.getHeading());
//    }
//
//    @Override
//    public void onLocationChanged(GoogleCompassSensor orientationManager) {
//
//    }
//    @Override
//    public void onAccuracyChanged(GoogleCompassSensor orientationManager) {
//
//    }

}
