package com.example.carpoolingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.io.IOException;
import java.io.OutputStreamWriter;

/*
* This class is used to represent the LogIn Activity
* This activity is only rendered when the the user has not logged/signed in from the device
* User is also provided with the forgot password option,in order to get back the account in a case when the user has forgotten the password to his account
* The user obtains a User object if the credentials entered are valid else a Toast of Invalid Credentials is obtained
* Validations are performed before the above step to ensure appropriate credential entries
* User's credentials are then stored in a file to prevent future log ins
* The key to the user's document in the "User's" collection  and user object are then passed to the next activity for use
 *  */

public class LogIn extends AppCompatActivity {

    EditText etEmail,etPassword;
    Button btnLogin;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    User user = new User();
    TextView tvSignIn,tvForgotPassword;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String token="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Login" + "</font>"));
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(LogIn.this,R.color.statusBar));
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvSignIn = (TextView) findViewById(R.id.tvSignIn);
        checkPermissions();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validations(etEmail.getText().toString(),etPassword.getText().toString())) {
                    btnLogin.setEnabled(false);
                    tvSignIn.setEnabled(false);
                    check(etEmail.getText().toString(), etPassword.getText().toString());
                }
            }
        });
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogIn.this,SignUp.class));
            }
        });
        tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString();
                if( email.trim().equals("") )
                {
                    etEmail.setError("Enter E-mail Id");
                    etEmail.requestFocus();
                    return;
                }
                if( !Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    etEmail.setError("Enter valid E-mail Id");
                    etEmail.requestFocus();
                    return;
                }
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(LogIn.this,"Check your mails to reset the password",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }
    public boolean checkPermissions(){
        if((ActivityCompat.checkSelfPermission(LogIn.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(LogIn.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(LogIn.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)&&
                (ActivityCompat.checkSelfPermission(LogIn.this,Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(LogIn.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE},
                    1);

        }
        return false;
    }
    public boolean validations(String email,String password){
        if( email.trim().equals("") )
        {
            etEmail.setError("Enter E-mail Id");
            etEmail.requestFocus();
            btnLogin.setEnabled(true);
            tvSignIn.setEnabled(true);
            return false;
        }
        if( !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            etEmail.setError("Enter valid E-mail Id");
            etEmail.requestFocus();
            btnLogin.setEnabled(true);
            tvSignIn.setEnabled(true);
            return false;
        }
        if( password.trim().equals("") )
        {
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
            btnLogin.setEnabled(true);
            tvSignIn.setEnabled(true);
            return false;
        }
        if( password.length() < 6 )
        {
            etPassword.setError("Password requires minimum of 6 characters");
            etPassword.requestFocus();
            btnLogin.setEnabled(true);
            tvSignIn.setEnabled(true);
            return false;
        }
        return true;
    }
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
                                                        btnLogin.setEnabled(true);
                                                        tvSignIn.setEnabled(true);
                                                    }

                                                    FirebaseMessaging.getInstance().getToken()
                                                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<String> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        btnLogin.setEnabled(true);
                                                                        tvSignIn.setEnabled(true);
                                                                        return;
                                                                    }
                                                                    token = task.getResult();
                                                                    user.setToken(token);
                                                                    db.collection("Users").document(document.getId())
                                                                            .update("token",token);
                                                                    Toast.makeText(LogIn.this,"Logged In successfully!", Toast.LENGTH_SHORT).show();
                                                                    Intent i = new Intent(LogIn.this,MapRender.class);
                                                                    i.putExtra("key",document.getId());
                                                                    i.putExtra("user",user);
                                                                    btnLogin.setEnabled(true);
                                                                    tvSignIn.setEnabled(true);
                                                                    startActivity(i);
                                                                }
                                                            });
                                                }
                                            }
                                        }else{
                                            btnLogin.setEnabled(true);
                                            tvSignIn.setEnabled(true);
                                            Toast.makeText(LogIn.this,"Please try again,Login unsucesdful!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                btnLogin.setEnabled(true);
                tvSignIn.setEnabled(true);
                Toast.makeText(LogIn.this,"Invalid login Credentials",Toast.LENGTH_LONG).show();
            }
        });
    }

}