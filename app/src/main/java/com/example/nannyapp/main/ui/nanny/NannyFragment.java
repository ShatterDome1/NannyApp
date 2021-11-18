package com.example.nannyapp.main.ui.nanny;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.nannyapp.databinding.FragmentNannyBinding;

public class NannyFragment extends Fragment {

    private NannyViewModel nannyViewModel;
    private FragmentNannyBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        nannyViewModel =
                new ViewModelProvider(this).get(NannyViewModel.class);

        binding = FragmentNannyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNanny;
        nannyViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}