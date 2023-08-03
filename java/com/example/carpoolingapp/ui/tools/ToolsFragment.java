package com.example.carpoolingapp.ui.tools;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.carpoolingapp.MapRender;
import com.example.carpoolingapp.R;
import com.example.carpoolingapp.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


/*
* This Fragment is used as a My Profile
* The user can edit their Name,Contact Number,Name of the Emergency Contact, Emergency Contact Number,Age and Gender
* The appropriate changes are made to the database
* Validations are performed before these changes have to be made
* */

public class ToolsFragment extends Fragment {

    private ToolsViewModel toolsViewModel;
    String key;
    User user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button btnDiscardChanges,btnSaveChanges;
    EditText txtName,txtEmergencyContactNumber,txtAge,txtEmergencyContactName;
    Spinner spnrGender;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(ToolsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tools, container, false);

        toolsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        ((MapRender) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
        ((MapRender) getActivity()).getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "My Profile" + "</font>"));


        btnDiscardChanges = (Button) root.findViewById(R.id.btnDiscardChanges);
        btnSaveChanges = (Button) root.findViewById(R.id.btnSaveChanges);

        txtName = (EditText)  root.findViewById(R.id.txtName);
        txtEmergencyContactNumber = (EditText)  root.findViewById(R.id.txtEmergencyContactNumber);
        txtEmergencyContactName = (EditText) root.findViewById(R.id.txtEmergencyContactName);
        txtAge = (EditText)  root.findViewById(R.id.txtAge);
        spnrGender = (Spinner)  root.findViewById(R.id.spnrGender);
        MapRender activity = (MapRender) getActivity();
        key = activity.getKey();
        user=activity.getUser();
        DocumentReference ref1=  db.collection("Users").document(key);
        ref1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                       txtName.setText(document.get("name").toString());
//                       txtContactNumber.setText(document.get("contact").toString());
                       txtEmergencyContactNumber.setText(document.get("emergencyContact").toString());
                       txtEmergencyContactName.setText(document.get("emergencyContactName").toString());
                       txtAge.setText(document.get("age").toString());
                       String gender = document.get("gender").toString();
                       int index=0;
                       if("Male".equals(gender))
                           index=1;
                       else if("Female".equals(gender))
                            index=2;
                       else if("Other".equals(gender))
                            index=3;
                       spnrGender.setSelection(index);

                    } else {
                    }
                } else {

                }
            }
        });


        btnDiscardChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),MapRender.class);
                i.putExtra("key",key);
                i.putExtra("user",user);
                startActivity(i);
            }
        });

        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validations()) {
                    user.setName(txtName.getText().toString().trim());
                    user.setEmergencyContactName(txtEmergencyContactName.getText().toString().trim());
                    user.setAge(txtAge.getText().toString().trim());
                    user.setGender(spnrGender.getSelectedItem().toString().trim());
                    user.setEmergencyContact( txtEmergencyContactNumber.getText().toString().trim());
                    db.collection("Users").document(key).update(
                            "name", txtName.getText().toString().trim(),
                            "emergencyContact", txtEmergencyContactNumber.getText().toString().trim(),
                            "age", txtAge.getText().toString().trim(),
                            "gender", spnrGender.getSelectedItem().toString().trim(),
                            "emergencyContactName",txtEmergencyContactName.getText().toString().trim()
                    );
                    Intent i = new Intent(getActivity(), MapRender.class);
                    i.putExtra("key", key);
                    i.putExtra("user", user);
                    startActivity(i);
                }
            }

        });
        return root;
    }
    private boolean validations(){
        if(spnrGender.getSelectedItem().toString().equals("Select your Gender")){
            return false;
        }
        else if( txtName.getText().toString().trim().equals("") )
        {
            txtName.setError("Enter Username");
            txtName.requestFocus();
            return false;
        }

        else if( txtEmergencyContactName.getText().toString().trim().equals("") )
        {
            txtEmergencyContactName.setError("Enter Emergency Contact Name");
            txtEmergencyContactName.requestFocus();
            return false;
        }
        else if( txtEmergencyContactNumber.getText().toString().trim().equals("") )
        {
            txtEmergencyContactNumber.setError("Enter Phone Number");
            txtEmergencyContactNumber.requestFocus();
            return false;
        }
        else if ( !txtEmergencyContactNumber.getText().toString().matches("^[789]\\d{9}$") )
        {
            txtEmergencyContactNumber.setError("Enter Valid Phone Number");
            txtEmergencyContactNumber.requestFocus();
            return false;
        }
        else if ( txtAge.getText().toString().trim().equals("") )
        {
            txtAge.setError("Enter Age");
            txtAge.requestFocus();
            return false;
        }
        else if( !txtAge.getText().toString().matches("[1-9][0-9]") )
        {
            txtAge.setError("Enter Valid Age");
            txtAge.requestFocus();
            return false;
        }
        return true;
    }

}