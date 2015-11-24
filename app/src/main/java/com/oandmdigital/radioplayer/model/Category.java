package com.oandmdigital.radioplayer.model;

public class Category {

    private String id;
    private String title;
    private String description;
    private String slug;


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSlug() {
        return slug;
    }


    @Override
    public String toString() {
        return "#" + getId() + " : " + getTitle();
    }

}
