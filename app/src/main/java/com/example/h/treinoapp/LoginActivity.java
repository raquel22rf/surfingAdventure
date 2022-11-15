package com.example.h.treinoapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private Button buttonLogin;
    private EditText textEmailAddress;
    private EditText textPassword;
    private TextView textRegister;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the Title bar of this activity screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        getSupportActionBar().hide(); //hide the title bar

        setContentView(R.layout.activity_login);

        //inicialicacao das variaveis----------------------------------------------{

        firebaseAuth = FirebaseAuth.getInstance();

        //caso o utilizador ainda esteja logged in
        if(firebaseAuth.getCurrentUser() != null){
            //reencaminhar para página de incio
            finish();
            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
        }

        textEmailAddress = (EditText) findViewById(R.id.textEmailAddress);
        textPassword = (EditText) findViewById(R.id.textPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        textRegister = (TextView) findViewById(R.id.textRegister);

        buttonLogin.setOnClickListener(this);
        textRegister.setOnClickListener(this);

        //-------------------------------------------------------------------------}
    }

    private void loginUser(){

        String  email = textEmailAddress.getText().toString().trim();
        String  pass = textPassword.getText().toString().trim();

        //Verificacao do conteudo das caixas de texto, caso estejam vazias enviar alerta e nao efectuar o registo
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //reencaminhar para a pagina de inicio
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                }
            }
        });

    }

    //Operacao de click nesta pagina
    public void onClick(View view){
        //Caso o que tenha sido clicado tenha sido o botao de login
        if (view == buttonLogin){
            loginUser();
        }
        if (view == textRegister){
            finish();
            startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        }
    }
}
