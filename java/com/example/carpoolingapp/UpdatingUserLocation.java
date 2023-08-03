package com.example.carpoolingapp;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.os.Handler;
import com.google.android.gms.maps.model.Marker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

/*
* This is a Service class which actively runs in the background
* The use of this class is to return the user's location so that it is reflected in the UI
* The Database is also updated in this Service
* The user's new location is checked after every 10 seconds
* */

public class UpdatingUserLocation extends Service {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FusedLocationProviderClient fusedLocationClient;
    String key;
    User user;
    final static public String MY_ACTION = "MY_ACTION";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        key = intent.getStringExtra("key");
        user = (User)intent.getSerializableExtra("user");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        Handler handler = new Handler();
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                updateDatabase();
                handler.postDelayed(this, 10000);
            }
        };
        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
    * This method is used to return the updatedLocation of the User
    * The changes are then made accordingly to the Database where the currentLatLng,currentLocality values are updated!
    * */

    private void updateDatabase(){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            LatLngDetails currentLocation = new LatLngDetails(location.getLatitude(), location.getLongitude());
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                                if(addresses.size()>0){
                                    if(user.getHasStartedDrive()){
                                        db.collection("activeCars").document(user.getActiveCarRef()).update(
                                            "currentLatLng",location.getLatitude()+ ","+location.getLongitude(),
                                            "currentSubLocality",addresses.get(0).getSubLocality()
                                        )
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //add to list
                                                sendUpdatedLoc(currentLocation);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                            }
                                        });
                                    }
                                }
                            }
                            catch (Exception e){
                            }
                        }
                    }
                });
    }

    /*
    * This method is used to send the updated location to the MyReceiver broadcast
    * */
    private void sendUpdatedLoc(LatLngDetails currentlocation){
        Intent i = new Intent();
        i.setAction(MY_ACTION);
        i.putExtra("updatedLocation",currentlocation);
        sendBroadcast(i);
    }
}
