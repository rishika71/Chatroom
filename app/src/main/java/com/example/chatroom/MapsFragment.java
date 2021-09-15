package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentMapsBinding;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.RideReq;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class MapsFragment extends Fragment {

    FragmentMapsBinding binding;


    AutocompleteSupportFragment autocompleteFragment, autocompleteFragment2;
    GoogleMap mMap;

    MapHelper mapHelper;
    private LatLng mOrigin;
    private LatLng mDestination;
    public static final String REQUEST_RIDE = "request";
    User user;
    IRequestRide am;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IRequestRide) {
            am = (IRequestRide) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
        mapHelper = am.getMapHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Request Ride");
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
//                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                        @Override
//                        public void onMapClick(LatLng point) {
//                            mapHelper.addMarker(mMap, point);
//                        }
//                    });
                }
            });
        }

        String apiKey = getString(R.string.api_key);

        Places.initialize(getContext(), apiKey);

        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment2);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getLatLng();
                mapHelper.addMarker(mMap, mOrigin);
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });

        autocompleteFragment2 = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment3);

        autocompleteFragment2.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));

        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getLatLng();
                mapHelper.addMarker(mMap, mDestination);
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });

        binding.button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOrigin != null & mDestination != null) {
                    RideReq rideReq = new RideReq(mOrigin, mDestination, new ArrayList<>(Arrays.asList(user.getId(), user.getDisplayName(), user.getPhotoref())), new ArrayList<>());

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("ride_id", rideReq.getRide_id());
                    data.put("pickup_lat", rideReq.getPickup().latitude);
                    data.put("pickup_long", rideReq.getPickup().longitude);
                    data.put("drop_lat", rideReq.getDrop().latitude);
                    data.put("drop_long", rideReq.getDrop().longitude);
                    data.put("requester", rideReq.getRequester());

                    user.setRideReq(rideReq);
                    FirebaseFirestore.getInstance().collection(Utils.DB_RIDE_REQ)
                            .document(rideReq.getRide_id())
                            .set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Bundle bundle = new Bundle();
                                bundle.putInt(REQUEST_RIDE, 1);
                                Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).navigate(R.id.action_mapsFragment_to_chatroomFragment, bundle);
                            } else {
                                task.getException().printStackTrace();
                            }
                        }
                    });


                } else {
                    Toast.makeText(getContext(), "Please select pickup and destination", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    interface IRequestRide {

        User getUser();

        MapHelper getMapHelper();

    }


}