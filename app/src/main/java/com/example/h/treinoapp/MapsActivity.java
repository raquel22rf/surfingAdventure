package com.example.h.treinoapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.SlidingDrawer;
import android.widget.Switch;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Criteria;
import android.content.Context;
import android.location.Geocoder;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MapsActivity extends AppCompatActivity implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMarkerClickListener,
        OnMarkerDragListener,
        GoogleMap.OnCameraChangeListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int ONE_MINUTE = 1000*60;
    private static final int LOW_AMOUNT = 5;
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    private LatLng latLng;
    private String lat_Lng;
    private LocationManager locManager;
    private boolean isCreator;

    private String currentUserID;

    private int totalAdventures;

    private DatabaseReference databaseAdventures;

    private List<Adventure> adventures;
    //private List<Marker> markers;
    private Map<String, Marker> markers;

    private DatabaseReference databaseCurrentUser;

    private List<String> ownAdventures;
    private Date start, end;



    private FusedLocationProviderClient mFusedLocationProviderClient;

    //---------------------------------- Session Variables ----------------------------------
    private FirebaseAuth firebaseAuth;
    //------------------------------------      end       -----------------------------------

    //------------------------------------ Menu Variables -----------------------------------
    private DrawerLayout mDL;
    private ActionBarDrawerToggle mToogle;
    private NavigationView mNV;
    //------------------------------------      end       -----------------------------------

    //------------------------------------- GPS Variables -----------------------------------

    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;
    private Location lastLocation;

    BroadcastReceiver batteryLow;
    BroadcastReceiver batteryOk;
    //------------------------------------      end       -----------------------------------

    //------------------------------------Button Variables-----------------------------------

    private Button b1, b2;
    private Button filters, clear;
    private Switch s1, s2;
    private PopupWindow popupWindow;
    private DatePickerDialog datePickerDialog1, datePickerDialog2;
    private View popupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = getFusedLocationProviderClient(this);

        isCreator();

        adventures = new ArrayList<>();
        //markers = new ArrayList<>();
        markers = new HashMap<>();
        ownAdventures = new ArrayList<>();

        //------------------------------- Session Control -----------------------------------

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            //reencaminhar para página de login
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        //--------------------------------     end      -------------------------------------

        //------------------------------------ Menu  ----------------------------------------



        mDL = (DrawerLayout) findViewById(R.id.drawerMap);
        mToogle = new ActionBarDrawerToggle(this, mDL, R.string.Open, R.string.Close);
        mDL.addDrawerListener(mToogle);
        mToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.navigation_viewMap);
        navigationView.setNavigationItemSelectedListener(this);



        //--------------------------------     end      -------------------------------------

        //------------------------------ ENERGY CHALLENGE  ----------------------------------

        //location
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //intervals for location updates
        locationRequest.setInterval(ONE_MINUTE / 2);
        locationRequest.setFastestInterval(ONE_MINUTE / 4);

        //battery
        batteryLow = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                locationRequest.setFastestInterval(ONE_MINUTE/2);
                locationRequest.setInterval(ONE_MINUTE);
            }
        };

        batteryOk = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setFastestInterval(ONE_MINUTE / 6);
                locationRequest.setInterval(ONE_MINUTE / 2);
            }
        };

        IntentFilter filterLow = new IntentFilter(Intent.ACTION_BATTERY_LOW);
        registerReceiver(batteryLow, filterLow);

        IntentFilter filterOk = new IntentFilter(Intent.ACTION_BATTERY_OKAY);
        registerReceiver(batteryOk, filterOk);

        //--------------------------------     end      -------------------------------------
    }

    private void isCreator(){
        final String activeUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        currentUserID = activeUserID;
        databaseCurrentUser = FirebaseDatabase.getInstance().getReference("users/" + currentUserID);
        databaseCurrentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Boolean isUserCreator = dataSnapshot.child("isCreator").getValue(Boolean.class);
                isCreator = isUserCreator;

                mNV = findViewById(R.id.navigation_viewMap);
                Menu nav_menu = mNV.getMenu();
                mNV.bringToFront();

                if (isCreator){
                    nav_menu.findItem(R.id.my_wannabes).setVisible(true);
                    nav_menu.findItem(R.id.nav_create).setVisible(true);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationProviderClient.removeLocationUpdates(this.mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(batteryLow);
        unregisterReceiver(batteryOk);
        super.onDestroy();
    }

    private void startLocationUpdates() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient client = LocationServices.getSettingsClient(this);
        client.checkLocationSettings(locationSettingsRequest);

        try {
            getFusedLocationProviderClient(MapsActivity.this)
                    .requestLocationUpdates(locationRequest, mLocationCallback = new LocationCallback() {

                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if(lastLocation != null) {

                                //If the difference between the last and current location is bigger
                                //then 2 then the user is walking faster then expected (is moving)
                                // and we need to change the rate at which we ask for a location update.
                                if (locationResult.getLastLocation().distanceTo(lastLocation) >= 2) {
                                    locationRequest.setFastestInterval(ONE_MINUTE/6);
                                    locationRequest.setInterval(ONE_MINUTE/2);
                                    System.out.println(" maior que 2 logo meto mais rapido ------------------------------------------>"+locationRequest.toString());
                                } else {
                                    locationRequest.setFastestInterval(ONE_MINUTE);
                                    locationRequest.setInterval(ONE_MINUTE * 2);
                                    System.out.println(" menor que 2 logo meto mais lento ------------------------------------------>"+locationRequest.toString());
                                }
                            }
                            lastLocation = locationResult.getLastLocation();

                        }
                    }, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);

        enableMyLocation();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showPopup(View v) {
        if (popupView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            popupView = layoutInflater.inflate(R.layout.filters_list, null);
            popupView.setBackgroundColor(getResources().getColor(R.color.White));

            popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            popupWindow.setBackgroundDrawable(popupView.getBackground());
            popupWindow.setOutsideTouchable(true);
            popupWindow.setTouchable(true);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    popupWindow.dismiss();
                }

            });
        }
        popupWindow.showAsDropDown(v);

        enableFilters();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void enableFilters() {
        if(popupWindow.isShowing()) {
            View v = popupView;
            s1 = v.findViewById(R.id.switch1);
            s2 = v.findViewById(R.id.switch2);
            b1 = v.findViewById(R.id.start_date);
            b2 = v.findViewById(R.id.end_date);
            clear = v.findViewById(R.id.clear);
        }

        s1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    // Hide markers
                    for (Adventure a : adventures) {
                        if (a.getHasCosts() && markers.get(a.getAdvID()) != null)
                            markers.get(a.getAdvID()).setVisible(false);
                    }
                } else {
                    // The toggle is disabled
                    // Show markers
                    for (Adventure a : adventures) {
                        boolean temp = ((Switch) popupView.findViewById(R.id.switch2)).isChecked();
                        if ((!temp || temp && ownAdventures.contains(a.getAdvID())) && (start == null || start.before(a.getAdvDate())) && (end == null || end.after(a.getAdvDate())))
                            if (markers.get(a.getAdvID()) != null)
                                markers.get(a.getAdvID()).setVisible(true);
                    }
                }
            }
        });

        s2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    // Hide markers
                    List<String> difference = new ArrayList<>(adventures.size());
                    for (Adventure a : adventures)
                        difference.add(a.getAdvID());
                    difference.removeAll(ownAdventures);
                    for (String a : difference)
                        if (markers.get(a) != null)
                            markers.get(a).setVisible(false);

                } else {
                    // The toggle is disabled
                    // Show markers
                    for (Adventure a : adventures) {
                        boolean temp = ((Switch) popupView.findViewById(R.id.switch1)).isChecked();
                        if ((!temp || temp && !a.getHasCosts()) && (start == null || start.before(a.getAdvDate())) && (end == null || end.after(a.getAdvDate())))
                            if (markers.get(a.getAdvID()) != null)
                                markers.get(a.getAdvID()).setVisible(true);
                    }
                }
            }
        });

        datePickerDialog1 = new DatePickerDialog(this, 0);
        datePickerDialog1.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, dayOfMonth);
                start = newDate.getTime();
                b1.setText(new SimpleDateFormat("dd-MM-yyyy").format(newDate.getTime()));
                for(Adventure a : adventures) {
                    boolean temp1 = ((Switch) popupView.findViewById(R.id.switch1)).isChecked();
                    boolean temp2 = ((Switch) popupView.findViewById(R.id.switch2)).isChecked();
                    if ((!temp1 || temp1 && !a.getHasCosts()) &&
                            (!temp2 || temp2 && ownAdventures.contains(a.getAdvID())) &&
                            (start.before(a.getAdvDate())) && (end == null || end.after(a.getAdvDate())))
                        if (markers.get(a.getAdvID()) != null)
                            markers.get(a.getAdvID()).setVisible(true);
                    if(a.getAdvDate().before(start) && markers.get(a.getAdvID()) != null)
                        markers.get(a.getAdvID()).setVisible(false);
                }
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog1.show();
            }
        });

        datePickerDialog2 = new DatePickerDialog(this, 0);
        datePickerDialog2.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, dayOfMonth);
                end = newDate.getTime();
                b2.setText(new SimpleDateFormat("dd-MM-yyyy").format(newDate.getTime()));
                for(Adventure a : adventures) {
                    boolean temp1 = ((Switch) popupView.findViewById(R.id.switch1)).isChecked();
                    boolean temp2 = ((Switch) popupView.findViewById(R.id.switch2)).isChecked();
                    if ((!temp1 || temp1 && !a.getHasCosts()) &&
                            (!temp2 || temp2 && ownAdventures.contains(a.getAdvID())) &&
                            (start == null || start.before(a.getAdvDate())) && (end.after(a.getAdvDate())))
                        if (markers.get(a.getAdvID()) != null)
                            markers.get(a.getAdvID()).setVisible(true);
                    if(a.getAdvDate().after(newDate.getTime()) && markers.get(a.getAdvID()) != null)
                        markers.get(a.getAdvID()).setVisible(false);
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog2.show();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s1.setChecked(false);
                s2.setChecked(false);
                b1.setText(R.string.start_date);
                b2.setText(R.string.end_date);
                datePickerDialog1.dismiss();
                datePickerDialog2.dismiss();
                start = null;
                end = null;
                for(Marker m : markers.values())
                    m.setVisible(true);
            }
        });

    }


        /**
         * Enables the My Location layer if the fine location permission has been granted.
         */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {

            //get current location
            Location locationCt;
            LocationManager locationManagerCt = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationCt = locationManagerCt.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            getDeviceLocation();

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            //enable location
            mMap.setMyLocationEnabled(true);

        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location mLastKnownLocation = (Location) task.getResult();
                        latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), 13));

                        lat_Lng = latLng.latitude + "_" + latLng.longitude;
                        getInformationOfDatabase();
                    } else {
                        Location mLastKnownLocation = (Location) task.getResult();
                        latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(	51.050407, 	13.737262), 15));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        lat_Lng = latLng.latitude + "_" + latLng.longitude;
                        getInformationOfDatabase();
                    }



                }
            });
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }


    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    private void getInformationOfDatabase() {

        filters = new Button(this);
        filters.setText("Show filters");
        addContentView(filters, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        filters.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (popupWindow != null) {
                    if (!popupWindow.isShowing())
                        showPopup(v);
                    else
                        popupWindow.dismiss();
                } else
                    showPopup(v);
            }

        });


            totalAdventures = 30000;

        LatLng sw = new LatLng(latLng.latitude - 	0.01, latLng.longitude - 	0.01);
        LatLng ne = new LatLng(latLng.latitude + 	0.01, latLng.longitude + 	0.01);

        LatLngBounds bounds = new LatLngBounds(sw, ne);
        String low_range = bounds.southwest.latitude + "_" + bounds.southwest.longitude;
        String hight_range = bounds.northeast.latitude + "_" + bounds.southwest.longitude;


        addMarkers(low_range, hight_range);

        if (adventures.size() <= LOW_AMOUNT){
            LatLng s = new LatLng(latLng.latitude - 1, latLng.longitude - 1);
            LatLng n = new LatLng(latLng.latitude + 1, latLng.longitude + 1);

            LatLngBounds bounds1 = new LatLngBounds(s, n);
            String low_range1 = bounds1.southwest.latitude + "_" + bounds1.southwest.longitude;
            String hight_range1 = bounds1.northeast.latitude + "_" + bounds1.southwest.longitude;

            addMarkers(low_range1, hight_range1);

        }
    }

    private void addMarkers(String low_range, String hight_range){
        databaseAdventures = FirebaseDatabase.getInstance().getReference("adventures");

        DatabaseReference databaseCurrentUserAdventures = FirebaseDatabase.getInstance().getReference("users/" + currentUserID + "/adventures");

        databaseCurrentUserAdventures.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String id = postSnapshot.child("id").getValue(String.class);
                    if(!ownAdventures.contains(id))
                    ownAdventures.add(id);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        databaseAdventures.orderByChild("lat_Lng").startAt(low_range).endAt(hight_range)
                //.limitToFirst(totalAdventures)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        //getting adventure
                        final String advID = dataSnapshot.child("advID").getValue(String.class);

                        if(!adventures.contains(advID)) {

                            String advName = dataSnapshot.child("advName").getValue(String.class);
                            String advDescription = dataSnapshot.child("advDescription").getValue(String.class);
                            Date advDate = dataSnapshot.child("advDate").getValue(Date.class);
                            double advLatitude = dataSnapshot.child("advLatitude").getValue(Double.class);
                            double advLongitude = dataSnapshot.child("advLongitude").getValue(Double.class);
                            String advPlace = dataSnapshot.child("advPlace").getValue(String.class);
                            List advComments = dataSnapshot.child("advComments").getValue(List.class);
                            String advLatLng = dataSnapshot.child("lat_Lng").getValue(String.class);
                            int advDurationHours = dataSnapshot.child("durationHours").getValue(Integer.class);
                            int advDurationMinutes = dataSnapshot.child("durationMinutes").getValue(Integer.class);
                            boolean advhasCosts = dataSnapshot.child("hasCosts").getValue(Boolean.class);
                            List advParticipants = dataSnapshot.child("advParticipants").getValue(List.class);
                            int max = dataSnapshot.child("maxPeople").getValue(Integer.class);


                            Adventure adventure = new Adventure(advID, advName, advLatitude, advLongitude, advDescription, advDate, advPlace, advComments, advLatLng, advDurationHours, advDurationMinutes, advhasCosts, advParticipants, max);

                            //adding adventure to the list
                            adventures.add(adventure);

                            boolean isOwner = false;
                            for (String id : ownAdventures) {
                                if (id.equals(advID))
                                isOwner = true;
                            }

                            LatLng latLng = new LatLng(advLatitude, advLongitude);
                            MarkerOptions markerOptions;
                            if (isOwner)
                                markerOptions = new MarkerOptions().position(latLng).title(advName).snippet(advDescription).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            else
                                markerOptions = new MarkerOptions().position(latLng).title(advName).snippet(advDescription);

                            Marker marker = mMap.addMarker(markerOptions);
                            //markers.add(marker);
                            markers.put(advID, marker);
                        }
                    }



                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        System.out.println("Camera moved to " + cameraPosition.target.toString());

        latLng = cameraPosition.target;
        LatLng sw = new LatLng(latLng.latitude - 	0.01, latLng.longitude - 	0.01);
        LatLng ne = new LatLng(latLng.latitude + 	0.01, latLng.longitude + 	0.01);

        LatLngBounds bounds = new LatLngBounds(sw, ne);
        String low_range = bounds.southwest.latitude + "_" + bounds.southwest.longitude;
        String hight_range = bounds.northeast.latitude + "_" + bounds.southwest.longitude;

        markers.clear();
        mMap.clear();
        addMarkers(low_range, hight_range);

        if (adventures.size() <= LOW_AMOUNT) {
            LatLng s = new LatLng(latLng.latitude - 1, latLng.longitude - 1);
            LatLng n = new LatLng(latLng.latitude + 1, latLng.longitude + 1);

            LatLngBounds bounds1 = new LatLngBounds(s, n);
            String low_range1 = bounds1.southwest.latitude + "_" + bounds1.southwest.longitude;
            String hight_range1 = bounds1.northeast.latitude + "_" + bounds1.southwest.longitude;

            addMarkers(low_range1, hight_range1);

        }
    }


    @Override
    public boolean onMarkerClick(Marker m) {


        for (int i = 0; i < markers.size(); i++) {
            if (m.equals(markers.get(i))) {
                final Adventure advClicked = adventures.get(i);
                m.showInfoWindow();

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent intent = new Intent(MapsActivity.this, ViewSingleAdventure.class);
                        intent.putExtra("adventure", advClicked);
                        intent.putExtra("isCreator", isCreator);
                        startActivity(intent);


                    }
                });

                return true;
            }
        }

        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }


    //------------------------------------ Menu Functions -----------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToogle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();


        if (id == R.id.nav_map) {
            finish();
            startActivity(new Intent(MapsActivity.this, MapsActivity.class));
        }
        if (id == R.id.nav_account) {
            finish();
            Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
            intent.putExtra("isCreator", isCreator);
            intent.putExtra("own", true);
            startActivity(intent);
        }
        if (id == R.id.nav_create) {
            finish();
            Intent intent = new Intent(MapsActivity.this, CreateAdventure.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_wannabes) {
            Intent intent = new Intent(MapsActivity.this, ViewWannabes.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_adventures) {
            Intent intent = new Intent(MapsActivity.this, ViewAdventures.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }

        if (id == R.id.nav_help) {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_LONG).show();
        }
        if (id == R.id.nav_settings) {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_LONG).show();
        }
        if (id == R.id.nav_logout) {
            logoutUser();
        }
        return false;
    }

    //------------------------------------      end       -----------------------------------

    //----------------------------------- Log Out Function ----------------------------------

    private void logoutUser() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }


    //------------------------------------      end       -----------------------------------



}