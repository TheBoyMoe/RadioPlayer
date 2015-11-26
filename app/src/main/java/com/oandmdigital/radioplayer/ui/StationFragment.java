package com.oandmdigital.radioplayer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.oandmdigital.radioplayer.R;
import com.oandmdigital.radioplayer.common.LoggingFragment;
import com.oandmdigital.radioplayer.event.DownloadStationsEvent;
import com.oandmdigital.radioplayer.event.StationOnClickEvent;
import com.oandmdigital.radioplayer.model.Category;
import com.oandmdigital.radioplayer.model.Station;
import com.oandmdigital.radioplayer.network.DownloadStationThread;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class StationFragment extends LoggingFragment {

    private static final String STATION_LIST = "station_list";
    public static final String CATEGORY_PARCELABLE = "category";
    private ListView listview;
    private StationAdapter adapter;
    private List<Station> items;
    private Category category;

    public static StationFragment newInstance(Category category) {

        StationFragment fragment = new StationFragment();
        Bundle args = new Bundle();
        args.putParcelable(CATEGORY_PARCELABLE, category);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // retrieve the category object from the arguments bundle
        category = getArguments().getParcelable(CATEGORY_PARCELABLE);

        if(category != null)
            // pass that object to the thread
            new DownloadStationThread("download_station_thread", category.getId()).start();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        listview = (ListView) inflater.inflate(R.layout.list_view, container, false);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Station stn = (Station) parent.getItemAtPosition(position);
                // pass the onCLick event up to the hosting activity to deal with
                EventBus.getDefault().post(new StationOnClickEvent(stn));
            }
        });


        // restore the station list from saved state on device rotation
        if(savedInstanceState != null) {
            List<Station> list = savedInstanceState.getParcelableArrayList(STATION_LIST);
            adapter = new StationAdapter(list);
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
    public void onEventMainThread(DownloadStationsEvent event) {
        // retrieve the stations list from the event bus & instantiate the adapter
        items = event.getStationList();
        adapter = new StationAdapter(items);
        listview.setAdapter(adapter);
    }


    // save the current station list & category object to the bundle on device rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATION_LIST, (ArrayList<? extends Parcelable>) items);
    }


    private class StationAdapter extends ArrayAdapter<Station> {

        // pass in the context and data set to the constructor
        public StationAdapter(List<Station> items) {
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
            Station item = getItem(position);
            holder.name.setText(item.getName());

            String description = item.getSlug();
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
