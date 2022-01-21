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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

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

    private ArrayList<ReviewModel> reviewList;
    private ReviewAdapter reviewAdapter;
    private RecyclerView reviewRecyclerView;

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

        reviewList = new ArrayList<>();
        initReviewList();

        reviewAdapter = new ReviewAdapter(getContext(), reviewList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        reviewRecyclerView = binding.parentDetailsReviewsViewer;
        reviewRecyclerView.setLayoutManager(linearLayoutManager);
        reviewRecyclerView.setAdapter(reviewAdapter);

        initUserInformation();

        initLeaveReviewDialog();

        return root;
    }

    private void initUserInformation() {
        firebaseFirestore.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                       DocumentSnapshot documentSnapshot = task.getResult();
                       parent = documentSnapshot.toObject(Parent.class);

                        StorageReference profileImageStorageReference = storageReference.child("images/" + userId);
                        final long ONE_MB = 1024 * 1024;
                        profileImageStorageReference
                                .getBytes(ONE_MB)
                                .addOnCompleteListener(storageTask -> {
                                    if (storageTask.isSuccessful()) {
                                        byte[] profilePicture = storageTask.getResult();

                                        initTextViews(parent, profilePicture);
                                    } else {
                                        Log.d(TAG, "onComplete: Failed to get profile image", storageTask.getException());
                                    }
                                });
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
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                Nanny nanny = documentSnapshot.toObject(Nanny.class);

                                showReviewDialog(nanny);
                            } else {
                                Log.d(TAG, "onComplete: failed to retrieve current user information");
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
                    binding.parentDetailsRating.setText("N/A");
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
                        User user = userDocument.toObject(User.class);

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
        binding.parentDetailsRating.setText(String.valueOf(sum / reviewList.size()));
    }
}