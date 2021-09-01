package com.example.chatroom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class CreateNewAccountFragment extends Fragment {

    private FirebaseAuth mAuth;

    final private String TAG = "demo";
    private static final int PICK_IMAGE = 100;

    Uri imageUri;
    String fileName;
    String email, password, firstName, lastName, city, gender;

    EditText emailEditText, passwordEditText, firstNameEditText, lastNameEditText, cityEditText;
    ImageView imageView;
    RadioGroup radioGroup;
    RadioButton radioGenderButton;


    public CreateNewAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && data != null && resultCode == RESULT_OK){
            imageUri = data.getData();
            //imageView.setImageURI(imageUri);

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            fileName = UUID.randomUUID().toString() + ".jpg";

            storageReference.child(mAuth.getUid()).child(fileName)
                    .putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                    }

                }
            });


        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.createAccount);

        View view = inflater.inflate(R.layout.fragment_create_new_account, container, false);

        firstNameEditText = view.findViewById(R.id.createFragmentFirstNameId);
        lastNameEditText = view.findViewById(R.id.createFragmentLastNameId);
        cityEditText = view.findViewById(R.id.createFragmentCityNameId);
        emailEditText = view.findViewById(R.id.createFragmentEmailId);
        passwordEditText = view.findViewById(R.id.createFragmentPasswordId);
        imageView = view.findViewById(R.id.userImage);
        radioGroup = view.findViewById(R.id.radioGroup);

        imageView.setImageResource(R.drawable.profile_image);

        int selectedId = radioGroup.getCheckedRadioButtonId();
        radioGenderButton = view.findViewById(selectedId);

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentContainerView2);

        //....Register Button......
        view.findViewById(R.id.registerButtonId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstName = firstNameEditText.getText().toString();
                lastName = lastNameEditText.getText().toString();
                city = cityEditText.getText().toString();
                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                gender = radioGenderButton.getText().toString();

                if(firstName.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterFirstName));
                }else if(lastName.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterLastName));
                }else if(city.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterCity));
                } else if(email.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterEmail));
                }else if(password.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.enterPassword));
                }else if(gender.isEmpty()){
                    getAlertDialogBox(getResources().getString(R.string.chooseGender));
                }else {

                    mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){

                                        //FirebaseUser user = mAuth.getCurrentUser(); //to get user's account details
                                        storeUserInfoToFirestore(firstName, lastName, city, gender, email, fileName);
                                        navController.navigate(R.id.action_createNewAccountFragment_to_chatroomsFragmentNav);

                                    } else
                                        getAlertDialogBox(task.getException().getMessage());

                                }
                            });
                }

            }
        });

        //....Cancel Button......
        view.findViewById(R.id.cancelButtonId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_createNewAccountFragment_to_loginFragmentNav);
            }
        });

        //......Select Images from gallery........
        view.findViewById(R.id.userImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

//        //......Select Images from gallery........
//        view.findViewById(R.id.userImage).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
//            }
//        });



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

    private void storeUserInfoToFirestore(String firstName, String lastName, String city, String gender, String email, String fileName){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> data = new HashMap<>();
        data.put("firstname", firstName);
        data.put("lastname", lastName);
        data.put("city", city);
        data.put("gender", gender);
        data.put("email", email);
        data.put("photoref", fileName);

        db.collection("profiles")
                .document(mAuth.getUid())
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }
}