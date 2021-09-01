package com.example.chatroom;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class UserProfileFragment extends Fragment {

    BottomNavigationView bottomNavigationView;

    TextView nameTextView, cityTextView, genderTextView, emailTextView;

    public UserProfileFragment() {
        // Required empty public constructor
    }


    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(R.string.userProfile);

        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        nameTextView = view.findViewById(R.id.nameTextViewId);
        cityTextView = view.findViewById(R.id.cityTextViewId);
        genderTextView = view.findViewById(R.id.genderTextViewId);
        emailTextView = view.findViewById(R.id.emailTextViewId);

        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profileIcons);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroomsIcon:
                        navController.navigate(R.id.action_userProfileFragment_to_chatroomsFragmentNav);
                        return true;
                    case R.id.usersIcon:
                        navController.navigate(R.id.action_userProfileFragment_to_usersFragment);
                        return true;
                    case R.id.profileIcons:
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_userProfileFragment_to_loginFragmentNav);
                        return true;

                }
                return false;
            }
        });

        return view;

    }
}