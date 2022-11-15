package com.example.h.treinoapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ViewComments extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference databaseComments, databaseCurrentUser;

    private Adventure adv;
    private boolean isCreator;

    private ListView listViewComments;
    private List<Comment> comments;

    private EditText commentText1;
    private Button buttonGoBack, buttonAddComment, buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_view_comments);

        Intent intent = getIntent();
        adv = (Adventure) intent.getSerializableExtra("adventure");
        isCreator = (boolean) intent.getSerializableExtra("isCreator");

        //get reference
        databaseComments = FirebaseDatabase.getInstance().getReference().child("adventures").child(adv.getAdvID()).child("comments");


        buttonGoBack = (Button) findViewById(R.id.buttonGoBack);
        buttonAddComment = (Button) findViewById(R.id.buttonAddComment);
        buttonAdd = (Button) findViewById(R.id.buttonAdd);

        commentText1 = (EditText) findViewById(R.id.commentText1);

        buttonGoBack.setOnClickListener(this);
        buttonAddComment.setOnClickListener(this);
        buttonAdd.setOnClickListener(this);

        listViewComments = (ListView) findViewById(R.id.listViewComments);
        comments = new ArrayList<Comment>();


        databaseComments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //clearing the previous comments list
                comments.clear();

                //iterating through all the nodes
                for (DataSnapshot postSnapshot2 : dataSnapshot.getChildren()){

                    //getting comment
                    String cmUserName = postSnapshot2.child("user").getValue(String.class);
                    String cmText = postSnapshot2.child("text").getValue(String.class);
                    Date cmDate = postSnapshot2.child("date").getValue(Date.class);

                    //create new comment
                    Comment comment = new Comment(cmUserName, cmText, cmDate);

                    comments.add(comment);
                }

                //creating adapter
                CommentsList commentAdapter = new CommentsList(ViewComments.this, comments);
                //attaching adapter to the listview
                listViewComments.setAdapter(commentAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public void addComment() {
        //get the current user
        final String activeUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseCurrentUser = FirebaseDatabase.getInstance().getReference("users");

        databaseCurrentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String activeUsername = dataSnapshot.child(activeUserID).child("username").getValue(String.class);


                String text = commentText1.getText().toString().trim();

                Date currentTime = Calendar.getInstance().getTime();

                Comment comment = new Comment(activeUsername, text, currentTime);

                //add comment too database
                databaseComments = FirebaseDatabase.getInstance().getReference("adventures/" + adv.getAdvID() + "/comments");
                String id = databaseComments.push().getKey();
                databaseComments.child(id).setValue(comment);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void onClick(View view){
        if(view == buttonGoBack){
            finish();

            Intent intent = new Intent(ViewComments.this, ViewSingleAdventure.class);
            intent.putExtra("adventure", adv);
            intent.putExtra("isCreator", isCreator);
            startActivity(intent);
        } else if (view == buttonAddComment) {
            commentText1.setVisibility(View.VISIBLE);
            buttonAdd.setVisibility(View.VISIBLE);
        } else if (view == buttonAdd) {
            addComment();
        }
    }
}
