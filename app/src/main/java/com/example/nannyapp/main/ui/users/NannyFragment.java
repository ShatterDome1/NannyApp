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

import com.example.nannyapp.databinding.FragmentNannyBinding;
import com.example.nannyapp.entity.Parent;
import com.example.nannyapp.entity.Role;
import com.example.nannyapp.main.adapter.user.CardAdapter;
import com.example.nannyapp.main.adapter.user.CardModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private CardAdapter cardAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNannyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initParentsList() {
        firebaseFirestore
                .collection("Users")
                .whereEqualTo("role", Role.PARENT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Parent parent = queryDocumentSnapshot.toObject(Parent.class);

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

                                            parentList.add(toCardModel(parent, profilePicture, userId));
                                            cardAdapter.notifyItemInserted(parentList.size());
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

    private CardModel toCardModel(Parent parent, byte[] profilePicture, String userId) {
        CardModel cardModel = new CardModel();
        cardModel.setFullName(parent.getLastName() + " " + parent.getFirstName());
        cardModel.setLocation(parent.getAddress());
        cardModel.setRating("4.5");
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
}