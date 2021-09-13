package com.example.chatroom;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatroom.adapter.ChatAdapter;
import com.example.chatroom.databinding.FragmentChatroomBinding;
import com.example.chatroom.models.Chat;
import com.example.chatroom.models.Chatroom;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ChatroomFragment extends Fragment {

    final private String TAG = "demo";

    FragmentChatroomBinding binding;

    IChat am;

    FirebaseAuth mAuth;

    FirebaseUser cur_user;

    public static final int REQUEST_LOCATION = 72;

    User user;

    FirebaseFirestore db;

    FusedLocationProviderClient mFusedLocationClient;

    Chatroom chatroom;

    NavController navController;

    @Override
    public void onStop() {
        chatroom.removeViewer(cur_user.getUid());
        HashMap<String, Object> upd = new HashMap<>();
        upd.put("viewers", chatroom.getViewers());
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
        super.onStop();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IChat) {
            am = (IChat) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        user = am.getUser();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            chatroom = (Chatroom) getArguments().getSerializable(Utils.DB_CHATROOM);
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Chatroom " + chatroom.getName());
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cur_user = mAuth.getCurrentUser();

        chatroom.addViewer(cur_user.getUid(), cur_user.getDisplayName());
        HashMap<String, Object> upd = new HashMap<>();
        upd.put("viewers", chatroom.getViewers());
        am.toggleDialog(true);
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).update(upd).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

        binding = FragmentChatroomBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                chatroom.setViewers((HashMap<String, String>) value.get("viewers"));
                StringBuilder user_str = new StringBuilder("Viewers - ");
                for (Map.Entry<String, String> s :
                        chatroom.getViewers().entrySet())
                    user_str.append(s.getValue()).append(", ");
                binding.textView6.setText(user_str.substring(0, user_str.length() - 2));
            }
        });


        binding.chatsView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.chatsView.setLayoutManager(llm);

        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).orderBy("created_at", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                am.toggleDialog(false);
                if (error != null) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value == null) {
                    return;
                }
                ArrayList<Chat> chats = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Chat chat = doc.toObject(Chat.class);
                    chat.setId(doc.getId());
                    chats.add(chat);
                }
                binding.chatsView.setAdapter(new ChatAdapter(chatroom, chats));
            }
        });

        binding.floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = binding.editTextTextPersonName.getText().toString();
                if (msg.isEmpty()) {
                    Toast.makeText(getContext(), "Send a message", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendChat(msg, Chat.CHAT_MESSAGE);
            }
        });

        return view;
    }

    public void sendChat(String msg, int chatType) {
        HashMap<String, Object> chat = new HashMap<>();
        chat.put("created_at", FieldValue.serverTimestamp());
        chat.put("content", msg);
        chat.put("owner", new ArrayList<>(Arrays.asList(cur_user.getUid(), cur_user.getDisplayName(), user.getPhotoref())));
        chat.put("chatType", chatType);
        chat.put("likedBy", new ArrayList<>());
        db.collection(Utils.DB_CHATROOM).document(chatroom.getId()).collection(Utils.DB_CHAT).add(chat).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                    binding.editTextTextPersonName.setText("");
                } else {
                    task.getException().printStackTrace();
                }
            }
        });
    }

    interface IChat {

        void toggleDialog(boolean show);

        User getUser();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        if (hasLocationPerms()) {
            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (isLocationEnabled(lm)) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            sendLocationChat(location.getLatitude(), location.getLongitude());
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please turn your location on!", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestLocationPerms();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                sendLocationChat(location.getLatitude(), location.getLongitude());
            }
        }, Looper.myLooper());
    }

    public boolean isLocationEnabled(LocationManager lm) {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    public void sendLocationChat(double lat, double longi) {
        sendChat(lat + "\n" + longi, Chat.CHAT_LOCATION);
    }

    public void requestLocationPerms() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
    }

    public boolean hasLocationPerms() {
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_request_ride:
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_send_location:
                getLastLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}