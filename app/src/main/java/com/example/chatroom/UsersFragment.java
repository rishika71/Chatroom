package com.example.chatroom;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentUsersBinding;
import com.example.chatroom.models.User;
import com.example.chatroom.models.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class UsersFragment extends Fragment {

    FragmentUsersBinding binding;

    IUsers am;

    NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IUsers) {
            am = (IUsers) context;
        } else {
            throw new RuntimeException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.users);

        binding = FragmentUsersBinding.inflate(inflater, container, false);

        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        View view = binding.getRoot();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        am.toggleDialog(true);
        CollectionReference ddb = db.collection(Utils.DB_PROFILE);
        ddb.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                am.toggleDialog(false);
                if (task.isSuccessful()) {
                    ArrayList<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        User user = snapshot.toObject(User.class);
                        user.setId(snapshot.getId());
                        users.add(user);
                        Log.d("DEMO", "onComplete: " + users);
                    }
                    ArrayAdapter<User> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, users);
                    binding.userList.setAdapter(adapter);
                    binding.userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            User user = users.get(position);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Utils.DB_PROFILE, user);
                            navController.navigate(R.id.action_usersFragment_to_viewUserFragment, bundle);
                        }
                    });
                } else {
                    task.getException().printStackTrace();
                }
            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.usersIcon);

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
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

    interface IUsers {

        void toggleDialog(boolean show);

    }
}