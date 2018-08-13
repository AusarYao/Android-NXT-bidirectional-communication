package com.example.auser.nxtcontroller;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class DrawRadarArc extends DrawComponent  {

    private float mMaxRadius;
    private PointF mCircleCenter;
    private int mRegularDrawTimes = 3; //Default number of semi-circles per arc

    public DrawRadarArc(float radius, PointF center){
        mMaxRadius = radius;
     //   maxRadarDistance = distance;
        mCircleCenter = center;

    }


    private void drawFrame(Canvas canvas, float radius){
        float mCenter_x = mCircleCenter.x,
                mCenter_y = mCircleCenter.y;
        Paint mPaint = new Paint();

        Path path = new Path();
        path.addCircle(radius,
                radius, radius,
                Path.Direction.CW);

        mPaint.setColor(Color.parseColor("#0ca5c5"));
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.FILL);


        final RectF oval = new RectF();
        mPaint.setStyle(Paint.Style.STROKE);

        oval.set(mCenter_x - radius,
                mCenter_y - radius,
                mCenter_x + radius,
                mCenter_y + radius);
        canvas.drawArc(oval, 180, 180, false, mPaint);

    }

    @Override
    public void regularDraw (Canvas canvas){
        numberBasedFrame(canvas, mRegularDrawTimes);

    }

    public void numberBasedFrame (Canvas canvas, int numOfCircle){

        float minimumRadius = (float) (mMaxRadius / numOfCircle);
        for ( int i = 0; i < numOfCircle; i++ ){
            float tmpRadius = minimumRadius * (float)(i + 1);
            drawFrame(canvas, tmpRadius);

        }

    }
/*
    public void gapBasedFrame (Canvas canvas, int gap){

        int numOfCircle = maxRadarDistance / gap;

        float pxGap = PXCMConverter.cm2px(gap);

        for ( int i = 0; i < numOfCircle; i++ ){
            float tmpRadius = mMaxRadius - (float)(pxGap * i);
            drawFrame(canvas, tmpRadius);

        }

    }*/

}
