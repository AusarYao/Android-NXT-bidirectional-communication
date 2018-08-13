package com.example.auser.nxtcontroller;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class DrawAxis extends DrawComponent{

    private float mWidth, mHeight;
    private float mRadius, mMaxDistance = 75;
    private PointF mOrigin;

    public DrawAxis(PointF tmpCenter, float width, float height){
        mOrigin = tmpCenter;
        mWidth = width;
        mHeight = height;
        mRadius = width / 2;
    }

    @Override
    public void regularDraw(Canvas canvas){
        Paint tmpPaint = new Paint();
        tmpPaint.setColor(Color.WHITE);
        tmpPaint.setStrokeWidth(5);
        tmpPaint.setStyle(Paint.Style.FILL);
        canvas.drawLine(0, mOrigin.y, mWidth,  mOrigin.y, tmpPaint);
        canvas.drawLine(mOrigin.x, 0, mOrigin.x,  mHeight, tmpPaint);

    }

/*
    public PointF getChangedPoint (PointF point, int distance, int angle){
        float scaledDistance = (float) distance * mScale;
        PointF tmpPointF = point;
        tmpPointF.x = tmpPointF.x + (float) cos(angle) * scaledDistance;
        tmpPointF.y = tmpPointF.y + (float) sin(angle) * scaledDistance;


        return tmpPointF;

    }*/

    public PointF Convert2Coordinate(int angle, int distance)
    {
        float scale = mRadius / mMaxDistance;
        double tmpAngle = PI * (double) angle / 180;
        double mScaled = (double) distance * scale;

        float tmpx = (float) (cos(tmpAngle) * (int) mScaled + mOrigin.x);
        float tmpy = (float) (-sin(tmpAngle) * (int) mScaled + mOrigin.y);

        return new PointF(tmpx, tmpy);

    }

    public PointF getOrigin(){
        return mOrigin;

    }
    public float getRadius(){
        return mRadius;
    }

}
