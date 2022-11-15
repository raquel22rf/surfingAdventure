package com.example.h.treinoapp;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout mDL;
    private ActionBarDrawerToggle mToogle;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_menu);


        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            //reencaminhar para página de login
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        mDL = (DrawerLayout) findViewById(R.id.drawer);
        mToogle = new ActionBarDrawerToggle(this, mDL, R.string.Open, R.string.Close);
        mDL.addDrawerListener(mToogle);
        mToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void logoutUser(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

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


        if (id == R.id.nav_map){
            finish();
            startActivity(new Intent(MenuActivity.this, MapsActivity.class));
        }
        if (id == R.id.nav_account){
            finish();
            startActivity(new Intent(MenuActivity.this, ProfileActivity.class));
        }
        if (id == R.id.nav_create){
            finish();
            startActivity(new Intent(MenuActivity.this, CreateAdventure.class));
        }
        if (id == R.id.my_wannabes) {
            finish();
            startActivity(new Intent(MenuActivity.this, ViewWannabes.class));
        }
        if (id == R.id.my_adventures){
            finish();
            startActivity(new Intent(MenuActivity.this, ViewAdventures.class));
        }
        if (id == R.id.nav_help){

        }
        if (id == R.id.nav_settings){

        }
        if (id == R.id.nav_logout){
            logoutUser();
        }
        return false;
    }
}
