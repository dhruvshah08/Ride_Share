package com.example.carpoolingapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* This Activity is used to represent AddCars
* The user here adds the cars that they will be using to Start the ride
* All the cars added previously added are first loaded into the Recycler View
* The vehicles must adhere to the general specifications and needs.
* After adding the details,they are added into the RecyclerView
* */

public class AddCars extends AppCompatActivity {

    Button btnSaveCar,btnDiscardCar;
    Spinner spnrCompanyName,spnrModelName,spnrNoOfPassengers;
    String totalNoOfSeats="Select Number Of Passengers";
    private static final String chooseModel="Select Car Model";
    private static final String chooseCompany="Select Car Company";
    String registerationNumber;
    String[] modelList={chooseModel};
    String companyChoice=chooseCompany,modelChoice=chooseModel;
    boolean isAddNewCAR=true;
    CarDetails carDetails;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText txtRegisterationNo;
    String key="";
    User user;
    private RecyclerView recycler;
    CarInfoAdapter listAdapter;
    private ArrayList<CarDetails> listOfCarDetails = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_as_driver);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        key=getIntent().getStringExtra("key");
        user=(User)getIntent().getSerializableExtra("user");
        recycler = findViewById(R.id.listOfMyCars);
        LinearLayoutManager layoutManager = new LinearLayoutManager(AddCars.this,LinearLayoutManager.HORIZONTAL,false);
        recycler.setLayoutManager(layoutManager);
        listAdapter = new CarInfoAdapter(listOfCarDetails, AddCars.this,key,user);
        recycler.setAdapter(listAdapter);
        loadingPreviouslyAddedCars();
       getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
       getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "Add Cars" + "</font>"));
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.statusBar));

        final FloatingActionButton fab = findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mbldr = new AlertDialog.Builder(AddCars.this);
                View mview = getLayoutInflater().inflate(R.layout.activity_register_car, null);
                mbldr.setView(mview);
                final AlertDialog aD = mbldr.create();
                aD.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                aD.show();
                btnSaveCar = (Button) mview.findViewById(R.id.saveCar);
                btnDiscardCar = (Button)  mview.findViewById(R.id.discardCar);
                spnrCompanyName = (Spinner)  mview.findViewById(R.id.companyName);
                spnrModelName = (Spinner)  mview.findViewById(R.id.modelName);
                spnrNoOfPassengers = (Spinner) mview.findViewById(R.id.spnrInitialNoOfSeats);
                txtRegisterationNo = (EditText) mview.findViewById(R.id.txtRegisterationNo);
                spnrCompanyName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        companyChoice = spnrCompanyName.getSelectedItem().toString().trim();
                        if(chooseCompany.equals(companyChoice)){
                            modelChoice = chooseModel;
                        }
                        checkOption(companyChoice);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                spnrModelName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        modelChoice = spnrModelName.getSelectedItem().toString().trim();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                spnrNoOfPassengers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                       totalNoOfSeats = spnrNoOfPassengers.getSelectedItem().toString().trim();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                btnSaveCar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        registerationNumber=txtRegisterationNo.getText().toString().trim();
                        if (validateCarDetails()) {
                            carDetails = new CarDetails(companyChoice, modelChoice ,Integer.parseInt(totalNoOfSeats),registerationNumber);
                            //add to db here!
                            db.collection("Drivers").document(user.getDriverRef()).collection("MyCars")
                                    .add(carDetails)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            listOfCarDetails.add(carDetails);
                                            listAdapter.notifyDataSetChanged();
                                            Toast.makeText(AddCars.this,companyChoice+ " "+modelChoice+" added successfully!",Toast.LENGTH_LONG).show();
                                            reinitialise();
                                            aD.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddCars.this,"Please try again,Car could'nt be added successfully!",Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                });
                btnDiscardCar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reinitialise();
                    }
                });
            }
        });
    }

    private void loadingPreviouslyAddedCars(){
        db.collection("Drivers").document(user.getDriverRef()).collection("MyCars")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String carCompanyName=document.get("carCompanyName").toString();
                                String carModelName=document.get("carModelName").toString();
                                int noOfSeats=Integer.parseInt(document.get("totalNumberOfSeats").toString());
                                String registrationNumber=document.get("registrationNumber").toString();
                                CarDetails carDetails = new CarDetails(carCompanyName,carModelName,noOfSeats,registrationNumber);
                                listOfCarDetails.add(carDetails);
                                listAdapter.notifyDataSetChanged();
                            }
                        } else {
                        }
                    }
                });

    }
    public boolean validateCarDetails(){
        String noOfFreePassengers=spnrNoOfPassengers.getSelectedItem().toString().trim();
        if(registerationNumber.length()!=17) {
            Toast.makeText(AddCars.this,"Please Enter Valid Registration number",Toast.LENGTH_SHORT).show();
            return false;
        }
        if("Select Number of Initial Seats".equals(noOfFreePassengers)){
            Toast.makeText(AddCars.this,"Please Select Number of Initial Seats",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(chooseCompany.equals(companyChoice)){
            Toast.makeText(AddCars.this,"Please select Car Company",Toast.LENGTH_SHORT).show();
            return false;
        }else if(chooseModel.equals(modelChoice)){
            Toast.makeText(AddCars.this,"Please select Car Model",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void toggleAddNewCar(){
        isAddNewCAR=!isAddNewCAR;
    }
    private void reinitialise(){
        spnrModelName.setSelection(0);
        spnrCompanyName.setSelection(0);
        spnrNoOfPassengers.setSelection(0);
        companyChoice = chooseCompany;
        modelChoice = chooseModel;
        modelList = new String[]{chooseModel};
        totalNoOfSeats="Select Number Of Passengers";
        registerationNumber="";
        txtRegisterationNo.setText("");

        toggleAddNewCar();
    }
    private void checkOption(String choice){
        if(choice.equals("Maruti Suzuki")) {
            modelList = getResources().getStringArray(R.array.marutiSuzuki);
        }
        else if(choice.equals("Hyundai")) {
            modelList = getResources().getStringArray(R.array.hyundai);
        }
        else if(choice.equals("Honda")) {
            modelList = getResources().getStringArray(R.array.honda);
        }
        else if(choice.equals("Toyota")) {
            modelList = getResources().getStringArray(R.array.toyota);
        }
        else if(choice.equals("Mahindra")) {
            modelList = getResources().getStringArray(R.array.mahindra);
        }
        else if(choice.equals("Volkswagen")) {
            modelList = getResources().getStringArray(R.array.volkswagon);
        }
        else if(choice.equals("Tata")) {
            modelList = getResources().getStringArray(R.array.tata);
        }
        else if(choice.equals("Ford")) {
            modelList = getResources().getStringArray(R.array.ford);
        }
        else if(choice.equals("Renault")) {
            modelList = getResources().getStringArray(R.array.renault);
        }else if(choice.equals("Select Car Model")){

        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, modelList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrModelName.setAdapter(arrayAdapter);
    }

    @Override
    public void onBackPressed() {
        Intent  i =new Intent(AddCars.this,MapRender.class);
        i.putExtra("key",key);
        i.putExtra("user",user);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
