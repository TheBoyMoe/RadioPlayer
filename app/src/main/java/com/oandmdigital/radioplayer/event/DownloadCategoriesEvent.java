package com.oandmdigital.radioplayer.event;

import com.oandmdigital.radioplayer.model.Category;

import java.util.ArrayList;


public class DownloadCategoriesEvent {

    private ArrayList<Category> categories;

    public DownloadCategoriesEvent(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public ArrayList<Category> getCategoryList() {
        return categories;
    }
}
