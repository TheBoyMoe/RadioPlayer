package com.oandmdigital.radioplayer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.event.DownloadCategoriesEvent;
import com.oandmdigital.radioplayer.event.PostCategoryEvent;
import com.oandmdigital.radioplayer.model.Category;
import com.oandmdigital.radioplayer.network.DownloadCategoryThread;

import java.util.List;

import de.greenrobot.event.EventBus;

public class CategoryFragment extends Fragment{

    private ListView listview;
    private CategoryAdapter adapter;

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
                EventBus.getDefault().post(new PostCategoryEvent(category));
            }
        });
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
        //setListAdapter(new CategoryAdapter(event.getCategories()));
        listview.setAdapter(new CategoryAdapter(event.getCategories()));
    }



//    @Override
//    public void onListItemClick(ListView listview, View view, int position, long id) {
//        // retrieve the category object of the list item clicked on
//        Category category = ((CategoryAdapter)getListAdapter()).getItem(position);
//
//        // post the category to the eventbus, retrieve in the hosting activity
//        EventBus.getDefault().post(new PostCategoryEvent(category));
//    }



    // basic ArrayAdapter which displays the category object through the default textview
//    private class CategoryAdapter extends ArrayAdapter<Category> {
//
//        // using the default TextView layout to display the category
//        public CategoryAdapter(List<Category> items) {
//            super(getActivity(), android.R.layout.simple_list_item_1, items);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            View view = super.getView(position, convertView, parent);
//            TextView title = (TextView) view.findViewById(android.R.id.text1);
//            title.setText(getItem(position).toString());
//
//            return view;
//        }
//    }


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
