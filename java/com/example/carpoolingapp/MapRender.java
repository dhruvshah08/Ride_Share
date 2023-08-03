package com.example.carpoolingapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/*
* This class represents the MapRender activity
* When the user has started/joined/requested for a ride option to start/join ride is not available
* If user has started/joined the ride option to emergency call is open as well
* The list of passengers list is displayed if the user has started a drive
* */

public class MapRender extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    boolean showEmergencyOptions = false;

    DrawerLayout drawer;
    NavigationView navigationView;
    String key;
    User user;
    Button btnStartTrip, btnJoinTrip;
    Button btnGetListOfCars;
    CheckBox chkMale, chkFemale, chkOther;
    Spinner spnrNoOfPassengers;
    Button btnStartRide;
    CheckBox chkMaleNewRide, chkFemaleNewRide, chkOtherNewRide;
    LinearLayout layout;
    boolean showTrips = false;
    int noOfFreeSeats = 0;
    TextView txtInitialNoOfSeats;
    FloatingActionButton fab;
    CarDetails carDetails;
    String userName,userAge,userGender,userContactNumber;
    String token="";
    int initialNumberOfSeats =1;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    AddressDetails sourceAddress, destinationAddress;
    int numberOfPassengers=1;
    ArrayList<RideDetails> passengersList = new ArrayList<>();
    private static final int REQUEST_PHONE_CALL = 1;
    FloatingActionButton btnEmergencyCall, btnCallPolice, btnCallMyEmergency, btnCallAmbulance;
    ImageView imageView;
    Uri newUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        key = getIntent().getStringExtra("key");
        user = (User) getIntent().getSerializableExtra("user");
        setContentView(R.layout.activity_map_render);
        sourceAddress = new AddressDetails();
        destinationAddress = new AddressDetails();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        layout = findViewById(R.id.tripDisplay);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_gallery, R.id.nav_home, R.id.nav_slideshow,R.id.nav_tools,R.id.nav_share)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Ride Share" + "</font>"));
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(MapRender.this,R.color.statusBar));

        View hView = navigationView.getHeaderView(0);
        TextView txtNameOfUser = (TextView) hView.findViewById(R.id.txtNameOfUser);
        txtNameOfUser.setText(user.getName());
        imageView =(ImageView) hView.findViewById(R.id.imageView);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round);

        String strUri=user.getProfilePicUrl();
        newUri = Uri.parse(strUri);
        Glide.with(MapRender.this).load(newUri).apply(options).into(imageView);

        btnStartTrip = (Button) findViewById(R.id.btnStartTrip);
        if (!user.getIsDriver()) {
            btnStartTrip.setVisibility(View.INVISIBLE);
        }else{
            Menu menu = navigationView.getMenu();
            MenuItem menuItem = menu.findItem(R.id.nav_gallery);
            menuItem.setTitle("Add Cars");
        }
        btnJoinTrip = (Button) findViewById(R.id.btnJoinTrip);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user.getHasStartedDrive() && !user.getActiveCarRef().equals("") ){
                        passengersList.clear();
                        //Here open a new Alert DIALOG DISPLAYING THE LIST OF PASSENGERS
                       db.collection("activeCars").document(user.getActiveCarRef()).collection("arrayOfPassengers")
                               .get()
                               .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                   @Override
                                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                       if (task.isSuccessful()) {
                                           AlertDialog.Builder mbldr = new AlertDialog.Builder(MapRender.this);
                                            View mview = getLayoutInflater().inflate(R.layout.members_in_car, null);
                                            mbldr.setView(mview);
                                            TextView txtNumberOfPassengers = (TextView)mview.findViewById(R.id.txtNumberOfPassengers);
                                            ListView listView = (ListView)mview.findViewById(R.id.listOfAllPassengers);
                                            Button btnEndTrip = (Button) mview.findViewById(R.id.btnEndTrip);
                                            int numberOfPassengers=0;

                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                RideDetails rideDetails = document.toObject(RideDetails.class);
                                                passengersList.add(rideDetails);
                                            }
                                           if(passengersList.size() <1){
                                              btnEndTrip.setVisibility(View.VISIBLE);
                                           }
                                           numberOfPassengers = passengersList.size();
                                           txtNumberOfPassengers.setText("Number of Passengers in Car : "+numberOfPassengers);
                                           PassengerListAdapter adapter = new PassengerListAdapter(MapRender.this,passengersList,key,user);
                                           listView.setAdapter(adapter);
                                           final AlertDialog aD = mbldr.create();
                                           aD.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                           aD.show();


                                           btnEndTrip.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   //Remove from DB here
                                                   DocumentReference ref2 =   db.collection("activeCars").document(user.getActiveCarRef());
                                                   ref2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                           if (task.isSuccessful()) {
                                                               DocumentSnapshot document1 = task.getResult();
                                                               if (document1.exists()) {
                                                                   ActiveCar activeCar = document1.toObject(ActiveCar.class);
                                                                   MyRideInfo myRideInfo = new MyRideInfo(activeCar,new RideDetails());

                                                                   db.collection("Users").document(key).collection("myRides")
                                                                           .add(myRideInfo)
                                                                           .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                               @Override
                                                                               public void onSuccess(DocumentReference documentReference) {
                                                                                   db.collection("activeCars").document(user.getActiveCarRef()).delete()
                                                                                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                               @Override
                                                                                               public void onSuccess(Void aVoid) {
                                                                                                   db.collection("Users").document(key).update(
                                                                                                           "hasStartedDrive",false,
                                                                                                           "activeCarRef",""
                                                                                                   ) .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                       @Override
                                                                                                       public void onSuccess(Void aVoid) {
                                                                                                           //Add to the DB here

                                                                                                           user.setHasStartedDrive(false);
                                                                                                           user.setActiveCarJoiningRef("");
                                                                                                           user.setActiveCarRef("");
                                                                                                           Toast.makeText(MapRender.this, "Trip ended successfully!", Toast.LENGTH_LONG).show();
                                                                                                           Intent i = new Intent(MapRender.this,MapRender.class);
                                                                                                           i.putExtra("key",key);
                                                                                                           i.putExtra("user",user);
                                                                                                           startActivity(i);
                                                                                                       }
                                                                                                   })
                                                                                                           .addOnFailureListener(new OnFailureListener() {
                                                                                                               @Override
                                                                                                               public void onFailure(@NonNull Exception e) {
                                                                                                                   Toast.makeText(MapRender.this, "Please try again,Trip couldn't be ended successfull!", Toast.LENGTH_LONG).show();
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
                                       } else {
                                       }
                                   }
                               });
                    }else {
                        toggleView();
                    }
                }
            });
        }

        btnEmergencyCall = (FloatingActionButton) findViewById(R.id.btnEmergencyCall);
        btnCallAmbulance = (FloatingActionButton) findViewById(R.id.btnCallAmbulance);
        btnCallMyEmergency = (FloatingActionButton) findViewById(R.id.btnCallMyEmergency);
        btnCallPolice = (FloatingActionButton) findViewById(R.id.btnCallPolice);


        if (user.getHasStartedDrive() || user.getHasJoinedDrive() || user.getHasSentJoiningRequest()) {
            btnStartTrip.setVisibility(View.INVISIBLE);
            btnJoinTrip.setVisibility(View.INVISIBLE);
        }
        if(user.getHasStartedDrive() || user.getHasJoinedDrive()){
            btnEmergencyCall.setVisibility(View.VISIBLE);
        }
        if(!user.getHasStartedDrive() && !user.getHasJoinedDrive()){
            btnEmergencyCall.setVisibility(View.INVISIBLE);
        }
        btnEmergencyCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmergencyOptions = !showEmergencyOptions;
                if (showEmergencyOptions) {
                    btnCallAmbulance.setVisibility(View.VISIBLE);
                    btnCallMyEmergency.setVisibility(View.VISIBLE);
                    btnCallPolice.setVisibility(View.VISIBLE);
                } else {
                    btnCallAmbulance.setVisibility(View.INVISIBLE);
                    btnCallMyEmergency.setVisibility(View.INVISIBLE);
                    btnCallPolice.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnCallMyEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + user.getEmergencyContact()));
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                   Toast.makeText(MapRender.this,"Calling "+user.getEmergencyContact(),Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }else{
                  ActivityCompat.requestPermissions(MapRender.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                }

            }
        });

        btnCallPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + "100"));
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                   Toast.makeText(MapRender.this,"Calling Police",Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }else{
                    ActivityCompat.requestPermissions(MapRender.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                }
            }
        });
        btnCallAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + "108"));
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MapRender.this, "Calling Ambulance", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }else{
                    ActivityCompat.requestPermissions(MapRender.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                }
            }
        });
        showRequests();

        if(btnStartTrip!=null) {
            btnStartTrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder mbldr = new AlertDialog.Builder(MapRender.this);
                    View mview = getLayoutInflater().inflate(R.layout.start_ride, null);
                    db.collection("Drivers").document(user.getDriverRef()).collection("MyCars")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.getResult().size() < 1){
                                        Toast.makeText(MapRender.this,"Please add cars that you would be using for the ride",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                            View v1 = vi.inflate(R.layout.select_car_card, null);
                                            TextView txtCompanyName = (TextView) v1.findViewById(R.id.carCompanyName);
                                            txtCompanyName.setText(document.get("carCompanyName")+"");
                                            TextView txtModelName = (TextView) v1.findViewById(R.id.carModelName);
                                            txtModelName.setText(document.get("carModelName")+"");
                                            TextView noOfSeats = (TextView) v1.findViewById(R.id.txtNoOfSeats);
                                            noOfSeats.setText(document.get("totalNumberOfSeats")+"");
                                            TextView regisNo = (TextView) v1.findViewById(R.id.txtRegisterationNumber);
                                            regisNo.setText(document.get("registrationNumber")+"");
                                            Button btnSelectCar = (Button) v1.findViewById(R.id.btnSelectCar);
                                            btnSelectCar.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String companyName=txtCompanyName.getText().toString().trim();
                                                    String modelName=txtModelName.getText().toString().trim();
                                                    String registrationNo=regisNo.getText().toString().trim();
                                                    int numOfSeats=Integer.parseInt(noOfSeats.getText().toString().trim());
                                                    carDetails  =  new CarDetails(companyName,modelName,numOfSeats,registrationNo);
                                                }
                                            });

                                            // insert into main view
                                            ViewGroup insertPoint = (ViewGroup) mview.findViewById(R.id.listOfMyCars);
                                            insertPoint.addView(v1, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

                                            //functionalities

                                            mbldr.setView(mview);
                                            final AlertDialog aD = mbldr.create();
                                            aD.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            aD.show();
                                            chkMaleNewRide = (CheckBox) mview.findViewById(R.id.chkMale);
                                            chkFemaleNewRide = (CheckBox) mview.findViewById(R.id.chkFemale);
                                            chkOtherNewRide = (CheckBox) mview.findViewById(R.id.chkOther);
                                            btnStartRide = (Button) mview.findViewById(R.id.btnStartRide);
                                            txtInitialNoOfSeats = (TextView) mview.findViewById(R.id.txtInitialNoOfSeats);
                                            Button btnIncrement = (Button)  mview.findViewById(R.id.btnIncrement);
                                            Button btnDecrement = (Button)  mview.findViewById(R.id.btnDecrement);


                                            btnIncrement.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(carDetails == null){
                                                        Toast.makeText(MapRender.this,"Please Select the Car first",Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        if(initialNumberOfSeats < carDetails.getTotalNumberOfSeats()-1) {
                                                            initialNumberOfSeats++;
                                                            txtInitialNoOfSeats.setText(initialNumberOfSeats + "");
                                                        }
                                                    }
                                                }
                                            });

                                            btnDecrement.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(carDetails == null){
                                                        Toast.makeText(MapRender.this,"Please Select the Car first",Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        if (initialNumberOfSeats > 1) {
                                                            initialNumberOfSeats--;
                                                            txtInitialNoOfSeats.setText(initialNumberOfSeats+ "");
                                                        }
                                                    }
                                                }
                                            });

                                            ArrayList<String> listOfGenders=new ArrayList<>();
                                            btnStartRide.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (carDetails == null) {
                                                        Toast.makeText(MapRender.this, "Please Select your Car for the ride!", Toast.LENGTH_LONG).show();
                                                        return;
                                                    }
