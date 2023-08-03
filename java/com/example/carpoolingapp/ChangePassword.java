package com.example.carpoolingapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePassword extends AppCompatActivity {

    Button btnChangePassword;
    EditText etOldPassword,etNewPassword,etConfirmPassword;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        init();
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                constraints();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(Objects.requireNonNull(user.getEmail()), etOldPassword.getText().toString());


                        user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if( task.isSuccessful() )
                                {
                                    Toast.makeText(ChangePassword.this,"User Found",Toast.LENGTH_SHORT).show();
                                    user.updatePassword(etNewPassword.getText().toString().trim())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(ChangePassword.this,"Password Changed",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                                else
                                {
                                    Toast.makeText(ChangePassword.this,"Incorrect Password",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


    }
    public void init()
    {
        etOldPassword = (EditText) findViewById(R.id.etOldPassword);
        etNewPassword = (EditText) findViewById(R.id.etNewPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        btnChangePassword = (Button) findViewById(R.id.btnChangePassword);
    }
    public void constraints()
    {
        String oldPassword = etOldPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        if(oldPassword.trim().equals(""))
        {
            etOldPassword.setError("Enter Old Password");
            etOldPassword.requestFocus();
            return;
        }
        if(newPassword.trim().equals(""))
        {
            etNewPassword.setError("Enter Old Password");
            etNewPassword.requestFocus();
            return;
        }
        if(confirmPassword.trim().equals(""))
        {
            etConfirmPassword.setError("Enter Old Password");
            etConfirmPassword.requestFocus();
            return;
        }
        if( !oldPassword.equals(confirmPassword) )
        {
            etConfirmPassword.setError("Password does not match");
            etConfirmPassword.requestFocus();
            return;
        }

    }

}