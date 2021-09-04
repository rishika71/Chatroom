package com.example.chatroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentUserProfileBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class UserProfileFragment extends Fragment {

    FragmentUserProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.userProfile);

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.bottomNavigation.setSelectedItemId(R.id.profileIcons);

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
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