//
                                                    String noOfFreeSeats = initialNumberOfSeats + "";
                                                    int numberOfFreeSeats = carDetails.getTotalNumberOfSeats() - 1;
                                                    if (!"Select Number of Free Seats".equals(noOfFreeSeats)) {
                                                        numberOfFreeSeats = Integer.parseInt(noOfFreeSeats);
                                                    } else {
                                                        Toast.makeText(MapRender.this, "Please Select Number of Free Seats", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }

                                                    if (chkMaleNewRide.isChecked()) {
                                                        listOfGenders.add("Male");
                                                    }
                                                    if (chkFemaleNewRide.isChecked()) {
                                                        listOfGenders.add("Female");
                                                    }
                                                    if (chkOtherNewRide.isChecked()) {
                                                        listOfGenders.add("Other");
                                                    }
                                                    if (listOfGenders.size() < 1) {
                                                        Toast.makeText(MapRender.this, "Please Select Gender's you are comfortable to share your ride with", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                    if (destinationAddress.getAddress() == null) {
                                                        Toast.makeText(MapRender.this, "Please Select your destination address!", Toast.LENGTH_LONG).show();
                                                        return;
                                                    }

                                                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                                    LocalDateTime now = LocalDateTime.now();
                                                    String startTime = dtf.format(now);

                                                    ActiveCar activeCar = new ActiveCar(carDetails, key, listOfGenders, startTime, sourceAddress.toString(), destinationAddress.toString(), numberOfFreeSeats, sourceAddress.getAddress().getSubLocality(), sourceAddress.getLatLng().toString());
                                                    //add to the DB here
                                                    db.collection("activeCars")
                                                            .add(activeCar)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    user.setHasStartedDrive(true);
                                                                    db.collection("Users").document(key).update(
                                                                            "hasStartedDrive", true
                                                                    )
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    db.collection("Users").document(key).update(
                                                                                            "activeCarRef", documentReference.getId()
                                                                                    )
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    user.setActiveCarRef(documentReference.getId());
                                                                                                    btnStartTrip.setVisibility(View.INVISIBLE);
                                                                                                    btnJoinTrip.setVisibility(View.INVISIBLE);
                                                                                                    btnEmergencyCall.setVisibility(View.VISIBLE);
                                                                                                    Toast.makeText(MapRender.this, "Ride started successfully!", Toast.LENGTH_LONG).show();
                                                                                                }
                                                                                            })
                                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                    Toast.makeText(MapRender.this, "Please try again,Join not successful!", Toast.LENGTH_LONG).show();
                                                                                                }
                                                                                            });
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(MapRender.this, "Please try again,Join  unsuccessful!", Toast.LENGTH_LONG).show();
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(MapRender.this, "Please try again,Ride could'nt be started successfully!", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                    aD.dismiss();
                                                }
                                            });
                                        }
                                    } else {
                                    }
                                }
                            });
                }
            });
        }
        if(btnJoinTrip!=null)

    {
        btnJoinTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mbldr = new AlertDialog.Builder(MapRender.this);
                View mview = getLayoutInflater().inflate(R.layout.activity_join_ride, null);
                mbldr.setView(mview);
                final AlertDialog aD = mbldr.create();
                aD.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                aD.show();
                chkMale = (CheckBox) mview.findViewById(R.id.chkMale);
                chkFemale = (CheckBox) mview.findViewById(R.id.chkFemale);
                chkOther = (CheckBox) mview.findViewById(R.id.chkOther);
                btnGetListOfCars = (Button) mview.findViewById(R.id.btnGetListOfCars);
                spnrNoOfPassengers = (Spinner) mview.findViewById(R.id.spnrNoOfPassengers);
                ArrayList<String> listOfGenders = new ArrayList<>();
                btnGetListOfCars.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String noOfFreePassengers = spnrNoOfPassengers.getSelectedItem().toString().trim();

                        if ("Select Number of Passengers".equals(noOfFreePassengers)) {
                            Toast.makeText(MapRender.this, "Please Select Number of Passengers", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (chkMale.isChecked()) {
                            listOfGenders.add("Male");
                        }
                        if (chkFemale.isChecked()) {
                            listOfGenders.add("Female");
                        }
                        if (chkOther.isChecked()) {
                            listOfGenders.add("Other");
                        }
                        if (listOfGenders.size() < 1) {
                            Toast.makeText(MapRender.this, "Please Select Gender's you are Comfortable to share your Ride with", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (destinationAddress.getAddress() == null) {
                            Toast.makeText(MapRender.this, "Please select your Destination Address!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        numberOfPassengers = Integer.parseInt(spnrNoOfPassengers.getSelectedItem().toString());
                        aD.dismiss();
                        Intent i = new Intent(MapRender.this, DisplayingCars.class);
                        i.putExtra("key", key);
                        i.putExtra("user", user);
                        i.putExtra("listOfGenders", listOfGenders);
                        i.putExtra("noOfPassengers", numberOfPassengers);
                        i.putExtra("mySourceAddress", sourceAddress.getAddress().getAddressLine(0));
                        i.putExtra("mySourceLoc", sourceAddress.getLatLng());
                        i.putExtra("myDestinationAddress", destinationAddress.getAddress().getAddressLine(0));
                        i.putExtra("myDestinationLoc", destinationAddress.getLatLng());
                        startActivity(i);
                    }
                });

            }
        });
    }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_render, menu);
        return true;
    }

    private void toggleView(){
        showTrips=!showTrips;
        if(showTrips){
            layout.setVisibility(View.VISIBLE);
        }else{
            layout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public String getKey() {
        return this.key;
    }
    public User getUser() {
        return this.user;
    }

    @Override
    public void onBackPressed() {

    }
    public void setSourceAddress(Address address,LatLngDetails latLng){
        sourceAddress.setAddress(address);
        sourceAddress.setLatLng(latLng);
    }
    public void setDestinationAddress(Address address,LatLngDetails latLng){
        destinationAddress.setAddress(address);
        destinationAddress.setLatLng(latLng);
    }
    public AddressDetails getSourceAddress(){
        return this.sourceAddress;
    }
    public AddressDetails getDestinationAddress(){
        return this.destinationAddress;
    }

    /*
    * Here if the User has started the drive,then show all the requests to join the ride at first
    * */

    private void showRequests(){
        //if user is driver
        if(user.getHasStartedDrive()){
            db.collection("activeCars").document(user.getActiveCarRef()).collection("requests")
                    .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                AlertDialog.Builder mbldr = new AlertDialog.Builder(MapRender.this);
                                mbldr.setCancelable(false);
                                View mview = getLayoutInflater().inflate(R.layout.accept_or_reject, null);
                                mbldr.setView(mview);
                                final AlertDialog aD = mbldr.create();
                                aD.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                aD.setCanceledOnTouchOutside(false);
                                aD.show();
                                Button btnAccept = mview.findViewById(R.id.btnAccept);
                                Button btnReject = mview.findViewById(R.id.btnReject);
                                TextView txtJoinRequest = mview.findViewById(R.id.txtJoinRequest);

                                //get details of it
                                RideDetails rideDetails = document.toObject(RideDetails.class);

                                //Get the token of the user who sent the request

                                DocumentReference ref2 =  db.collection( "Users").document(rideDetails.getPassengerKey());
                                ref2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document1 = task.getResult();
                                            if (document1.exists()) {
                                                token = document1.get("token").toString();
                                                userName = document1.get("name").toString();
                                                userAge = document1.get("age").toString();
                                                userGender = document1.get("gender").toString();
                                                userContactNumber = document1.get("contact").toString();
                                                txtJoinRequest.setText(userName+"("+userGender.charAt(0)+userAge +") has requested to join the drive with "+rideDetails.getJoinRideRequest().getNumberOfSeats()+ "seats, "+rideDetails.getJoinRideRequest().getDistanceAwayToCarSelected()+ "kms away!");
                                            } else {
                                            }
                                        } else {

                                        }
                                    }
                                });

                                btnReject.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        db.collection( "Users").document(rideDetails.getPassengerKey())
                                        .update(
                                                "hasSentJoiningRequest",false
                                        ) .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                String title = "Ride Status";
                                                String message = "Your request has been rejected!";
                                                FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token,title,message,getApplicationContext(),MapRender.this);
                                                notificationsSender.SendNotifications();
                                                db.collection("activeCars").document(user.getActiveCarRef()).collection("requests").document(document.getId()).delete();
                                                aD.dismiss();
                                                Toast.makeText(MapRender.this, "Request Rejected!", Toast.LENGTH_LONG).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MapRender.this, "Request couldn't be responded to successful!", Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }
                                });
                                btnAccept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //update user carRef object to this carRef]
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                        LocalDateTime now = LocalDateTime.now();
                                        String joiningTime=dtf.format(now);
                                        rideDetails.getJoinRideRequest().setRidejoiningTime(joiningTime);

                                        /*
                                        * Update the following in te DB:
                                        * Update the joining passenger's ref to the driver's ref
                                        * Add the rideDetails object to the array of passengers
                                        * Decrement amount of seats in the car left
                                        * Set the hasJoined and other values in db accordingly
                                        * Remove this record from the requests
                                        * */

                                        db.collection( "Users").document(rideDetails.getPassengerKey())
                                                .update("activeCarRef",user.getActiveCarRef())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        db.collection("activeCars").document(user.getActiveCarRef()).collection("arrayOfPassengers")
                                                                .add(rideDetails)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        DocumentReference ref1 = db.collection("activeCars").document(user.getActiveCarRef());
                                                                        ref1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    DocumentSnapshot document1 = task.getResult();
                                                                                    if (document1.exists()) {
                                                                                        noOfFreeSeats= Integer.parseInt(document1.get("numberOfFreeSeats").toString());
                                                                                        noOfFreeSeats-= rideDetails.getJoinRideRequest().getNumberOfSeats();
                                                                                        db.collection( "activeCars").document(user.getActiveCarRef())
                                                                                                .update("numberOfFreeSeats",noOfFreeSeats
                                                                                                );
                                                                                        db.collection( "Users").document(rideDetails.getPassengerKey())
                                                                                                .update("activeCarJoiningRef",documentReference.getId(),
                                                                                                        "hasJoinedDrive",true,
                                                                                                        "hasSentJoiningRequest",false
                                                                                                );
                                                                                        db.collection("activeCars").document(user.getActiveCarRef()).collection("requests").document(document.getId()).delete();

                                                                                        //token of the receiver who sent
                                                                                        FirebaseMessaging.getInstance().subscribeToTopic("all");
                                                                                        String title = "Ride Status";
                                                                                        String message = "Your request has been accepted!";
                                                                                        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token,title,message,getApplicationContext(),MapRender.this);
                                                                                        notificationsSender.SendNotifications();
                                                                                        aD.dismiss();
                                                                                        Toast.makeText(MapRender.this,"Request Accepted!",Toast.LENGTH_SHORT).show();
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
                                                                        Toast.makeText(MapRender.this,"Please try again,Request could'nt be responded to successfully!",Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(MapRender.this,"Please try again,Request could'nt be responded to successfully!",Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                });
                            }
                        } else {
                        }
                    }
                });
        }
    }

}
