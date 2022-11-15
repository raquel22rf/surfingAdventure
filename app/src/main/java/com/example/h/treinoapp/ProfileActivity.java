package com.example.h.treinoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, Serializable, NavigationView.OnNavigationItemSelectedListener {

    private Button buttonGoBack, buttonAskToBeCreator, buttonUploadPhoto, buttonToChoosePhoto, buttonToSavePhoto, buttonPending;
    private DatabaseReference databaseCurrentUser;
    private Uri filePath;
    private String currentUserID;
    private boolean isCreator;
    private String userID;
    private boolean own;

    private final int PICK_IMAGE_REQUEST = 71;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseImages;
    private DatabaseReference databaseShow;
    private DatabaseReference databaseAskToBeCreator;

    private List<String> images;
    private GridView gridViewImages;

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

        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        isCreator = (boolean) intent.getSerializableExtra("isCreator");
        own = (boolean) intent.getSerializableExtra("own");

        if(!own){
            userID = (String) intent.getSerializableExtra("userID");
            addCurrentUser(userID);
        } else {
            final String activeUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            currentUserID = activeUserID;
            addCurrentUser(activeUserID);
        }



        //check if the user already asked to be a creator
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("wannabe creators");
        Query query = ref.orderByChild("id").equalTo(currentUserID);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("id").getValue().toString().equals(currentUserID)) {
                        buttonAskToBeCreator.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseImages = FirebaseDatabase.getInstance().getReference("users/" + currentUserID + "/images");
        databaseShow = FirebaseDatabase.getInstance().getReference("users/" + currentUserID + "/images");
        databaseAskToBeCreator = FirebaseDatabase.getInstance().getReference("wannabe creators/");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        buttonAskToBeCreator = (Button) findViewById(R.id.buttonAskToBeCreator);
        buttonUploadPhoto = (Button) findViewById(R.id.buttonUploadPhoto);
        buttonToChoosePhoto = (Button) findViewById(R.id.buttonToChoosePhoto);
        buttonToSavePhoto = (Button) findViewById(R.id.buttonToSavePhoto);
        gridViewImages = (GridView) findViewById(R.id.gridViewImages);
        buttonPending = (Button) findViewById(R.id.buttonPending);

//        buttonGoBack.setOnClickListener(this);
        buttonAskToBeCreator.setOnClickListener(this);
        buttonUploadPhoto.setOnClickListener(this);
        buttonToChoosePhoto.setOnClickListener(this);
        buttonToSavePhoto.setOnClickListener(this);
        buttonPending.setOnClickListener(this);

        images = new ArrayList<>();

        //------------------------------- Session Control -----------------------------------

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            //reencaminhar para página de login
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        //--------------------------------     end      -------------------------------------

        //------------------------------------ Menu  ----------------------------------------

        mDL = (DrawerLayout) findViewById(R.id.drawerPrf);
        mToogle = new ActionBarDrawerToggle(this, mDL, R.string.Open, R.string.Close);
        mDL.addDrawerListener(mToogle);
        mToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_viewPrf);
        navigationView.setNavigationItemSelectedListener(this);

        Menu nav_menu = navigationView.getMenu();

        System.out.println("------isCreatorMap----------------------" + isCreator);
        if (isCreator){
            nav_menu.findItem(R.id.my_wannabes).setVisible(true);
            nav_menu.findItem(R.id.nav_create).setVisible(true);
        }


        //--------------------------------     end      -------------------------------------

        showImages();

        gridViewImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedURL = images.get(position);

                Intent intent = new Intent(ProfileActivity.this, ViewImage.class);
                intent.putExtra("url", clickedURL);
                intent.putExtra("currentUser", currentUserID);
                intent.putExtra("isCreator", isCreator);

                startActivity(intent);

                //starting the activity with intent
                finish();
            }
        });

        if(!own) {
            buttonAskToBeCreator.setVisibility(View.GONE);
            buttonUploadPhoto.setVisibility(View.GONE);
            buttonToChoosePhoto.setVisibility(View.GONE);
            buttonToSavePhoto.setVisibility(View.GONE);
            buttonPending.setVisibility(View.GONE);
        }
    }

    public void addCurrentUser(final String id) {
        databaseCurrentUser = FirebaseDatabase.getInstance().getReference("users");
        databaseCurrentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String activeUsername = dataSnapshot.child(id).child("username").getValue(String.class);
                String activeUserEmail = dataSnapshot.child(id).child("email").getValue(String.class);
                final Boolean isCreator = dataSnapshot.child(id).child("isCreator").getValue(Boolean.class);

                TextView textViewUserName = (TextView) findViewById(R.id.textViewUserName);
                TextView textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);

                textViewUserName.setText(activeUsername);
                textViewUserEmail.setText(activeUserEmail);


                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = database.getReference("wannabe creators");
                Query query = ref.orderByChild("id").equalTo(currentUserID);


                if (!isCreator) {
                    if(own) buttonAskToBeCreator.setVisibility(View.VISIBLE);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.child("id").getValue().toString().equals(currentUserID)){


                                    buttonAskToBeCreator.setVisibility(View.GONE);
                                    buttonPending.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            try {
                Bitmap bitman = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void savePhoto() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String uuiu = UUID.randomUUID().toString();
            final StorageReference ref = storageReference.child(currentUserID + "/images/" + uuiu);

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                            //add url to database
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadedURL = uri;
                                    String i = databaseImages.push().getKey();
                                    databaseImages.child(i).child("url").setValue(downloadedURL.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded" + (int) progress + "%");

                        }
                    });
        }

        buttonToChoosePhoto.setVisibility(View.INVISIBLE);
        buttonToSavePhoto.setVisibility(View.INVISIBLE);
    }

    private void showImages() {
        databaseShow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                images.clear();

                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String url = postSnapshot.child("url").getValue(String.class);
                    images.add(url);
                }

                ImagesGrid imagesAdapter = new ImagesGrid(ProfileActivity.this, images);

                gridViewImages.setAdapter(imagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void askToBeCreator() {
        String id = databaseAskToBeCreator.push().getKey();
        databaseAskToBeCreator.child(id).child("id").setValue(currentUserID);
    }

    @Override
    public void onClick(View v) {

        if (v == buttonAskToBeCreator) {
            askToBeCreator();
            buttonAskToBeCreator.setVisibility(View.GONE);
            buttonPending.setVisibility(View.VISIBLE);

        } else if (v == buttonPending) {
            Toast.makeText(this, "Request Pending!", Toast.LENGTH_SHORT).show();

        } else if (v == buttonUploadPhoto) {
            buttonToChoosePhoto.setVisibility(View.VISIBLE);

        } else if (v == buttonToChoosePhoto) {
            chooseImage();
            buttonToSavePhoto.setVisibility(View.VISIBLE);

        } else if (v == buttonToSavePhoto) {
            savePhoto();
        }
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
            startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
        }
        if (id == R.id.nav_account) {
            finish();
            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
            intent.putExtra("isCreator", isCreator);
            intent.putExtra("own", true);
            startActivity(intent);
        }
        if (id == R.id.nav_create) {
            finish();
            Intent intent = new Intent(ProfileActivity.this, CreateAdventure.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_wannabes) {
            Intent intent = new Intent(ProfileActivity.this, ViewWannabes.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
        if (id == R.id.my_adventures) {
            Intent intent = new Intent(ProfileActivity.this, ViewAdventures.class);
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