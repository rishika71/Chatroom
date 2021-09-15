package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentRideOfferDetailBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.RideOffer;
import com.example.chatroom.models.Trip;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class RideOfferDetailFragment extends Fragment {

    public static final String RIDE_STARTED = "ride";
    FragmentRideOfferDetailBinding binding;
    Chat chat;

    User user;

    GoogleMap mMap;

    MapHelper mapHelper;

    NavController navController;

    FirebaseFirestore db;

    RideOffer rideOffer;

    IOfferDetails am;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IOfferDetails) {
            am = (IOfferDetails) context;
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
        if (getArguments() != null) {
            chat = (Chat) getArguments().getSerializable(Utils.DB_CHAT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Ride Offer");
        binding = FragmentRideOfferDetailBinding.inflate(inflater, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);
        View view = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView4);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                }
            });
        }

        am.toggleDialog(true);
        db.collection(Utils.DB_RIDE_OFFER).document(chat.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                am.toggleDialog(false);
                if (task.isSuccessful()) {
                    rideOffer = task.getResult().toObject(RideOffer.class);
                    binding.textView27.setText(rideOffer.getOfferorName());
                    binding.textView28.setText(Utils.getDateString(chat.getCreated_at()));
                    if (rideOffer.getOfferorRef() != null) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(rideOffer.getOfferorId()).child(rideOffer.getOfferorRef());
                        GlideApp.with(view)
                                .load(storageReference)
                                .into(binding.imageView8);
                    } else {
                        GlideApp.with(view)
                                .load(R.drawable.profile_image)
                                .into(binding.imageView8);
                    }
                    ArrayList<Double> location = rideOffer.getLocation();
                    mapHelper.addMarker(mMap, new LatLng(location.get(0), location.get(1)));
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

        binding.button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapHelper.hasLocationPerms() && mapHelper.isLocationEnabled()) {
                    mapHelper.getLastLocation(new MapHelper.ILastLocation() {
                        @Override
                        public void onFetch(double lat, double longi) {

                        }

                        @Override
                        public void onUpdate(double lat, double longi) {
                            Trip trip = new Trip(rideOffer.getRide_id(), new Date(), rideOffer.getRider(), rideOffer.getOfferor());
                            trip.setRider_location(new ArrayList<>(Arrays.asList(lat, longi)));
                            setRide(db.collection(Utils.DB_TRIP).document(trip.getRiderId()).collection(Utils.DB_TRIPS), trip, Utils.RIDE_TYPE);
                            setRide(db.collection(Utils.DB_TRIP).document(trip.getDriverId()).collection(Utils.DB_TRIPS), trip, Utils.DRIVE_TYPE);
                            trip.setType(Utils.RIDE_TYPE);
                            user.setTrip(trip);
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

        binding.button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        return view;
    }

    public void setRide(CollectionReference dbc, Trip ride, String type) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("ride_id", ride.getRide_id());
        data.put("started_at", ride.getStarted_at());
        data.put("type", type);
        data.put("rider", ride.getRider());
        data.put("driver", ride.getDriver());
        data.put("ongoing", ride.isOngoing());
        data.put("rider_location", ride.getRider_location());

        dbc.add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    ride.setId(task.getResult().getId());
                    Bundle bundle = new Bundle();
                    bundle.putInt(RIDE_STARTED, 1);
                    Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).navigate(R.id.action_rideOfferDetailFragment_to_chatroomFragment, bundle);
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    interface IOfferDetails {

        User getUser();

        MapHelper getMapHelper();

        void toggleDialog(boolean show);

    }

}