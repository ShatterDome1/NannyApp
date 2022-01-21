package com.example.nannyapp.main.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.example.nannyapp.R;
import com.example.nannyapp.entity.Role;
import com.example.nannyapp.entity.User;
import com.example.nannyapp.main.ui.users.NannyFragmentDirections;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = HomeFragment.class.getSimpleName();

    private LatLng currentUserAddress;
    private Role currentUserRole;
    private String currentUserId;

    private ArrayList<Pair<MarkerOptions, String>> userMarkerOptions;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userMarkerOptions = new ArrayList<>();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        getCurrentUserAddress(googleMap);
    }

    private void getCurrentUserAddress(GoogleMap googleMap) {
        firebaseFirestore.collection("Users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        User user = documentSnapshot.toObject(User.class);
                        currentUserAddress = new LatLng(user.getAddressLat(), user.getAddressLng());
                        currentUserRole = user.getRole();

                        getUsersBasedOnRole(googleMap);
                    } else {
                        Log.d(TAG, "getCurrentUserAddress: failed to retrieve current user details", task.getException());
                    }
                });
    }

    private void getUsersBasedOnRole(GoogleMap googleMap) {
        Role roleToSearch = currentUserRole.equals(Role.PARENT) ? Role.NANNY : Role.PARENT;
        Log.d(TAG, "getUsersBasedOnRole: looking for users with role: " + roleToSearch);
        firebaseFirestore.collection("Users")
                .whereEqualTo("role", roleToSearch.getRole())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot documentSnapshot: querySnapshot) {
                            User user = documentSnapshot.toObject(User.class);

                            LatLng userAddressLocation = new LatLng(user.getAddressLat(), user.getAddressLng());
                            String fullName = user.getFirstName() + " " + user.getLastName();

                            userMarkerOptions.add(
                                    new Pair<>(new MarkerOptions().position(userAddressLocation).title(fullName), documentSnapshot.getId() + " " + user.getRole()));

                            initMapMarkers(googleMap);
                        }
                    } else {
                        Log.d(TAG, "getUsersBasedOnRole: failed to retrieve users", task.getException());
                    }
                });
    }

    private void initMapMarkers(GoogleMap googleMap) {
        // Add current user pin
        googleMap.setOnInfoWindowClickListener(this);

        googleMap.setMinZoomPreference(10.0f);
        googleMap.setMaxZoomPreference(16.0f);

        googleMap.addMarker(new MarkerOptions().position(currentUserAddress)
                .title("Me")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
        .setTag(firebaseAuth.getCurrentUser().getUid());
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentUserAddress));

        for (Pair<MarkerOptions, String> userMarkerAndId: userMarkerOptions) {
            googleMap.addMarker(userMarkerAndId.first).setTag(userMarkerAndId.second);
        }
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        String userId = marker.getTag().toString().split(" ")[0];
        if (!userId.equals(currentUserId)) {
            String role = marker.getTag().toString().split(" ")[1];
            Log.d(TAG, "onInfoWindowClick: move to user details with id= " + userId);
            NavDirections navDirections = role.equals(Role.PARENT.getRole()) ?
                    HomeFragmentDirections.actionNavHomeToParentDetails(userId) : HomeFragmentDirections.actionNavHomeToNannyDetails(userId);
            Navigation.findNavController(getView()).navigate(navDirections);
        }
    }
}