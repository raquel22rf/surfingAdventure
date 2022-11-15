package com.example.h.treinoapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewAdventures extends AppCompatActivity implements Serializable, NavigationView.OnNavigationItemSelectedListener{

    private DatabaseReference databaseAdventures;
    private DatabaseReference databaseCurrentUserAdventures;
    private ListView listViewAdventures;
    private List<Adventure> adventures, totalAdventures;
    private List<String> adventuresID;
    private List<Comment> comments;
    private List<Object> participants;
    private String currentUserID;
    private boolean isCreator;

    //---------------------------------- Session Variables ----------------------------------
    private FirebaseAuth firebaseAuth;
    //------------------------------------      end       -----------------------------------

    //------------------------------------ Menu Variables -----------------------------------
    private DrawerLayout mDL;
    private ActionBarDrawerToggle mToogle;
    //------------------------------------      end       -----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_view_adventures);

        Intent intent = getIntent();
        isCreator = (boolean) intent.getSerializableExtra("isCreator");
        //------------------------------- Session Control -----------------------------------

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            //reencaminhar para página de login
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        //--------------------------------     end      -------------------------------------

        //------------------------------------ Menu  ----------------------------------------

        mDL = (DrawerLayout) findViewById(R.id.drawerAdv);
        mToogle = new ActionBarDrawerToggle(this, mDL, R.string.Open, R.string.Close);
        mDL.addDrawerListener(mToogle);
        mToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_viewAdv);
        navigationView.setNavigationItemSelectedListener(this);

        Menu nav_menu = navigationView.getMenu();

        System.out.println("------isCreatorMap----------------------" + isCreator);
        if (isCreator){
            nav_menu.findItem(R.id.my_wannabes).setVisible(true);
            nav_menu.findItem(R.id.nav_create).setVisible(true);
        }
        //--------------------------------     end      -------------------------------------



        listViewAdventures = (ListView) findViewById(R.id.listViewAdventures);
        adventures = new ArrayList<>();
        comments = new ArrayList<>();
        adventuresID = new ArrayList<>();
        totalAdventures = new ArrayList<>();
        participants = new ArrayList<>();

        listViewAdventures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //getting the selected adventure

                Adventure adv = adventures.get(i);

                Intent intent = new Intent(ViewAdventures.this, ViewSingleAdventure.class);
                intent.putExtra("adventure", adv);
                intent.putExtra("isCreator", isCreator);

                startActivity(intent);

                //starting the activity with intent
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        final String activeUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserID = activeUserID;

        //get users adventures id
        databaseCurrentUserAdventures = FirebaseDatabase.getInstance().getReference("users/" + currentUserID + "/adventures");
        databaseCurrentUserAdventures.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adventuresID.clear();

                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String advId = postSnapshot.child("id").getValue(String.class);
                    adventuresID.add(advId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //get all adventures
        databaseAdventures = FirebaseDatabase.getInstance().getReference("adventures");
        databaseAdventures.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //clearing the previous adventures list
                totalAdventures.clear();

                //iterating through all the nodes
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    //getting adventure
                    String advID = postSnapshot.child("advID").getValue(String.class);
                    String advName = postSnapshot.child("advName").getValue(String.class);
                    String advDescription = postSnapshot.child("advDescription").getValue(String.class);
                    Date advDate = postSnapshot.child("advDate").getValue(Date.class);
                    double advLatitude = postSnapshot.child("advLatitude").getValue(Double.class);
                    double advLongitude = postSnapshot.child("advLongitude").getValue(Double.class);
                    String advPlace = postSnapshot.child("advPlace").getValue(String.class);
                    String lat_Lng = postSnapshot.child("lat_Lng").getValue(String.class);
                    int advDurationHours = postSnapshot.child("durationHours").getValue(Integer.class);
                    int advDurationMinutes = postSnapshot.child("durationMinutes").getValue(Integer.class);
                    boolean advhasCosts = postSnapshot.child("hasCosts").getValue(Boolean.class);
                    Integer max = postSnapshot.child("maxPeople").getValue(Integer.class);


                    //create a new adventure
                    Adventure adventure = new Adventure(advID, advName, advLatitude, advLongitude, advDescription, advDate, advPlace, comments, lat_Lng,  advDurationHours,advDurationMinutes, advhasCosts, participants, max);
                    //adding adventure to the list
                    totalAdventures.add(adventure);
                }

                adventures.clear();

                //match the ids
                for (int i = 0; i < adventuresID.size(); i++){
                    for (int j = 0; j < totalAdventures.size(); j++){
                        if (adventuresID.get(i).equals(totalAdventures.get(j).getAdvID())){
                            adventures.add(totalAdventures.get(j));
                        }
                    }
                }

                //creating adapter
                AdventuresList artistAdapter = new AdventuresList(ViewAdventures.this, adventures);
                //attaching adapter to the listview
                listViewAdventures.setAdapter(artistAdapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
            startActivity(new Intent(ViewAdventures.this, MapsActivity.class));
        }
        if (id == R.id.nav_account) {
            finish();
            Intent intent = new Intent(ViewAdventures.this, ProfileActivity.class);
            intent.putExtra("isCreator", isCreator);
            intent.putExtra("own", true);
            startActivity(intent);
        }
        if (id == R.id.nav_create) {
            finish();
            Intent intent = new Intent(ViewAdventures.this, CreateAdventure.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_wannabes) {
            Intent intent = new Intent(ViewAdventures.this, ViewWannabes.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_adventures) {
            Intent intent = new Intent(ViewAdventures.this, ViewAdventures.class);
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
