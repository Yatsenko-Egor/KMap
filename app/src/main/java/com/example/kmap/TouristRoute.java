package com.example.kmap;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class TouristRoute implements Parcelable {
    public String name;
    public ArrayList<MapPoint> points;
    public String info;

    public TouristRoute(String name, String info, ArrayList<MapPoint> points) {
        this.name = name;
        this.points = points;
        this.info = info;
    }

    public TouristRoute() {
        this.name = "";
        this.points = new ArrayList<>();
        this.info = "";
    }

    protected TouristRoute(Parcel in) {
        name = in.readString();
        info = in.readString();
        points = in.createTypedArrayList(MapPoint.CREATOR);
    }

    public static final Creator<TouristRoute> CREATOR = new Creator<TouristRoute>() {
        @Override
        public TouristRoute createFromParcel(Parcel in) {
            return new TouristRoute(in);
        }

        @Override
        public TouristRoute[] newArray(int size) {
            return new TouristRoute[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(info);
        dest.writeTypedList(points);
    }

    public String getName() {
        return name;
    }

    public ArrayList<MapPoint> getPoints() {
        return points;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPoints(ArrayList<MapPoint> points) {
        this.points = points;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}