package com.example.hasthi.nsbmtravelmate;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.hasthi.nsbmtravelmate.adapters.TimeTableAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import models.Trip;

public class TimeTableActivity extends AppCompatActivity {

    private ArrayList<Trip> trips;
    private DatabaseReference mTimeTableDatabase;
    private TimeTableAdapter adapter;
    private Context c;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        rv = findViewById(R.id.time_table_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        trips = new ArrayList<>();

        mTimeTableDatabase = FirebaseDatabase.getInstance().getReference("timeTable");
        ValueEventListener postListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Trip trip = snapshot.getValue(Trip.class);
                        if (trip != null) {
                            trips.add(trip);
                        }
                    }

                    adapter = new TimeTableAdapter(c, (ArrayList<Trip>) trips);
                    rv.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mTimeTableDatabase.addValueEventListener(postListener2);

    }
}
