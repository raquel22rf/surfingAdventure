package com.example.h.treinoapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CommentsList extends ArrayAdapter <Comment> {

    private Activity context;
    private List<Comment> comments;
    private Comment comment;

    public CommentsList(Activity context, List<Comment> comments){
        super(context, R.layout.activity_view_comments, comments);
        this.context = context;
        this.comments = comments;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();


        View listViewItem = inflater.inflate(R.layout.comments_list, null, true);

        TextView textViewUserName = (TextView) listViewItem.findViewById(R.id.textViewUserName);
        TextView textViewDate = (TextView) listViewItem.findViewById(R.id.textViewDate);
        TextView textViewComment = (TextView) listViewItem.findViewById(R.id.textViewComment);

        comment = comments.get(position);

        textViewUserName.setText(comment.getUser());

        int month = comment.getDate().getMonth()+1;
        int year = comment.getDate().getYear()+1900;

        textViewDate.setText(comment.getDate().getDate() + "/" + month + "/" + year + "  at  " + comment.getDate().getHours() + ":" + comment.getDate().getMinutes());

        textViewComment.setText(comment.getText());


        return listViewItem;
    }
}
