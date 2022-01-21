package com.example.nannyapp.main.ui.profile;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nannyapp.R;
import com.example.nannyapp.databinding.FragmentNannyProfileBinding;
import com.example.nannyapp.entity.Nanny;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NannyProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NannyProfileFragment extends Fragment {
    private static final String TAG = NannyProfileFragment.class.getSimpleName();
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    private FragmentNannyProfileBinding binding;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText phoneNumber;
    private EditText address;
    private EditText dateOfBirth;
    private EditText skills;
    private EditText experience;

    private LatLng addressLocation;

    private Button save;

    final Calendar myCalendar = Calendar.getInstance();

    public static NannyProfileFragment newInstance() {
        return new NannyProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNannyProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        firstName = binding.nannyProfileFirstName;
        lastName = binding.nannyProfileLastName;
        email = binding.nannyProfileEmail;
        phoneNumber = binding.nannyProfilePhoneNumber;
        address = binding.nannyProfileAddress;
        dateOfBirth = binding.nannyProfileDateOfBirth;
        skills = binding.nannyProfileSkills;
        experience = binding.nannyProfileExperience;
        save = binding.nannyProfileSave;

        initForm();
        initSaveListener();

        initPlaces();
        initAutoComplete();
    }

    private void initForm() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore.collection("Users")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Nanny currentUserAdditionalInfo = documentSnapshot.toObject(Nanny.class);

                initAdditionalFields(firebaseUser, currentUserAdditionalInfo);
            } else {
                Log.d(TAG, "onComplete: failed to retrieve user", task.getException());
            }
        });
        initDateOfBirthPicker();
    }

    private void initDateOfBirthPicker() {
        DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();
            }
        };

        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),
                        date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myformat = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myformat, Locale.ENGLISH);
        dateOfBirth.setText(dateFormat.format(myCalendar.getTime()));
    }

    private void initAdditionalFields(FirebaseUser firebaseUser, Nanny currentUser) {
        email.setText(firebaseUser.getEmail());
        email.setEnabled(false);

        String userFirstName = currentUser.getFirstName();
        if (userFirstName != null && !userFirstName.isEmpty()) {
            this.firstName.setText(userFirstName);
        }

        String userLastName = currentUser.getLastName();
        if (userLastName != null && !userLastName.isEmpty()) {
            this.lastName.setText(userLastName);
        }

        String userPhoneNumber = currentUser.getPhoneNumber();
        if (userPhoneNumber != null && !userPhoneNumber.isEmpty()) {
            this.phoneNumber.setText(userPhoneNumber);
        }

        String userAddress = currentUser.getAddress();
        if (userAddress != null && !userAddress.isEmpty()) {
            this.address.setText(userAddress);
        }

        String userDateOfBirth = currentUser.getDateOfBirth();
        if (userDateOfBirth != null && !userDateOfBirth.isEmpty()) {
            this.dateOfBirth.setText(userDateOfBirth);
        }

        String userSkills = currentUser.getSkills();
        if (userSkills != null && !userSkills.isEmpty()) {
            this.skills.setText(userSkills);
        }

        String experience = currentUser.getExperience();
        if (experience != null && !experience.isEmpty()) {
            this.experience.setText(experience);
        }
    }

    private void initSaveListener() {
        this.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFormFields()) {
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("firstName", firstName.getText().toString());
                    updates.put("lastName", lastName.getText().toString());
                    updates.put("phoneNumber", phoneNumber.getText().toString());
                    updates.put("address", address.getText().toString());
                    updates.put("dateOfBirth", dateOfBirth.getText().toString());
                    updates.put("skills", skills.getText().toString());
                    updates.put("experience", experience.getText().toString());
                    updates.put("addressLat", addressLocation.latitude);
                    updates.put("addressLng", addressLocation.longitude);

                    firebaseFirestore.collection("Users")
                            .document(currentUser.getUid())
                            .update(updates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Profile details updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d(TAG, "onComplete: failed to update profile", task.getException());
                                }
                            });
                }
            }
        });
    }

    private void initPlaces() {
        ApplicationInfo applicationInfo = null;
        String apiKey = null;
        try {
            applicationInfo = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (applicationInfo != null ){
            apiKey = applicationInfo.metaData.getString("com.google.android.geo.API_KEY");
        }

        // Initialize the SDK
        Places.initialize(getContext(), apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(getContext());

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setCountries("RO")
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            for (AutocompletePrediction prediction: response.getAutocompletePredictions()) {
                Log.d(TAG, "initPlaces: " + prediction.getPlaceId());
                Log.d(TAG, "initPlaces: " + prediction.getPrimaryText(null).toString());
            }
        }).addOnFailureListener(exception -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "initPlaces: ", apiException.getCause());
            }
        });
    }

    private void initAutoComplete() {
        binding.nannyProfileAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                        .build(getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getAddress() + ", " + place.getLatLng());
                binding.nannyProfileAddress.setText(place.getAddress());
                this.addressLocation = place.getLatLng();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        if (address.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter your address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dateOfBirth.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (skills.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please let us know your relevant skills", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}