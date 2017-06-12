package com.example.abdel.raye7challenge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by abdel on 6/12/2017.
 */

public class TripAdapter extends BaseAdapter {

    List<Trip> tripsList;
    Context context;

    public TripAdapter(List<Trip> tripsList, Context context) {
        this.tripsList = tripsList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return tripsList.size();
    }

    @Override
    public Object getItem(int position) {
        return tripsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = layoutInflater.inflate(R.layout.list_item,parent,false);

        TextView location = (TextView) convertView.findViewById(R.id.location_textview);
        TextView date = (TextView) convertView.findViewById(R.id.date_textview);
        TextView time = (TextView) convertView.findViewById(R.id.time_textview);

        location.setText(tripsList.get(position).getLocation());
        date.setText(tripsList.get(position).getDate());
        time.setText(tripsList.get(position).getTime());

        return convertView;
    }
}
