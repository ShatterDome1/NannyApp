package com.example.nannyapp.main.ui.users;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nannyapp.R;
import com.example.nannyapp.databinding.FragmentNannyBinding;
import com.example.nannyapp.entity.Nanny;
import com.example.nannyapp.entity.Parent;
import com.example.nannyapp.entity.Review;
import com.example.nannyapp.entity.Role;
import com.example.nannyapp.entity.User;
import com.example.nannyapp.main.MainActivity;
import com.example.nannyapp.main.adapter.user.CardAdapter;
import com.example.nannyapp.main.adapter.user.CardModel;
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

import java.util.ArrayList;

public class NannyFragment extends Fragment implements CardAdapter.OnItemClickListener {
    private static final String TAG = NannyFragment.class.getSimpleName();

    private FragmentNannyBinding binding;

    private RecyclerView parentRecyclerView;
    private ArrayList<CardModel> parentList;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;

    private CardAdapter cardAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNannyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseAuth = FirebaseAuth.getInstance();


        parentList = new ArrayList<>();
        initParentsList();

        cardAdapter = new CardAdapter(getContext(), parentList, this);

        // below line is for setting a layout manager for our recycler view.
        // here we are creating vertical list so we will provide orientation as vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        // in below two lines we are setting layoutmanager and adapter to our recycler view.
        parentRecyclerView = binding.nannyCardViewer;
        parentRecyclerView.setLayoutManager(linearLayoutManager);
        parentRecyclerView.setAdapter(cardAdapter);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).verifyUserInformationHasBeenAdded(Navigation.findNavController(getView()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initParentsList() {
        firebaseFirestore
                .collection("Users")
                .whereEqualTo("role", Role.PARENT)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            Parent parent = queryDocumentSnapshot.toObject(Parent.class);

                            String userId = queryDocumentSnapshot.getId();
                            StorageReference profileImageStorageReference = storageReference.child("images/" + userId);
                            final long ONE_MB = 1024 * 1024;
                            profileImageStorageReference
                                    .getBytes(ONE_MB)
                                    .addOnCompleteListener(storageTask -> {
                                        if (storageTask.isSuccessful()) {
                                            byte[] profilePicture = storageTask.getResult();
                                            getReviewsAverageAndUpdateRecyclerView(parent, profilePicture, userId);
                                        } else {
                                            Log.d(TAG, "onComplete: Failed to get profile image", storageTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.d(TAG, "onComplete: failed to retrieve parents", task.getException());
                    }
                });
    }

    private CardModel toCardModel(Parent parent, byte[] profilePicture, String userId, String ratingAverage) {
        CardModel cardModel = new CardModel();
        cardModel.setFullName(parent.getLastName() + " " + parent.getFirstName());
        cardModel.setLocation(parent.getAddress());
        cardModel.setRating(ratingAverage);
        cardModel.setProfilePicture(profilePicture);
        cardModel.setId(userId);
        return cardModel;
    }

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: opening parent with id: " + cardAdapter.getItemAtPosition(position).getId());

        CardModel selectedCard = cardAdapter.getItemAtPosition(position);

        NavDirections navDirections = NannyFragmentDirections.actionNavNannyToParentDetails(selectedCard.getId());
        Navigation.findNavController(getView()).navigate(navDirections);
    }


    private void getReviewsAverageAndUpdateRecyclerView(Parent parent, byte[] profilePicture, String userId) {
        Query query = firebaseFirestore.collection("Reviews")
                .whereEqualTo("userId", userId);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                Float sum = 0.0f;
                for (QueryDocumentSnapshot queryDocumentSnapshot: querySnapshot) {
                    Review review = queryDocumentSnapshot.toObject(Review.class);
                    sum += review.getRating();
                }
                String ratingAverageStr = Float.compare(sum, 0.0f) == 0 ? "No reviews" : String.valueOf(sum / querySnapshot.size());
                parentList.add(toCardModel(parent, profilePicture, userId, ratingAverageStr));
                cardAdapter.notifyItemInserted(parentList.size());
            } else {
                Log.d(TAG, "onComplete: Failed to retrieve reviews for userId = " + userId);
            }
        });
    }
}