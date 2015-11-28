package com.oandmdigital.radioplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Stream implements Parcelable {

    private String stream;
    private Integer bitrate;
    private String contentType;
    private Integer status;

    public String getStream() {
        return stream;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public String getContentType() {
        return contentType;
    }

    public Integer getStatus() {
        return status;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.stream);
        dest.writeValue(this.bitrate);
        dest.writeString(this.contentType);
        dest.writeValue(this.status);
    }

    public Stream() {  }

    protected Stream(Parcel in) {
        this.stream = in.readString();
        this.bitrate = (Integer) in.readValue(Integer.class.getClassLoader());
        this.contentType = in.readString();
        this.status = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<Stream> CREATOR = new Parcelable.Creator<Stream>() {
        public Stream createFromParcel(Parcel source) {
            return new Stream(source);
        }

        public Stream[] newArray(int size) {
            return new Stream[size];
        }
    };
}
