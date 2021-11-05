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
import android.widget.EditText;
import android.widget.Toast;

import com.example.nannyapp.R;
import com.example.nannyapp.login.LoginActivity;
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

    private EditText loginEmail;
    private EditText loginPassword;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        View view = getView();

        loginEmail = view.findViewById(R.id.login_email);
        loginPassword = view.findViewById(R.id.login_password);

        initOnClickListeners(view);
    }

    private void initOnClickListeners(View view) {
        view.findViewById(R.id.login_create_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        view.findViewById(R.id.login_forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = LoginFragmentDirections.actionLoginFragmentToResetFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signInWithEmailAndPassword(loginEmail.getText().toString(),
                        loginPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            if (checkUserEmailVerification(currentUser)) {
                                startActivity(new Intent(getActivity(), MainActivity.class));
                            }
                            Log.d(TAG, "Sign in with email successful");
                        } else {
                            Log.w(TAG, "Sign in with email failed");
                        }
                    }
                });
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

}