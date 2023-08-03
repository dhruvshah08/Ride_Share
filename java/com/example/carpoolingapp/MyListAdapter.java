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
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

/*
* This Adapter is used to represent all the Cars that the user can join as a passenger
* This is used in the DisplayingCars activity
* */

public class MyListAdapter extends ArrayAdapter<AvailableCarDetails>{

    private final Activity context;
    private TextView txtCarCompanyModel,txtDestinationAddress,txtDistanceAway,txtNoOfFreeSeats,txtUserDetails;
    private Button btnRequestPickup;
    private ArrayList<AvailableCarDetails> listOfAvailableCars;
    private String key;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User user;
    private String userName,userAge,userGender,userContactNumber;
    private String token="";
    private String driverKey="";
    private JoinRideRequest joinRideRequest;

    public MyListAdapter(Activity context, ArrayList<AvailableCarDetails> listOfAvailableCars,String key,User user, JoinRideRequest joinRideRequest) {
        super(context, R.layout.available_car,listOfAvailableCars);
        // TODO Auto-generated constructor stub
        this.context=context;
        this.listOfAvailableCars =listOfAvailableCars;
        this.key=key;
        this.user=user;
        this.joinRideRequest = joinRideRequest;
    }

    public View getView(int position,View view,ViewGroup parent) {

        AvailableCarDetails availableCarDetails = getItem(position);
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.available_car, null,true);

        txtCarCompanyModel = (TextView) rowView.findViewById(R.id.txtCarCompanyModel);
        txtUserDetails = (TextView) rowView.findViewById(R.id.txtUserDetails);
        txtDestinationAddress = (TextView) rowView.findViewById(R.id.txtDestinationAddress);
        txtDistanceAway = (TextView) rowView.findViewById(R.id.txtDistanceAway);
        txtNoOfFreeSeats = (TextView) rowView.findViewById(R.id.txtNoOfFreeSeats);
        btnRequestPickup = (Button) rowView.findViewById(R.id.btnRequestPickup);

        txtCarCompanyModel.setText(availableCarDetails.getActiveCar().getCarDetails().getCarCompanyName() + " "+availableCarDetails.getActiveCar().getCarDetails().getCarModelName());
        driverKey = availableCarDetails.getActiveCarRef();

        //get the details of the driver
        DocumentReference ref1 = db.collection("Users").document(availableCarDetails.getActiveCar().getDriverKey());
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
                        txtUserDetails.setText(userName+"("+userGender.charAt(0)+userAge +") "+userContactNumber);
                    } else {
                    }
                } else {

                }
            }
        });

        txtDestinationAddress.setText(availableCarDetails.getActiveCar().getDestinationAddress().split(";")[0]);
        txtNoOfFreeSeats.setText("Hurry up! Only "+availableCarDetails.getActiveCar().getNumberOfFreeSeats()+" seats left now!!");
        txtDistanceAway.setText(availableCarDetails.getDistanceAway() + "kms away");

        /*
        * Updates are done to the Database
        * When the user chooses the ride to request the pickup,the notification is sent to the Driver
        * */
        btnRequestPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinRideRequest.setDistanceAwayToCarSelected(availableCarDetails.getDistanceAway());
                RideDetails rideDetails = new RideDetails(joinRideRequest,key);

                /*
                * Things to update in DB:
                * setRequestSent to true
                * add into the requests collection
                * */

                user.setHasSentJoiningRequest(true);
                DocumentReference ref1= db.collection("Users").document(key);
                ref1.update("hasSentJoiningRequest",true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                db.collection("activeCars").document(availableCarDetails.getActiveCarRef()).collection("requests")
                                .add(rideDetails)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        ref1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document1 = task.getResult();
                                                    if (document1.exists()) {
                                                        token = document1.get("token").toString();//DRIVER'S TOKEN RECEIVER'S TOKEN
                                                        String title="Respond to Request";
                                                        String message="Someone want's to join the ride,"+rideDetails.getJoinRideRequest().getDistanceAwayToCarSelected()+"kms away!";
                                                        FirebaseMessaging.getInstance().subscribeToTopic("all");
                                                        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token,title,message,getContext(),context);
                                                        notificationsSender.SendNotifications();
                                                        Intent i = new Intent(getContext(),MapRender.class);
                                                        i.putExtra("key",key);
                                                        i.putExtra("user",user);
                                                        context.startActivity(i);
                                                        Toast.makeText(getContext(),"Request sent successfully!",Toast.LENGTH_SHORT).show();

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
                                        Toast.makeText(getContext(),"Request couldn't be sent!",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(),"Request couldn't be sent!",Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });
        return rowView;

    };
}
/*
 * Add to the DB inside the active car's as a pendingRequest field
 * At driver's end read it,and then send the notification to the user!
 * Then remove this request from the user
 * If driver rejects then send notification to the user that request has been rejected
 * If accepted then send confirmatory status and decrease no of seats and add details in the array of passenger!
 * After this design the UI of driver and user when they are in a ride,option to end ride for both as well!
 */