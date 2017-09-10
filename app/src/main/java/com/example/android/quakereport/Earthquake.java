package com.example.android.quakereport;

/**
 * Created by nirmal vats on 6/17/2017.
 */

public class Earthquake {

    private double mMagnitude;
    private String mPlace;
    private long mDate;
    private String muri;

    public Earthquake(double magnitude, String place, long date,String uri){
        mMagnitude=magnitude;
        mPlace=place;
        mDate=date;
        muri=uri;
    }

    public double getmMagnitude(){
        return mMagnitude;
    }

    public String getmPlace(){
        return mPlace;
    }

    public long getmDate(){
        return mDate;
    }

    public String getmuri(){
        return muri;
    }

}
