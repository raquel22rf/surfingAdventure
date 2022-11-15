package com.example.h.treinoapp;

import java.util.ArrayList;
import java.util.List;

public class User {


    //----------------------------Variables------------------------------

    private String username;
    private String email;
    private String password;
    private List<String> adventures;
    private Boolean isCreator;

    //---------------------------Constructor-----------------------------

    public User(){}

    public User(String username, String email, String password){
        this.email = email;
        this.username = username;
        this.password = password;
        this.adventures = new ArrayList<>();
        isCreator = false;
    }

    //-------------------------------GETS--------------------------------

    public String getUsername(){
        return username;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    public Boolean getIsCreator(){
        return isCreator;
    }

    public List<String> getAdventures(){ return adventures; }

    //-------------------------------SETS--------------------------------

    public void updateEmail(String email){
        this.email = email;
    }

    public void updateUsername(String username){
        this.username = username;
    }

    public void updatePassword(String password){
        this.password = password;
    }

    public void addAdventure(String idAdv){
        adventures.add(idAdv);
    }

    public void updateIsCreator(boolean status){
        isCreator = status;
    }

}
