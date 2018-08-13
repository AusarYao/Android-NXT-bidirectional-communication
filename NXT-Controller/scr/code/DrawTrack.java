package com.example.auser.nxtcontroller;
/*
*
* This class is used to draw the point of data collected from NXT
*
*
*/
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.constraint.solver.widgets.Rectangle;

import java.util.ArrayList;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class DrawTrack extends DrawComponent {

    private ArrayList<PointF> mCoordinateList = new ArrayList<>();
    private ArrayList<Integer> mRawDataList = new ArrayList<>();
    private int mCapacity = 21; // There are 21 scans per set of data, If you change this number, change the program on the NXT side as well
    private DrawAxis mDrawAxis;
    private int mPointSize = 14;

    public DrawTrack(DrawAxis drawAxis){
        mDrawAxis = drawAxis;
        mRawDataList.clear();
        mCoordinateList.clear();
    }

    public boolean updateData(int distance){
        mRawDataList.add(distance);
        int count = mRawDataList.size();
        if(count <= mCapacity) {
            int tmpAngle = (count - 1) * 9;
            mCoordinateList.add(mDrawAxis.Convert2Coordinate(tmpAngle, distance));
            return TRUE;
        }else{
            return FALSE;
        }
    }

    @Override
    public void regularDraw(Canvas canvas){
        if (mCoordinateList.size() != 0) {
            drawLineCenter(canvas);
            drawLineCoor(canvas);
            drawPoint(canvas);
        }

    }

    private void drawLineCenter(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        if(mCoordinateList.size() == mCapacity){
            paint.setColor(Color.TRANSPARENT);
        }else{
            paint.setColor(Color.BLUE);

        }
        PointF tmpCoordinate = mCoordinateList.get(mCoordinateList.size() - 1);
        float tmpx = tmpCoordinate.x;
        float tmpy = tmpCoordinate.y;
        PointF center = mDrawAxis.getOrigin();
        canvas.drawLine(center.x, center.y, tmpx, tmpy, paint);


    }

    private void drawLineCoor   (Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(8);
        int tmpSize = mCoordinateList.size() - 1;
        paint.setColor(Color.parseColor("#992929"));
        if (tmpSize != 0) {
            for (int i = 0; i < tmpSize; i++) {
                PointF prePoint = mCoordinateList.get(i),
                        curPoint = mCoordinateList.get(i + 1);
                canvas.drawLine(prePoint.x, prePoint.y, curPoint.x,
                        curPoint.y, paint);
            }

        }

    }

    private void drawPoint(Canvas canvas){
        Paint paint = new Paint();
        paint.setStrokeWidth(mPointSize);
        paint.setColor(Color.parseColor("#56d2c7")); //Color of point drawn

        for (int i = 0; i < mCoordinateList.size(); i++) {
            PointF curPoint = mCoordinateList.get(i);
            canvas.drawPoint(curPoint.x,
                    curPoint.y, paint);
        }
    }

 /*   public ArrayList<Integer> getTrackData(){
        return mRawDataList;

    }*/

}
