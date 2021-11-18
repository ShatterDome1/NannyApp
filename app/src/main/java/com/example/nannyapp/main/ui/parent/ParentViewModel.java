package com.example.nannyapp.main.ui.parent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ParentViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ParentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is parent fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}