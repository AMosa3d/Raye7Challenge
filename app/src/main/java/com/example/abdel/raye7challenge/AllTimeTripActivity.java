package com.example.abdel.raye7challenge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class AllTimeTripActivity extends AppCompatActivity {

    ListView listView;
    Realm realm;
    TripAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_time_trip);

        listView = (ListView)findViewById(R.id.trips_listView);

        realm.init(this);
        realm = Realm.getDefaultInstance();

        final RealmResults<Trip> trips = (realm.where(Trip.class)).findAll();

        adapter = new TripAdapter(trips,this);
        listView.setAdapter(adapter);
    }
}
