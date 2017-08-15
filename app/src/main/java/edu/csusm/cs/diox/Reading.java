package edu.csusm.cs.diox;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Steven on 8/10/2017.
 */

public class Reading implements Parcelable{
    private Location mLocation;

    public long getTimeEpochSeconds() {
        return mTimeEpochSeconds;
    }

    public void setTimeEpochSeconds(long timeEpochSeconds) {
        mTimeEpochSeconds = timeEpochSeconds;
    }

    public int getBeaconID() {
        return mBeaconID;
    }

    public void setBeaconID(int beaconID) {
        mBeaconID = beaconID;
    }

    public double getConcentrationPPM() {
        return mConcentrationPPM;
    }

    public void setConcentrationPPM(double concentrationPPM) {
        mConcentrationPPM = concentrationPPM;
    }

    private long mTimeEpochSeconds;
    private int mBeaconID;
    private double mConcentrationPPM;

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags){
        p.writeLong(mTimeEpochSeconds);
        p.writeInt(mBeaconID);
        p.writeParcelable(mLocation,0);
        p.writeDouble(mConcentrationPPM);
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public Reading(long time, int beacon, Location location, double concentration){
        mTimeEpochSeconds = time;
        mBeaconID = beacon;
        mLocation = location;
        mConcentrationPPM = concentration;
    }

    private Reading(Parcel in){
        mTimeEpochSeconds = in.readLong();
        mBeaconID = in.readInt();
        mLocation = Location.CREATOR.createFromParcel(in);
        mConcentrationPPM = in.readDouble();
    }

    public static final Parcelable.Creator<Reading> CREATOR
            = new Parcelable.Creator<Reading>(){
        public Reading createFromParcel(Parcel in){
            return new Reading(in);
        }
        public Reading[] newArray(int size){
            return new Reading[size];
        }
    };
}
