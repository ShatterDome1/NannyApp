package com.example.nannyapp.main.ui.details;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nannyapp.R;
import com.example.nannyapp.databinding.FragmentNannyBinding;
import com.example.nannyapp.databinding.FragmentNannyDetailsBinding;
import com.example.nannyapp.databinding.FragmentParentDetailsBinding;
import com.example.nannyapp.entity.Nanny;
import com.example.nannyapp.entity.Parent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class NannyDetailsFragment extends Fragment {
    private static final String TAG = NannyDetailsFragment.class.getSimpleName();

    private String userId;

    private FragmentNannyDetailsBinding binding;
    private NannyDetailsFragmentArgs args;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = NannyDetailsFragmentArgs.fromBundle(getArguments());
        userId = args.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNannyDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        initUserInformation();

        return root;
    }

    private void initUserInformation() {
        firebaseFirestore.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            Nanny nanny = documentSnapshot.toObject(Nanny.class);

                            StorageReference profileImageStorageReference = storageReference.child("images/" + userId);
                            final long ONE_MB = 1024 * 1024;
                            profileImageStorageReference
                                    .getBytes(ONE_MB)
                                    .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                        @Override
                                        public void onComplete(@NonNull Task<byte[]> task) {
                                            if (task.isSuccessful()) {
                                                byte[] profilePicture = task.getResult();

                                               initTextViews(nanny, profilePicture);
                                            } else {
                                                Log.d(TAG, "onComplete: Failed to get profile image", task.getException());
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void initTextViews(Nanny nanny, byte[] profilePicture) {
        Bitmap image = BitmapFactory.decodeByteArray(profilePicture, 0, profilePicture.length);
        binding.nannyDetailsImage.setImageBitmap(image);
        binding.nannyDetailsName.setText(nanny.getFirstName() + " " + nanny.getLastName());
        binding.nannyDetailsAddress.setText(nanny.getAddress());
        binding.nannyDetailsPhone.setText(nanny.getPhoneNumber());
        binding.nannyDetailsEmail.setText(nanny.getEmail());
        binding.nannyDetailsSkillsValue.setText(nanny.getSkills());

        if (nanny.getExperience().isEmpty() || nanny.getExperience() == null) {
            binding.nannyDetailsExperienceLabel.setVisibility(View.INVISIBLE);
            binding.nannyDetailsExperienceValue.setVisibility(View.INVISIBLE);
        } else {
            binding.nannyDetailsExperienceValue.setText(nanny.getExperience());
        }

        String[] dateOfBirth = nanny.getDateOfBirth().split("/");
        String age = getAge(Integer.parseInt(dateOfBirth[2]), Integer.parseInt(dateOfBirth[1]), Integer.parseInt(dateOfBirth[0]));
        binding.nannyDetailsAge.setText(age);
    }

    private String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }
}