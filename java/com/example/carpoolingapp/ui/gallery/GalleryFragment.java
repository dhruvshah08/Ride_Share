package com.example.carpoolingapp.ui.gallery;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.carpoolingapp.AddCars;
import com.example.carpoolingapp.MapRender;
import com.example.carpoolingapp.R;
import com.example.carpoolingapp.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/*
* This fragment is used to represent Join as Driver
* This fragment used to enable the user to Join as Driver which in turn allows them start a ride as a driver
* If the user is already a driver,they are directed to the AddCars activity where the can view,add,delete the cars that they will be using
* The user needs to first read the terms and conditions and if they adhere to it,they can check the Checkbox which allows them to Join as a Driver
* */

public class GalleryFragment extends Fragment{

    private GalleryViewModel galleryViewModel;
    String key;
    User user;
    CheckBox chkTermsAgreed;
    Button btnJoinAsDriver;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapRender activity = (MapRender) getActivity();
        key = activity.getKey();
        user=activity.getUser();
        if(user.getIsDriver()){
            Intent i=new Intent(getActivity(),AddCars.class);
            i.putExtra("key", key);
            i.putExtra("user", user);
            startActivity(i);
        }
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        ((MapRender) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
        ((MapRender) getActivity()).getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Join as Driver" + "</font>"));
        chkTermsAgreed = (CheckBox) root.findViewById(R.id.chkTermsAgreed);
        btnJoinAsDriver = (Button) root.findViewById(R.id.btnJoinAsDriver);



        if(chkTermsAgreed!=null) {
            chkTermsAgreed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chkTermsAgreed.isChecked()) {
                        btnJoinAsDriver.setEnabled(true);
                    } else {
                        btnJoinAsDriver.setEnabled(false);
                    }
                }
            });
        }
        if(btnJoinAsDriver!=null) {
            btnJoinAsDriver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("keyRef", key);
                    //add into the DB
                    db.collection("Drivers")
                            .add(data)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    //now get the key from extras and set isDriver to false;
                                    user.setIsDriver(true);
                                    user.setDriverRef(documentReference.getId());
                                    db.collection("Users").document(key).update(
                                            "isDriver", true,
                                            "driverRef",documentReference.getId()
                                    )
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getActivity(), "Congratulations,you have successfully joined as a Driver!", Toast.LENGTH_LONG).show();
                                                    Intent i = new Intent(getActivity(), MapRender.class);
                                                    i.putExtra("key", key);
                                                    i.putExtra("user", user);
                                                    startActivity(i);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getActivity(), "Please try again,Join not successfull!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Please try again,Join not successfull!", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
        }


        return root;
    }

}