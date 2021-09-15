package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentRiderDetailsBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.RideOffer;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RideDetailsFragment extends Fragment {

    public static final String RIDE_OFFER = "rideoffer";
    FragmentRiderDetailsBinding binding;
    Chat chat;
    User user;
    Chatroom chatroom;
    IRiderDetails am;
    MapHelper mapHelper;
    FirebaseFirestore db;
    private GoogleMap mMap;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRiderDetails) {
            am = (IRiderDetails) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
        chatroom = user.getChatroom();
        mapHelper = am.getMapHelper();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chat = (Chat) getArguments().getSerializable(Utils.DB_CHAT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapHelper.stopUpdates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Rider Details");
        binding = FragmentRiderDetailsBinding.inflate(inflater, container, false);

        binding.textView20.setText(chat.getOwnerName());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView3);

        String[] loc = chat.getContent().split("\n");

        LatLng mOrigin = new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
        LatLng mDestination = new LatLng(Double.parseDouble(loc[2]), Double.parseDouble(loc[3]));

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mapHelper.addMarker(mMap, mOrigin);
                    mapHelper.addMarker(mMap, mDestination);
                }
            });
        }

        if (chat.getOwnerRef() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(chat.getOwnerId()).child(chat.getOwnerRef());
            GlideApp.with(binding.getRoot())
                    .load(storageReference)
                    .into(binding.imageView7);
        } else {
            GlideApp.with(binding.getRoot())
                    .load(R.drawable.profile_image)
                    .into(binding.imageView7);
        }

        binding.button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapHelper.hasLocationPerms() && mapHelper.isLocationEnabled()) {

                    mapHelper.getLastLocation(new MapHelper.ILastLocation() {
                        @Override
                        public void onFetch(double lat, double longi) {
                        }

                        @Override
                        public void onUpdate(double lat, double longi) {
                            RideOffer rideOffer = new RideOffer(chat.getId(), new ArrayList<>(Arrays.asList(chat.getOwnerId(), chat.getOwnerName(), chat.getOwnerRef())), new ArrayList<>(Arrays.asList(user.getId(), user.getDisplayName(), user.getPhotoref())), new ArrayList<>(Arrays.asList(lat, longi)));

                            HashMap<String, Object> data = new HashMap<>();
                            data.put("ride_id", chat.getId());
                            data.put("rider", rideOffer.getRider());
                            data.put("offeror", rideOffer.getOfferor());
                            data.put("location", rideOffer.getLocation());

                            user.setRideOffer(rideOffer);

                            db.collection(Utils.DB_RIDE_OFFER)
                                    .document(chat.getId())
                                    .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Bundle bundle = new Bundle();
                                        bundle.putInt(RIDE_OFFER, 1);
                                        Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).navigate(R.id.action_rideDetailsFragment_to_chatroomFragment, bundle);
                                    } else {
                                        task.getException().printStackTrace();
                                    }
                                }
                            });

                        }

                        @Override
                        public boolean stopAfterOneUpdate() {
                            return true;
                        }
                    });

                } else {
                    mapHelper.sendLocOffMessage();
                }
            }
        });

        binding.button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).popBackStack();
            }
        });

        binding.textView24.setText(Utils.getDateString(chat.getCreated_at()));
        return binding.getRoot();
    }

    interface IRiderDetails {

        User getUser();

        MapHelper getMapHelper();

    }
}