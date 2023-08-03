package com.example.carpoolingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This class is used to represent the SignUp activity
 * */

public class SignUp extends AppCompatActivity {

    EditText etEmail,etPassword,etConfirmPassword,etContact,etEmerygencyContact,etAge,etName,etEmergencyContactName;
    Spinner spinerGender;
    Button btnSignUp,btnVerify;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    User userObject = new User();
    TextView tvLogin;
    String key="",token;

    ImageButton btnChoose;
    ImageView imageView;
    FirebaseStorage firebaseStorage;
    Uri imageUri;
    Uri newUri;
    StorageReference storageReference;
    FirebaseAuth auth;
    String verificationCode;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Signup" + "</font>"));
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(SignUp.this,R.color.statusBar));
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        etName = (EditText) findViewById(R.id.etName);
        etContact = (EditText) findViewById(R.id.etPhone);
        etEmerygencyContact = (EditText) findViewById(R.id.etEmergencyContact);
        etAge = (EditText) findViewById(R.id.etAge);
        spinerGender = (Spinner) findViewById(R.id.spinnerGender);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnVerify = (Button) findViewById(R.id.btnOpenVerify);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        etEmergencyContactName = (EditText) findViewById(R.id.etEmergencyContactName);
        btnChoose = (ImageButton) findViewById(R.id.btnChoose);
        imageView = (ImageView) findViewById(R.id.imageViewProfilePhoto);
        firebaseStorage = FirebaseStorage.getInstance();
        StartFirebaseLogin();
        checkPermissions();
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnVerify.setEnabled(false);
                String temp1=etContact.getText().toString();
                String phoneNumber="+91 "+temp1;
                String textChk="[6-9][0-9]{9}";
                Pattern ptrChk= Pattern.compile(textChk);
                Matcher matcher=ptrChk.matcher(temp1);
                if(matcher.matches()) {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            SignUp.this,
                            mCallback);
                    btnVerify.setEnabled(true);
                }
                else{
                    etContact.setError("Invalid Number");
                    etContact.requestFocus();
                    btnVerify.setEnabled(true);
                    return;
                }
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSignUp.setEnabled(false);
                tvLogin.setEnabled(false);
                check();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUp.this, LogIn.class));
            }
        });
    }


    public void check()
    {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String emergencyContact = etEmerygencyContact.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String gender = spinerGender.getSelectedItem().toString().trim();
        String emergencyContactName = etEmergencyContactName.getText().toString().trim();
        if("Select your gender".equals(gender)){
            Toast.makeText(SignUp.this,"Select your gender",Toast.LENGTH_SHORT).show();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }

        if( email.trim().equals("") )
        {
            etEmail.setError("Enter E-mail Id");
            etEmail.requestFocus();
            tvLogin.setEnabled(true);
            btnSignUp.setEnabled(true);
            return;
        }
        if( !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            etEmail.setError("Enter valid E-mail Id");
            etEmail.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if( password.trim().equals("") )
        {
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if( password.length() < 6 )
        {
            etPassword.setError("Password requires minimum of 6 characters");
            etPassword.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if( confirmPassword.trim().equals("") )
        {
            etConfirmPassword.setError("Enter Confirm Password");
            etConfirmPassword.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if( !confirmPassword.equals(password) )
        {
            etConfirmPassword.setError("Password does not match");
            etConfirmPassword.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if( name.trim().equals("") )
        {
            etName.setError("Enter Username");
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            etName.requestFocus();
            return;
        }
        if( emergencyContactName.trim().equals("") )
        {
            etEmergencyContactName.setError("Enter Username");
            etEmergencyContactName.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if( contact.trim().equals("") )
        {
            etContact.setError("Enter Phone Number");
            etContact.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if ( !contact.matches("^[789]\\d{9}$") )
        {
            etContact.setError("Enter Valid Phone Number");
            etContact.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if( emergencyContact.trim().equals("") )
        {
            etEmerygencyContact.setError("Enter Phone Number");
            etEmerygencyContact.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if ( !emergencyContact.matches("^[789]\\d{9}$") )
        {
            etEmerygencyContact.setError("Enter Valid Phone Number");
            etEmerygencyContact.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if ( age.trim().equals("") )
        {
            etAge.setError("Enter Age");
            etAge.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if( !age.matches("[1-9][0-9]") )
        {
            etAge.setError("Enter Valid Age");
            etAge.requestFocus();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        if(imageUri == null){
            Toast.makeText(this,"Please choose your Profile photo",Toast.LENGTH_SHORT).show();
            btnSignUp.setEnabled(true);
            tvLogin.setEnabled(true);
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            btnSignUp.setEnabled(true);
                            tvLogin.setEnabled(true);
                            return;
                        }
                        token = task.getResult();
                        userObject.setToken(token);
                    }
                });
        userObject.setEmail(email);
        userObject.setName(name);
        userObject.setContact(contact);
        userObject.setEmergencyContact(emergencyContact);
        userObject.setEmergencyContactName(emergencyContactName);
        userObject.setGender(gender);
        userObject.setAge(age);
        userObject.setIsDriver(false);
        userObject.setDriverRef("");
        userObject.setHasJoinedDrive(false);
        userObject.setHasStartedDrive(false);
        userObject.setActiveCarRef("");
        userObject.setHasSentJoiningRequest(false);

        userObject.setActiveCarJoiningRef("");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            db.collection("Users")
                                    .add(userObject)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            key=documentReference.getId();
                                            storageReference = firebaseStorage.getReference().child("images/"+key);
                                            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        firebaseStorage.getReference().child("images/"+key).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                newUri = uri;
                                                                userObject.setProfilePicUrl(newUri.toString());
                                                                db.collection("Users").document(key)
                                                                        .update("profilePicUrl",newUri.toString())
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                //Add to the DB here
                                                                                Intent i = new Intent(SignUp.this,MapRender.class);
                                                                                i.putExtra("key",key);
                                                                                i.putExtra("user",userObject);
                                                                                startActivity(i);
                                                                                Toast.makeText(SignUp.this,"SignUp Successful!",Toast.LENGTH_LONG).show();
                                                                                btnSignUp.setEnabled(true);
                                                                                tvLogin.setEnabled(true);
                                                                                //Write to the file
                                                                                try( OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("carpoolingcreds.txt", Context.MODE_PRIVATE));){
                                                                                    outputStreamWriter.write("Email : "+email);
                                                                                    outputStreamWriter.write("\nPassword : "+password);
                                                                                } catch (IOException e) {
                                                                                }
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                btnSignUp.setEnabled(false);
                                                                                tvLogin.setEnabled(true);
                                                                                Toast.makeText(SignUp.this,"Please try again,SignUp unsuccessful!",Toast.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            btnSignUp.setEnabled(false);
                                            Toast.makeText(SignUp.this,"Please try again,SignUp unsuccessful!",Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            btnSignUp.setEnabled(false);
                            tvLogin.setEnabled(true);
                            Toast.makeText(SignUp.this, "Account already exists!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if( result != null )
                    {
                        imageView.setImageURI(result);
                        imageUri = result;
                    }
                }
            });
    public boolean checkPermissions(){
        if((ActivityCompat.checkSelfPermission(SignUp.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(SignUp.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(SignUp.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)&&
                (ActivityCompat.checkSelfPermission(SignUp.this,Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(SignUp.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE},
                    1);

        }
        return false;
    }
    private void StartFirebaseLogin() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                etContact.setEnabled(false);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                btnSignUp.setEnabled(false);
                Toast.makeText(SignUp.this,"Sorry! Verification Failed!\nPlease use the of your Current Device!",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                btnVerify.setEnabled(false);
                btnSignUp.setEnabled(true);
            }
        };
    }

}
