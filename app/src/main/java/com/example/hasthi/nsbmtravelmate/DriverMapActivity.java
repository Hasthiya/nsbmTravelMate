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

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = ">>>>";

    private Button tripButton;
    private Spinner spinner;

    private Context thisContext;
    Boolean mRequestingLocationUpdates;

    private GoogleMap mMap;
    List<Polyline> lines = new ArrayList<>();
    List<Marker> markers = new ArrayList<>();

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

    private FetchUrl fetchUrl;

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
                selectedTrip = trips.get(i);
                updateRouteInfo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        loadTrip();
    }

    private Boolean isTripSelected() {
        return selectedTrip != null;
    }

    private Boolean isTripsAvailable() {
        return trips.size() > 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTripSelected()) {
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

        for (int x = 0; x < trips.size(); x++) {
            spinnerArray.add(trips.get(x).getKey());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerArray);
        spinner.setAdapter(adapter);
    }

    private void startTrip() {
        tripButton.setText(R.string.stop_trip_button);
        isTripStarted = true;

        startMapActivity();
        loadRouteInMap();
        updateRouteInfo();
    }

    private void stopTrip() {
        isTripStarted = false;
        tripButton.setText(R.string.start_trip_button);

        clearMap();
        cancelBackgroundTasks();
        stopMapActivity();
        updateRouteInfo();
    }

    private void clearMap() {
        if (lines.size() > 0) {
            lines.get(0).remove();
            lines.clear();
        }

        if (markers.size() > 0) {
            for (Marker marker : markers) {
                marker.remove();
            }
        }
    }

    private void cancelBackgroundTasks() {
        if (fetchUrl != null && fetchUrl.isCancelled()) {
            fetchUrl.cancel(true);
        }
    }

    private void updateRouteInfo() {
        routeInfo = new RouteInfo();
        routeInfo.setDriver_id(driverKey);
        routeInfo.setDriverAvailable(isTripStarted);

        mRouteDatabase.child(selectedTrip.getKey()).setValue(routeInfo);
    }

    private void loadRouteInMap() {
        // Getting URL to the Google Directions API
        String url = getUrl(selectedTrip.getTrip_starting_point().toLatLng(), selectedTrip.getTrip_ending_point().toLatLng());
        fetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        fetchUrl.execute(url);
    }

    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        thisContext = context;
        return super.onCreateView(name, context, attrs);
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
                    busMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.getLatitude(), latLng.getLongitude())).title("Marker in Sydney"));
                    busMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_bus));
                    busMarker.setAnchor(0.5f, 0.5f);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latLng.getLatitude(), latLng.getLongitude())));
                }

                busMarker.setPosition(new LatLng(latLng.getLatitude(), latLng.getLongitude()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.getLatitude(), latLng.getLongitude()), 14), 1500, null);

                if (isTripSelected() && isTripStarted) {
//                    selectedTrip.setTrip_current_point(latLng);
//                    mDatabase.child(selectedTrip.getKey()).setValue(selectedTrip);
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
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            //TODO Display Error Message
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

    private void addPolyLinesToMap(PolylineOptions lineOptions) {
        markers.add(mMap.addMarker(new MarkerOptions().position(selectedTrip.getTrip_starting_point().toLatLng())));
        markers.add(mMap.addMarker(new MarkerOptions().position(selectedTrip.getTrip_ending_point().toLatLng())));
        lines.add(mMap.addPolyline(lineOptions));
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        ParserTask parserTask;

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (parserTask != null) {
                parserTask.cancel(true);
            }
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);

            if (parserTask != null) {
                parserTask.cancel(true);
            }
        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                addPolyLinesToMap(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

}
