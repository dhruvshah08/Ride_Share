package com.example.carpoolingapp.ui.home;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.carpoolingapp.AddCars;
import com.example.carpoolingapp.LatLngDetails;
import com.example.carpoolingapp.MapRender;
import com.example.carpoolingapp.RideStatus;
import com.example.carpoolingapp.UpdatingUserLocation;
import com.example.carpoolingapp.User;

import com.example.carpoolingapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Locale;

/*
*  This Fragment is used to represent the HomeFragment
*  The Fragment mainly holds a map which is the primary objective of this project
*  The map displays the following:
*       1.The user's current location
*       2.If the user has already joined or started the ride,then their source and destination markers
*       3.If the user is still searching for the ride,they can also see the cars available in their sub locality.
*  The user can change their destination address by clicking at the point on the map where they intend to end/leave the ride
*  @param key is used to indicate the key to the user's document in the "User's" collection
*  @param user is used to indicate the user object of the user logged in
*
*
*
* A Service is initiated which actively returns the user's updated location which in turn is reflected on the map
* MyReceiver is a BroadCast receiver which receives the updates the location from the service and updates it onto the map
* Before all of this is done, the user is asked to provide the permission to access their Current Location,internet and Network State
*
* A Ride Status label is also provided at the top of the map to display the status which can be one of the following:
*   1.None
*   2.Started Ride
*   3.Joined Ride
*   4.Waiting for response
* */

public class HomeFragment extends Fragment  implements OnMapReadyCallback {

    private HomeViewModel homeViewModel;
    EditText txtSource,txtDestination;
    private GoogleMap mMap;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FusedLocationProviderClient fusedLocationClient;
    Marker marker = null;
    String key;
    User user;
    TextView txtRideStatus;
    MyReceiver myReceiver;
    Marker currenLocMarker=null;

    LatLng currentLocation;

