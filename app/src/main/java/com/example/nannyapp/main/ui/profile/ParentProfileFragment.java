package com.example.nannyapp.main.ui.profile;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nannyapp.databinding.FragmentParentProfileBinding;
import com.example.nannyapp.entity.Parent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ParentProfileFragment extends Fragment {
    private static final String TAG = ParentProfileFragment.class.getSimpleName();
    private FragmentParentProfileBinding binding;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText phoneNumber;
    private EditText noChildren;
    private EditText address;
    private EditText description;
    private Button save;

    public static ParentProfileFragment newInstance() {
        return new ParentProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentParentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        firstName = binding.profileFirstName;
        lastName = binding.profileLastName;
        email = binding.profileEmail;
        phoneNumber = binding.profilePhoneNumber;
        noChildren = binding.profileNumberKids;
        address = binding.profileAddress;
        description = binding.profileDescription;
        save = binding.profileSave;

        initForm();
        initSaveListener();
    }

    private void initForm() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        firebaseFirestore.collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Parent currentUserAdditionalInfo;
                    DocumentSnapshot documentSnapshot = task.getResult();
                    currentUserAdditionalInfo = documentSnapshot.toObject(Parent.class);

                    initAdditionalFields(currentUser, currentUserAdditionalInfo);
                }
            }
        });

    }

    private void initAdditionalFields(FirebaseUser currentUser, Parent user) {
        email.setText(currentUser.getEmail());
        email.setEnabled(false);

        String userFirstName = user.getFirstName();
        if (userFirstName != null && !userFirstName.isEmpty()) {
            this.firstName.setText(userFirstName);
        }

        String userLastName = user.getLastName();
        if (userLastName != null && !userLastName.isEmpty()) {
            this.lastName.setText(userLastName);
        }

        String userPhoneNumber = user.getPhoneNumber();
        if (userPhoneNumber != null && !userPhoneNumber.isEmpty()) {
            this.phoneNumber.setText(userPhoneNumber);
        }

        String userNoChildren = user.getNoChildren();
        if (userNoChildren != null && !userNoChildren.isEmpty()) {
            this.noChildren.setText(userNoChildren);
        }

        String userAddress = user.getAddress();
        if (userAddress != null && !userAddress.isEmpty()) {
            this.address.setText(userAddress);
        }

        String userDescription = user.getDescription();
        if (userDescription != null && !userDescription.isEmpty()) {
            this.description.setText(userDescription);
        }
    }

    private void initSaveListener() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFormFields()) {
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("firstName", firstName.getText().toString());
                    updates.put("lastName", lastName.getText().toString());
                    updates.put("phoneNumber", phoneNumber.getText().toString());
                    updates.put("noChildren", noChildren.getText().toString());
                    updates.put("address", address.getText().toString());
                    updates.put("description", description.getText().toString());

                    firebaseFirestore.collection("Users")
                            .document(currentUser.getUid())
                            .update(updates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Profile details updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "onComplete: failed to update profile", task.getException());
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean validateFormFields() {
        if (firstName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please add first name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (lastName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please add last name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (phoneNumber.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please add phone number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (noChildren.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter how many children you have", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (address.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter your address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (description.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please let us know what you're looking for", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}