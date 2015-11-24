package com.oandmdigital.radioplayer.event;

import com.oandmdigital.radioplayer.model.Category;

public class PostCategoryEvent {

    private Category category;

    public PostCategoryEvent(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }


}
