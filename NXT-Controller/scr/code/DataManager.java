package com.example.auser.nxtcontroller;
/*
 *This class is unfinished. It should be used to manage data from NXT, but now it has no use.
 */

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class DataManager {
    public Bitmap mAutoMapBitmap;
    public ArrayList<PointF> mAutoMapCoordinates = new ArrayList<>();
    private ArrayList<List<PointF>> mAutoMapCoordinatesSet = new ArrayList<>();
    public ArrayList<String> mRawData = new ArrayList<>();
    private int mCount = 0, mDataSetSize = 21;
    public final static float mRadius = 100;
    public final static int mMaxDetectDis = 75;
    private float mScale = mRadius / mMaxDetectDis;
    private ArrayList<ArrayList<String>> mRawDataSet = new ArrayList<>();




}
