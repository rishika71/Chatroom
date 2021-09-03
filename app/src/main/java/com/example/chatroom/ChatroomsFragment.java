package com.example.chatroom;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.chatroom.databinding.FragmentChatroomsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;


public class ChatroomsFragment extends Fragment {

    BottomNavigationView bottomNavigationView;

    FragmentChatroomsBinding binding;

    public static ChatroomsFragment newInstance(String param1, String param2) {
        ChatroomsFragment fragment = new ChatroomsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.chatrooms);

        binding = FragmentChatroomsBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        binding.bottomNavigation.setSelectedItemId(R.id.chatroomsIcon);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatroomsIcon:
                        return true;
                    case R.id.usersIcon:
                        navController.navigate(R.id.action_chatroomsFragmentNav_to_usersFragment);
                        return true;
                    case R.id.profileIcons:
                        navController.navigate(R.id.action_chatroomsFragmentNav_to_userProfileFragment);
                        return true;
                    case R.id.logOutIcons:
                        FirebaseAuth.getInstance().signOut();
                        navController.navigate(R.id.action_chatroomsFragmentNav_to_loginFragmentNav);
                        return true;

                }
                return false;
            }
        });

        return view;

    }
}