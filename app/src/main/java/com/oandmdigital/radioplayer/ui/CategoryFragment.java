package com.oandmdigital.radioplayer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.event.CategoryOnClickEvent;
import com.oandmdigital.radioplayer.event.DownloadCategoriesEvent;
import com.oandmdigital.radioplayer.model.Category;
import com.oandmdigital.radioplayer.network.DownloadCategoryThread;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CategoryFragment extends Fragment{

    private static final String SAVED_CATEGORY_LIST = "list";
    private ListView listview;
    private List<Category> items;
    private CategoryAdapter adapter;


    public static CategoryFragment newInstance() {

        CategoryFragment fragment = new CategoryFragment();
        // Bundle args = new Bundle();

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new DownloadCategoryThread().start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        listview = (ListView) inflater.inflate(R.layout.list_view, container, false);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) parent.getItemAtPosition(position);
                // pass the onClick event up to the hosting activity to deal with
                EventBus.getDefault().post(new CategoryOnClickEvent(category));
            }
        });

        // restore the category list from saved state on device rotation
        if(savedInstanceState != null) {
            List<Category> list = savedInstanceState.getParcelableArrayList(SAVED_CATEGORY_LIST);
            adapter = new CategoryAdapter(list);
            listview.setAdapter(adapter);
        }

        return listview;
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
        items = event.getCategoryList();
        adapter = new CategoryAdapter(items);
        listview.setAdapter(adapter);
    }


    // save the items list on device configuration change
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVED_CATEGORY_LIST, (ArrayList<? extends Parcelable>) items);
    }


    // ArrayAdapter which implements a custom list item and view holder pattern
    private class CategoryAdapter extends ArrayAdapter<Category>{

        // pass in the context and data set to the constructor
        public CategoryAdapter(List<Category> items) {
            super(getActivity(), 0, items);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null)
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item, parent, false);

            ViewHolder holder = (ViewHolder) convertView.getTag();
            if(holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            // populate the holder elements
            Category item = getItem(position);
            holder.name.setText(item.getTitle());

            String description = item.getDescription();
            if(description == null || description.isEmpty())
                description = item.getSlug();
            holder.description.setText(description);

            return convertView;
        }
    }



    private class ViewHolder {

        TextView name;
        TextView description;

        public ViewHolder(View row) {
            name = (TextView) row.findViewById(R.id.name);
            description = (TextView) row.findViewById(R.id.description);
        }

    }



}
