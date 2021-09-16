package com.example.chatroom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentTripInfoBinding;
import com.example.chatroom.models.MapHelper;
import com.example.chatroom.models.Trip;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TripInfoFragment extends Fragment {

    FragmentTripInfoBinding binding;

    Trip trip;

    GoogleMap mMap;

    User user;

    MapHelper mapHelper;

    ITripInfo am;

    Marker m1, m2;

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
            trip = (Trip) getArguments().getSerializable(Utils.DB_TRIPS);
        }
    }

    public void updLocation(String type, ArrayList<Double> current_pos) {
        mapHelper.getLastLocation(new MapHelper.ILastLocation() {
            @Override
            public void onUpdate(double lat, double longi) {
                if (current_pos.get(0) != lat && current_pos.get(1) != longi) {
                    HashMap<String, Object> upd = new HashMap<>();
                    upd.put(type, new ArrayList<>(Arrays.asList(lat, longi)));
                    db.collection(Utils.DB_TRIPS).document(trip.getId()).update(upd);
                }
            }

            @Override
            public boolean stopAfterOneUpdate() {
                return false;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mapHelper.stopUpdates();
    }

    public float getDistance(LatLng start, LatLng end) {
        Location loc1 = new Location("");
        loc1.setLatitude(start.latitude);
        loc1.setLongitude(start.longitude);
        Location loc2 = new Location("");
        loc2.setLatitude(end.latitude);
        loc2.setLongitude(end.longitude);
        return loc1.distanceTo(loc2);
    }

    public void setImage(ImageView image, String uid, String photoRef) {
        if (photoRef != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(uid).child(photoRef);
            GlideApp.with(binding.getRoot())
                    .load(storageReference)
                    .into(image);
        } else {
            GlideApp.with(binding.getRoot())
                    .load(R.drawable.profile_image)
                    .into(image);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Ongoing Trip Details");
        binding = FragmentTripInfoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView5);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    m1 = mapHelper.justAddMarker(mMap, trip.getDriverLatLng(), "Driver Location");
                    m2 = mapHelper.justAddMarker(mMap, trip.getRiderLatLng(), "Rider Location");
                    mapHelper.camUpdate(mMap, trip.getDriverLatLng(), 15);
                    mapHelper.drawRoute(mMap, trip.getDriverLatLng(), trip.getRiderLatLng());
                }
            });
        }

        binding.textView35.setText("Rider - " + trip.getRiderName());
        binding.textView36.setText("Driver - " + trip.getDriverName());
        binding.textView37.setText("Started at - " + Utils.getDateString(trip.getStarted_at()));
        binding.textView34.setText("Status - " + trip.getStatus());

        setImage(binding.imageView9, trip.getRiderId(), trip.getRiderRef());
        setImage(binding.imageView10, trip.getDriverId(), trip.getDriverRef());

        if (user.getId().equals(trip.getDriverId())) {
            updLocation("driver_location", trip.getDriver_location());
        } else {
            updLocation("rider_location", trip.getRider_location());
        }

        db.collection(Utils.DB_TRIPS).document(trip.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                trip = value.toObject(Trip.class);
                trip.setId(value.getId());
                if (!trip.isOngoing()) {
                    trip_finished();
                    return;
                }
                m1.setPosition(trip.getDriverLatLng());
                mapHelper.camUpdate(mMap, trip.getDriverLatLng(), 15);
                if (getDistance(trip.getDriverLatLng(), trip.getRiderLatLng()) <= 15) {
                    trip.setOngoing(false);
                    db.collection(Utils.DB_TRIPS).document(trip.getId()).update("ongoing", false).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            trip_finished();
                        }
                    });
                }
            }
        });

        binding.button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).popBackStack();
            }
        });
        return view;
    }

    public void trip_finished() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.info)
                .setMessage("Trip has finished!")
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Navigation.findNavController(getActivity(), R.id.fragmentContainerView2).popBackStack();

                    }
                })
                .show();
    }

    interface ITripInfo {

        User getUser();

        MapHelper getMapHelper();

    }
}