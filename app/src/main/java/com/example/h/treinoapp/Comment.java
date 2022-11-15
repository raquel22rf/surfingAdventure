package com.example.h.treinoapp;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    private String user;
    private String text;
    private Date date;


    public Comment(){

    }

    public Comment (String user, String text, Date date) {
        this.user = user;
        this.text = text;
        this.date = date;
    }

    public String  getUser() {
        return user;
    }

    public String getText (){
        return text;
    }

    public Date getDate () {
        return date;
    }
}
