package com.oandmdigital.radioplayer.ui;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.oandmdigital.radioplayer.event.DownloadCategoriesEvent;
import com.oandmdigital.radioplayer.event.PostCategoryEvent;
import com.oandmdigital.radioplayer.model.Category;
import com.oandmdigital.radioplayer.network.DownloadCategoryThread;

import java.util.List;

import de.greenrobot.event.EventBus;

public class CategoryFragment extends ListFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new DownloadCategoryThread().start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(DownloadCategoriesEvent event) {
        // bind the category list to the adapter and display
        setListAdapter(new CategoryAdapter(event.getCategories()));
    }


    @Override
    public void onListItemClick(ListView listview, View view, int position, long id) {
        // retrieve the category object of the list item clicked on
        Category category = ((CategoryAdapter)getListAdapter()).getItem(position);

        // post the category to the eventbus, retrieve in the hosting activity
        EventBus.getDefault().post(new PostCategoryEvent(category));
    }

    private class CategoryAdapter extends ArrayAdapter<Category> {

        // using the default TextView layout to display the category
        public CategoryAdapter(List<Category> items) {
            super(getActivity(), android.R.layout.simple_list_item_1, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = super.getView(position, convertView, parent);
            TextView title = (TextView) view.findViewById(android.R.id.text1);
            title.setText(getItem(position).toString());

            return view;
        }
    }


}
