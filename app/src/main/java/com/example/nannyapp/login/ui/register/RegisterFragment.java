package com.example.nannyapp.login.ui.register;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.nannyapp.R;
import com.example.nannyapp.entity.User;
import com.example.nannyapp.login.ui.login.LoginFragmentDirections;
import com.example.nannyapp.utils.Role;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {
    private static final String TAG = RegisterFragment.class.getSimpleName();

    private RegisterViewModel mViewModel;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private EditText firstName;
    private EditText lastName;
    private RadioGroup radioGroup;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        View view = getView();
        email = view.findViewById(R.id.register_email);
        password = view.findViewById(R.id.register_password);
        confirmPassword = view.findViewById(R.id.register_confirm_password);
        radioGroup = view.findViewById(R.id.register_radio_group);
        firstName = view.findViewById(R.id.register_first_name);
        lastName = view.findViewById(R.id.register_last_name);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        initOnClickListener(view);
    }

    private void initOnClickListener(View view) {
        view.findViewById(R.id.register_create_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInput()) {
                    createAccount();
                }
            }
        });
    }

    private void createAccount() {
        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),
                password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Account created successfully");
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    sendVerificationEmail(user);
                    createUserDocument(user.getUid());

                    Log.d(TAG, user.toString());
                } else {
                    Log.d(TAG, "Account creation failed", task.getException());
                    Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createUserDocument(String uid) {
        int selectedRole = radioGroup.getCheckedRadioButtonId();
        String selectedRadioText = ((RadioButton) getActivity().findViewById(selectedRole)).getText().toString();

        User user = new User();
        user.setRole(selectedRadioText.equals("Parent")? Role.PARENT : Role.NANNY);
        user.setFirstName(firstName.getText().toString());
        user.setLastName(lastName.getText().toString());

        firebaseFirestore.collection("Users").document(uid).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: firestore user added");
                } else {
                    Log.d(TAG, "onComplete: firestore user addition failed", task.getException());
                }
            }
        });
    }

    private boolean checkInput() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "Please select your role", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((firstName.getText().toString().isEmpty() || lastName.getText().toString().isEmpty())) {
            Toast.makeText(getContext(), "Please fill your name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (email.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!validateEmailFormat()) {
            Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.getText().toString().length() < 6) {
            Toast.makeText(getContext(), "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateEmailFormat() {
        String regex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email.getText().toString());
        return matcher.matches();
    }

    private void sendVerificationEmail(FirebaseUser currentUser) {
        currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Verification email sent", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();

                    NavDirections action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment();
                    Navigation.findNavController(getView()).navigate(action);
                } else {
                    Toast.makeText(getContext(), "Email verification failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Email verification failed", task.getException());
                }
            }
        });
    }

}