package com.example.carpoolingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/*
* This adapter is a RecyclerViewAdapter
* It is used to add the details of the car in the RecyclerView
* It is rendered in the AddCars Activity
* An option to delete the car is also provided
* car xml is used to display the details
* */

public class CarInfoAdapter extends RecyclerView.Adapter<CarInfoAdapter.CarHolder> {

    private ArrayList<CarDetails> myCarList;
    private Context mContext;

    private String documentToDelete="",key="";
    private User user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public CarInfoAdapter(ArrayList<CarDetails> myCarList, Context context,String key,User user) {
        this.myCarList = myCarList;
        this.mContext = context;
        this.key=key;
        this.user=user;
    }

    @Override
    public CarHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        // Inflate the layout view you have created for the list rows here
        View view = layoutInflater.inflate(R.layout.card, parent, false);
        return new CarHolder(view);
    }

    @Override
    public int getItemCount() {
        return myCarList == null? 0: myCarList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CarHolder holder, final int position) {
        final CarDetails carDetails = myCarList.get(position);
        holder.setCarCompanyName(carDetails.getCarCompanyName());
        holder.setCarModelName(carDetails.getCarModelName());
        holder.setTxtNoOfSeats(carDetails.getTotalNumberOfSeats()+"");
        holder.setTxtRegisterationNumber(carDetails.getRegistrationNumber()+"");



    }

    public class CarHolder extends RecyclerView.ViewHolder {

        private TextView txtCarCompanyName,txtCarModelName,txtNoOfSeats,txtRegisterationNumber;
        private ImageButton btnDeleteCar;

        public CarHolder(View itemView) {
            super(itemView);
            txtCarCompanyName =  itemView.findViewById(R.id.txtCarCompanyName);
            txtCarModelName = itemView.findViewById(R.id.txtCarModelName);
            txtNoOfSeats = itemView.findViewById(R.id.txtNoOfSeats);
            txtRegisterationNumber = itemView.findViewById(R.id.txtRegisterationNumber);
            btnDeleteCar = itemView.findViewById(R.id.btnDeleteCar);
            btnDeleteCar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!"".equals(txtRegisterationNumber.getText().toString())){
                        db.collection("Drivers").document(user.getDriverRef()).collection("MyCars")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if(txtRegisterationNumber.getText().toString().equals(document.get("registrationNumber").toString())){
                                            documentToDelete = document.getId();
                                            db.collection("Drivers").document(user.getDriverRef()).collection("MyCars")
                                                    .document(documentToDelete).delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Intent i = new Intent(mContext,AddCars.class);
                                                            i.putExtra("key",key);
                                                            i.putExtra("user",user);
                                                            mContext.startActivity(i);
                                                            Toast.makeText(mContext, "Car removed successfully!", Toast.LENGTH_LONG).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(mContext, "Please try again,Car couldn't be removed successfully!", Toast.LENGTH_LONG).show();
                                                        }
                                                    });

                                        }
                                    }
                                }
                            }
                        });

                    }
                }
            });
        }

        public void setCarCompanyName(String carCompanyName) {
            txtCarCompanyName.setText(carCompanyName);
        }

        public void setCarModelName(String carModelName) {
            txtCarModelName.setText(carModelName);
        }
        public void setTxtNoOfSeats(String noOfSeats) {
            txtNoOfSeats.setText(noOfSeats);
        }

        public void setTxtRegisterationNumber(String regisNo) {
            txtRegisterationNumber.setText(regisNo);
        }
    }
}