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

import com.example.chatroom.databinding.FragmentUsersBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;


public class UsersFragment extends Fragment {

    BottomNavigationView bottomNavigationView;

    FragmentUsersBinding binding;

    public static UsersFragment newInstance(String param1, String param2) {
        return new UsersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(R.string.users);

        binding = FragmentUsersBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.bottomNavigation.setSelectedItemId(R.id.usersIcon);

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