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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;


public class UsersFragment extends Fragment {

    BottomNavigationView bottomNavigationView;

    public UsersFragment() {
        // Required empty public constructor
    }


    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();

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
        getActivity().setTitle(R.string.users);

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.usersIcon);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroomsIcon:
                        navController.navigate(R.id.action_usersFragment_to_chatroomsFragmentNav);
                        return true;
                    case R.id.usersIcon:
                        return true;
                    case R.id.profileIcons:
                        navController.navigate(R.id.action_usersFragment_to_userProfileFragment);
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_usersFragment_to_loginFragmentNav);
                        return true;

                }
                return false;
            }
        });

        return view;
    }
}