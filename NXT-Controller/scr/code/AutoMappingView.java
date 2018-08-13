package com.example.auser.nxtcontroller;

/*
*The function of this class is to use other draw class to make the a graph of data from NXT
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class AutoMappingView extends View {

    public AutoMappingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public AutoMappingView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public AutoMappingView(Context context) {
        super(context);

    }

    private ArrayList<DrawComponent> mDrawComponents = new ArrayList<>();
    private DrawTrack mCurDrawTrack;
    private boolean isIni = false; // ArrayAdapter in DynamicListView requires context of this view,
                                   //which involves using onLayout again, and this will cause
                                   //DrawTrack to be initialized two times(we only need one time

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(DrawComponent tmpDrawComponent : mDrawComponents){
            tmpDrawComponent.regularDraw(canvas);
        }

    }

    public void updateTrackData(int distance){
        if (!mCurDrawTrack.updateData(distance)){
            mCurDrawTrack = new DrawTrack((DrawAxis) mDrawComponents.get(0));
            mCurDrawTrack.updateData(distance);
            mDrawComponents.add(mCurDrawTrack);

        }
        invalidate();

    }

    /*
        public PointF getCurrentCenter(){
            return mCurrentCenter;

        }
    */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {//This runs before onDraw when AutoMappingView is created
        if (!isIni) {
            float x = (float) (right - left) / 2, y = (float) (bottom - top) / 10 * 9;
            PointF center = new PointF(x, y);
            DrawAxis drawAxis = new DrawAxis(center, right, bottom);
            mDrawComponents.add(drawAxis);
            DrawRadarArc drawRadarArc = new DrawRadarArc(drawAxis.getRadius(), center);
            mDrawComponents.add(drawRadarArc);
            mCurDrawTrack = new DrawTrack(drawAxis);
            mDrawComponents.add(mCurDrawTrack);
            this.setBackgroundColor(Color.BLACK);
            isIni = true;
        }
    }

}