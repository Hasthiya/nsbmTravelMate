package com.example.hasthi.nsbmtravelmate;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import controllers.DataParser;
import models.LatLang;
import models.RouteInfo;
import models.Trip;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private static final String TAG = ">>>>";

    private Button tripButton;
    private Spinner spinner;

    private Context thisContext;
    private Boolean mRequestingLocationUpdates;

    private GoogleMap mMap;
    private List<Polyline> lines = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private LocationSettingsRequest mLocationSettingsRequest;
    private SettingsClient mSettingsClient;
    private FusedLocationProviderClient mFusedLocationClient;

    private ArrayList<Trip> trips = new ArrayList<>();
    private Trip selectedTrip;
    private RouteInfo routeInfo;
    private String driverKey;
    private boolean isTripStarted;

    private DatabaseReference mDatabase;
    private DatabaseReference mRouteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mDatabase = FirebaseDatabase.getInstance().getReference("timeTable");
        mRouteDatabase = FirebaseDatabase.getInstance().getReference("available_drivers");
        driverKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRequestingLocationUpdates = true;

        tripButton = findViewById(R.id.tripButton);
        spinner = findViewById(R.id.spinner);

        tripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTripStarted) {
                    if (isTripsAvailable() && isTripSelected()) {
                        startTrip();
                    }
                } else {
                    stopTrip();
                }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    selectedTrip = trips.get(i - 1);
                    updateRouteInfo();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        loadTrip();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTripSelected()) {
            startLocationUpdates();
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        thisContext = context;
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private Boolean isTripSelected() {
        return selectedTrip != null;
    }

    private Boolean isTripsAvailable() {
        return trips.size() > 0;
    }

    private void loadTrip() {
        mDatabase.orderByChild("driver_id").equalTo(driverKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Trip trip = snapshot.getValue(Trip.class);

                    if (trip != null) {
                        trip.setKey(snapshot.getKey());
                        trips.add(trip);
                    }
                }

                if (isTripsAvailable()) {
                    onTripRetrievedSuccess();
                } else {
                    Log.wtf(TAG, "Trip Failed");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(TAG, ">>>>>>On Cancelled");
            }
        });
    }

    private void onTripRetrievedSuccess() {
        startMapActivity();
        updateSpinner();
    }

    private void updateSpinner() {
        List<String> spinnerArray = new ArrayList<>();

        spinnerArray.add("Select A Route");

        for (int x = 0; x < trips.size(); x++) {
            String name = trips.get(x).getDisplay_name();
            if (name == null) {
                name = trips.get(x).getKey();
            }

            spinnerArray.add(name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerArray);
        spinner.setAdapter(adapter);
    }

    private void startTrip() {
        tripButton.setText(R.string.stop_trip_button);
        isTripStarted = true;
        spinner.setVisibility(View.INVISIBLE);

        startMapActivity();
        loadRouteInMap();
        updateRouteInfo();
        updateTrip();
    }

    private void stopTrip() {
        isTripStarted = false;
        tripButton.setText(R.string.start_trip_button);
        spinner.setVisibility(View.VISIBLE);
        spinner.setSelection(0);

        clearMap();
        stopMapActivity();
        updateRouteInfo();
        updateTrip();
    }

    private void clearMap() {
        for (Polyline line : lines) {
            line.remove();
        }

        for (Marker marker : markers) {
            marker.remove();
        }

        lines.clear();
        markers.clear();
    }

    private void updateRouteInfo() {
        routeInfo = new RouteInfo();
        routeInfo.setDriver_id(driverKey);
        routeInfo.setDriverAvailable(isTripStarted);

        mRouteDatabase.child(selectedTrip.getKey()).setValue(routeInfo);
    }

    private void updateTrip() {
        if (isTripStarted) {
            selectedTrip.setTrip_status(Trip.TRIP_STATUS_STARTED);
        } else {
            selectedTrip.setTrip_status(Trip.TRIP_STATUS_FINISHED);
        }

        mDatabase.child(selectedTrip.getKey()).setValue(selectedTrip);
    }

    private void loadRouteInMap() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(selectedTrip.getTrip_starting_point().toLatLng(), selectedTrip.getTrip_ending_point().toLatLng())
                .build();
        routing.execute();
    }

    private void startMapActivity() {
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationUpdate();
    }

    private void stopMapActivity() {
        mLocationCallback = null;
        mLocationRequest = null;
        mLocationSettingsRequest = null;
    }

    Marker busMarker = null;
    private boolean isMapZoomed;

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location mCurrentLocation = locationResult.getLastLocation();
                LatLang latLng = new LatLang(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                if (!isMapZoomed) {
                    isMapZoomed = true;
                    busMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.getLatitude(), latLng.getLongitude())));
                    busMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_bus));
                    busMarker.setAnchor(0.5f, 0.5f);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latLng.getLatitude(), latLng.getLongitude())));
                }

                busMarker.setPosition(new LatLng(latLng.getLatitude(), latLng.getLongitude()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.getLatitude(), latLng.getLongitude()), 14), 1500, null);

                if (isTripSelected() && isTripStarted) {
                    routeInfo.setCurrent_location(latLng);
                    mRouteDatabase.child(selectedTrip.getKey()).setValue(routeInfo);
                }
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void checkLocationUpdate() {
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(DriverMapActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    34);
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(thisContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(thisContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ");
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(DriverMapActivity.this, 0x1);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(DriverMapActivity.this, errorMessage, Toast.LENGTH_LONG).show();
//                                mRequestingLocationUpdates = false;
                        }
                    }
                });
    }

    private void addPolyLinesToMap(PolylineOptions lineOptions) {
        lineOptions.width(10);
        lineOptions.color(Color.RED);

        markers.add(mMap.addMarker(new MarkerOptions().position(selectedTrip.getTrip_starting_point().toLatLng())));
        markers.add(mMap.addMarker(new MarkerOptions().position(selectedTrip.getTrip_ending_point().toLatLng())));
        lines.add(mMap.addPolyline(lineOptions));
    }

    private void showToast(String message) {
        Toast.makeText(thisContext, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null){
            showToast("Error " + e.getMessage());
        } else {
            showToast("Something Went Wrong Try Again");
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(arrayList.get(0).getPoints());
        addPolyLinesToMap(polylineOptions);
    }

    @Override
    public void onRoutingCancelled() {

    }

}
