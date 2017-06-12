package com.example.abdel.raye7challenge;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,AsyncRespond {

    GoogleMap myMap;
    EditText fromEditText, toEditText;
    Button swapButton, searchButton;
    LocationManager locationManager;
    List<List<List<LatLng>>> routesList = new ArrayList<>();
    Menu menu;
    int currentSelectedRouteIndex = -1;
    Polyline routePolyline = null;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm.init(this);
        realm = Realm.getDefaultInstance();

        fromEditText = (EditText) findViewById(R.id.from_editText);
        toEditText = (EditText) findViewById(R.id.to_editText);
        swapButton = (Button) findViewById(R.id.swap_button);
        searchButton = (Button) findViewById(R.id.search_location_button);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //*********************************** on click listeners ************************//
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromText = fromEditText.getText().toString();
                fromEditText.setText(toEditText.getText().toString());
                toEditText.setText(fromText);
                EditNewLocationsOnMap();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditNewLocationsOnMap();
            }
        });


    }


    void EditNewLocationsOnMap()
    {
        myMap.clear();
        String fromText = fromEditText.getText().toString(),
                toText = toEditText.getText().toString();
        LatLng sourceLocation = null, destinationLocation = null;


        //********************************** Search Data LatLng Type *************************************//

        if (LocationDataType(fromText) == 0)
        {
            sourceLocation = ConvertStringToLatLng(fromText);
            myMap.addMarker(new MarkerOptions().position(sourceLocation));
        }

        if (LocationDataType(toText) == 0)
        {
            destinationLocation = ConvertStringToLatLng(toText);
            myMap.addMarker(new MarkerOptions().position(destinationLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }



        //********************************** Search Data Address Type *************************************//

        if (!fromText.equals(null) && !fromText.equals("") && sourceLocation == null)//******** validation ***********//
        {

            Address searchAddress = searchForLocations(fromText);

            if (searchAddress != null) {
                sourceLocation = new LatLng(searchAddress.getLatitude(), searchAddress.getLongitude());

                myMap.addMarker(new MarkerOptions().position(sourceLocation));
            }
        }


        if (!toText.equals(null) && !toText.equals("") && destinationLocation == null)//******** validation ***********//
        {

            Address searchAddress = searchForLocations(toText);

            if (searchAddress != null) {
                destinationLocation = new LatLng(searchAddress.getLatitude(), searchAddress.getLongitude());

                myMap.addMarker(new MarkerOptions().position(destinationLocation)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                );
            }
        }


        //************************** setup map camera to be focused on source and destination markers

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean locationIncluded = false;

        if (sourceLocation != null)
        {
            builder.include(sourceLocation);
            locationIncluded = true;
        }
        else
            fromEditText.setText("");


        if (destinationLocation != null)
        {
            builder.include(destinationLocation);
            locationIncluded = true;
        }
        else
            toEditText.setText("");



        if(locationIncluded)
        {
            int offset = 150;    //offset to the bounds of the camera
            myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), offset));
        }

        routesList.clear();

        MenuItem menuItem = menu.findItem(R.id.show_routes_menu_item);
        menuItem.setTitle("Generate Routes");
        currentSelectedRouteIndex = -1;
        routePolyline = null;
    }



    //this function is a very simple algorithm to determine the type of the data in the edittext whether it is latlng or address
    //simply it checks if the string has all numbers then it is latlng
    //returns 0 for latlng and 1 for address
    int LocationDataType(String location)
    {
        if (location.length() == 0)
            return 1;

        char[] arr = location.toCharArray();
        for(int i=0;i<location.length();i++)
        {
            if (arr[i] != '0' && arr[i] != '1' && arr[i] != '2' && arr[i] != '3' && arr[i] != '4' && arr[i] != '5' && arr[i] != '6' && arr[i] != '7' && arr[i] != '8' && arr[i] != '9' && arr[i] != ',' && arr[i] != '+' && arr[i] != '-')
                return 1;//address type
        }

        return 0;//latlng type
    }

    LatLng ConvertStringToLatLng (String location)
    {
        String[] parts = location.split(",");
        return new LatLng(Double.parseDouble(parts[0]),Double.parseDouble(parts[1]));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;

        setupMap();
    }

    void setupMap()
    {


        //***************************** set destination by holding the map **************************************//
        myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng destinationLocation) {

                toEditText.setText(destinationLocation.latitude + "," + destinationLocation.longitude);
                EditNewLocationsOnMap();

            }
        });


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            return;
        }

        myMap.setMyLocationEnabled(true);

        myMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                Location currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(),true));

                if(currentLocation != null)
                {
                    fromEditText.setText(currentLocation.getLatitude() + "," + currentLocation.getLongitude());
                    EditNewLocationsOnMap();
                }

                return true;
            }
        });

    }

    Address searchForLocations(String location)
    {
        Geocoder geocoder = new Geocoder(MainActivity.this);//takes context
        List<Address> searchAddressList = null;
        try {
            searchAddressList = geocoder.getFromLocationName(location,1);//only get one location - getfromlocationname returns a list
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (searchAddressList.size() == 0 || searchAddressList == null)
            return null;

        return searchAddressList.get(0); //it has only 1 address
    }


    void DrawDirection(int index)
    {

        if (routesList.size() == 0 || routesList == null)
            return;

        List<LatLng> points;
        PolylineOptions polylineOptions = new PolylineOptions();

        List<List<LatLng>> path = routesList.get(index);

        for(int i=0;i<path.size();i++)
        {
            points = path.get(i);

            polylineOptions.addAll(points);
            polylineOptions.width(10);
            polylineOptions.color(Color.RED);
        }

        routePolyline = myMap.addPolyline(polylineOptions);
    }


    @Override
    public void processFinished(List<List<List<LatLng>>> r) {
        routesList = r;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if(id == R.id.show_routes_menu_item)
        {
            String generate = "Generate Routes", show = "Show Routes";
            if (item.getTitle().equals(generate))
            {
                String fromText = fromEditText.getText().toString(),
                        toText = toEditText.getText().toString();

                RouteDataAsyncTask routeDataAsyncTask = new RouteDataAsyncTask(getString(R.string.google_direction_API_key),MainActivity.this);
                routeDataAsyncTask.execute(fromText,toText);

                item.setTitle(show);
            }

            else if (routesList.size() != 0 && routesList != null)
            {
                //show a routes list , user can choose one

                ListView routesListView = new ListView(this);

                setRoutesListView(routesListView);

            }

        }

        else if (id == R.id.all_time_trip_menu_item)
        {
            Intent intent = new Intent(this,AllTimeTripActivity.class);
            startActivity(intent);
        }

        else if (id == R.id.show_traffic_menu_item)
        {
            String enable = "Enable Traffic", disable = "Disable Traffic";

            if (item.getTitle().equals(enable))
            {
                myMap.setTrafficEnabled(true);      //Bonus traffic data
                item.setTitle(disable);
            }

            else        //equals disable
            {
                myMap.setTrafficEnabled(false);
                item.setTitle(enable);
            }
        }

        else if (id == R.id.book_trip_menu_item)
        {
            setBookingDialog();     //sets the layout of the booking dialog and its functionality
        }


        return super.onOptionsItemSelected(item);
    }

    void setRoutesListView(ListView listView)
    {

        //get items
        List<String> routesNames = new ArrayList<>();
        for(int i=0;i<routesList.size();i++)
            routesNames.add("Route "+ (i+1));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        adapter.addAll(routesNames);
        listView.setAdapter(adapter);



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Route");
        builder.setView(listView);
        final Dialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (routePolyline != null)  //there is a drawn route
                    routePolyline.remove();

                dialog.dismiss();
                currentSelectedRouteIndex = position;
                DrawDirection(currentSelectedRouteIndex);
            }
        });

    }

    void BookTrip(final String location, final String date, final String time)//crates an object and add it to realm database
    {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Trip trip = realm.createObject(Trip.class);
                trip.setLocation(location);
                trip.setDate(date);
                trip.setTime(time);
            }
        });
    }

    void setBookingDialog()
    {

        final Dialog bookingDialog = new Dialog(MainActivity.this);
        bookingDialog.setContentView(R.layout.pick_date_time_layout);
        bookingDialog.setTitle("Select Date and TIme");

        final TextView dateTextView = (TextView) bookingDialog.findViewById(R.id.booking_date_textview);
        final TextView timeTextView = (TextView) bookingDialog.findViewById(R.id.booking_time_button);
        Button dateButton = (Button) bookingDialog.findViewById(R.id.booking_date_button);
        final Button timeButton = (Button) bookingDialog.findViewById(R.id.booking_time_button);
        Button bookingButton = (Button) bookingDialog.findViewById(R.id.booking_trip_button);
        Button cancelButton = (Button) bookingDialog.findViewById(R.id.canceling_trip_button);

        bookingDialog.show();

        //************************************* booking dialog on click listeners ********************************************//

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();

                int cYear = calendar.get(Calendar.YEAR);
                int cMonth = calendar.get(Calendar.MONTH);
                int cDay  = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateTextView.setText(dayOfMonth + "-" + month + "-" + year);

                    }
                },cYear,cMonth,cDay);

                datePickerDialog.show();
            }
        });



        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();

                int cHour = calendar.get(Calendar.HOUR_OF_DAY);
                int cMinute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        timeTextView.setText(hourOfDay + ":" + minute);

                    }
                },cHour,cMinute,false);

                timePickerDialog.show();
            }
        });



        bookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dateTextView.getText().equals("Date") && !timeTextView.getText().equals("Time"))
                {
                    BookTrip(fromEditText.getText() + " To " + toEditText.getText(),dateTextView.getText().toString(),timeTextView.getText().toString());
                    bookingDialog.dismiss();
                }
            }
        });



        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookingDialog.dismiss();
            }
        });

    }
}
