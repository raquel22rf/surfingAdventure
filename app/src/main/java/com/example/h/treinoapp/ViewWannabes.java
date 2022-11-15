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
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewWannabes extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private DatabaseReference databaseWannabesID, databaseUsers;
    private List<String> wannabesID, wannabesUsers, users;
    private boolean isCreator;

    //---------------------------------- Session Variables ----------------------------------
    private FirebaseAuth firebaseAuth;
    //------------------------------------      end       -----------------------------------

    //------------------------------------ Menu Variables -----------------------------------
    private DrawerLayout mDL;
    private ActionBarDrawerToggle mToogle;
    //------------------------------------      end       -----------------------------------

    private ListView listViewWannabes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_view_wannabes);

        databaseWannabesID = FirebaseDatabase.getInstance().getReference().child("wannabe creators");
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("users");

        Intent intent = getIntent();
        isCreator = (boolean) intent.getSerializableExtra("isCreator");
        //------------------------------- Session Control -----------------------------------

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            //reencaminhar para página de login
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        //--------------------------------     end      -------------------------------------
        //------------------------------------ Menu  ----------------------------------------

        mDL = (DrawerLayout) findViewById(R.id.drawerWanna);
        mToogle = new ActionBarDrawerToggle(this, mDL, R.string.Open, R.string.Close);
        mDL.addDrawerListener(mToogle);
        mToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_Wanna);
        navigationView.setNavigationItemSelectedListener(this);

        Menu nav_menu = navigationView.getMenu();

        if (isCreator){
            nav_menu.findItem(R.id.my_wannabes).setVisible(true);
            nav_menu.findItem(R.id.nav_create).setVisible(true);

        }
        //--------------------------------     end      -------------------------------------

        listViewWannabes = (ListView) findViewById(R.id.listViewWannabes);
        wannabesID = new ArrayList<>();
        wannabesUsers = new ArrayList<>();
        users = new ArrayList<>();

        viewWannabes();
    }

    private void viewWannabes(){
        databaseWannabesID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //clearing the previous comments list
                wannabesID.clear();

                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){

                    String id = postSnapshot.child("id").getValue(String.class);

                    wannabesID.add(id);
                }

                databaseUsers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        wannabesUsers.clear();
                        users.clear();
                        for (int i = 0; i < wannabesID.size(); i++){
                            for (DataSnapshot postSnapshot1 : dataSnapshot.getChildren()){

                                String userID = postSnapshot1.getKey();

                                if (wannabesID.get(i).equals(userID)){
                                    String username = postSnapshot1.child("username").getValue(String.class);
                                    wannabesUsers.add(username);
                                    users.add(userID);
                                }
                            }
                        }

                        //creating adapter
                        WannabesList wannabesAdapter = new WannabesList(ViewWannabes.this, wannabesUsers, wannabesID);
                        //attaching adapter to the listview
                        listViewWannabes.setAdapter(wannabesAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
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
            startActivity(new Intent(ViewWannabes.this, MapsActivity.class));
        }
        if (id == R.id.nav_account) {
            finish();
            Intent intent = new Intent(ViewWannabes.this, ProfileActivity.class);
            intent.putExtra("isCreator", isCreator);
            intent.putExtra("own", true);
            startActivity(intent);
        }
        if (id == R.id.nav_create) {
            finish();
            Intent intent = new Intent(ViewWannabes.this, CreateAdventure.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_wannabes) {
            Intent intent = new Intent(ViewWannabes.this, ViewWannabes.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_adventures) {
            Intent intent = new Intent(ViewWannabes.this, ViewAdventures.class);
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
