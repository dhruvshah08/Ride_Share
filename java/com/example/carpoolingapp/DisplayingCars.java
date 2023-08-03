package com.example.carpoolingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/*
* This Activity is used to display the list of cars
*
* The cars are only rendered only and only if :
*   1.The user's current location and the car's location is less than 1.5kms
*   2.The user's destination and car's destination is less than 1.5km
*   3.List of genders you are comfortable with match with that of car
*   4.Number of seats requested are less than the number of free seats in the car
*
* MyListAdapter is used to render all the details of the car
* */

public class DisplayingCars extends AppCompatActivity {
    String key="";
    User user;
    List listOfGenders;
    int numberOfPassengers;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<AvailableCarDetails> listOfAvailableCarDetails;
    String mySourceAddress,myDestinationAddress;
    LatLngDetails mySourceLoc,myDestinationLoc;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        key=getIntent().getStringExtra("key");
        user=(User)getIntent().getSerializableExtra("user");
        listOfGenders=getIntent().getStringArrayListExtra("listOfGenders");
        numberOfPassengers=getIntent().getIntExtra("noOfPassengers",1);
        mySourceAddress=getIntent().getStringExtra("mySourceAddress");
        mySourceLoc=(LatLngDetails) getIntent().getSerializableExtra("mySourceLoc");
        myDestinationLoc=(LatLngDetails) getIntent().getSerializableExtra("myDestinationLoc");
        myDestinationAddress=getIntent().getStringExtra("myDestinationAddress");
        setContentView(R.layout.activity_displaying_cars);
        listOfAvailableCarDetails = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listOfAvailableCars);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Choose your Carpooling Car" + "</font>"));

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(DisplayingCars.this,R.color.statusBar));

        db.collection("activeCars")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if(value.size() <1){
                            Toast.makeText(DisplayingCars.this,"Sorry! No Cars are available",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (e != null) {
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            ActiveCar activeCar = doc.toObject(ActiveCar.class);
                            if (doc.get("driverKey").equals(user.getDriverRef())) {
                                continue;
                            }
                            if (numberOfPassengers <= Integer.parseInt(activeCar.getCarDetails().getTotalNumberOfSeats() + "")) {
                                ArrayList<String> list = (ArrayList<String>) activeCar.getGendersComfortableWith();
                                if (list.equals(listOfGenders)) {
                                    String driverCurrentLoc = activeCar.getCurrentLatLng();
                                    double driverCurrentLat = Double.parseDouble(driverCurrentLoc.split(",")[0].trim());
                                    double driverCurrentLng = Double.parseDouble(driverCurrentLoc.split(",")[1].trim());

                                    String destinationAddress = activeCar.getDestinationAddress();
                                    String destlatLngStr = destinationAddress.split(";")[1];
                                    double destLat = Double.parseDouble(destlatLngStr.split(",")[0].trim());
                                    double destLng = Double.parseDouble(destlatLngStr.split(",")[1].trim());

                                   /*The car would be rendered only and only if:
                                    1.Distance between Driver's current Location and User's current Location< 1.5
                                    2.Distance between Driver's destination Location and user's destination Location  <1.5
                                   */

                                    String activeCarRef = doc.getId();
                                    double d1 = findDistanceBetween(mySourceLoc.getLatitude(), mySourceLoc.getLongitude(), driverCurrentLat, driverCurrentLng);
                                    double d2 = findDistanceBetween(myDestinationLoc.getLatitude(), myDestinationLoc.getLongitude(), destLat, destLng);
                                    if (d1 < 1.5 && d2 < 1.5) {
                                        //diff between driver's current loc and my loc
                                        double distanceAway = findDistanceBetween(driverCurrentLat, driverCurrentLng, mySourceLoc.getLatitude(), mySourceLoc.getLongitude());
                                        AvailableCarDetails availableCarDetails = new AvailableCarDetails(activeCar, distanceAway, activeCarRef);
                                        listOfAvailableCarDetails.add(availableCarDetails);
                                    }
                                }
                            }
                        }
                        if(listOfAvailableCarDetails.size() < 1){
                            Toast.makeText(DisplayingCars.this,"Sorry! No Cars are available that satisfy your requirements",Toast.LENGTH_LONG).show();
                            return;
                        }
                        JoinRideRequest joinRideRequest  = new JoinRideRequest(mySourceAddress,myDestinationAddress,mySourceLoc,myDestinationLoc,numberOfPassengers);
                        MyListAdapter adapter = new MyListAdapter(DisplayingCars.this,listOfAvailableCarDetails,key,user,joinRideRequest);
                        listView.setAdapter(adapter);

                    }
                });
    }

    private double findDistanceBetween(double lat1,double lon1,double lat2,double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km

        return Math.round(d * 100) / 100;
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
}
