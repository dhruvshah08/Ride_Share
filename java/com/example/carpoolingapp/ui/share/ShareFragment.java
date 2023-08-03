package com.example.carpoolingapp.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.carpoolingapp.LogIn;
import com.example.carpoolingapp.R;

/*
* This fragment is used as Logout
* The text file from the device is deleted consisting of the user's credentials and is redirected to the LogIn activity
* */

public class ShareFragment extends Fragment {

    private ShareViewModel shareViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        shareViewModel =
                ViewModelProviders.of(this).get(ShareViewModel.class);
        View root = inflater.inflate(R.layout.fragment_share, container, false);
        shareViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        //delete the creds file
        if(getActivity().deleteFile("carpoolingcreds.txt")){
            Toast.makeText(getContext(),"Logged Out",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getActivity(), LogIn.class);
            startActivity(i);
        }else{
            Toast.makeText(getContext(),"Please try again,couldn't Log out successfully!",Toast.LENGTH_SHORT).show();

        }
        return root;
    }
}