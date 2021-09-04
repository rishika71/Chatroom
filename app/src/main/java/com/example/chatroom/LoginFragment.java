package com.example.chatroom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.chatroom.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    NavController navController;

    FragmentLoginBinding binding;

    String email, password;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            navController.navigate(R.id.action_loginFragmentNav_to_chatroomsFragmentNav);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        getActivity().setTitle(R.string.login);

        binding = FragmentLoginBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        //NavController navController = Navigation.findNavController(view);
        navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        //........Create New Account
        binding.createNewAccountId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_loginFragmentNav_to_createNewAccountFragment);
            }
        });

        //........Forgot Password
        binding.forgetPasswordButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               navController.navigate(R.id.action_loginFragmentNav_to_forgotPasswordFragment);
            }
        });

        //......Login Button......
        binding.loginButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = binding.emailTextFieldId.getText().toString();
                password = binding.passwordTextFieldId.getText().toString();

                if(email.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterEmail));
                }else if(password.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterPassword));
                }else{

                    mAuth = FirebaseAuth.getInstance();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        navController.navigate(R.id.action_loginFragmentNav_to_chatroomsFragmentNav);

                                    } else{
                                        getAlertDialogBox(task.getException().getMessage());
                                    }

                                }
                            });
                }
            }
        });
        return view;
    }


    public void getAlertDialogBox(String errorMessage){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.errorMessage))
                .setMessage(errorMessage);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }
}