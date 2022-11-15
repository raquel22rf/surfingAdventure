package com.example.h.treinoapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ViewSingleAdventure extends AppCompatActivity implements Serializable, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private Button buttonComments, buttonFollow, buttonUnfollow, buttonFull;
    private Adventure adv;
    private boolean isCreator;
    private List<Object> AdvParticipants;
    private DatabaseReference databaseCurrentParticipants;

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

        setContentView(R.layout.activity_view_single_adventure);

        buttonComments = (Button) findViewById(R.id.buttonComments);
        buttonFollow = (Button) findViewById(R.id.buttonFollow);
        buttonUnfollow = (Button) findViewById(R.id.buttonUnfollow);
        buttonFull = (Button) findViewById(R.id.buttonFull);

        buttonComments.setOnClickListener(this);
        buttonFollow.setOnClickListener(this);
        buttonUnfollow.setOnClickListener(this);
        buttonFull.setOnClickListener(this);


        Intent intent1 = getIntent();
        isCreator = (boolean) intent1.getSerializableExtra("isCreator");

        Intent intent = getIntent();
        adv = (Adventure) intent.getSerializableExtra("adventure");

        //------------------------------- Session Control -----------------------------------

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            //reencaminhar para página de login
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        //--------------------------------     end      -------------------------------------

        //------------------------------------ Menu  ----------------------------------------

        mDL = (DrawerLayout) findViewById(R.id.drawerSAdv);
        mToogle = new ActionBarDrawerToggle(this, mDL, R.string.Open, R.string.Close);
        mDL.addDrawerListener(mToogle);
        mToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_viewSAdv);
        navigationView.setNavigationItemSelectedListener(this);

        Menu nav_menu = navigationView.getMenu();
        System.out.println("---------------single------" + isCreator);
        if (isCreator){
            nav_menu.findItem(R.id.my_wannabes).setVisible(true);
            nav_menu.findItem(R.id.nav_create).setVisible(true);

        }
        //--------------------------------     end      -------------------------------------

        AdvParticipants = new ArrayList<>();

        //get id of participants from adventure
        databaseCurrentParticipants = FirebaseDatabase.getInstance().getReference("adventures/" + adv.getAdvID() + "/participants" );
        databaseCurrentParticipants.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AdvParticipants.clear();

                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String advId = postSnapshot.child("id").getValue(String.class);
                    AdvParticipants.add(advId);
                }

                TextView textViewParticipants = (TextView) findViewById(R.id.textViewParticipants);
                textViewParticipants.setText( AdvParticipants.size() + " / " + adv.getMaxPeople() + " Adventurers");

                //System.out.println("--------------------MAX: " + adv.getMaxPeople() + " ------------------------SIZE: " + AdvParticipants.size());
                if(adv.getMaxPeople() <= AdvParticipants.size()){
                    buttonFollow.setVisibility(View.GONE);
                    buttonFull.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        TextView textViewName = (TextView) findViewById(R.id.textViewName);
        textViewName.setText(adv.getAdvName());
        textViewName.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        //textViewName.setAllCaps(true);

        TextView textViewPlace = (TextView) findViewById(R.id.viewPlace);
        textViewPlace.setText(adv.getAdvPlace());

        TextView textViewDescription = (TextView) findViewById(R.id.textViewDescription);
        textViewDescription.setText(adv.getAdvDescription());

        TextView viewDate = (TextView) findViewById(R.id.viewDate);

        int month = adv.getAdvDate().getMonth()+1;
        int year = adv.getAdvDate().getYear()+1900;

        viewDate.setText(adv.getAdvDate().getDate() + "/" + month + "/" + year + "  at  " + adv.getAdvDate().getHours() + ":" + adv.getAdvDate().getMinutes());

        TextView viewDuration = (TextView) findViewById(R.id.viewDuration);
        viewDuration.setText(adv.getDurationHours() + "h" + adv.getDurationMinutes());

        TextView viewCost = (TextView) findViewById(R.id.viewCost);

        if(adv.getHasCosts()){
            viewCost.setText("Yes");
        }else {
            viewCost.setText("No");
        }



        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = database.getReference("users/" + userID + "/adventures" );
        Query query = ref.orderByChild("id").equalTo(adv.getAdvID());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    if (snapshot.child("id").getValue().toString().equals(adv.getAdvID())){
                        buttonFollow.setVisibility(View.GONE);
                        buttonUnfollow.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {

        TextView textViewParticipants = (TextView) findViewById(R.id.textViewParticipants);

        if (v == buttonComments) {
            finish();

            Intent intent = new Intent(ViewSingleAdventure.this, ViewComments.class);
            intent.putExtra("adventure", adv);
            intent.putExtra("isCreator", isCreator);

            startActivity(intent);
        }
        else if(v == buttonFollow){

            final FirebaseDatabase database = FirebaseDatabase.getInstance();

            //get current user id
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //---------------------------------Add do user list---------------------------------

            DatabaseReference ref = database.getReference("users/" + userID + "/adventures" );

            String i = ref.push().getKey();
            ref.child(i).child("id").setValue(adv.getAdvID());
            //-------------------------------Add do adventure list------------------------------
            DatabaseReference advRef = database.getReference("adventures/" + adv.getAdvID() + "/participants" );

            String j = ref.push().getKey();
            advRef.child(j).child("id").setValue(userID);

            //----------------------------------------------------------------------------------

            buttonFollow.setVisibility(View.GONE);
            buttonUnfollow.setVisibility(View.VISIBLE);
            textViewParticipants.setText( (AdvParticipants.size()+1) + " / " + adv.getMaxPeople() + " Adventurers");
            Toast.makeText(this, "Adventure added!", Toast.LENGTH_SHORT).show();
        }
        else if(v == buttonUnfollow){

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //---------------------------------Remove do user list---------------------------------

            DatabaseReference ref = database.getReference("users/" + userID + "/adventures" );

            Query query = ref.orderByChild("id").equalTo(adv.getAdvID());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //---------------------------------Remove da adventure list----------------------------

            DatabaseReference AdvRef = database.getReference("adventures/" + adv.getAdvID() + "/participants" );

            Query advQuery = AdvRef.orderByChild("id").equalTo(userID);
            advQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            buttonFollow.setVisibility(View.VISIBLE);
            buttonUnfollow.setVisibility(View.GONE);
            Toast.makeText(this, "Adventure left!", Toast.LENGTH_SHORT).show();
            textViewParticipants.setText( (AdvParticipants.size()-1) + " / " + adv.getMaxPeople() + " Adventurers");

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
            startActivity(new Intent(ViewSingleAdventure.this, MapsActivity.class));
        }
        if (id == R.id.nav_account) {
            finish();
            Intent intent = new Intent(ViewSingleAdventure.this, ProfileActivity.class);
            intent.putExtra("isCreator", isCreator);
            intent.putExtra("own", true);
            startActivity(intent);
        }
        if (id == R.id.nav_create) {
            finish();
            Intent intent = new Intent(ViewSingleAdventure.this, CreateAdventure.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_wannabes) {
            Intent intent = new Intent(ViewSingleAdventure.this, ViewWannabes.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_adventures) {
            Intent intent = new Intent(ViewSingleAdventure.this, ViewAdventures.class);
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
