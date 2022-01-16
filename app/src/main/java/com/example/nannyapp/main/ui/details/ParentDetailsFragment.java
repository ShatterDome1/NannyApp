package com.example.nannyapp.main.ui.details;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nannyapp.databinding.FragmentParentDetailsBinding;
import com.example.nannyapp.entity.Nanny;
import com.example.nannyapp.entity.Parent;
import com.example.nannyapp.main.ui.dialog.ReviewDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ParentDetailsFragment extends Fragment {
    private static final String TAG = ParentDetailsFragment.class.getSimpleName();

    private String userId;
    private String reviewerId;

    private FragmentParentDetailsBinding binding;
    private ParentDetailsFragmentArgs args;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;

    private Parent parent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = ParentDetailsFragmentArgs.fromBundle(getArguments());
        userId = args.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentParentDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        userId = ParentDetailsFragmentArgs.fromBundle(getArguments()).getUserId();
        this.reviewerId = firebaseAuth.getCurrentUser().getUid();

        initUserInformation();

        initLeaveReviewDialog();

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
                   parent = documentSnapshot.toObject(Parent.class);

                    StorageReference profileImageStorageReference = storageReference.child("images/" + userId);
                    final long ONE_MB = 1024 * 1024;
                    profileImageStorageReference
                            .getBytes(ONE_MB)
                            .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                @Override
                                public void onComplete(@NonNull Task<byte[]> task) {
                                    if (task.isSuccessful()) {
                                        byte[] profilePicture = task.getResult();

                                        initTextViews(parent, profilePicture);
                                    } else {
                                        Log.d(TAG, "onComplete: Failed to get profile image", task.getException());
                                    }
                                }
                            });
                }
            }
        });
    }

    private void initTextViews(Parent parent, byte[] profilePicture) {
        Bitmap image = BitmapFactory.decodeByteArray(profilePicture, 0, profilePicture.length);
        binding.parentDetailsImage.setImageBitmap(image);
        binding.parentDetailsName.setText(parent.getFirstName() + " " + parent.getLastName());
        binding.parentDetailsAddress.setText(parent.getAddress());
        binding.parentDetailsPhone.setText(parent.getPhoneNumber());
        binding.parentDetailsEmail.setText(parent.getEmail());
        binding.parentDetailsDescriptionValue.setText(parent.getDescription());
        binding.parentDetailsChildren.setText(parent.getNoChildren());
    }

    private void initLeaveReviewDialog() {
        binding.parentDetailsAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Users")
                        .document(reviewerId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    Nanny nanny = documentSnapshot.toObject(Nanny.class);

                                    showReviewDialog(nanny);
                                } else {
                                    Log.d(TAG, "onComplete: failed to retrieve current user information");
                                }
                            }
                        });
            }
        });
    }

    public void showReviewDialog(Nanny nanny) {
        String nannyFullName = parent.getFirstName() + " " + parent.getLastName();
        DialogFragment dialogFragment = new ReviewDialog(nannyFullName, nanny.getFirstName(), nanny.getLastName(), reviewerId, userId);
        dialogFragment.show(getChildFragmentManager(), "ReviewDialog");
    }
}