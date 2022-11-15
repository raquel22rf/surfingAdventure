package com.example.h.treinoapp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class WannabesList extends ArrayAdapter<String> implements View.OnClickListener {
    private Activity context;
    private List<String> wannabeCreators;
    private List<String> wannabeIds;
    private String username;
    private TextView wannabeUsername;
    private Button buttonConfirm, buttonRefuse;
    private boolean isCreator;

    private DatabaseReference databaseWannabes, databaseUsers;

    public WannabesList(Activity context, List<String> wannabeCreators, List<String> wannabeIds) {
        super(context, R.layout.activity_view_wannabes, wannabeCreators);
        this.context = context;
        this.wannabeCreators = wannabeCreators;
        this.wannabeIds = wannabeIds;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.wannabes_list, null, true);

        wannabeUsername = (TextView) listViewItem.findViewById(R.id.wannabeUsername);
        buttonConfirm = (Button) listViewItem.findViewById(R.id.buttonConfirm);
        buttonRefuse = (Button) listViewItem.findViewById(R.id.buttonRefuse);

        username = wannabeCreators.get(position);
        wannabeUsername.setText(username);

        wannabeUsername.setTag(position);
        wannabeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();

                Intent intent = new Intent(context.getBaseContext(), ProfileActivity.class);
                intent.putExtra("isCreator", true);
                intent.putExtra("own", false);
                intent.putExtra("userID", wannabeIds.get(position));

                context.startActivity(intent);
            }
        });
        buttonConfirm.setTag(position);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();

                confirm(position);
                deleteOfWannabes(position);
            }
        });
        buttonRefuse.setTag(position);
        buttonRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();

                deleteOfWannabes(position);
            }
        });

        return listViewItem;
    }

    private void confirm(int position) {
        final int pos = position;

        databaseWannabes = FirebaseDatabase.getInstance().getReference().child("wannabe creators");
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("users");

        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String username = postSnapshot.child("username").getValue(String.class);
                    if (wannabeCreators.size() > pos && username.equals(wannabeCreators.get(pos))){
                        boolean isCreatorDB = postSnapshot.child("isCreator").getValue(boolean.class);
                        isCreator = isCreatorDB;

                        if (!isCreator) {
                            postSnapshot.child("isCreator").getRef().setValue(true);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteOfWannabes(int position){
        final int pos = position;

        databaseWannabes = FirebaseDatabase.getInstance().getReference().child("wannabe creators");
        databaseWannabes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String id = postSnapshot.child("id").getValue(String.class);

                    if (wannabeIds.size() > pos && id.equals(wannabeIds.get(pos))) {
                        postSnapshot.getRef().removeValue();
                        break;
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
        System.out.println(v);
        if (v == buttonConfirm) {
            System.out.println("buttonConfirm worked");
            //confirm();
            //deleteOfWannabes();
        } else if (v == buttonRefuse) {
            System.out.println("buttonRefuse worked");
            //deleteOfWannabes();
        }
    }

}
