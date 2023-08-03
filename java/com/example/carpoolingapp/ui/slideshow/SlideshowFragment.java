package com.example.carpoolingapp.ui.slideshow;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.carpoolingapp.MapRender;
import com.example.carpoolingapp.MyRideAdapter;
import com.example.carpoolingapp.MyRideInfo;
import com.example.carpoolingapp.R;
import com.example.carpoolingapp.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/*
* This fragment is used to display the list of all the rides that the user has joined/started
* The MyRideInfo is obtained and then added into the list which is passed as a parameter to the MyRideAdapter object
* along with the user's object and key
* */

public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;
    String key;
    User user;
    ListView listView;
    ArrayList<MyRideInfo> listOFMyRides  = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        slideshowViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        ((MapRender) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e7e8f1")));
        ((MapRender) getActivity()).getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + "My Rides" + "</font>"));


        listView = root.findViewById(R.id.listOfMyRides);
        MapRender activity = (MapRender) getActivity();
        key = activity.getKey();
        user=activity.getUser();

        //Load the Rides here!

        db.collection("Users").document(key)
                .collection("myRides")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() < 1){
                            Toast.makeText(getContext(),"No Rides taken yet",Toast.LENGTH_LONG).show();
                         return;
                        }
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MyRideInfo rideDetails =document.toObject(MyRideInfo.class);
                                listOFMyRides.add(rideDetails);
                            }

                            MyRideAdapter adapter = new MyRideAdapter(getActivity(),listOFMyRides,key,user);
                            listView.setAdapter(adapter);
                        } else {
                        }
                    }
                });


        return root;
    }
}