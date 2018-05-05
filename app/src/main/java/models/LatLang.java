package models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ShimaK on 05-May-18.
 */

public class LatLang {

    private double latitude;
    private double longitude;

    public LatLang() {
    }

    public LatLang(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }
}
