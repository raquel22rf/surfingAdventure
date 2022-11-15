package com.example.h.treinoapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.location.Geocoder;
import android.location.Address;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class CreateAdventure extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener  {
    private static final String TAG = "CreateAdventure";

    private Button buttonCreateAdventure;
    private EditText createAdventureName, createAdventurePlace, createAdventureDescription, maxPeople;
    private TextView createAdventureDate, createAdventureDuration;
    private Date date, duration;

    private Address adventureAddress;

    private boolean isCreator, hasCosts;

    private DatabaseReference databaseAdventures;
    private DatePickerDialog.OnDateSetListener dListener1, dListener2;
    private TimePickerDialog.OnTimeSetListener tListener1, tListener2;

    private FirebaseAuth firebaseAuth;

    //------------------------------------ Menu Variables -----------------------------------
    private DrawerLayout mDL;
    private ActionBarDrawerToggle mToogle;
    //------------------------------------      end       -----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //setContentView();
        setContentView(R.layout.activity_create_adventure);

        Intent intent = getIntent();
        isCreator = (boolean) intent.getSerializableExtra("isCreator");

        databaseAdventures = FirebaseDatabase.getInstance().getReference("adventures");

        buttonCreateAdventure = (Button) findViewById(R.id.buttonCreateAdventure);
        createAdventureName = (EditText) findViewById(R.id.createAdventureName);
        createAdventurePlace = (EditText) findViewById(R.id.createAdventurePlace);
        createAdventureDescription = (EditText) findViewById(R.id.createAdventureDescription);
        createAdventureDate = (TextView) findViewById(R.id.createAdventureDate);
        createAdventureDuration = (TextView) findViewById(R.id.createAdventureDuration);
        maxPeople = (EditText) findViewById(R.id.maxPeople);


        buttonCreateAdventure.setOnClickListener(this);
        createAdventureDate.setOnClickListener(this);
        createAdventureDuration.setOnClickListener(this);

        date = new Date();
        duration = new Date();


        dListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d(TAG, "onDateSet: date: " + year + "/" + month + "/" + dayOfMonth);
                System.out.println("------------------+++++++++" + year);
                date.setYear(year);
                date.setMonth(month);
                date.setDate(dayOfMonth);
               //System.out.println("-----------------------data----" + year);
            }
        };

        tListener1 = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                date.setHours(hourOfDay);
                date.setMinutes(minute);
            }
        };

        tListener2 = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                duration.setHours(hourOfDay);
                duration.setMinutes(minute);

                String date1 = duration.getHours() + "h" + duration.getMinutes();
                createAdventureDuration.setText(date1);
            }
        };

        dListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + dayOfMonth + "/" + year);

                String date1 = dayOfMonth + "/" + month + "/" + year + "   " + date.getHours() + ":" + date.getMinutes();
                createAdventureDate.setText(date1);
            }
        };



        //------------------------------- Session Control -----------------------------------

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            //reencaminhar para página de login
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        //--------------------------------     end      -------------------------------------

        //-----------------------------------  Menu  ----------------------------------------

        mDL = (DrawerLayout) findViewById(R.id.drawerCreate);
        mToogle = new ActionBarDrawerToggle(this, mDL, R.string.Open, R.string.Close);
        mDL.addDrawerListener(mToogle);
        mToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_viewCreate);
        navigationView.setNavigationItemSelectedListener(this);

        Menu nav_menu = navigationView.getMenu();

        if (isCreator){
            nav_menu.findItem(R.id.my_wannabes).setVisible(true);
            nav_menu.findItem(R.id.nav_create).setVisible(true);
        }

        //--------------------------------     end      -------------------------------------

    }

    public void onClick(View view){
        if(view == createAdventureDate){
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            DatePickerDialog dialog = new DatePickerDialog(
                    CreateAdventure.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dListener1,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();dialog.show();

            TimePickerDialog dialogT = new TimePickerDialog(
                    CreateAdventure.this,
                    tListener1,
                    hour, minute,
                    DateFormat.is24HourFormat(this)
            );
            dialogT.show();

        }else if(view == buttonCreateAdventure){
            geoLocate();
            addAdventure();
        }else if(view == createAdventureDuration){
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            TimePickerDialog dialogT = new TimePickerDialog(
                    CreateAdventure.this,
                    tListener2,
                    hour, minute,
                    DateFormat.is24HourFormat(this)
            );
            dialogT.show();
        }
    }

    public void checkboxClicked(View view){
        hasCosts = true;
    }

        private void geoLocate(){
            Log.d(TAG, "geoLocate: geolocating");

            String searchString = createAdventurePlace.getText().toString();



            Geocoder geocoder = new Geocoder(CreateAdventure.this);
            List<Address> list = new ArrayList<>();
            try{
                list = geocoder.getFromLocationName(searchString, 1);
            }catch (IOException e){
                Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
            }

            if(list.size() > 0){
                adventureAddress = list.get(0);

                Log.d(TAG, "geoLocate: found a location: " + adventureAddress.toString());
                //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            }

        }

    private void addAdventure(){

        String name = createAdventureName.getText().toString().trim();
        double latitude = adventureAddress.getLatitude();
        double longitude = adventureAddress.getLongitude();
        String place = createAdventurePlace.getText().toString().trim();
        List comments = new ArrayList();
        String lat_Lng = latitude + "_" + longitude;
        List participants = new ArrayList();
        int max = Integer.parseInt(maxPeople.getText().toString());

        String description = createAdventureDescription.getText().toString().trim();



        if(!TextUtils.isEmpty(name)){
            //get adventures'id
            String id = databaseAdventures.push().getKey();

            //create adventure

            Adventure adventure = new Adventure(id, name, latitude, longitude, description, date, place, comments, lat_Lng, duration.getHours(), duration.getMinutes(), hasCosts, participants, max);

            databaseAdventures.child(id).setValue(adventure);

            //add adventure to the user
            final FirebaseDatabase database = FirebaseDatabase.getInstance();

            //get current user id
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref = database.getReference("users/" + userID + "/adventures");
            ref.child(id).child("id").setValue(adventure.getAdvID());

            Toast.makeText(this, "Adventure added", Toast.LENGTH_LONG).show();

            finish();
            startActivity(new Intent(CreateAdventure.this, MapsActivity.class));

            DatabaseReference advRef = database.getReference("adventures/" + id + "/participants" );
            String j = ref.push().getKey();
            advRef.child(j).child("id").setValue(userID);

        }else {
            Toast.makeText(this, "Adventure's Name Needed!", Toast.LENGTH_LONG).show();
        }
    }

    //------------------------------------ Menu Functions -----------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (mToogle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.nav_map) {
            finish();
            startActivity(new Intent(CreateAdventure.this, MapsActivity.class));
        }
        if (id == R.id.nav_account) {
            finish();
            Intent intent = new Intent(CreateAdventure.this, ProfileActivity.class);
            intent.putExtra("isCreator", isCreator);
            intent.putExtra("own", true);
            startActivity(intent);
        }
        if (id == R.id.nav_create) {
            finish();
            Intent intent = new Intent(CreateAdventure.this, CreateAdventure.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_wannabes) {
            Intent intent = new Intent(CreateAdventure.this, ViewWannabes.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_adventures) {
            Intent intent = new Intent(CreateAdventure.this, ViewAdventures.class);
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

    private void logoutUser(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    //------------------------------------      end       -----------------------------------

}
