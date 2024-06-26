package com.example.kmap;

import android.os.Parcel;
import android.os.Parcelable;

public class Sight implements Parcelable {
    public String name;
    public int type;
    public double latitude;
    public double longitude;
    public String info;

    public String id;

    public static final Creator<Sight> CREATOR = new Creator<Sight>() {
        @Override
        public Sight createFromParcel(Parcel in) {
            return new Sight(in);
        }

        @Override
        public Sight[] newArray(int size) {
            return new Sight[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(type);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(info);
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Sight() {
        this.name = "";
        this.type = 0;
        this.latitude = 0;
        this.longitude = 0;
        this.info = "";
        this.id = "";
    }

    public Sight(String name, int type, double latitude, double longitude, String info) {
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.info = info;
    }

    public Sight(String id, String name, int type, double latitude, double longitude, String info) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.info = info;
    }

    protected Sight(Parcel in) {
        name = in.readString();
        type = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        info = in.readString();
        id = in.readString();
    }
}
