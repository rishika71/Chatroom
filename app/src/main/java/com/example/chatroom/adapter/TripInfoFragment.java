package com.example.chatroom.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chatroom.databinding.FragmentTripInfoBinding;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.Trip;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.firebase.firestore.FirebaseFirestore;

public class TripInfoFragment extends Fragment {


    FragmentTripInfoBinding binding;

    Trip trip;

    User user;

    MapHelper mapHelper;

    ITripInfo am;

    FirebaseFirestore db;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ITripInfo) {
            am = (ITripInfo) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
        mapHelper = am.getMapHelper();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            trip = (Trip) getArguments().getSerializable(Utils.DB_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Ongoing Trip Details");
        binding = FragmentTripInfoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }

    interface ITripInfo {

        User getUser();

        MapHelper getMapHelper();

    }
}