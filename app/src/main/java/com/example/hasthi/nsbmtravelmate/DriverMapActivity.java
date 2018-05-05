package com.example.hasthi.nsbmtravelmate;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import models.LatLang;
import models.Trip;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = ">>>>";

    private Button tripButton;

    private Context thisContext;
    Boolean mRequestingLocationUpdates;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private Location mLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private LocationSettingsRequest mLocationSettingsRequest;
    private SettingsClient mSettingsClient;
    private FusedLocationProviderClient mFusedLocationClient;

    private Trip selectedTrip;
    private String driverKey;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mDatabase = FirebaseDatabase.getInstance().getReference("timeTable");
        driverKey = getIntent().getStringExtra("DRIVER_KEY");

        mRequestingLocationUpdates = true;

        tripButton = findViewById(R.id.tripButton);

        loadTrip();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedTrip != null) {
            startLocationUpdates();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void loadTrip() {
       mDatabase.orderByChild("driver_id").equalTo(driverKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Trip trip = null;

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    trip = snapshot.getValue(Trip.class);
                    trip.setKey(snapshot.getKey());

                    break;
                }

                if (trip != null) {
                    onTripRetrievedSuccess(trip);
                } else {
                    Log.wtf(TAG,"Trip Failed");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(TAG, ">>>>>>On Cancelled");
            }
        });
    }

    private void onTripRetrievedSuccess(Trip trip) {
        selectedTrip = trip;
        selectedTrip.setTrip_starting_point(new LatLang(6.942635, 79.864718));
        selectedTrip.setTrip_ending_point(new LatLang(6.874161, 79.891023));
        mDatabase.child(trip.getKey()).setValue(trip);
        startMapActivity();

//        // Getting URL to the Google Directions API
//        String url = getDirectionsUrl(trip.getTrip_starting_point().toLatLng(), trip.trip_ending_point.toLatLng());
//
//        DownloadTask downloadTask = new DownloadTask();
//
//        // Start downloading json data from Google Directions API
//        downloadTask.execute(url);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        thisContext = context;
        return super.onCreateView(name, context, attrs);
    }

    public void startMapActivity() {
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationUpdate();
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
                    busMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.getLatitude(), latLng.getLongitude())).title("Marker in Sydney"));
                    busMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_bus));
                    busMarker.setAnchor(0.5f, 0.5f);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latLng.getLatitude(), latLng.getLongitude())));
                }

                busMarker.setPosition(new LatLng(latLng.getLatitude(), latLng.getLongitude()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.getLatitude(), latLng.getLongitude()), 14), 1500, null);

                selectedTrip.setTrip_current_point(latLng);
                mDatabase.child(selectedTrip.getKey()).setValue(selectedTrip);
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
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
//            showSnackbar("Location Permission",
//                    "Ok", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            // Request permission
//                            ActivityCompat.requestPermissions(MapsActivity.this,
//                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                                    REQUEST_PERMISSIONS_REQUEST_CODE);
//                        }
//                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(DriverMapActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    34);
        }
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

}
