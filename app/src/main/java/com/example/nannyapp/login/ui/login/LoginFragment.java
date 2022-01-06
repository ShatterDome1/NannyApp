package com.example.nannyapp.login.ui.login;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
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
import android.widget.Toast;

import com.example.nannyapp.databinding.FragmentLoginBinding;
import com.example.nannyapp.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
    private static final String TAG = LoginFragment.class.getSimpleName();

    private LoginViewModel mViewModel;
    private FirebaseAuth firebaseAuth;
    private FragmentLoginBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initOnClickListeners();
    }

    private void initOnClickListeners() {
        binding.loginCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.loginForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = LoginFragmentDirections.actionLoginFragmentToResetFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateLoginFields()) {
                    firebaseAuth.signInWithEmailAndPassword(
                            binding.loginEmail.getText().toString(),
                            binding.loginPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                if (checkUserEmailVerification(currentUser)) {
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                Log.d(TAG, "Sign in with email successful");
                            } else {
                                Toast.makeText(getContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Sign in with email failed");
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean checkUserEmailVerification(FirebaseUser currentUser) {
        boolean isOk = true;
        if (!currentUser.isEmailVerified()) {
            isOk = false;
            firebaseAuth.signOut();
            Toast.makeText(getContext(), "Please verify email", Toast.LENGTH_SHORT).show();
        }
        return isOk;
    }

    private boolean validateLoginFields() {
        if (binding.loginEmail.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.loginPassword.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}