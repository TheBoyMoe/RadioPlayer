package com.oandmdigital.radioplayer.event;

import com.oandmdigital.radioplayer.model.Category;

public class CategoryOnClickEvent {

    private Category category;

    public CategoryOnClickEvent(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }


}
