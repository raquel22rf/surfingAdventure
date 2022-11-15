package com.example.h.treinoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;


public class FirstActivity extends AppCompatActivity implements View.OnClickListener, Serializable{


    private Button buttonRegister;
    private EditText textUsername;
    private EditText textEmailAddress;
    private EditText textPassword;
    private TextView textSignin;

    private DatabaseReference databaseUsers;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the Title bar of this activity screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_first_activitie);

        //inicialicacao das variaveis----------------------------------------------{

        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        textUsername = (EditText) findViewById(R.id.textUsername);
        textEmailAddress = (EditText) findViewById(R.id.textEmailAddress);
        textPassword = (EditText) findViewById(R.id.textPassword);
        textSignin = (TextView) findViewById(R.id.textSignIn);

        buttonRegister.setOnClickListener(this);
        textSignin.setOnClickListener(this);

        //-------------------------------------------------------------------------}

    }


    //operacao de registo de utilizadores
    private void registerUser(){
        final String  username = textUsername.getText().toString().trim();
        final String  email = textEmailAddress.getText().toString().trim();
        final String pass = textPassword.getText().toString().trim();

        //Verificacao do conteudo das caixas de texto, caso estejam vazias enviar alerta e nao efectuar o registo
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        //fazer a verificacao se
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.orderByChild("username").equalTo(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if the username already exists
                if (dataSnapshot.exists())
                {
                    Toast.makeText(FirstActivity.this, "Username Already Exists!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(FirstActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {


                            if (task.isSuccessful()) {

                                User user = new User(username, email, pass);
                                System.out.println("info: " + username + " " + email + " " + pass);
                                System.out.println("info: " + user.getUsername() + " " + user.getEmail() + " " + user.getPassword());


                                String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                databaseUsers.child(id).setValue(user);
                                Toast.makeText(FirstActivity.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(getApplicationContext(), MapsActivity.class));

                            } else {

                                Toast.makeText(FirstActivity.this, "Could not Register the User", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    //Operacao de click nesta pagina
    public void onClick(View view){
        //Caso o que tenha sido clicado tenha sido o botao de registo
        if (view == buttonRegister){
            registerUser();
        }
        if (view == textSignin){
            finish();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

    }
}
