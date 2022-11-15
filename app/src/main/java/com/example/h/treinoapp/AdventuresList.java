package com.example.h.treinoapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AdventuresList extends ArrayAdapter<Adventure> {

    private Activity context;
    private List<Adventure> adventures;

    public AdventuresList(Activity context, List<Adventure> adventures) {
        super(context, R.layout.activity_view_adventures, adventures);
        this.context = context;
        this.adventures = adventures;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.adventures_list, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        TextView textViewPlace = (TextView) listViewItem.findViewById(R.id.viewPlace);
        TextView textViewDescription = (TextView) listViewItem.findViewById(R.id.textViewDescription);
        TextView viewDate = (TextView) listViewItem.findViewById(R.id.viewDate);

        Adventure adventure = adventures.get(position);

        textViewName.setText(adventure.getAdvName());
        textViewDescription.setText(adventure.getAdvDescription());

        int month = adventure.getAdvDate().getMonth()+1;
        int year = adventure.getAdvDate().getYear()+1900;

        viewDate.setText(adventure.getAdvDate().getDate() + "/" + month + "/" + year + "  at  " + adventure.getAdvDate().getHours() + ":" + adventure.getAdvDate().getMinutes());
        textViewPlace.setText(adventure.getAdvPlace());

        return listViewItem;
    }
}
