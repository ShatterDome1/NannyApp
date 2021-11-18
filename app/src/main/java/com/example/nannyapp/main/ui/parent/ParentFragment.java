package com.example.nannyapp.main.ui.parent;

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

import com.example.nannyapp.databinding.FragmentParentBinding;

public class ParentFragment extends Fragment {

    private ParentViewModel parentViewModel;
    private FragmentParentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        parentViewModel =
                new ViewModelProvider(this).get(ParentViewModel.class);

        binding = FragmentParentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textParent;
        parentViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
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