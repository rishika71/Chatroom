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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;


public class ChatroomsFragment extends Fragment {

    BottomNavigationView bottomNavigationView;

    public ChatroomsFragment() {
        // Required empty public constructor
    }


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
        // Inflate the layout for this fragment

        getActivity().setTitle(R.string.chatrooms);

        View view = inflater.inflate(R.layout.fragment_chatrooms, container, false);

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.chatroomsIcon);

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

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = Navigation.findNavController(view);

        Button button = view.findViewById(R.id.cancelButtonID);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.loginFragmentId, true).build();
                navController.navigate(R.id.action_chatroomsFragmentNav_to_loginFragmentNav);

            }
        });
    }
}