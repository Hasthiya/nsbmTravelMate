package models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by ShimaK on 03-May-18.
 */
@IgnoreExtraProperties
public class Trip {

    public String key;
    public String arrival_time;
    public String departure_time;
    public String driver_id;
    public String driver_name;
    public String trip_date;
    public String trip_time;
    public String trip_progress;
    public LatLang trip_starting_point;
    public LatLang trip_ending_point;
    public LatLang trip_current_point;
    public String last_updated_time;

    public Trip() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getArrival_time() {
        return arrival_time;
    }

    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }

    public String getDeparture_time() {
        return departure_time;
    }

    public void setDeparture_time(String departure_time) {
        this.departure_time = departure_time;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getTrip_date() {
        return trip_date;
    }

    public void setTrip_date(String trip_date) {
        this.trip_date = trip_date;
    }

    public String getTrip_time() {
        return trip_time;
    }

    public void setTrip_time(String trip_time) {
        this.trip_time = trip_time;
    }

    public String getTrip_progress() {
        return trip_progress;
    }

    public void setTrip_progress(String trip_progress) {
        this.trip_progress = trip_progress;
    }

    public LatLang getTrip_starting_point() {
        return trip_starting_point;
    }

    public void setTrip_starting_point(LatLang trip_starting_point) {
        this.trip_starting_point = trip_starting_point;
    }

    public LatLang getTrip_ending_point() {
        return trip_ending_point;
    }

    public void setTrip_ending_point(LatLang trip_ending_point) {
        this.trip_ending_point = trip_ending_point;
    }

    public LatLang getTrip_current_point() {
        return trip_current_point;
    }

    public void setTrip_current_point(LatLang trip_current_point) {
        this.trip_current_point = trip_current_point;
    }

    public String getLast_updated_time() {
        return last_updated_time;
    }

    public void setLast_updated_time(String last_updated_time) {
        this.last_updated_time = last_updated_time;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "arrival_time='" + arrival_time + '\'' +
                ", departure_time='" + departure_time + '\'' +
                ", driver_id='" + driver_id + '\'' +
                ", driver_name='" + driver_name + '\'' +
                ", trip_date='" + trip_date + '\'' +
                ", trip_time='" + trip_time + '\'' +
                ", trip_progress='" + trip_progress + '\'' +
                ", trip_starting_point=" + trip_starting_point +
                ", trip_ending_point=" + trip_ending_point +
                '}';
    }
}
