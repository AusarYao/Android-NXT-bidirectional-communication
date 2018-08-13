package com.example.auser.nxtcontroller;

public final class PXCMConverter {


    private PXCMConverter(){

    }

    public static float cm2px(int cm){
        float returnPx = (float)((double)cm * (double)96 / 2.54);
        return returnPx;

    }

    public static int px2cm (float px){

        int returnCm = (int)((double)px / (double)96 * 2.54);
        return returnCm;

    }

}
