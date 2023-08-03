package com.example.carpoolingapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/*
* This adapter is just used to display the details of the ride from the MyRideInfo object  into the available_car.xml file
* It is used in the Slideshow fragment
* The details displayed are:
*   Date and Time of the Ride
*   Source Address
*   Destination Address
*   Gender's the ride is shared with
*   Number of seats occupied
*   Car Details
*   Driver Details
* */

public class MyRideAdapter extends ArrayAdapter<MyRideInfo> {
    String key;
    User user;
    String userName,userAge,userGender,userContact;
    ArrayList<MyRideInfo> listOfMyRides;
    private final Activity context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public MyRideAdapter(Activity context, ArrayList<MyRideInfo> listOfMyRides, String key, User user) {
        super(context, R.layout.available_car,listOfMyRides);
        // TODO Auto-generated constructor stub
        this.context=context;
        this.key=key;
        this.listOfMyRides=listOfMyRides;
        this.user=user;
    }
    public View getView(int position, View view, ViewGroup parent) {
        MyRideInfo rideDetails = getItem(position);
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.my_ride, null,true);

        TextView txtDateOfRide=(TextView) rowView.findViewById(R.id.txtDateOfRide);
        TextView txtSourceAddress=(TextView) rowView.findViewById(R.id.txtSourceAddress);
        TextView txtDestinationAddress=(TextView) rowView.findViewById(R.id.txtDestinationAddress);
        TextView txtGendersRideSharedWith=(TextView) rowView.findViewById(R.id.txtGendersRideSharedWith);
        TextView txtNoOfSeats=(TextView) rowView.findViewById(R.id.txtNoOfSeats);
        TextView txtCarDetails=(TextView) rowView.findViewById(R.id.txtCarDetails);
        TextView txtDriverDetails=(TextView) rowView.findViewById(R.id.txtDriverDetails);
        TextView lblNo = (TextView) rowView.findViewById(R.id.lblNo);
        TextView txtRideStatus=(TextView) rowView.findViewById(R.id.txtRideStatus);

        DocumentReference ref1 = db.collection("Users").document(rideDetails.getActiveCar().getDriverKey());
        ref1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document1 = task.getResult();
                    if (document1.exists()) {
                        userName = document1.get("name").toString();
                        userAge = document1.get("age").toString();
                        userGender = document1.get("gender").toString();
                        userContact = document1.get("contact").toString();
                        txtDriverDetails.setText(userName + " ("+userGender.charAt(0)+userAge+")");
                    } else {
                    }
                } else {

                }
            }
        });

        if(key.equals(rideDetails.getActiveCar().getDriverKey())){
            txtDateOfRide.setText(rideDetails.getActiveCar().getStartTime());
            txtSourceAddress.setText(rideDetails.getActiveCar().getSourceAddress());
            txtDestinationAddress.setText(rideDetails.getActiveCar().getDestinationAddress());
            lblNo.setVisibility(View.GONE);
            txtNoOfSeats.setVisibility(View.GONE);
            txtRideStatus.setText("Driver");
        }
        else{
            txtDateOfRide.setText(rideDetails.getRideDetails().getJoinRideRequest().getRidejoiningTime());
            txtSourceAddress.setText(rideDetails.getRideDetails().getJoinRideRequest().getSourceAddress());
            txtDestinationAddress.setText(rideDetails.getRideDetails().getJoinRideRequest().getDestinationAddress());
            txtNoOfSeats.setText(rideDetails.getRideDetails().getJoinRideRequest().getNumberOfSeats()+"");
            txtRideStatus.setText("Passenger");
        }
        txtGendersRideSharedWith.setText(rideDetails.getActiveCar().getGendersComfortableWith().toString());
        txtCarDetails.setText(rideDetails.getActiveCar().getCarDetails().getCarCompanyName()+ " "+rideDetails.getActiveCar().getCarDetails().getCarModelName());


        return rowView;

    };
}