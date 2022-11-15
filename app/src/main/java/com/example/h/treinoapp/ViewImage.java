package com.example.h.treinoapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import com.google.firebase.storage.StorageReference;

import android.util.Log;

public class ViewImage extends AppCompatActivity implements View.OnClickListener{

    private String url, currentUserID;
    private boolean isCreator;

    private DatabaseReference databaseImages;
    private Button buttonBack, buttonDeleteImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_view_image);

        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonDeleteImage = (Button) findViewById(R.id.buttonDeleteImage);

        buttonBack.setOnClickListener(this);
        buttonDeleteImage.setOnClickListener(this);

        Intent intent = getIntent();
        url = (String) intent.getSerializableExtra("url");
        currentUserID = (String) intent.getSerializableExtra("currentUser");
        isCreator = (boolean) intent.getSerializableExtra("isCreator");

        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);

        Picasso.with(getApplicationContext())
                .load(url)
                .into(imageView2);

        databaseImages = FirebaseDatabase.getInstance().getReference("users/" + currentUserID + "/images");
    }


    private void deleteImage(){

        //delete from database
        Query query = databaseImages.orderByChild("url").equalTo(url);

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


        //delete from storage
        FirebaseStorage storage;
        storage = FirebaseStorage.getInstance();
        StorageReference photoRef = storage.getReferenceFromUrl(url);

        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //success
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == buttonBack) {
            finish();
            Intent intent = new Intent(ViewImage.this, ProfileActivity.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        } else if(v == buttonDeleteImage) {
            finish();
            deleteImage();
            Intent intent = new Intent(ViewImage.this, ProfileActivity.class);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        }
    }
}