package com.example.kmap;

import android.os.Parcel;
import android.os.Parcelable;

public class MapPoint implements Parcelable {
    public double longitude;
    public double latitude;
    public String sight_id;

    public MapPoint() {
        this.longitude = 0;
        this.latitude = 0;
        this.sight_id = "";
    }

    public MapPoint(double longitude, double latitude, String sight_id) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.sight_id = sight_id;
    }

    public MapPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.sight_id = "";
    }

    protected MapPoint(Parcel in) {
        longitude = in.readDouble();
        latitude = in.readDouble();
        sight_id = in.readString();
    }

    public static final Creator<MapPoint> CREATOR = new Creator<MapPoint>() {
        @Override
        public MapPoint createFromParcel(Parcel in) {
            return new MapPoint(in);
        }

        @Override
        public MapPoint[] newArray(int size) {
            return new MapPoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(sight_id);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getSight_id() {
        return sight_id;
    }

    public void setSight_id(String sight_id) {
        this.sight_id = sight_id;
    }
}