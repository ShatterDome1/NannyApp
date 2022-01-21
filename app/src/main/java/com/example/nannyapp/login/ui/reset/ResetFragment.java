package com.example.nannyapp.login.ui.reset;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nannyapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResetFragment extends Fragment {
    private static final String TAG = ResetFragment.class.getSimpleName();

    private EditText email;
    private Button resetButton;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        View view = getView();
        email = view.findViewById(R.id.forgot_email);
        resetButton = view.findViewById(R.id.forgot_reset_password);

        initOnClickListener(view);
    }

    private void initOnClickListener(View view) {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEmailFormat()) {
                    sendPasswordResetEmail();
                } else {
                    Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateEmailFormat() {
        String regex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email.getText().toString());
        return matcher.matches();
    }

    private void sendPasswordResetEmail() {
        firebaseAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();

                NavDirections action = ResetFragmentDirections.actionResetFragmentToLoginFragment();
                Navigation.findNavController(getView()).navigate(action);
            } else {
                Toast.makeText(getContext(), "Password reset failed. Please try again", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onComplete: ", task.getException());
            }
        });
    }
}