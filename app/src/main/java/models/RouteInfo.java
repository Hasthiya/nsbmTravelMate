package models;

/**
 * Created by ShimaK on 05-May-18.
 */

public class RouteInfo {

    private String driver_id;
    private boolean isDriverAvailable;
    private LatLang current_location;

    public RouteInfo() {
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public boolean isDriverAvailable() {
        return isDriverAvailable;
    }

    public void setDriverAvailable(boolean driverAvailable) {
        isDriverAvailable = driverAvailable;
    }

    public LatLang getCurrent_location() {
        return current_location;
    }

    public void setCurrent_location(LatLang current_location) {
        this.current_location = current_location;
    }
}
