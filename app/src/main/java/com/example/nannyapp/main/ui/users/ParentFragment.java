package com.example.nannyapp.main.ui.users;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.nannyapp.databinding.FragmentParentBinding;
import com.example.nannyapp.entity.Nanny;
import com.example.nannyapp.entity.Review;
import com.example.nannyapp.entity.Role;
import com.example.nannyapp.main.adapter.user.CardAdapter;
import com.example.nannyapp.main.adapter.user.CardModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ParentFragment extends Fragment implements CardAdapter.OnItemClickListener {
    private static final String TAG = ParentFragment.class.getSimpleName();

    private FragmentParentBinding binding;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private ArrayList<CardModel> nannyList;
    private CardAdapter cardAdapter;
    private RecyclerView nannyRecyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        nannyList = new ArrayList<>();
        initNannyList();

        cardAdapter = new CardAdapter(getContext(), nannyList, this);

        // below line is for setting a layout manager for our recycler view.
        // here we are creating vertical list so we will provide orientation as vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        // in below two lines we are setting layoutmanager and adapter to our recycler view.
        nannyRecyclerView = binding.parentCardViewer;
        nannyRecyclerView.setLayoutManager(linearLayoutManager);
        nannyRecyclerView.setAdapter(cardAdapter);

        return root;
    }

    private void initNannyList() {
        firebaseFirestore
                .collection("Users")
                .whereEqualTo("role", Role.NANNY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Nanny nanny = queryDocumentSnapshot.toObject(Nanny.class);

                                String userId = queryDocumentSnapshot.getId();
                                StorageReference profileImageStorageReference = storageReference.child("images/" + userId);
                                final long ONE_MB = 1024 * 1024;
                                profileImageStorageReference
                                        .getBytes(ONE_MB)
                                        .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                            @Override
                                            public void onComplete(@NonNull Task<byte[]> task) {
                                                if (task.isSuccessful()) {
                                                    byte[] profilePicture = task.getResult();
                                                    getReviewsAverageAndUpdateRecyclerView(nanny, profilePicture, userId);
                                                } else {
                                                    Log.d(TAG, "onComplete: Failed to get profile image", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "onComplete: failed to retrieve parents", task.getException());
                        }
                    }
                });
    }

    private CardModel toCardModel(Nanny nanny, byte[] profilePicture, String userId, String ratingAverage) {
        CardModel cardModel = new CardModel();
        cardModel.setFullName(nanny.getLastName() + " " + nanny.getFirstName());
        cardModel.setLocation(nanny.getAddress());
        cardModel.setRating(ratingAverage);
        cardModel.setProfilePicture(profilePicture);
        cardModel.setId(userId);
        return cardModel;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: opening nanny with id: " + cardAdapter.getItemAtPosition(position).getId());

        CardModel selectedCard = cardAdapter.getItemAtPosition(position);

        NavDirections navDirections = ParentFragmentDirections.actionNavParentToNannyDetails(selectedCard.getId());
        Navigation.findNavController(getView()).navigate(navDirections);
    }

    private void getReviewsAverageAndUpdateRecyclerView(Nanny nanny, byte[] profilePicture, String userId) {
        Query query = firebaseFirestore.collection("Reviews")
                .whereEqualTo("userId", userId);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    Float sum = 0.0f;
                    for (QueryDocumentSnapshot queryDocumentSnapshot: querySnapshot) {
                        Review review = queryDocumentSnapshot.toObject(Review.class);
                        sum += review.getRating();
                    }
                    String ratingAverageStr = String.valueOf(sum / querySnapshot.size());
                    nannyList.add(toCardModel(nanny, profilePicture, userId, ratingAverageStr));
                    cardAdapter.notifyItemInserted(nannyList.size());
                } else {
                    Log.d(TAG, "onComplete: Failed to retrieve reviews for userId = " + userId);
                }
            }
        });
    }
}