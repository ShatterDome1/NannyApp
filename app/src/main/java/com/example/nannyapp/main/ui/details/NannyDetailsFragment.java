package com.example.nannyapp.main.ui.details;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.nannyapp.R;
import com.example.nannyapp.databinding.FragmentNannyBinding;
import com.example.nannyapp.databinding.FragmentNannyDetailsBinding;
import com.example.nannyapp.databinding.FragmentParentDetailsBinding;
import com.example.nannyapp.entity.Nanny;
import com.example.nannyapp.entity.Parent;
import com.example.nannyapp.entity.Review;
import com.example.nannyapp.entity.User;
import com.example.nannyapp.main.adapter.review.ReviewAdapter;
import com.example.nannyapp.main.adapter.review.ReviewModel;
import com.example.nannyapp.main.ui.dialog.ReviewDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class NannyDetailsFragment extends Fragment {
    private static final String TAG = NannyDetailsFragment.class.getSimpleName();

    private String userId;
    private String reviewerId;

    private FragmentNannyDetailsBinding binding;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ReviewModel> reviewList;
    private ReviewAdapter reviewAdapter;
    private RecyclerView reviewRecyclerView;

    private Nanny nanny;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNannyDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        this.userId = NannyDetailsFragmentArgs.fromBundle(getArguments()).getUserId();
        this.reviewerId = firebaseAuth.getCurrentUser().getUid();

        initUserInformation();

        initLeaveReviewDialog();

        reviewList = new ArrayList<>();
        initReviewList();

        reviewAdapter = new ReviewAdapter(getContext(), reviewList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        reviewRecyclerView = binding.nannyDetailsReviewsViewer;
        reviewRecyclerView.setLayoutManager(linearLayoutManager);
        reviewRecyclerView.setAdapter(reviewAdapter);

        return root;
    }

    private void initUserInformation() {
        firebaseFirestore.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        nanny = documentSnapshot.toObject(Nanny.class);

                        StorageReference profileImageStorageReference = storageReference.child("images/" + userId);
                        final long ONE_MB = 1024 * 1024;
                        profileImageStorageReference
                                .getBytes(ONE_MB)
                                .addOnCompleteListener(storageTask -> {
                                    if (storageTask.isSuccessful()) {
                                        byte[] profilePicture = storageTask.getResult();
                                        initTextViews(nanny, profilePicture);
                                    } else {
                                        Log.d(TAG, "onComplete: Failed to get profile image", storageTask.getException());
                                    }
                                });
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

        return ageInt.toString();
    }

    private void initLeaveReviewDialog() {
        binding.nannyDetailsAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Users")
                    .document(reviewerId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            Parent parent = documentSnapshot.toObject(Parent.class);

                            showReviewDialog(parent);
                        } else {
                            Log.d(TAG, "onComplete: failed to retrieve current user information");
                        }
                    });
            }
        });
    }

    public void showReviewDialog(Parent parent) {
        String nannyFullName = nanny.getFirstName() + " " + nanny.getLastName();
        DialogFragment dialogFragment = new ReviewDialog(nannyFullName, parent.getFirstName(), parent.getLastName(), reviewerId, userId);
        dialogFragment.show(getChildFragmentManager(), "ReviewDialog");
    }

    public void initReviewList() {
        Log.d(TAG, "initReviewList: collecting reviews uid = " + userId);
        Query reviewQuery = firebaseFirestore.collection("Reviews")
                .whereEqualTo("userId", userId);

        reviewQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "onComplete: retrieved reviews = " + task.getResult().size());
                QuerySnapshot reviewDocuments = task.getResult();
                if (reviewDocuments.size() != 0) {
                    for (QueryDocumentSnapshot reviewDocument : reviewDocuments) {
                        Review review = reviewDocument.toObject(Review.class);
                        String reviewerId = reviewDocument.getId().split(" ")[1];

                        getReviewerDetailsAndAddToRecyclerList(review, reviewerId);
                    }
                } else {
                    binding.nannyDetailsRating.setText("N/A");
                }
            } else {
                Log.d(TAG, "onComplete: failed to retrieve reviews", task.getException());
            }
        });
    }

    private void getReviewerDetailsAndAddToRecyclerList(Review review, String reviewerId) {
        firebaseFirestore.collection("Users")
                .document(reviewerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot userDocument = task.getResult();
                        User user = task.getResult().toObject(User.class);
                        ReviewModel reviewModel = toReviewModel(review, user);
                        reviewList.add(reviewModel);
                        reviewAdapter.notifyItemInserted(reviewList.size());

                        calculateAndSetReviewAverage();
                    } else {
                        Log.d(TAG, "onComplete: failed to retrieve reviewer details", task.getException());
                    }
                });
    }

    private ReviewModel toReviewModel(Review review, User user) {
        ReviewModel reviewModel = new ReviewModel();
        reviewModel.setFirstName(user.getFirstName());
        reviewModel.setLastName(user.getLastName());
        reviewModel.setComment(review.getComment());
        reviewModel.setRating(review.getRating());

        return reviewModel;
    }

    private void calculateAndSetReviewAverage() {
        Float sum = 0.0f;
        for (ReviewModel review: reviewList) {
            sum += review.getRating();
        }
        binding.nannyDetailsRating.setText(String.valueOf(sum / reviewList.size()));
    }
}