    @Override
    public void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(getContext(), UpdatingUserLocation.class);
        serviceIntent.putExtra("key",key);
        serviceIntent.putExtra("user",user);
        getContext().startService(serviceIntent);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        if(((MapRender) getActivity()).getSupportActionBar()!=null){
            ((MapRender) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
            ((MapRender) getActivity()).getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Ride Share" + "</font>"));
        }

        MapRender activity = (MapRender) getActivity();
        key = activity.getKey();
        user=activity.getUser();

        txtRideStatus = (TextView) root.findViewById(R.id.txtRideStatus);
        if(user.getHasStartedDrive())
            txtRideStatus.setText(RideStatus.STARTED_RIDE);
        else if(user.getHasJoinedDrive())
            txtRideStatus.setText(RideStatus.JOINED_RIDE);
        else if(user.getHasSentJoiningRequest())
            txtRideStatus.setText(RideStatus.REQUEST_SENT);
        else
            txtRideStatus.setText(RideStatus.NONE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        txtSource = (EditText) root.findViewById(R.id.txtSource);

        txtDestination = (EditText) root.findViewById(R.id.txtDestination);

        if(checkPermissions())
        {
            SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        //Initial Markers of source and destination address!
        if(user.getHasStartedDrive() || user.getHasJoinedDrive()){
            DocumentReference ref1= db.collection("activeCars").document(user.getActiveCarRef());
            ref1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            txtSource.setText(document.get("sourceAddress").toString().split(" ; ")[0]);
                            txtDestination.setText(document.get("destinationAddress").toString().split(" ; ")[0]);
                            double lat1=Double.parseDouble(document.get("sourceAddress").toString().split(" ; ")[1].split(",")[0].trim());
                            double lng1=Double.parseDouble(document.get("sourceAddress").toString().split(" ; ")[1].split(",")[1].trim());
                            LatLng srcLoc = new LatLng(lat1,lng1);
                            mMap.addMarker(new MarkerOptions().position(srcLoc).title("Source Location"));

                            double lat2=Double.parseDouble(document.get("destinationAddress").toString().split(" ; ")[1].split(",")[0].trim());
                            double lng2=Double.parseDouble(document.get("destinationAddress").toString().split(" ; ")[1].split(",")[1].trim());
                            LatLng destLoc = new LatLng(lat2,lng2);
                            mMap.addMarker(new MarkerOptions().position(destLoc).title("Destination Location"));

                        } else {
                        }
                    } else {

                    }
                }
            });
        }

        //This one represents the user's current location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if(user.getHasStartedDrive() || user.getHasJoinedDrive()){
                                currenLocMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).title("Current Location"));
                            }else{
                                currenLocMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                            }
                            myReceiver = new MyReceiver(currenLocMarker);
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(UpdatingUserLocation.MY_ACTION);
                            getContext().registerReceiver(myReceiver, intentFilter);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,12.0f));
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                                if(addresses.size()>0){
                                    if(!user.getHasStartedDrive()){
                                        txtSource.setText(addresses.get(0).getAddressLine(0));
                                        double lat = addresses.get(0).getLatitude();
                                        double lng = addresses.get(0).getLongitude();
                                        LatLngDetails latLng = new LatLngDetails(lat,lng);
                                        loadCarsInMyLocality(latLng);
                                        ((MapRender) getActivity()).setSourceAddress(addresses.get(0),new LatLngDetails(location.getLatitude(),location.getLongitude()));
                                    }
                                }
                            }
                            catch (Exception e){
                            }

                        }
                    }
                });
        return root;
    }
    public boolean checkPermissions(){
        if((ActivityCompat.checkSelfPermission(this.getActivity().getApplicationContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this.getActivity().getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this.getActivity().getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)&&
                (ActivityCompat.checkSelfPermission(this.getActivity().getApplicationContext(),Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }
        else {
             ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE},
                    1);

        }
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (myReceiver!=null) {
                getContext().unregisterReceiver(myReceiver);
            }
        } catch (IllegalArgumentException e) {
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mMap!=null && !user.getHasStartedDrive() && !user.getHasJoinedDrive() && !user.getHasSentJoiningRequest()){
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                        if(addresses.size()>0){
                            currentLocation = new LatLng(latLng.latitude, latLng.longitude);
                            if(marker == null) {
                                marker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Destination Location"));//.icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,12.0f));
                            }else{
                                marker.setPosition(latLng);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude,latLng.longitude),12.0f));
                            }
                            txtDestination.setText(addresses.get(0).getAddressLine(0));
                            ((MapRender) getActivity()).setDestinationAddress(addresses.get(0),new LatLngDetails(latLng.latitude,latLng.longitude));
                        }
                    }
                    catch (Exception e){
                    }

                }
            });
        }else{
        }
    }

    /*
    * This method is used to display the car's on the map which are at a distance < 1.5kms
    * */
    private void loadCarsInMyLocality(LatLngDetails latLngDetails){
        if(user.getHasJoinedDrive() || user.getHasStartedDrive()){
            return;
        }
        db.collection("activeCars")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            //only if difference between current location and the driver's locality
                            String srclatLngStr=doc.get("sourceAddress").toString().split(";")[1];
                            double srcLat=Double.parseDouble(srclatLngStr.split(",")[0].trim());
                            double srctLng=Double.parseDouble(srclatLngStr.split(",")[1].trim());
                            double d = findDistanceBetween(latLngDetails.getLatitude(),latLngDetails.getLongitude(),srcLat,srctLng);


                            //SHOW CARS IN 1.5K,
                            if(d < 1.5){
                                String latlng=doc.get("currentLatLng").toString();
                                String arr[]=latlng.split(",");
                                double lat=Double.parseDouble(arr[0]);
                                double lng=Double.parseDouble((arr[1]));
                                LatLng currentLocation = new LatLng(lat,lng);
                                //add marker to the map with car symbol
                                mMap.addMarker(new MarkerOptions().position(currentLocation).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                            }else{
                               }
                        }
                    }
                });
    }

    /*
    * This method is used to find the distance between 2 points on the map denoted but their respective Latitude and Longitudes
    * */
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

/*
* This class is a Broadcast Receiver which is used to actively listen to the updated Location received from the service and reflect it on the map!
* */
 class MyReceiver extends BroadcastReceiver {
    Marker currmarker;
    MyReceiver(Marker currmarker){
         this.currmarker=currmarker;
     }
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // TODO Auto-generated method stub
        LatLngDetails currentLoc = (LatLngDetails) arg1.getSerializableExtra("updatedLocation");
        if(currmarker!=null){
            currmarker.setPosition(new LatLng(currentLoc.getLatitude(),currentLoc.getLongitude()));
        }else{

        }
    }

}