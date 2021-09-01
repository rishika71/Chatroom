package com.example.chatroom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    final private String TAG = "demo";
    private FirebaseAuth mAuth;

    TextInputEditText emailEditText;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(R.string.forgotPassword);

        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        emailEditText = view.findViewById(R.id.emailTextFieldId);

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        view.findViewById(R.id.resetPasswordButtonId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth = FirebaseAuth.getInstance();
                String emailAddress = emailEditText.getText().toString();

                if(!emailAddress.isEmpty()){
                    mAuth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), getResources().getString(R.string.passwordResetEmail), Toast.LENGTH_LONG).show();
                                        Log.d(TAG, "Email sent.");
                                        Log.d(TAG, emailAddress);
                                        navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragmentNav);
                                    }
                                }
                            });
                }else{
                    getAlertDialogBox(getResources().getString(R.string.enterEmail));
                }

            }
        });

        //....Cancel Button......
        view.findViewById(R.id.cancelResetPasswordId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragmentNav);
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