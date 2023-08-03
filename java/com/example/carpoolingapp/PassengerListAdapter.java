package com.example.carpoolingapp;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/*
* This Adapter is used to display the details of the passenger
* This adapter is used in the MapRender activity
* An option to end the ride for the user is also provided
* passenger_details xml is used to render the details
* */

public class PassengerListAdapter extends ArrayAdapter<RideDetails> {
    String key;
    String userName,userAge,userGender,userContactNumber;
    int numberOfSeats;
    private final Activity context;
    String userKey="";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<RideDetails> listOfPassengers;
    User user;
    public PassengerListAdapter(Activity context, ArrayList<RideDetails> listOfPassengers, String key, User user) {
        super(context, R.layout.available_car,listOfPassengers);
        // TODO Auto-generated constructor stub
        this.context=context;
        this.key=key;
        this.listOfPassengers=listOfPassengers;
        this.user=user;
    }
    public View getView(int position, View view, ViewGroup parent) {
        RideDetails rideDetails = getItem(position);
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.passenger_details, null,true);

        Button btnEndTrip = rowView.findViewById(R.id.btnEndTripForUser);
        TextView txtPassengerDetails = rowView.findViewById(R.id.txtPassengerDetails);


        String key = rideDetails.getPassengerKey();
        DocumentReference ref1= db.collection("Users").document(key);
        ref1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userName = document.get("name").toString();
                        userAge = document.get("age").toString();
                        userGender = document.get("gender").toString();
                        userContactNumber = document.get("contact").toString();
                        numberOfSeats = rideDetails.getJoinRideRequest().getNumberOfSeats();
                        txtPassengerDetails.setText(userName+ "("+userGender.charAt(0)+userAge+") occupying "+numberOfSeats + "seats");
                    } else {
                    }
                } else {

                }
            }
        });
        btnEndTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //write code to end the trip for the user here
                //remove the entry from the DB here!
                //set the user's on joinRide to false

                DocumentReference ref1= db.collection("Users").document(key);
                ref1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document1 = task.getResult();
                            if (document1.exists()) {
                              userKey=document1.get("activeCarJoiningRef").toString();
                                db.collection("activeCars").document(user.getActiveCarRef()).collection("arrayOfPassengers").document(userKey).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.collection("Users").document(key).update(
                                                "activeCarRef", "",
                                                "activeCarJoiningRef","",
                                                "hasJoinedDrive",false
                                        )
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        DocumentReference ref2 = db.collection("activeCars").document(user.getActiveCarRef());
                                                        ref2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document1 = task.getResult();
                                                                    if (document1.exists()) {
                                                                        //Add to db here!
                                                                        ActiveCar activeCar = document1.toObject(ActiveCar.class);
                                                                        MyRideInfo myRideInfo = new MyRideInfo(activeCar,rideDetails);
                                                                        db.collection("Users").document(key).collection("myRides")
                                                                                .add(myRideInfo)
                                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                    @Override
                                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                                        Toast.makeText(context, "Ride for "+userName+ " has been ended successfully!", Toast.LENGTH_LONG).show();
                                                                                        Intent i = new Intent(context,MapRender.class);
                                                                                        i.putExtra("key",key);
                                                                                        i.putExtra("user",user);
                                                                                        context.startActivity(i);
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Toast.makeText(context, "Ride couldn't be ended for "+userName, Toast.LENGTH_LONG).show();
                                                                                    }
                                                                                });

                                                                    } else {
                                                                    }
                                                                } else {
                                                                }
                                                            }
                                                        });
                                                        }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    }
                                                });
                                        }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Ride couldn't be ended for "+userName, Toast.LENGTH_LONG).show();
                                    }
                                });

                            } else {
                            }
                        } else {

                        }
                    }
                });
            }
        });
        return rowView;

    };
}