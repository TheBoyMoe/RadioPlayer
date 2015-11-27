package com.oandmdigital.radioplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Station implements Parcelable {

    private Integer id;
    private String name;
    private String country;
    private Image image;
    private String slug;
    private String website;
    private List<Stream> streams = new ArrayList<>();
    private String createdAt;
    private String updatedAt;


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public Image getImage() {
        return image;
    }

    public String getSlug() {
        return slug;
    }

    public String getWebsite() {
        return website;
    }

    public List<Stream> getStreams() {
        return streams;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }


    @Override
    public String toString() {
        return String.format("#%d %s %s", getId(), getName(), getCountry());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeString(this.country);
        dest.writeParcelable(this.image, 0);
        dest.writeString(this.slug);
        dest.writeString(this.website);
        dest.writeTypedList(streams);
        dest.writeString(this.createdAt);
        dest.writeString(this.updatedAt);
    }

    public Station() {
    }

    protected Station(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.name = in.readString();
        this.country = in.readString();
        this.image = in.readParcelable(Image.class.getClassLoader());
        this.slug = in.readString();
        this.website = in.readString();
        this.streams = in.createTypedArrayList(Stream.CREATOR);
        this.createdAt = in.readString();
        this.updatedAt = in.readString();
    }

    public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {
        public Station createFromParcel(Parcel source) {
            return new Station(source);
        }

        public Station[] newArray(int size) {
            return new Station[size];
        }
    };
}
