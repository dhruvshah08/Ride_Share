package com.example.carpoolingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


/*
* This Activity is used to represent the first activity that would be displayed when the app is run(Splash Screen)
* It is also used to check if the user has pre-logged in from their device in which case the credentials are loaded from the file stored
* */

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    User user = new User();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String token="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.statusBar));
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Car Pooling App" + "</font>"));
        getSupportActionBar().hide();
        try (InputStream inputStream = getApplicationContext().openFileInput("carpoolingcreds.txt");) {
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                if(bufferedReader!=null) {
                    String email = bufferedReader.readLine().split(" : ")[1].trim();
                    String password = bufferedReader.readLine().split(" : ")[1].trim();
                    check(email, password);
                }
            }
        }
        catch (IOException e) {
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    Intent i = new Intent(MainActivity.this, LogIn.class); startActivity(i);
                    finish(); } }, 3000);
        }
    }
    /*
    * This method is used to check if the credentials obtained from the file are valid
    * @param email denotes the email of the User
    * @param password denotes the password of the User
    * We obtain the User class from the document which is then passed forward to the next activities
    * */
    public void check(String email,String password)
    {
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        db.collection("Users")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                user = document.toObject(User.class);
                                                if( user.getEmail().equals(email) )
                                                {
                                                    try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("carpoolingcreds.txt", Context.MODE_PRIVATE));){
                                                        outputStreamWriter.write("Email : "+email);
                                                        outputStreamWriter.write("\nPassword : "+password);
                                                    } catch (IOException e) {
                                                    }

                                                    FirebaseMessaging.getInstance().getToken()
                                                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<String> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        return;
                                                                    }
                                                                    token = task.getResult();
                                                                    user.setToken(token);
                                                                    db.collection("Users").document(document.getId())
                                                                            .update("token",token);
                                                                }
                                                            });
                                                    Toast.makeText(MainActivity.this,"Logged In successfully!", Toast.LENGTH_SHORT).show();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override public void run() {
                                                            Intent i = new Intent(MainActivity.this, MapRender.class);
                                                            i.putExtra("key",document.getId());
                                                            i.putExtra("user",user);
                                                            startActivity(i);
                                                            finish(); } }, 3000);

                                                    break;
                                                }
                                            }
                                        }else{
                                            new Handler().postDelayed(new Runnable() {
                                                @Override public void run() {
                                                    Intent i = new Intent(MainActivity.this, LogIn.class); startActivity(i);
                                                    finish(); } }, 3000);
                                        }

                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        Intent i = new Intent(MainActivity.this, LogIn.class); startActivity(i);
                        finish(); } }, 3000);
            }
        });
    }
}
