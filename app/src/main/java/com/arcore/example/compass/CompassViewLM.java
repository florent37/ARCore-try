package com.arcore.example.compass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.arcore.example.R;

public class CompassViewLM extends View implements OnAzimuthListener {

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
    private int mBitMapWidth;
    private int mBitMapHeight;
    private static final String LOG_TAG = CompassViewLM.class.getSimpleName();

    public CompassViewLM(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CompassViewLM(Context context) {
        super(context);
        init();
    }

    public CompassViewLM(Context context, AttributeSet attrs) {
        super(context, attrs);
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
            invalidate();
            Log.e(LOG_TAG, "azimuth changed " + newAzimuth);
        }
    }

    public void updateMatrix() {
        mMatrix.reset();
        mMatrix.postRotate(mAzimuth, mBitMapWidth / 2, mBitMapHeight / 2);
        mMatrix.postTranslate(centerX, centerY);
    }

    @Override
    public void onAzimuthChanged(float newAzimuth) {
        setAzimuth(newAzimuth);
    }
}
