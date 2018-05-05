package com.example.hasthi.nsbmtravelmate.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hasthi.nsbmtravelmate.R;

import java.util.List;

import models.Trip;

/**
 * Created by Hasthi on 5/6/2018.
 */

public class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.ViewHolder> {

    private Context context;
    private List<Trip> trip;

    public TimeTableAdapter(Context context, List<Trip> trip) {
        this.context = context;
        this.trip = trip;
    }

    @Override
    public TimeTableAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_time_table_block, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TimeTableAdapter.ViewHolder holder, final int position) {

        holder.arrivalTime.setText(trip.get(position).getArrival_time());
        holder.driverName.setText(trip.get(position).getDriver_name());
        holder.date.setText(trip.get(position).getTrip_date());
        holder.departureTime.setText(trip.get(position).getDeparture_time());
        holder.time.setText(trip.get(position).getTrip_time());


    }

    @Override
    public int getItemCount() {
        return trip.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView arrivalTime;
        public TextView driverName;
        public TextView date;
        public TextView departureTime;
        public TextView time;




        public ViewHolder(View itemView) {
            super(itemView);
            arrivalTime = itemView.findViewById(R.id.arrival_time);
            driverName = itemView.findViewById(R.id.driver_name);
            date = itemView.findViewById(R.id.date);
            departureTime = itemView.findViewById(R.id.departure_time);
            time = itemView.findViewById(R.id.time);


        }

    }

}
