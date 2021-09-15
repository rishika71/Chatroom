package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatroom.adapter.TripAdapter;
import com.example.chatroom.databinding.FragmentTripListBinding;
import com.example.chatroom.models.Trip;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class TripListFragment extends Fragment {

    FragmentTripListBinding binding;

    User user;

    ITripList am;

    FirebaseFirestore db;

    String type;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ITripList) {
            am = (ITripList) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            type = getArguments().getString(ChatroomFragment.TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Your " + type + "s");
        binding = FragmentTripListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.tripview.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.tripview.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.tripview.getContext(),
                llm.getOrientation());
        binding.tripview.addItemDecoration(dividerItemDecoration);

        am.toggleDialog(true);

        db.collection(Utils.DB_TRIP).document(user.getId()).collection(Utils.DB_TRIPS).orderBy("started_at", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                ArrayList<Trip> rides = new ArrayList<>();
                int i = 0;
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("type") == type) {
                        Trip ride = doc.toObject(Trip.class);
                        ride.setNumber(++i);
                        ride.setId(doc.getId());
                        rides.add(ride);
                    }
                }
                binding.tripview.setAdapter(new TripAdapter(type, user, rides));
            }
        });

        return view;
    }

    interface ITripList {
        User getUser();

        void toggleDialog(boolean show);
    }
}